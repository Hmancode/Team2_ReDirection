import java.util.Scanner;
// This is our main class, where the user interacts with the app.
// Right now, it requires a user register, aka create an account. 
// once the user is registered, their data is then saved. After that,
// the user can log in 

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserDatabase userDB = new UserDatabase();
        PostTable postTable = PostTable.loadFromFile("posts.dat");
        User user = null;

        System.out.println("Welcome to ReDirection!");

        String loggedInUser = null;

        // --- LOGIN / REGISTER PHASE ---
        while (loggedInUser == null) {
            System.out.println("\nChoose: register | login | exit");
            String option = scanner.nextLine().trim();

            if (option.equalsIgnoreCase("register")) {
                System.out.print("Username: ");
                String username = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();
                if (userDB.register(username, password)) {
                    System.out.println(" You can now log in!");
                }

            } else if (option.equalsIgnoreCase("login")) {
                System.out.print("Username: ");
                String username = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();
                if (userDB.login(username, password)) {
                    loggedInUser = username;
                    user = new User(username);
                    System.out.println("Logged in as @" + username);
                }

            } else if (option.equalsIgnoreCase("exit")) {
                System.out.println(" Exiting...");
                scanner.close();
                return;
            } else {
                System.out.println(" Invalid option.");
            }
        }

        // --- POSTING PHASE ---
        boolean running = true;
        while (running) {
            System.out.println("\n--- Menu ---");
            System.out.println("1. Create a new post");
            System.out.println("2. View all posts");
            System.out.println("3. Feed (view & interact with posts)");
            System.out.println("4. Save and exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                // --- CREATE POST ---
                case "1":
                    System.out.print("Enter your post text: ");
                    String text = scanner.nextLine();
                    postTable.addPost(new Post(loggedInUser, text));
                    System.out.println(" Post created!");
                    postTable.saveToFile("posts.dat");
                    break;

                // --- VIEW ALL POSTS ---
                case "2":
                    System.out.println("\n--- All Posts ---");
                    postTable.displayPosts();
                    break;

                // --- FEED ---
                case "3":
                    System.out.println("\n--- Social Feed ---");
                    if (postTable.getPosts().isEmpty()) {
                        System.out.println("No posts yet.");
                        break;
                    }

                    int postIndex = 0;
                    boolean viewingFeed = true;

                    while (viewingFeed && postIndex < postTable.getPosts().size()) {
                        Post post = postTable.getPosts().get(postIndex);

                        System.out.println("\n--------------------------------");
                        System.out.println(post);
                        System.out.println("Options:");
                        System.out.println("U - Upvote");
                        System.out.println("D - Downvote");
                        System.out.println("C - Comment");
                        System.out.println("N - Next post");
                        System.out.println("Q - Quit viewing feed");
                        System.out.print("Enter choice: ");

                        String action = scanner.nextLine().trim().toUpperCase();

                        switch (action) {
                            case "U":
                                post.upvote(loggedInUser);
                                postTable.saveToFile("posts.dat");
                                System.out.println("You upvoted the post!");
                                break;
                            case "D":
                                post.downvote(loggedInUser);
                                postTable.saveToFile("posts.dat");
                                System.out.println("You downvoted the post.");
                                break;
                            case "C":
                                System.out.print("Enter your comment: ");
                                String commentText = scanner.nextLine();
                                post.comment(loggedInUser, commentText);
                                postTable.saveToFile("posts.dat");
                                System.out.println("Comment added!");
                                break;
                            case "N":
                                postIndex++;
                                break;
                            case "Q":
                                viewingFeed = false;
                                break;
                            default:
                                System.out.println("Invalid option. Try again.");
                        }
                    }

                    if (postIndex >= postTable.getPosts().size()) {
                        System.out.println("\nYou’ve reached the end of the feed.");
                    }
                    break;

                // --- SAVE & EXIT ---
                case "4":
                    System.out.println("Saving data...");
                    postTable.saveToFile("posts.dat");
                    System.out.println("Logging out...");
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }

        scanner.close();
    }
}