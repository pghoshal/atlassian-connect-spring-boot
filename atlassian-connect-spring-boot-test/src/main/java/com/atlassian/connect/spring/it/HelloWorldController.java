package com.atlassian.connect.spring.it;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.IgnoreJwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HelloWorldController {

    @IgnoreJwt
    @RequestMapping(value = "/hello-world", method = RequestMethod.GET)
    public ModelAndView helloWorld(@RequestParam String username) {
        ModelAndView model = new ModelAndView();
        model.setViewName("hello");
        model.addObject("userName", username);
        return model;
    }

    @RequestMapping(value = "/hello-world-jwt", method = RequestMethod.GET)
    public ModelAndView helloWorldJwt(AtlassianHostUser hostUser) {
        return helloWorld(hostUser.getUserKey().get());
    }
}
