package com.example.demo;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
public class RegistrationController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService; // <-- ADDED THIS FIELD

    // ADDED UserService to the constructor
    public RegistrationController(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  UserService userService) { 
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService; // <-- ASSIGNMENT
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@RequestParam String username,
                                      @RequestParam String password,
                                      Model model) {

        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already exists!");
            return "register";
        }

        // 1. Create and Save User
        String hashed = passwordEncoder.encode(password);
        User user = new User(username, hashed);
        userRepository.save(user);

        // 2. Programmatically log the new user in (AUTO-LOGIN)
        UserDetails userDetails = userService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Redirect to the main feed
        return "redirect:/allposts";
    }
}