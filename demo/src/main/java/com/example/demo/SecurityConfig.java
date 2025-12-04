package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final UserService userService; // <--- Field must be here

    // <--- Constructor injection must be here --->
    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // 1. Configure CSRF: Keep it ENABLED globally (the default) but IGNORE the H2 console path.
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**")) 
            
            // 2. Authorization Rules
            .authorizeHttpRequests(auth -> auth
                // Allow public access to these paths (H2 console MUST be here)
                .requestMatchers("/", "/register", "/login", "/css/**", "/js/**", "/images/**", "/h2-console/**")
                .permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // 3. Form Login Configuration
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true) 
                .permitAll()
            )
            
            // 4. Logout Configuration
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            
            // 5. Frame Options for H2 Console: REQUIRED for H2 to work in a browser iframe
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}