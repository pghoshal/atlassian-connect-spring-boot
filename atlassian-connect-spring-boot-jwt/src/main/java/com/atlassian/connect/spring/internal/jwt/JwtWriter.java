package com.atlassian.connect.spring.internal.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;

public class JwtWriter {

    private final JWSAlgorithm algorithm;
    private final JWSSigner signer;

    private final static String JWT = "JWT";

    public JwtWriter(JWSAlgorithm algorithm, JWSSigner signer) {
        this.algorithm = algorithm;
        this.signer = signer;
    }

    public String jsonToJwt(String json) throws JwtSigningException {
        // Serialise JWS object to compact format
        return generateJwsObject(json).serialize();
    }

    JWSObject generateJwsObject(String payload) {
        JWSHeader header = new JWSHeader.Builder(algorithm)
                .type(new JOSEObjectType(JWT))
                .build();

        // Create JWS object
        JWSObject jwsObject = new JWSObject(header, new Payload(payload));

        try {
            jwsObject.sign(signer);
        } catch (JOSEException e) {
            throw new JwtSigningException(e);
        }
        return jwsObject;
    }
}
