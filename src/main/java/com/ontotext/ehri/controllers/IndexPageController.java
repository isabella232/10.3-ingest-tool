package com.ontotext.ehri.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Boyan on 22-Nov-16.
 */
@Controller
@RequestMapping(value = "/")
public class IndexPageController {

    @RequestMapping(method = RequestMethod.GET)
    public String home() {
        return "index";
    }
}
