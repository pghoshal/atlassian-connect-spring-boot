package com.atlassian.connect.spring.internal;

import com.atlassian.connect.spring.IgnoreJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

/**
 * An error controller providing mapping to a simple JSON response.
 */
@RestController
public class AtlassianConnectErrorController extends AbstractErrorController {

    private static final String PATH = "/error";

    @Autowired
    public AtlassianConnectErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }

    @RequestMapping(value = PATH)
    @IgnoreJwt
    public ErrorJson error(HttpServletRequest request, HttpServletResponse response) {
        boolean includeStackTrace = false;
        getRequestException(request).map((exception) -> getStatus(request, exception))
                .ifPresent((status) -> response.setStatus(status.value()));
        return new ErrorJson(response.getStatus(), getErrorAttributes(request, includeStackTrace));
    }

    private Optional<Exception> getRequestException(HttpServletRequest request) {
        return Optional.ofNullable((Exception) request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION));
    }

    private HttpStatus getStatus(HttpServletRequest request, Exception requestException) {
        Optional<HttpStatus> optionalResponseStatus = getResponseStatusFromExceptionAnnotation(requestException);
        optionalResponseStatus = Optional.ofNullable(optionalResponseStatus.orElseGet(() -> getResponseStatusForException(requestException)));
        return optionalResponseStatus.orElseGet(() -> super.getStatus(request));
    }

    private Optional<HttpStatus> getResponseStatusFromExceptionAnnotation(Exception requestException) {
        return Optional.ofNullable(requestException.getClass().getAnnotation(ResponseStatus.class)).map(ResponseStatus::code);
    }

    private HttpStatus getResponseStatusForException(Exception requestException) {
        return requestException instanceof AuthenticationException ? HttpStatus.UNAUTHORIZED : null;
    }
    public static class ErrorJson {

        public Integer status;
        public String error;
        public String message;
        public String timeStamp;
        public String trace;

        public ErrorJson(int status, Map<String, Object> errorAttributes) {
            this.status = status;
            this.error = (String) errorAttributes.get("error");
            this.message = (String) errorAttributes.get("message");
            this.timeStamp = errorAttributes.get("timestamp").toString();
            this.trace = (String) errorAttributes.get("trace");
        }
    }
}
