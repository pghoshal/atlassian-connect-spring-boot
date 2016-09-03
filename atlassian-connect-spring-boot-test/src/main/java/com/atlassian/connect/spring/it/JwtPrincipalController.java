package com.atlassian.connect.spring.it;

import com.atlassian.connect.spring.AtlassianHostUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class JwtPrincipalController {


    @RequestMapping(value = "/jwt", method = GET, produces = "application/json")
    public AtlassianHostUser getPrincipal(@AuthenticationPrincipal AtlassianHostUser hostUser) {
        return hostUser;
    }
}
