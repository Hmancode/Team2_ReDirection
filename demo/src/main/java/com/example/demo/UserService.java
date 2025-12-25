package com.example.demo;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    // For login: Returns the User entity, which now implements UserDetails
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // The User entity (which is a UserDetails) is returned directly
        return repo.findByUsername(username)
                   .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username)); // TEST TO MAKE SURE THIS ACTUALLY THROWS!
    }

    // helper to register
    public User register(String username, String passwordHash) {
        User u = new User(username, passwordHash);
        return repo.save(u);
    }
}