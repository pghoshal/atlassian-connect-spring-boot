package com.atlassian.connect.spring.internal.jwt;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Jwt {

    private final String iss;
    private final String sub;
    private final String payload;

    public Jwt(String iss, String sub, String payload) {
        this.iss = iss;
        this.sub = sub;
        this.payload = payload;
    }

    public String getIssuer() {
        return iss;
    }

    public String getSubject() {
        return sub;
    }

    public String getJsonPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("iss", iss)
                .append("sub", sub)
                .append("payload", payload)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        Jwt rhs = (Jwt) o;
        return new EqualsBuilder()
                .append(iss, rhs.iss)
                .append(sub, rhs.sub)
                .append(payload, rhs.payload)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(31, 17)
                .append(iss)
                .append(sub)
                .append(payload)
                .hashCode();
    }
}
