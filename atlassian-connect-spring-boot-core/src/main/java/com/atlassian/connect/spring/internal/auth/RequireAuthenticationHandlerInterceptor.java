package com.atlassian.connect.spring.internal.auth;

import com.atlassian.connect.spring.IgnoreJwt;
import com.atlassian.connect.spring.internal.auth.jwt.JwtAuthentication;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;

/**
 * A handler interceptor that enforces JWT authentication for all handler methods not annotated with {@link IgnoreJwt}.
 */
@Component
public class RequireAuthenticationHandlerInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private AddonDescriptorLoader addonDescriptorLoader;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private ServerProperties serverProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (noPreviousError(request) && handlerRequiresJwtAuthentication(handler) && !isPathIgnored(request) && !requestIsSigned()) {
            response.addHeader(HttpHeaders.WWW_AUTHENTICATE, String.format("JWT realm=\"%s\"", addonDescriptorLoader.getDescriptor().getKey()));
            response.sendError(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        return true;
    }

    private boolean isPathIgnored(HttpServletRequest request) {
        String[] pathsArray = serverProperties.getPathsArray(securityProperties.getIgnored());
        for (String path : pathsArray) {
            if (new AntPathRequestMatcher(path).matches(request)) {
                return true;
            }
        }
        return false;
    }

    private boolean handlerRequiresJwtAuthentication(Object handler) {
        return handler instanceof HandlerMethod && !handlerHasAnnotation((HandlerMethod) handler, IgnoreJwt.class);
    }

    private <T extends Annotation> boolean handlerHasAnnotation(HandlerMethod method, Class<T> annotationClass) {
        return method.getMethod().isAnnotationPresent(annotationClass)
                || method.getBeanType().isAnnotationPresent(annotationClass);
    }

    private boolean requestIsSigned() {
        return SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthentication;
    }

    private boolean noPreviousError(HttpServletRequest request) {
        return !request.getDispatcherType().equals(DispatcherType.ERROR);
    }
}
