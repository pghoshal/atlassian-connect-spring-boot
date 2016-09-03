package com.atlassian.connect.spring.it;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class PublicController {

    @RequestMapping(value = "/no-auth", method = GET, produces = "application/json")
    public String noAuth() {
        return "No authentication required";
    }
}
