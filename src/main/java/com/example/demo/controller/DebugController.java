package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DebugController {

    @GetMapping("/debug/echo-headers")
    public ResponseEntity<Map<String, String>> echoHeaders(@RequestHeader Map<String, String> headers) {
        // Return a subset to avoid huge output; header names will be lower-cased by default
        return ResponseEntity.ok(Map.of(
                "authorization", headers.getOrDefault("authorization", headers.getOrDefault("Authorization", "")),
                "host", headers.getOrDefault("host", ""),
                "user-agent", headers.getOrDefault("user-agent", "")
        ));
    }
}

