package com.atlassian.connect.spring.internal.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public class JwtReader {

    /**
     * The JWT spec says that implementers "MAY provide for some small leeway, usually no more than a few minutes, to account for clock skew".
     * Calculations of the current time for the purposes of accepting or rejecting time-based claims (e.g. "exp" and "nbf") will allow for the current time
     * being plus or minus this leeway, resulting in some time-based claims that are marginally before or after the current time being accepted instead of rejected.
     */
    private static final int TIME_CLAIM_LEEWAY_SECONDS = 30;

    private static final String UNEXPECTED_TYPE_MESSAGE_PREFIX = "Unexpected type of JSON object member with key ";
    private static final Set<String> NUMERIC_CLAIM_NAMES = new HashSet<String>(asList("exp", "iat", "nbf"));
    private final JWSVerifier verifier;

    public JwtReader(String sharedSecret) {
        this.verifier = new MACVerifier(sharedSecret);
    }

    public Jwt readAndVerify(final String jwt, final String queryStringHash) throws JwtParseException, JwtVerificationException {
        return read(jwt, queryStringHash, true);
    }

    private Jwt read(final String jwt, final String queryStringHash, final boolean verify) throws JwtParseException, JwtVerificationException {
        JWSObject jwsObject;

        if (verify) {
            jwsObject = verify(jwt);
        } else {
            try {
                jwsObject = JWSObject.parse(jwt);
            } catch (ParseException e) {
                throw new JwtParseException(e);
            }
        }

        JSONObject jsonPayload = jwsObject.getPayload().toJSONObject();
        JWTClaimsSet claims;

        try {
            claims = JWTClaimsSet.parse(jsonPayload);
        } catch (ParseException e) {
            // if possible, provide a hint to the add-on developer
            if (e.getMessage().startsWith(UNEXPECTED_TYPE_MESSAGE_PREFIX)) {
                String claimName = e.getMessage().replace(UNEXPECTED_TYPE_MESSAGE_PREFIX, "").replaceAll("\"", "");

                if (NUMERIC_CLAIM_NAMES.contains(claimName)) {
                    throw new JwtInvalidClaimException(String.format("Expecting claim '%s' to be numeric but it is a string", claimName), e);
                }

                throw new JwtParseException("Perhaps a claim is of the wrong type (e.g. expecting integer but found string): " + e.getMessage(), e);
            }

            throw new JwtParseException(e);
        }

        if (claims.getIssueTime() == null || claims.getExpirationTime() == null) {
            throw new JwtInvalidClaimException("'exp' and 'iat' are required claims. Atlassian JWT does not allow JWTs with " +
                    "unlimited lifetimes.");
        }

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, -TIME_CLAIM_LEEWAY_SECONDS);
        Date nowMinusLeeway = calendar.getTime();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, TIME_CLAIM_LEEWAY_SECONDS);
        Date nowPlusLeeway = calendar.getTime();

        if (null != claims.getNotBeforeTime()) {
            // sanity check: if the token is invalid before, on and after a given time then it is always invalid and the issuer has made a mistake
            if (!claims.getExpirationTime().after(claims.getNotBeforeTime())) {
                throw new JwtInvalidClaimException(String.format("The expiration time must be after the not-before time but exp=%s and nbf=%s", claims.getExpirationTime(), claims.getNotBeforeTime()));
            }

            if (claims.getNotBeforeTime().after(nowPlusLeeway)) {
                throw new JwtTooEarlyException(claims.getNotBeforeTime(), now, TIME_CLAIM_LEEWAY_SECONDS);
            }
        }

        if (claims.getExpirationTime().before(nowMinusLeeway)) {
            throw new JwtExpiredException(claims.getExpirationTime(), now, TIME_CLAIM_LEEWAY_SECONDS);
        }

        if (queryStringHash != null) {
            Object claim = claims.getClaim(HttpRequestCanonicalizer.QUERY_STRING_HASH_CLAIM_NAME);
            if (null == claim) {
                throw new JwtMissingClaimException(String.format("Claim '%s' is missing.", HttpRequestCanonicalizer.QUERY_STRING_HASH_CLAIM_NAME));
            }

            if (!queryStringHash.equals(claim)) {
                throw new JwtInvalidClaimException(String.format("Expecting claim '%s' to have value '%s' but instead it has the value '%s'",
                        HttpRequestCanonicalizer.QUERY_STRING_HASH_CLAIM_NAME, queryStringHash, claim));
            }
        }

        return new Jwt(claims.getIssuer(), claims.getSubject(), jsonPayload.toString());
    }

    private JWSObject verify(final String jwt) throws JwtParseException, JwtVerificationException {
        try {
            final JWSObject jwsObject = JWSObject.parse(jwt);

            if (!jwsObject.verify(verifier)) {
                throw new JwtSignatureMismatchException(jwt);
            }

            return jwsObject;
        } catch (ParseException e) {
            throw new JwtParseException(e);
        } catch (JOSEException e) {
            throw new JwtSignatureMismatchException(e);
        }
    }
}
