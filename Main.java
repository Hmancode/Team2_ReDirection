import java.util.Scanner;
<<<<<<< HEAD
// This is our main class, where the user interacts with the app.
// Right now, it requires a user register, aka create an account. 
// once the user is registered, their data is then saved. After that,
// the user can log in 
=======
>>>>>>> e2d43645516bf596a67f7c54bf6af72860814441

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
<<<<<<< HEAD
                    System.out.println("Logged in as @" + username);
=======
                    System.out.println("🎉 Logged in as @" + username);
>>>>>>> e2d43645516bf596a67f7c54bf6af72860814441
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
<<<<<<< HEAD
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
=======
            System.out.println("3. Interact with post");
            System.out.println("4. Feed (view and interacct with posts)");
            System.out.println("5. save and exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) { //create new post
                case "1":
                    System.out.print("Enter your post text: ");
                    String text = scanner.nextLine(); //takes user input
                    postTable.addPost(new Post(loggedInUser, text)); 
                    System.out.println(" Post created!");
                    break;

>>>>>>> e2d43645516bf596a67f7c54bf6af72860814441
                case "2":
                    System.out.println("\n--- All Posts ---");
                    postTable.displayPosts();
                    break;

<<<<<<< HEAD
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
=======
                
                case "3": // this should be replaced with "see feed" and on the feed you can choose to interact with a pose -hj
                    System.out.println("\n--- Interact with a Post ---");
                    postTable.displayPosts(); // Show posts with indexes or IDs

                    System.out.print("Enter the post number to interact with: ");
                    try {
                        int index = Integer.parseInt(scanner.nextLine());
                        if (index >= 0 && index < postTable.getPosts().size()) {
                        Post post = postTable.getPosts().get(index);

                        boolean interacting = true;
                        while (interacting) {
                        System.out.println("\nYou selected @" + post.getUsername() + "'s post:");
                        System.out.println(post);
                        System.out.println("Choose an action:");
                        System.out.println("1. Like ");
                        System.out.println("2. Downvote ");
                        System.out.println("3. Comment ");
                        System.out.println("4. Go back");
                        System.out.print("Enter choice: ");

                        String action = scanner.nextLine();

                        switch (action) {
                            case "1":
                                post.like();
                                System.out.println("You liked the post!");
                                break;
                            case "2":
                                post.downvote();
                                System.out.println("You downvoted the post.");
                                break;
                            case "3":
                                System.out.print("Enter your comment: ");
                                String commentText = scanner.nextLine();
                                post.comment(loggedInUser, commentText);
                                System.out.println("Comment added!");
                                break;
                            case "4":
                                interacting = false;
                                break;
                            default:
                                System.out.println("Invalid choice. Try again.");
                        }

                        // Save after each interaction
                        postTable.saveToFile("posts.dat");
                        }
                        } else {
                        System.out.println("Invalid post number.");
                        }
                    } catch (NumberFormatException e) {
                      System.out.println("Please enter a valid number.");
                    }
                    break;
                case "4":
                    System.out.println("\n--- Feed ---");
                    user.getFeed().display(postTable);
                    // display posts in feed
                    break;
                case "5":
>>>>>>> e2d43645516bf596a67f7c54bf6af72860814441
                    System.out.println("Saving data...");
                    postTable.saveToFile("posts.dat");
                    System.out.println("Logging out...");
                    running = false;
                    break;

                default:
<<<<<<< HEAD
                    System.out.println("Invalid choice.");
=======
                    System.out.println("loginInvalid choice.");
>>>>>>> e2d43645516bf596a67f7c54bf6af72860814441
            }
        }

        scanner.close();
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> e2d43645516bf596a67f7c54bf6af72860814441
