package com.hawks.raspi.controller;

import com.hawks.raspi.helpers.IpAddress;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping(value = "")
public class HomeController {

    @RequestMapping(method = RequestMethod.GET, value = "")
    ModelAndView index(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("index"); // This will look for index.html in /templates/
        mav.addObject("base_url", IpAddress.getDomain(request));
        return mav;
    }

}

