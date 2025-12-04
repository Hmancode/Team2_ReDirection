package com.redirection.app.model;

import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

// This class represents a Post and implements Serializable
// to preserve data across sessions.

public class Post implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String text;
    private LocalDateTime timestamp;
    private int upvotes;
    private int downvotes;
    private ArrayList<String> comments;

    // Keep track of which users already voted
    private transient HashSet<String> upvoters;
    private transient HashSet<String> downvoters;

    // Constructor
    public Post(String username, String text) {
        this.username = username;
        this.text = text;
        this.timestamp = LocalDateTime.now();
        this.upvotes = 0;
        this.downvotes = 0;
        this.comments = new ArrayList<>();
        this.upvoters = new HashSet<>();
        this.downvoters = new HashSet<>();
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    // Voting methods
    public void upvote(String user) {
        if (upvoters.contains(user)) {
            System.out.println("You already upvoted this post.");
            return;
        }
        if (downvoters.contains(user)) {
            downvoters.remove(user);
            downvotes--;
        }
        upvoters.add(user);
        upvotes++;
        System.out.println("You upvoted this post!");
    }

    public void downvote(String user) {
        if (downvoters.contains(user)) {
            System.out.println("You already downvoted this post.");
            return;
        }
        if (upvoters.contains(user)) {
            upvoters.remove(user);
            upvotes--;
        }
        downvoters.add(user);
        downvotes++;
        System.out.println("You downvoted this post.");
    }

    // Add a comment
    public void comment(String user, String comment) {
        comments.add("@" + user + ": " + comment);
    }

    // Handle deserialization
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // Initialize sets if missing from old saves
        if (upvoters == null) upvoters = new HashSet<>();
        if (downvoters == null) downvoters = new HashSet<>();
        if (comments == null) comments = new ArrayList<>();
    }

    // Format for display
    @Override
    public String toString() {
        return "Post by @" + username + " at " + timestamp +
               "\nText: " + text +
               "\nUpvotes: " + upvotes + " | Downvotes: " + downvotes +
               "\nComments: " + comments + "\n";
    }
}