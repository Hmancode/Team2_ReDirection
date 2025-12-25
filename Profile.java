public class Profile {
    private String username;
    private String bio;
    private PostTable posts;

    public Profile(String username) {
        this.username = username;
        this.bio = "";
        this.posts = new PostTable();
    }

    public PostTable getPosts() {
        return posts;
    }

    public String getUsername() {
        return username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @Override
    public String toString() {
        return "Username: " + username + "\nBio: " + bio;
    }
}