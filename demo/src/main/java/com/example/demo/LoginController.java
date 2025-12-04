package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    private String username;
    private String password; 

    @GetMapping("/login") // the login (already registered)
    public String login(String u, String p) {
        username = u;
        password = p; 
        return "login";
    }
}
