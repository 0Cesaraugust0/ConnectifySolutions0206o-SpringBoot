package com.connectify.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PrototypeController {

    @GetMapping("/prototype")
    public String prototype() {
        return "prototype/index";
    }
}
