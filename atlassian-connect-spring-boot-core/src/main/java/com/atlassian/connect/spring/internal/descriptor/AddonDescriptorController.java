package com.atlassian.connect.spring.internal.descriptor;

import com.atlassian.connect.spring.IgnoreJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * A controller that serves the Atlassian Connect add-on descriptor (<code>atlassian-connect.json</code>).
 */
@IgnoreJwt
@Controller
public class AddonDescriptorController {

    @Autowired
    private AddonDescriptorLoader addonDescriptorLoader;

    @RequestMapping(value = "/", method = GET)
    public RedirectView getRoot() {
        RedirectView redirectView = new RedirectView("/atlassian-connect.json");
        redirectView.setHttp10Compatible(false);
        redirectView.setStatusCode(HttpStatus.FOUND);
        return redirectView;
    }

    @RequestMapping(value = "/atlassian-connect.json", method = GET, produces = "application/json")
    @ResponseBody
    public String getDescriptor() throws IOException {
        return addonDescriptorLoader.getRawDescriptor();
    }
}
