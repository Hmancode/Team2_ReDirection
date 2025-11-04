public class Feed extends ScreenState {
    Feed() {
        super("Feed");
    }

    public void display(PostTable postTable) {
        super.display();
        // display all available posts
        if (postTable.getPosts().isEmpty()) {
            System.out.println("...No posts available...");
            return;
        }
        System.out.println("Available Posts in Feed:");
        for (Post post : postTable.getPosts()) {
            System.out.println(post);
        }
    }
    
}
