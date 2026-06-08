package com.hawks.raspi.controller;

import com.hawks.raspi.Constants;
import com.hawks.raspi.helpers.IpAddress;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;

@RestController
@RequestMapping(value = "bash")
public class BashController {

    @RequestMapping(method = RequestMethod.GET, value = "/auth")
    ModelAndView index(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("password");
        mav.addObject("action", "/bash/ui?");
        return mav;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/ui")
    ModelAndView index2(HttpServletRequest request, @RequestParam("password") String password) {
        if (Constants.password.equals(password)) {
            ModelAndView mav = new ModelAndView("bash");
            mav.addObject("url", IpAddress.getDomain(request) + "bash");
            return mav;
        } else {
            ModelAndView mav = new ModelAndView("error");
            return mav;
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> index(HttpServletRequest request, @RequestBody HashMap<String, Object> body) throws IOException, InterruptedException {
        HashMap<String, Object> response = new HashMap<>();
        try {
            String command = (String) body.get("cmd");
            if (!StringUtils.hasText(command)) {
                return ResponseEntity.internalServerError().body("Invalid cmd!");
            }

            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            if (os.contains("win")) {
                pb = new ProcessBuilder("cmd.exe", "/c", command);
            } else {
                pb = new ProcessBuilder("bash", "-c", command);
            }

            Process process = pb.start();

            String output = new String(process.getInputStream().readAllBytes());
            String error = new String(process.getErrorStream().readAllBytes());

            process.waitFor();

            if (!output.isEmpty()) {
                response.put("output", output);
            }
            if (!error.isEmpty()) {
                response.put("error", error);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to retrieve url: " + e.getMessage());
        }
    }

}

