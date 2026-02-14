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
        // This method will handle GET requests to /api/test and return a simple string
        // response
        // You can replace this with any logic you want to test, for now it simply
        // returns "Hello, World!"

        return "Hello, World!";
    }

    @PostMapping("test")
    public String postMethodName(@RequestBody String entity) {
        // You can add any processing logic here if needed
        // For now, it simply returns the received entity as a response
        // Assuming the entity is a simple string, you can return it directly

        return entity;
    }

}
