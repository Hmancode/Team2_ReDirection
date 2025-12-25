package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    // Spring Data JPA generates: SELECT * FROM post WHERE username = ?
    List<Post> findByUsername(String username);
}