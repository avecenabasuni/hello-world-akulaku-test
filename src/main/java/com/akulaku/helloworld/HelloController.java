package com.akulaku.helloworld; 

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello, World! Welcome to my first Spring Boot application for Akulaku SRE (v2)";
    }

    // Health check is now handled by Spring Boot Actuator at:
    //   /actuator/health
    //   /actuator/health/liveness
    //   /actuator/health/readiness
    //   /actuator/prometheus (for metrics)
}