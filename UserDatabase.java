import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//hash holds password connected to account
public class UserDatabase {
    private HashMap<String, String> users = new HashMap<>();
    private final String FILE_NAME = "users.txt";

    public UserDatabase() {
        loadUsersFromFile();
    }

    //if someone tries to make a new account, check if name exists
    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            System.out.println("Username already exists.");
            return false;
        }
        //if not, then link username to password yay
        users.put(username, password);
        saveUsersToFile();
        System.out.println("User registered successfully!");
        return true;
    }
    //if signing in and username is not in the database, error
    public boolean login(String username, String password) {
        if (!users.containsKey(username)) {
            System.out.println("Username not found.");
            return false;
        }
        //else, and password matches, successful. if password doesnt match then incorrect
        if (users.get(username).equals(password)) {
            System.out.println("Login successful!");
            return true;
        } else {
            System.out.println("Incorrect password.");
            return false;
        }
    }

    //arbitrary, saves the names into a text file so that when a new
    //session starts, old data gets saved. 
    private void saveUsersToFile() {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {
            for (String username : users.keySet()) {
                writer.write(username + ":" + users.get(username) + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    private void loadUsersFromFile() {
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) return; // if file doesn’t exist yet, skip

            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(":");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }
}
