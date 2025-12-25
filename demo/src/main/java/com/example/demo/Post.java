package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String text;
    private LocalDateTime timestamp = LocalDateTime.now();

    private int upvotes = 0;
    private int downvotes = 0;

    @ElementCollection
    private List<String> comments = new ArrayList<>();

    // For preventing multiple votes
    @ElementCollection
    private Set<String> upvoters = new HashSet<>();

    @ElementCollection
    private Set<String> downvoters = new HashSet<>();

    public Post() {}

    public Post(String username, String text) {
        this.username = username;
        this.text = text;
    }

    // Voting logic
    public void upvote(String user) {
    if (!upvoters.contains(user)) {
        upvoters.add(user);
        upvotes++;
        downvoters.remove(user);
    }
}


    public void downvote(String user) {
    if (!downvoters.contains(user)) {
        downvoters.add(user);
        downvotes++;
        upvoters.remove(user);
    }
}


    // Comment
    public void addComment(String user, String commentText) {
        comments.add("@" + user + ": " + commentText);
    }

    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getText() { return text; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public int getUpvotes() { return upvotes; }
    public int getDownvotes() { return downvotes; }
    public List<String> getComments() { return comments; }
}
