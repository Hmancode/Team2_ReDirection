// Bridge between DB and Main - hj

public class User {
    private String username;
    private String password;
    private Profile profile;
    private Feed feed;

    public User(String username) {
        this.username = username;
        this.profile = new Profile(username);
        this.feed = new Feed();
    }

    public Feed getFeed() {
        return feed;
    }

    public boolean set(String username) {
        //reload data from database based on username
        return true;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Profile getProfile() {
        return profile;
    }
}
