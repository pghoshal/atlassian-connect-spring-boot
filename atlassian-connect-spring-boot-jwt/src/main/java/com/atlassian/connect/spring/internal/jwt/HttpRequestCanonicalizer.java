package com.atlassian.connect.spring.internal.jwt;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpRequestCanonicalizer {

    /**
     * Instructions for computing the query hash parameter ("qsh") from a HTTP request.
     * -------------------------------------------------------------------------------------
     * Overview:       query hash = hash(canonical-request)
     *          canonical-request = canonical-method + '&amp;' + canonical-URI + '&amp;' + canonical-query-string
     * 1. Compute canonical method.
     *    Simply the upper-case of the method name (e.g. "GET", "PUT").
     * 2. Append the character '&amp;'
     * 3. Compute canonical URI.
     *    Discard the protocol, server, port, context path and query parameters from the full URL.
     *    For requests targeting add-ons discard the `baseUrl` in the add-on descriptor.
     *    (Removing the context path allows a reverse proxy to redirect incoming requests for "jira.example.com/getsomething"
     *    to "example.com/jira/getsomething" without breaking authentication. The requester cannot know that the reverse proxy
     *    will prepend the context path "/jira" to the originally requested path "/getsomething".)
     *    Empty-string is not permitted; use "/" instead.
     *    Do not suffix with a '/' character unless it is the only character.
     *    Url-encode any '&amp;' characters in the path.
     *    E.g. in "http://server:80/some/path/?param=value" the canonical URI is "/some/path"
     *     and in "http://server:80" the canonical URI is "/".
     * 4. Append the character '&amp;'.
     * 5. Compute the canonical query string.
     *    Sort the query parameters primarily by their percent-encoded names and secondarily by their percent-encoded values.
     *    Sorting is by codepoint: sort(["a", "A", "b", "B"]) =&gt; ["A", "B", "a", "b"].
     *    For each parameter append its percent-encoded name, the '=' character and then its percent-encoded value.
     *    In the case of repeated parameters append the ',' character and subsequent percent-encoded values.
     *    Ignore the JWT query string parameter, if present.
     *    Some particular values to be aware of: "+" is encoded as "%20",
     *                                           "*" as "%2A" and
     *                                           "~" as "~".
     *                                           (These values used for consistency with OAuth1.)
     *    An example: for a GET request to the not-yet-percent-encoded URL "http://localhost:2990/path/to/service?zee_last=param&amp;repeated=parameter 1&amp;first=param&amp;repeated=parameter 2"
     *    the canonical request is "GET&amp;/path/to/service&amp;first=param&amp;repeated=parameter%201,parameter%202&amp;zee_last=param".
     * 6. Convert the canonical request string to bytes.
     *    The encoding used to represent characters as bytes is UTF-8.
     * 7. Hash the canonical request bytes using the SHA-256 algorithm.
     *    E.g. The SHA-256 hash of "foo" is "2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae".
     */
    public static final String QUERY_STRING_HASH_CLAIM_NAME = "qsh";

    /**
     * When the JWT message is specified in the query string of a URL then this is the parameter name.
     *
     * E.g. "jwt" in:
     * <pre>
     * http://server:80/some/path?otherparam=value&amp;jwt=eyJhbGciOiJIUzI1NiIsI.eyJleHAiOjEzNzg5NCI6MTM3ODk1MjQ4OH0.cDihfcsKW_We_EY21tIs55dVwjU
     * </pre>
     */
    public static final String JWT_PARAM_NAME = "jwt";

    /**
     * As appears between "value1" and "param2" in the URL "http://server/path?param1=value1&amp;param2=value2".
     */
    public static final char QUERY_PARAMS_SEPARATOR = '&';

    /**
     * The character between "a" and "b%20c" in "some_param=a,b%20c"
     */
    private static final String ENCODED_PARAM_VALUE_SEPARATOR = ",";
    /**
     * For separating the method, URI etc in a canonical request string.
     */
    private static final char CANONICAL_REQUEST_PART_SEPARATOR = '&';

    /**
     * Assemble the components of the HTTP request into the correct format so that they can be signed or hashed.
     * @param request {@link CanonicalHttpRequest} that provides the necessary components
     * @return {@link String} encoding the canonical form of this request as required for constructing query string hash values
     * @throws UnsupportedEncodingException {@link UnsupportedEncodingException} if the {@link java.net.URLEncoder} cannot encode the request's field's characters
     */
    public static String canonicalize(CanonicalHttpRequest request) throws UnsupportedEncodingException {
        return String.format("%s%s%s%s%s", canonicalizeMethod(request), CANONICAL_REQUEST_PART_SEPARATOR, canonicalizeUri(request), CANONICAL_REQUEST_PART_SEPARATOR, canonicalizeQueryParameters(request));
    }

    /**
     * Canonicalize the given {@link CanonicalHttpRequest} and hash it.
     * This request hash can be included as a JWT claim to verify that request components are genuine.
     * @param request {@link CanonicalHttpRequest} to be canonicalized and hashed
     * @return {@link String} hash suitable for use as a JWT claim value
     * @throws UnsupportedEncodingException if the {@link java.net.URLEncoder} cannot encode the request's field's characters
     * @throws NoSuchAlgorithmException if the hashing algorithm does not exist at runtime
     */
    public static String computeCanonicalRequestHash(CanonicalHttpRequest request) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        // prevent the code in this method being repeated in every call site that needs a request hash,
        // encapsulate the knowledge of the type of hash that we are using
        return computeSha256Hash(canonicalize(request));
    }

    /**
     * Compute the SHA-256 hash of hashInput.
     * E.g. The SHA-256 has of "foo" is "2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae".
     * @param hashInput {@link String} to be hashed.
     * @return {@link String} hash
     * @throws NoSuchAlgorithmException if the hashing algorithm does not exist at runtime
     */
    private static String computeSha256Hash(String hashInput) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (null == hashInput) {
            throw new IllegalArgumentException("hashInput cannot be null");
        }

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashInputBytes = hashInput.getBytes();
        digest.update(hashInputBytes, 0, hashInputBytes.length);
        return new String(Hex.encode(digest.digest()));
    }

    private static String canonicalizeUri(CanonicalHttpRequest request) throws UnsupportedEncodingException {
        String path = StringUtils.defaultIfBlank(StringUtils.removeEnd(request.getRelativePath(), "/"), "/");
        final String separatorAsString = String.valueOf(CANONICAL_REQUEST_PART_SEPARATOR);

        // If the separator is not URL encoded then the following URLs have the same query-string-hash:
        //   https://djtest9.jira-dev.com/rest/api/2/project&a=b?x=y
        //   https://djtest9.jira-dev.com/rest/api/2/project?a=b&x=y
        path = path.replaceAll(separatorAsString, percentEncode(separatorAsString));

        return path.startsWith("/") ? path : "/" + path;
    }

    private static String canonicalizeMethod(CanonicalHttpRequest request) {
        return StringUtils.upperCase(request.getMethod());
    }

    private static String canonicalizeQueryParameters(CanonicalHttpRequest request) throws UnsupportedEncodingException {
        String result = "";

        if (null != request.getParameterMap()) {
            List<ComparableParameter> parameterList = new ArrayList<ComparableParameter>(request.getParameterMap().size());

            for (Map.Entry<String, String[]> parameter : request.getParameterMap().entrySet()) {
                if (!JWT_PARAM_NAME.equals(parameter.getKey())) {
                    parameterList.add(new ComparableParameter(parameter));
                }
            }

            Collections.sort(parameterList);
            result = percentEncode(getParameters(parameterList));
        }

        return result;
    }

    /**
     * Retrieve the original parameters from a sorted collection.
     */
    private static List<Map.Entry<String, String[]>> getParameters(Collection<ComparableParameter> parameters) {
        if (parameters == null) {
            return null;
        }

        List<Map.Entry<String, String[]>> list = new ArrayList<Map.Entry<String, String[]>>(parameters.size());

        for (ComparableParameter parameter : parameters) {
            list.add(parameter.parameter);
        }

        return list;
    }

    /**
     * Construct a form-urlencoded document containing the given sequence of
     * name/parameter pairs.
     */
    private static String percentEncode(Iterable<? extends Map.Entry<String, String[]>> parameters) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();

        // IOException should not be throws as we are not messing around with it between creation and use
        // (e.g. by closing it) but the methods on the OutputStream interface don't know that
        try {
            percentEncode(parameters, b);
            return new String(b.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write a form-urlencoded document into the given stream, containing the
     * given sequence of name/parameter pairs.
     */
    private static void percentEncode(Iterable<? extends Map.Entry<String, String[]>> parameters, OutputStream into) throws IOException {
        if (parameters != null) {
            boolean first = true;

            for (Map.Entry<String, String[]> parameter : parameters) {
                if (first) {
                    first = false;
                } else {
                    into.write(QUERY_PARAMS_SEPARATOR);
                }

                into.write(percentEncode(safeToString(parameter.getKey())).getBytes());
                into.write('=');
                List<String> percentEncodedValues = new ArrayList<String>(parameter.getValue().length);

                for (String value : parameter.getValue()) {
                    percentEncodedValues.add(percentEncode(value));
                }
                String valueString = percentEncodedValues.stream().collect(Collectors.joining(ENCODED_PARAM_VALUE_SEPARATOR));
                into.write(valueString.getBytes());
            }
        }
    }

    /**
     * {@link URLEncoder}#encode() but encode some characters differently to URLEncoder, to match OAuth1 and VisualVault.
     * @param value {@link String} to be percent-encoded
     * @return encoded {@link String}
     * @throws UnsupportedEncodingException if {@link URLEncoder} does not support UTF-8
     */
    private static String percentEncode(String value) throws UnsupportedEncodingException {
        if (value == null) {
            return "";
        }

        return URLEncoder.encode(value, "UTF-8")
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    private static String safeToString(Object from) {
        return null == from ? null : from.toString();
    }

    /**
     * An efficiently sortable wrapper around a parameter.
     */
    private static class ComparableParameter implements Comparable<ComparableParameter> {
        ComparableParameter(Map.Entry<String, String[]> parameter) throws UnsupportedEncodingException {
            this.parameter = parameter;
            String name = safeToString(parameter.getKey());
            List<String> sortedValues = Arrays.asList(parameter.getValue());
            Collections.sort(sortedValues);
            String value = sortedValues.stream().collect(Collectors.joining(","));
            this.key = percentEncode(name) + ' ' + percentEncode(value);
            // ' ' is used because it comes before any character
            // that can appear in a percentEncoded string.
        }

        final Map.Entry<String, String[]> parameter;

        private final String key;

        public int compareTo(ComparableParameter that) {
            return this.key.compareTo(that.key);
        }

        @Override
        public String toString() {
            return key;
        }
    }
}
