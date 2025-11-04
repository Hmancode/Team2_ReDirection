import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.ObjectInputStream;
import java.io.IOException;

// This class is a Post class, which implements Serializable to 
// save the data throughout sessions.
public class Post implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String text;
    private LocalDateTime timestamp;
    private int upvotes;
    private int downvotes;
    private ArrayList<String> comments;

    // Keep track of which users already voted
    private HashSet<String> upvoters;
    private HashSet<String> downvoters;

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

    // getUsername returns the user's username when called.
    public String getUsername() { 
        return username; 
    }

    // getText returns the text post when called.
    public String getText() { 
        return text; 
    }

    // getTimestamp returns the time a post was posted.
    public LocalDateTime getTimestamp() { 
        return timestamp; 
    }

    // getUpvotes returns the number of upvotes on a post.
    public int getUpvotes() { 
        return upvotes; 
    }

    // getDownvotes returns the number of downvotes on a post.
    public int getDownvotes() { 
        return downvotes; 
    }

    // getComments returns the comments under a post.
    public ArrayList<String> getComments() { 
        return comments; 
    }

    // upvote adds an upvote if the user hasn't upvoted yet.
    public void upvote(String user) {
        if (upvoters.contains(user)) {
            System.out.println("⚠️ You already upvoted this post.");
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

    // downvote adds a downvote if the user hasn't downvoted yet.
    public void downvote(String user) {
        if (downvoters.contains(user)) {
            System.out.println("⚠️ You already downvoted this post.");
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

    // comment allows the user to comment on a post.
    public void comment(String user, String comment) { 
        comments.add("@" + user + ": " + comment); 
    }

    // Formats the post for display.
    @Override 
    public String toString() {
        return "Post by @" + username + " at " + timestamp +
               "\nText: " + text +
               "\nUpvotes: " + upvotes + " | Downvotes: " + downvotes +
               "\nComments: " + comments + "\n";
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject(); // perform default deserialization

    // initialize fields that may be null when reading older saved objects
    if (upvoters == null) upvoters = new HashSet<>();
    if (downvoters == null) downvoters = new HashSet<>();
    if (comments == null) comments = new ArrayList<>();
}
}
