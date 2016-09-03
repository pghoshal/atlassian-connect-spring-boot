package com.atlassian.connect.spring.it;

import com.atlassian.connect.spring.IgnoreJwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class AddonDataController {

    private static final String ADDONS_PATH = "/rest/atlassian-connect/1/addons/";

    @Value("${add-on.key}")
    private String addonKey;

    @Autowired
    private RestTemplate restTemplate;

    @IgnoreJwt
    @RequestMapping(value = "/addon-public", method = GET, produces = "application/json")
    @ResponseBody
    public String getAddonPublic() {
        return restTemplate.getForEntity("http://localhost:2990/jira" + ADDONS_PATH + addonKey, String.class).getBody();
    }

    @RequestMapping(value = "/addon", method = GET)
    public ModelAndView getAddon() throws IOException {
        String addon = prettyPrintJson(restTemplate.getForEntity(ADDONS_PATH + addonKey, String.class).getBody());
        ModelAndView model = new ModelAndView();
        model.setViewName("addon");
        model.addObject("addon", addon);
        return model;
    }

    private String prettyPrintJson(String addon) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Object json = mapper.readValue(addon, Object.class);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }

}
