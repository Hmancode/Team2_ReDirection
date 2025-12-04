package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;
// ... (Javadoc omitted for brevity)

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Principal principal) {
        if (principal != null) {
            // THIS BLOCK: User is already logged in, redirect them immediately to the full forum feed
            return "redirect:/allposts";
        }
        // User is not logged in, show the home page with options
        return "home"; 
    }
}