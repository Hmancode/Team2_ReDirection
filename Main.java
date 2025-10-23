import java.util.Scanner;

//This is the main file that requires you login before you post or view your feed
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserDatabase userDB = new UserDatabase();
        PostTable postTable = PostTable.loadFromFile("posts.dat");

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
                    System.out.println("🎉 Logged in as @" + username);
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
            System.out.println("3. Logout and Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter your post text: ");
                    String text = scanner.nextLine();
                    postTable.addPost(new Post(loggedInUser, text));
                    System.out.println(" Post created!");
                    break;

                case "2":
                    System.out.println("\n--- All Posts ---");
                    postTable.displayPosts();
                    break;

                case "3":
                    System.out.println("Saving data...");
                    postTable.saveToFile("posts.dat");
                    System.out.println("Logging out...");
                    running = false;
                    break;

                default:
                    System.out.println("loginInvalid choice.");
            }
        }

        scanner.close();
    }
}
