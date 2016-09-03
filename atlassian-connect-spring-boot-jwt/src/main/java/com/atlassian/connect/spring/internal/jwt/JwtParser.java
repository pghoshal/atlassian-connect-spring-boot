package com.atlassian.connect.spring.internal.jwt;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;

import java.text.ParseException;

public class JwtParser {

    public Jwt parse(String jwt) throws JwtParseException {
        JWSObject jwsObject = parseJWSObject(jwt);
        try {
            JWTClaimsSet claims = JWTClaimsSet.parse(jwsObject.getPayload().toJSONObject());
            return new Jwt(claims.getIssuer(), claims.getSubject(), jwsObject.getPayload().toString());
        } catch (ParseException e) {
            throw new JwtParseException(e);
        }
    }

    private JWSObject parseJWSObject(String jwt) throws JwtParseException {
        JWSObject jwsObject;

        try {
            jwsObject = JWSObject.parse(jwt);
        } catch (ParseException e) {
            throw new JwtParseException(e);
        }
        return jwsObject;
    }
}
