package com.taxyaar.sign.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("test")
    public String test() {
        return "Hello, World!";
    }

    @PostMapping("test")
    public String postMethodName(@RequestBody String entity) {

        return entity;
    }

}
