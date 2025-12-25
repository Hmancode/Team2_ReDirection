package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal; // <-- Ensure this line is present
import java.util.List;
import java.util.Comparator; // Added for sorting

@Controller
public class PostController {

    @Autowired
    private PostRepository postRepository;

    // --- 1. My Posts Feed (/posts) ---
    // Shows only the logged-in user's posts (e.g., for a "My Activity" tab)
    @GetMapping("/posts")
    public String showMyPosts(Model model, Principal principal) {
        String username = principal.getName();
        // Fetch only posts belonging to the current user
        List<Post> myPosts = postRepository.findByUsername(username);
        
        // Sort by newest first
        myPosts.sort(Comparator.comparing(Post::getTimestamp).reversed());
        
        model.addAttribute("posts", myPosts);
        model.addAttribute("username", username);
        model.addAttribute("pageTitle", "My Posts");
        
        return "posts"; 
    }
    
    // --- 2. Community Feed (/allposts) - The main forum view (Requirement 3 target) ---
    @GetMapping("/allposts")
    public String showAllPosts(Model model, Principal principal) {
        // Fetch ALL posts for the community feed (the main forum view)
        List<Post> allPosts = postRepository.findAll();
        
        // Sort by newest first
        allPosts.sort(Comparator.comparing(Post::getTimestamp).reversed());

        model.addAttribute("posts", allPosts);
        model.addAttribute("username", principal != null ? principal.getName() : null); // Needed for the post creation form
        model.addAttribute("pageTitle", "Community Feed");
        
        return "posts"; 
    }

    // --- 3. Post Creation ---
    @PostMapping("/posts")
    public String createPost(@RequestParam String text, Principal principal) {
        String username = principal.getName();
        Post post = new Post(username, text);
        postRepository.save(post);
        // Redirect to the full community feed (/allposts) after creation 
        return "redirect:/allposts"; 
    }

    // --- 4. Voting and Commenting ---
    // Actions redirect back to the main forum feed for a smooth experience
    @PostMapping("/posts/{id}/upvote")
    public String upvote(@PathVariable Long id, Principal principal) {
        String user = principal.getName();
        postRepository.findById(id).ifPresent(p -> {
            p.upvote(user);
            postRepository.save(p);
        });
        return "redirect:/allposts"; 
    }

    @PostMapping("/posts/{id}/downvote")
    public String downvote(@PathVariable Long id, Principal principal) {
        String user = principal.getName();
        postRepository.findById(id).ifPresent(p -> {
            p.downvote(user);
            postRepository.save(p);
        });
        return "redirect:/allposts";
    }

    @PostMapping("/posts/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam String comment,
                             Principal principal) {
        String user = principal.getName();
        postRepository.findById(id).ifPresent(p -> {
            p.addComment(user, comment);
            postRepository.save(p);
        });
        return "redirect:/allposts";
    }
}