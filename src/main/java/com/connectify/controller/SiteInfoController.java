package com.connectify.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SiteInfoController {

    @GetMapping("/terms")
    public String terms() {
        return "legal/terms";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "legal/privacy";
    }

    @GetMapping("/cookies")
    public String cookies() {
        return "legal/cookies";
    }

    @GetMapping("/help")
    public String help() {
        return "help/index";
    }
}
