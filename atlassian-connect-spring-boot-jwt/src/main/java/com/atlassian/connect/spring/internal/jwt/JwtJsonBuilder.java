package com.atlassian.connect.spring.internal.jwt;

import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JwtJsonBuilder {

    private final JSONObject json = new JSONObject();

    public JwtJsonBuilder() {
        long issuedAt = System.currentTimeMillis() / 1000;
        issuedAt(issuedAt);
        expirationTime(issuedAt + 180L); // default JWT lifetime is 3 minutes
    }

    public JwtJsonBuilder audience(String aud) {
        json.put("aud", aud);
        return this;
    }

    public JwtJsonBuilder expirationTime(long exp) {
        json.put("exp", exp);
        return this;
    }

    public boolean isClaimSet(String name) {
        return json.containsKey(name);
    }

    public JwtJsonBuilder issuedAt(long iat) {
        json.put("iat", iat);
        return this;
    }

    public JwtJsonBuilder issuer(String iss) {
        json.put("iss", iss);
        return this;
    }

    public JwtJsonBuilder jwtId(String jti) {
        json.put("jti", jti);
        return this;
    }

    public JwtJsonBuilder notBefore(long nbf) {
        json.put("nbf", nbf);
        return this;
    }

    public JwtJsonBuilder subject(String sub) {
        json.put("sub", sub);
        return this;
    }

    public JwtJsonBuilder type(String typ) {
        json.put("typ", typ);
        return this;
    }

    public JwtJsonBuilder queryHash(String qsh) {
        json.put("qsh", qsh);
        return this;
    }


    @SuppressWarnings("unchecked")
    public JwtJsonBuilder claim(String name, Object obj) {
        Object current = json.get(name);
        json.put(name, merge(name, current, obj));
        return this;
    }

    public String build() {
        return json.toString();
    }

    @Override
    public String toString() {
        return json.toString();
    }

    @SuppressWarnings("unchecked")
    private Object merge(String name, Object first, Object second) {
        if (first instanceof List && second instanceof List) {
            List merged = new ArrayList((List) first);
            merged.addAll((List) second);
            return merged;
        } else if (first instanceof Map && second instanceof Map) {
            Map merged = new HashMap((Map) first);
            // merge each of the entries in second recursively
            Set<Map.Entry> entries = ((Map) second).entrySet();
            for (Map.Entry entry : entries) {
                merged.put(entry.getKey(), merge(name + "." + entry.getKey(), merged.get(entry.getKey()), entry.getValue()));
            }
            return merged;
        }

        if (first != null && second != null && !first.equals(second)) {
            throw new IllegalStateException("Cannot set claim '" + name + "' to '" + second +
                    "'; it's already set as '" + first + "'");
        }

        return second == null ? first : second;
    }
}
