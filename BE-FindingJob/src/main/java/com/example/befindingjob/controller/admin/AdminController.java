package com.example.befindingjob.controller.admin;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping(value = "/tips", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> getTipsHtml() {
        Resource resource = new ClassPathResource("static/tips.html");
        return ResponseEntity.ok().body(resource);
    }
}
