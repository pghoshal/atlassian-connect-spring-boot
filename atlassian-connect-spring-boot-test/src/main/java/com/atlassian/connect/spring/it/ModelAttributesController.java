package com.atlassian.connect.spring.it;

import com.atlassian.connect.spring.IgnoreJwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

@Controller
public class ModelAttributesController {

    @IgnoreJwt
    @RequestMapping(value = "/model-public", method = RequestMethod.GET)
    public ModelAndView getAnonymousModelAttributes(Model model) {
        return new ModelAndView(new MappingJackson2JsonView(), model.asMap());
    }

    @RequestMapping(value = "/model", method = RequestMethod.GET)
    public ModelAndView getModelAttributes(Model model) {
        return getAnonymousModelAttributes(model);
    }
}
