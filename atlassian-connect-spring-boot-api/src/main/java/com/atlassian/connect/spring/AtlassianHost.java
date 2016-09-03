package com.atlassian.connect.spring;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Calendar;
import java.util.Objects;

/**
 * This class represents an Atlassian Connect host with an installation of this add-on.
 */
public class AtlassianHost {

    @Id
    private String clientKey;                   // unique-client-identifier"

    private String publicKey;                   // "MIGf....ZRWzwIDAQAB"

    private String sharedSecret;                // "a-secret-key-not-to-be-lost"

    private String baseUrl;                     // "http://example.atlassian.net"

    private String productType;                 // "jira"

    private String description;                 // "Atlassian JIRA at https://example.atlassian.net"

    private String serviceEntitlementNumber;    // "SEN-number"

    private boolean addonInstalled;

    @CreatedDate
    private Calendar createdDate;

    @LastModifiedDate
    private Calendar lastModifiedDate;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getServiceEntitlementNumber() {
        return serviceEntitlementNumber;
    }

    public void setServiceEntitlementNumber(String serviceEntitlementNumber) {
        this.serviceEntitlementNumber = serviceEntitlementNumber;
    }

    public boolean isAddonInstalled() {
        return addonInstalled;
    }

    public void setAddonInstalled(boolean addonInstalled) {
        this.addonInstalled = addonInstalled;
    }

    public Calendar getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Calendar createdDate) {
        this.createdDate = createdDate;
    }

    public Calendar getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Calendar lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtlassianHost that = (AtlassianHost) o;
        return addonInstalled == that.addonInstalled &&
                Objects.equals(clientKey, that.clientKey) &&
                Objects.equals(publicKey, that.publicKey) &&
                Objects.equals(sharedSecret, that.sharedSecret) &&
                Objects.equals(baseUrl, that.baseUrl) &&
                Objects.equals(productType, that.productType) &&
                Objects.equals(description, that.description) &&
                Objects.equals(serviceEntitlementNumber, that.serviceEntitlementNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientKey, publicKey, sharedSecret, baseUrl, productType, description, serviceEntitlementNumber, addonInstalled);
    }
}
