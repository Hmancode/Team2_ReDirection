package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
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

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
            return "dashboard";
        }
        // Redirect to login if not authenticated
        return "redirect:/login";
    }

    @GetMapping("/resources")
    public String resources(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
            return "resources";
        }
        return "redirect:/login";
    }

    @GetMapping("/people")
    public String people(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
            return "people";
        }
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
            return "profile";
        }
        return "redirect:/login";
    }
}