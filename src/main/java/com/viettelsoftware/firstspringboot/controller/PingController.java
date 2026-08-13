package com.viettelsoftware.firstspringboot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {
    @GetMapping("/ping")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ping() {}
}
