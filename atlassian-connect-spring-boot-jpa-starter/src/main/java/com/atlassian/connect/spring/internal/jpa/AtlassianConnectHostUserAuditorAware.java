package com.atlassian.connect.spring.internal.jpa;

import com.atlassian.connect.spring.AtlassianHostUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AtlassianConnectHostUserAuditorAware implements AuditorAware<String> {

    @Override
    public String getCurrentAuditor() {
        Optional<String> optionalUserKey = Optional.empty();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof AtlassianHostUser) {
                AtlassianHostUser hostUser = (AtlassianHostUser) principal;
                optionalUserKey = hostUser.getUserKey();
            }
        }
        return optionalUserKey.orElse(null);
    }
}
