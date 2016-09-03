package com.atlassian.connect.spring.internal.lifecycle;

import com.atlassian.connect.spring.AddonInstalledEvent;
import com.atlassian.connect.spring.AddonUninstalledEvent;
import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.AsynchronousApplicationEventPublisher;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * A controller that handles the add-on installation and uninstallation lifecycle callbacks.
 *
 * @see LifecycleControllerHandlerMapping
 */
@RestController
public class LifecycleController {

    private static final Logger log = LoggerFactory.getLogger(LifecycleController.class);

    @Autowired
    private AtlassianHostRepository hostRepository;

    @Autowired
    private AddonDescriptorLoader addonDescriptorLoader;

    @Autowired
    private AsynchronousApplicationEventPublisher eventPublisher;

    public static Method getInstalledMethod() {
        return getSafeMethod("installed");
    }

    public static Method getUninstalledMethod() {
        return getSafeMethod("uninstalled");
    }

    private static Method getSafeMethod(String name) {
        try {
            return LifecycleController.class.getMethod(name, LifecycleEvent.class, AtlassianHostUser.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public ResponseEntity installed(@Valid @RequestBody LifecycleEvent lifecycleEvent, @AuthenticationPrincipal AtlassianHostUser hostUser) {
        assertExpectedEventType(lifecycleEvent, "installed");

        if (hostUser == null) {
            Optional<AtlassianHost> maybeExistingHost = getHostFromLifecycleEvent(lifecycleEvent);
            if (maybeExistingHost.isPresent()) {
                log.error("Incoming installation request was not properly authenticated, but we have already installed " +
                        "the add-on for host " + lifecycleEvent.clientKey + ". Subsequent installation requests must " +
                        "include valid JWT. Returning 401.");
                return responseForMissingJwt();
            }
        } else {
            assertHostAuthorized(lifecycleEvent, hostUser);
        }

        AtlassianHost host = new AtlassianHost();
        host.setClientKey(lifecycleEvent.clientKey);
        host.setPublicKey(lifecycleEvent.publicKey);
        host.setSharedSecret(lifecycleEvent.sharedSecret);
        host.setBaseUrl(lifecycleEvent.baseUrl);
        host.setProductType(lifecycleEvent.productType);
        host.setDescription(lifecycleEvent.description);
        host.setServiceEntitlementNumber(lifecycleEvent.serviceEntitlementNumber);
        host.setAddonInstalled(true);
        hostRepository.save(host);
        log.info("Saved host");
        eventPublisher.publishEventAsynchronously(new AddonInstalledEvent(this, host));
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    public ResponseEntity uninstalled(@Valid @RequestBody LifecycleEvent lifecycleEvent, @AuthenticationPrincipal AtlassianHostUser hostUser) {
        assertExpectedEventType(lifecycleEvent, "uninstalled");

        Optional<AtlassianHost> maybeExistingHost = getHostFromLifecycleEvent(lifecycleEvent);
        if (hostUser == null) {
            if (maybeExistingHost.isPresent()) {
                log.error("Incoming installation request was not properly authenticated, but we have already installed " +
                        "the add-on for host " + lifecycleEvent.clientKey + ". Subsequent installation requests must " +
                        "include valid JWT. Returning 401.");
                return responseForMissingJwt();
            }
        } else {
            assertHostAuthorized(lifecycleEvent, hostUser);
        }

        if (maybeExistingHost.isPresent()) {
            AtlassianHost host = maybeExistingHost.get();
            host.setAddonInstalled(false);
            hostRepository.save(host);
            eventPublisher.publishEventAsynchronously(new AddonUninstalledEvent(this, host));
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    private Optional<AtlassianHost> getHostFromLifecycleEvent(LifecycleEvent lifecycleEvent) {
        return Optional.ofNullable(hostRepository.findOne(lifecycleEvent.clientKey));
    }

    private void assertExpectedEventType(LifecycleEvent lifecycleEvent, String expectedEventType) {
        String eventType = lifecycleEvent.eventType;
        if (!expectedEventType.equals(eventType)) {
            log.error(String.format("Received lifecycle callback with unexpected event type %s, expected %s", eventType, expectedEventType));
            throw new InvalidLifecycleEventTypeException();
        }
    }

    private void assertHostAuthorized(LifecycleEvent lifecycleEvent, AtlassianHostUser hostUser) {
        if (!hostUser.getHost().getClientKey().equals(lifecycleEvent.clientKey)) {
            log.error("Installation request was authenticated for host " + hostUser.getHost().getClientKey() +
                    ", but the host in the body of the request is " + lifecycleEvent.clientKey + ". Returning 403.");
            throw new HostForbiddenException();
        }
    }

    private ResponseEntity responseForMissingJwt() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.WWW_AUTHENTICATE, String.format("JWT realm=\"%s\"", addonDescriptorLoader.getDescriptor().getKey()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).build();
    }

    private ResponseEntity<Void> responseForUnauthorizedHost() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Invalid lifecycle event type")
    private static class InvalidLifecycleEventTypeException extends RuntimeException {}

    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    private static class HostForbiddenException extends RuntimeException {}
}
