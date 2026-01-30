package com.sma.core.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/healthcheck")
public class HealthcheckController {

    @GetMapping
    public String healthcheck() {
        log.info("Healthcheck endpoint called");
        return "OK";
    }
}
