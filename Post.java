import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

//This class is a Post class, which implements serializable to 
//save the data throughout sessions.
public class Post implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String text;
    private LocalDateTime timestamp;
    private int likes;
    private ArrayList<String> comments;
    private int downvotes;

    public Post(String username, String text) {
        this.username = username;
        this.text = text;
        this.timestamp = LocalDateTime.now();
        this.likes = 0;
        this.comments = new ArrayList<String>();
    }
    //getUsername returns the users username when called.
    public String getUsername()  
    { 
        return username; 
    }
    //getText returns the text post when called.
    public String getText() 
    { 
        return text; 
    }
    //getTimeStamp returns the timestamp of when a post was posted when called
    public LocalDateTime getTimestamp() 
    { 
        return timestamp; 
    }
    //getLikes returns the number of likes that a post has when called
    public int getUpvotes() 
    { 
        return likes; 
    }
    //getDownvotes returns number of downvotes a post has when called
    public int getDownvotes()
    {
        return downvotes;
    }
    //getComments returns the comments under a given post when called. 
    public ArrayList<String> getComments() 
    { 
        return comments; 
    }
    //like, when implemented, increments the like counter on a post and the getLikes method displays this new value
    public void like() 
    { 
        likes ++; 
    }
    //downvote, when implemented, increments the downvote counter. Does nothing to upvotes.
    public void downvote()
    {
        downvotes ++;
    }
    //comment allows the user to comment on a given post
    public void comment(String user, String comment) 
    { 
        comments.add("@" + user + ": " + comment) ; 
    }

    //Formats the post. This overrides the parent classes toString method, and returns this instead. 
    @Override 
    public String toString() 
    {
        return "Post by @" + username + " at " + timestamp +
               "\nText: " + text +
               "\nLikes: " + likes + ", Comments: " + comments + "\n";
    }
}
