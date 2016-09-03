package com.atlassian.connect.spring.internal;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * A controller utility class that maps the standard Atlassian Connect iframe context parameters to Spring model
 * attributes.
 */
@ControllerAdvice
public class AtlassianConnectContextModelAttributeProvider {

    private static final String ALL_JS_FILENAME = "all.js";

    private static final String ALL_DEBUG_JS_FILENAME = "all-debug.js";

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private AtlassianConnectProperties atlassianConnectProperties;

    @ModelAttribute("atlassian-connect-license")
    public String getLicense() {
        return request.getParameter("lic");
    }

    @ModelAttribute("atlassian-connect-locale")
    public String getLocale() {
        return request.getParameter("loc");
    }

    @ModelAttribute("atlassian-connect-timezone")
    public String getTimezone() {
        return request.getParameter("tz");
    }

    @ModelAttribute("atlassian-connect-all-js-url")
    public String getAllJsUrl() {
        return getHostBaseUrl().map(this::createAllJsUrl).orElse("");
    }

    private Optional<String> getHostBaseUrl() {
        Optional<String> optionalBaseUrl = getHostBaseUrlFromPrincipal();
        if (!optionalBaseUrl.isPresent()) {
            optionalBaseUrl = getHostBaseUrlFromQueryParameters();
        }
        return optionalBaseUrl;
    }

    private Optional<String> getHostBaseUrlFromPrincipal() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(AtlassianHostUser.class::isInstance)
                .map(AtlassianHostUser.class::cast)
                .map(AtlassianHostUser::getHost)
                .map(AtlassianHost::getBaseUrl);
    }

    private Optional<String> getHostBaseUrlFromQueryParameters() {
        String hostUrl = request.getParameter("xdm_e");
        String contextPath = request.getParameter("cp");

        Optional<String> optionalBaseUrl = Optional.empty();
        if (!StringUtils.isEmpty(hostUrl)) {
            if (!StringUtils.isEmpty(contextPath)) {
                optionalBaseUrl = Optional.of(hostUrl + contextPath);
            } else {
                optionalBaseUrl = Optional.of(hostUrl);
            }
        }
        return optionalBaseUrl;
    }

    private String createAllJsUrl(String hostBaseUrl) {
        return String.format("%s/%s/%s", hostBaseUrl, "atlassian-connect", getAllJsFilename());
    }

    private String getAllJsFilename() {
        return atlassianConnectProperties.isDebugAllJs() ? ALL_DEBUG_JS_FILENAME : ALL_JS_FILENAME;
    }
}
