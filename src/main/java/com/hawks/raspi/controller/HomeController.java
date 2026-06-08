package com.hawks.raspi.controller;

import com.hawks.raspi.Constants;
import com.hawks.raspi.helpers.IpAddress;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;

@RestController
@RequestMapping(value = "")
public class HomeController {

    @RequestMapping(method = RequestMethod.GET, value = "")
    ModelAndView index(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("index"); // This will look for index.html in /templates/
        mav.addObject("base_url", IpAddress.getDomain(request));
        return mav;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/set_password")
    String setPassword(@RequestParam("password") String password) {
        if ("".equals(password) || Constants.password != null) {
            return "error";
        } else {
            Constants.password = password;
            return "done";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/alive")
    public HashMap<String, Object> alive(HttpServletRequest request) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("status", 200);
        return map;
    }


}

