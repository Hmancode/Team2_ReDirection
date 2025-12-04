# ReDirection - Technical Documentation

## Overview
ReDirection is a Spring Boot-based web forum application with user authentication, post creation, voting, and commenting features. It uses Java 17, Spring Security for authentication, JPA/Hibernate for database operations, and Thymeleaf for server-side template rendering.

**Technology Stack:**
- Backend: Java 17, Spring Boot 3.5.7
- Database: H2 (in-memory)
- Security: Spring Security with BCrypt password encoding
- Templating: Thymeleaf
- Build Tool: Maven
- Server: Runs on port 8083

---

## Project Structure

```
demo/
├── pom.xml                              # Maven configuration with dependencies
├── src/main/
│   ├── java/com/example/demo/
│   │   ├── ReDirectionApplication.java  # Spring Boot entry point
│   │   ├── User.java                    # User entity (implements UserDetails)
│   │   ├── Post.java                    # Post entity with voting/comments
│   │   ├── UserRepository.java          # JPA repository for User queries
│   │   ├── PostRepository.java          # JPA repository for Post queries
│   │   ├── UserService.java             # Spring service for user details loading
│   │   ├── SecurityConfig.java          # Spring Security configuration
│   │   ├── HomeController.java          # Home page routing
│   │   ├── LoginController.java         # Login page routing
│   │   ├── RegistrationController.java  # Registration logic & auto-login
│   │   ├── PostController.java          # Post CRUD & voting operations
│   │   └── TestController.java          # Testing/debug endpoints
│   └── resources/
│       ├── application.properties       # Server configuration
│       └── templates/
│           ├── home.html                # Landing page (unauthenticated)
│           ├── login.html               # Login form
│           ├── register.html            # Registration form
│           ├── posts.html               # Forum feed (authenticated)
│           └── forum.html               # Alternate forum view
└── src/test/                           # Unit tests
```

---

## Core Entities

### User Entity (`User.java`)

**Database Table:** `app_user`

**Fields:**
- `id` (Long, PK, Auto-generated): Unique user identifier
- `username` (String, Unique): User login name
- `passwordHash` (String): BCrypt hashed password
- `role` (String): Authorization role (default: "USER")

**Key Methods:**
- `implements UserDetails`: Makes User compatible with Spring Security
- `getAuthorities()`: Returns granted authorities based on role
- `getPassword()`: Returns passwordHash for Spring Security
- `getUsername()`: Returns username for authentication
- Account status methods: `isAccountNonExpired()`, `isAccountNonLocked()`, `isCredentialsNonExpired()`, `isEnabled()` (all return true)

**Usage:** Spring Security loads this entity when authenticating users. The `UserDetails` interface ensures Spring can validate credentials and authorities.

---

### Post Entity (`Post.java`)

**Database Table:** `post`

**Fields:**
- `id` (Long, PK, Auto-generated): Unique post identifier
- `username` (String): Author of the post
- `text` (String): Post content
- `timestamp` (LocalDateTime): Creation time (defaults to current time)
- `upvotes` (int): Count of upvotes
- `downvotes` (int): Count of downvotes
- `comments` (List<String>): List of formatted comments
- `upvoters` (Set<String>): Usernames that upvoted (prevents duplicate votes)
- `downvoters` (Set<String>): Usernames that downvoted (prevents duplicate votes)

**Key Methods:**
- `upvote(String user)`: Adds user to upvoters if not already present, increments upvotes, removes from downvoters
- `downvote(String user)`: Adds user to downvoters if not already present, increments downvotes, removes from upvoters
- `addComment(String user, String commentText)`: Appends formatted comment "@username: text"
- Getters for all fields

**Voting Logic:** 
- Each user can only upvote OR downvote once per post
- Changing vote removes the previous vote
- Votes are tracked in Sets to prevent duplicates

---

## Repositories (Data Access Layer)

### UserRepository (`UserRepository.java`)
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```
**Methods:**
- `findByUsername(String username)`: Spring Data JPA auto-generates query: `SELECT * FROM app_user WHERE username = ?`
- Inherited CRUD methods: `save()`, `findById()`, `findAll()`, `delete()`

---

### PostRepository (`PostRepository.java`)
```java
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUsername(String username);
}
```
**Methods:**
- `findByUsername(String username)`: Auto-generates: `SELECT * FROM post WHERE username = ?`
- Inherited CRUD methods for all Post operations

---

## Services

### UserService (`UserService.java`)

Implements `UserDetailsService` for Spring Security integration.

**Key Methods:**
- `loadUserByUsername(String username)`: 
  - Called by Spring Security during login
  - Queries UserRepository for username
  - Returns User entity (which implements UserDetails)
  - Throws `UsernameNotFoundException` if user doesn't exist

- `register(String username, String passwordHash)`:
  - Helper method to create new User
  - Saves to repository and returns saved entity

**Purpose:** Acts as the bridge between Spring Security and the User database. Spring Security calls this to load user details during authentication.

---

## Security Configuration (`SecurityConfig.java`)

**Components:**

1. **PasswordEncoder Bean:**
   ```java
   @Bean
   public PasswordEncoder passwordEncoder() {
       return new BCryptPasswordEncoder();
   }
   ```
   - Uses BCrypt for password hashing
   - Salts automatically, makes passwords unreadable

2. **DaoAuthenticationProvider Bean:**
   ```java
   provider.setUserDetailsService(userService);
   provider.setPasswordEncoder(passwordEncoder());
   ```
   - Configures Spring Security to use UserService for loading user details
   - Configures BCrypt for password verification

3. **SecurityFilterChain Bean - Public Routes:**
   - `/` - Home page
   - `/register` - Registration page
   - `/login` - Login page
   - `/css/**`, `/js/**`, `/images/**` - Static resources
   - `/h2-console/**` - H2 database admin (development only)

4. **SecurityFilterChain Bean - Protected Routes:**
   - All other routes require authentication

5. **Form Login Configuration:**
   - Login page: `/login`
   - Default success URL: `/allposts` (redirects authenticated users to forum feed)

6. **Logout Configuration:**
   - Success URL: `/login?logout`

7. **CSRF Protection:**
   - Globally enabled
   - Disabled for H2 console: `.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))`

8. **Frame Options:**
   - Disabled for H2 console access in browser

---

## Controllers

### HomeController (`HomeController.java`)

**Route: GET `/`**

```java
@GetMapping("/")
public String home(Principal principal) {
    if (principal != null) {
        return "redirect:/allposts";  // Logged-in user
    }
    return "home";  // Unauthenticated user
}
```

**Logic:**
- If user is authenticated (Principal is not null), redirect to `/allposts` (main feed)
- If unauthenticated, show home page with login/register options

---

### LoginController (`LoginController.java`)

**Route: GET `/login`**

```java
@GetMapping("/login")
public String login(String u, String p) {
    // Note: This is a placeholder; actual authentication is handled by Spring Security
    return "login";
}
```

**Note:** This controller has minimal functionality. Actual login form submission is handled by Spring Security's default login processing.

---

### RegistrationController (`RegistrationController.java`)

**Route 1: GET `/register`**
- Returns registration form template

**Route 2: POST `/register`**
```java
@PostMapping("/register")
public String processRegistration(@RequestParam String username,
                                  @RequestParam String password,
                                  Model model)
```

**Process:**
1. Check if username already exists
   - If exists: return register page with error message
2. Hash password using BCryptPasswordEncoder
3. Create new User entity with hashed password
4. Save to UserRepository
5. **Auto-login:**
   - Load UserDetails from UserService
   - Create `UsernamePasswordAuthenticationToken` with user authorities
   - Set authentication in SecurityContext
6. Redirect to `/allposts` (main forum feed)

**Key Feature:** Users are automatically logged in after registration (no need to manually login)

---

### PostController (`PostController.java`)

**Route 1: GET `/posts`**
```java
@GetMapping("/posts")
public String showMyPosts(Model model, Principal principal)
```
- Fetches posts created by logged-in user only
- Sorts by newest first
- Returns "posts" template with user's posts
- Used for "My Activity" or personal feed view

**Route 2: GET `/allposts`** (Main Forum Feed)
```java
@GetMapping("/allposts")
public String showAllPosts(Model model, Principal principal)
```
- Fetches ALL posts from all users
- Sorts by newest first
- Returns "posts" template with community feed
- This is the primary forum view after login

**Route 3: POST `/posts`** (Create Post)
```java
@PostMapping("/posts")
public String createPost(@RequestParam String text, Principal principal)
```
- Gets logged-in username from Principal
- Creates new Post with username and text
- Saves to PostRepository
- Redirects to `/allposts` (refresh feed with new post)

**Route 4: POST `/posts/{id}/upvote`** (Upvote Post)
```java
@PostMapping("/posts/{id}/upvote")
public String upvote(@PathVariable Long id, Principal principal)
```
- Gets post by ID
- Calls `post.upvote(username)` (handles vote logic and duplicate prevention)
- Saves post
- Redirects to `/allposts`

**Route 5: POST `/posts/{id}/downvote`** (Downvote Post)
- Same as upvote but calls `post.downvote(username)`

**Route 6: POST `/posts/{id}/comment`** (Add Comment)
```java
@PostMapping("/posts/{id}/comment")
public String addComment(@PathVariable Long id,
                         @RequestParam String comment,
                         Principal principal)
```
- Gets post by ID
- Calls `post.addComment(username, commentText)`
- Saves post
- Redirects to `/allposts`

---

## Authentication & Authorization Flow

```
Unauthenticated User
    ↓
GET / (HomeController)
    ↓
Redirects to login/home page (choose register or login)
    ↓
[If Registering]
POST /register (RegistrationController)
    ├─ Check username exists
    ├─ Hash password (BCrypt)
    ├─ Save User to DB
    ├─ Auto-login user (set SecurityContext)
    └─ Redirect to /allposts
    
[If Logging In]
Form submission to /login → Spring Security handles
    ├─ Spring Security calls UserService.loadUserByUsername()
    ├─ Compares submitted password vs BCrypt hash
    ├─ If match: creates Authentication token
    ├─ Sets SecurityContext
    └─ Redirects to /allposts (defaultSuccessUrl)

Authenticated User
    ↓
Can access: /allposts, /posts, /register, /login, etc.
All actions (create, vote, comment) tied to Principal.getName()
```

---

## Database & Persistence

**Database:** H2 (in-memory)
- Configured in Spring Boot automatically
- Data persists during application runtime
- Data is lost when application restarts

**JPA/Hibernate:**
- Annotations: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@ElementCollection`
- Repositories extend `JpaRepository` for automatic CRUD implementation
- Spring Data JPA generates SQL queries from method names

**Key Database Features:**
- Username uniqueness constraint: `@Column(unique = true)` on User.username
- Auto-increment IDs for User and Post
- ElementCollection tables for List/Set fields (comments, upvoters, downvoters)

---

## Frontend (Thymeleaf Templates)

### Template Processing
- Thymeleaf is server-side template engine
- Processes templates with `th:` attributes
- Binds backend data to HTML using Spring Model objects
- Generates final HTML sent to browser

### posts.html (Main Forum)
**Key Sections:**
- Post creation form (POST /posts)
- List of all posts with:
  - Author username
  - Post text
  - Timestamp
  - Vote counts (upvotes/downvotes)
  - Vote buttons (POST /posts/{id}/upvote, POST /posts/{id}/downvote)
  - Comment input and display (POST /posts/{id}/comment)

**Data from Controller:**
- `posts` (List<Post>): Posts to display
- `username` (String): Current logged-in user
- `pageTitle` (String): Page heading

---

## Common Development Tasks

### Adding a New Field to User Entity

1. **Add field to User.java:**
   ```java
   private String email;
   
   public String getEmail() { return email; }
   public void setEmail(String email) { this.email = email; }
   ```

2. **Update registration form (register.html):**
   ```html
   <input type="email" name="email" required>
   ```

3. **Update RegistrationController.processRegistration():**
   ```java
   @RequestParam String email
   user.setEmail(email);
   ```

4. **Restart application** (H2 will auto-create table with new column)

---

### Adding a New Post Feature (e.g., Edit/Delete)

1. **Add method to Post entity:**
   ```java
   public void editText(String newText) {
       this.text = newText;
   }
   ```

2. **Add route to PostController:**
   ```java
   @PostMapping("/posts/{id}/edit")
   public String editPost(@PathVariable Long id, 
                          @RequestParam String text,
                          Principal principal) {
       postRepository.findById(id).ifPresent(p -> {
           if (p.getUsername().equals(principal.getName())) {  // Ownership check
               p.editText(text);
               postRepository.save(p);
           }
       });
       return "redirect:/allposts";
   }
   ```

3. **Add form to posts.html (only for post owner):**
   ```html
   <th:block th:if="${post.username == username}">
       <form th:action="@{/posts/{id}/edit(id=${post.id})}" method="post">
           <input type="text" name="text" th:value="${post.text}">
           <button type="submit">Edit</button>
       </form>
   </th:block>
   ```

---

### Running the Application

1. **Build:** `mvn clean package`
2. **Run:** `mvn spring-boot:run`
3. **Access:** `http://localhost:8083/`
4. **H2 Console (dev):** `http://localhost:8083/h2-console`
   - JDBC URL: `jdbc:h2:mem:testdb`
   - User: `sa`
   - Password: (leave blank)

---

## Key Configuration Files

### application.properties
```properties
spring.application.name=demo
server.port=8083
```

### pom.xml Dependencies
- `spring-boot-starter-web`: Web MVC framework
- `spring-boot-starter-security`: Authentication & authorization
- `spring-boot-starter-thymeleaf`: Template engine
- `spring-boot-starter-data-jpa`: Database ORM
- `h2`: In-memory database

---

## Important Notes for Modifications

1. **Principal.getName()** returns the authenticated username (available in all controller methods with `Principal` parameter)

2. **All POST endpoints redirect to /allposts** to refresh the forum view with changes

3. **Vote prevention is in Post entity** (upvoters/downvoters Sets) - not in controller, so it's centralized

4. **Spring Security handles login form submission** - no controller method needed; it uses default behavior

5. **BCrypt password encoding is automatic** - never store plain passwords; always use `passwordEncoder.encode()`

6. **H2 Console is disabled in production** - remove `/h2-console/**` from SecurityConfig for production

7. **CSRF protection is on** - all POST forms need CSRF token (Thymeleaf adds automatically)

---

## Security Considerations

- ✅ Passwords are hashed with BCrypt (salted)
- ✅ Spring Security prevents unauthorized access
- ✅ CSRF tokens protect against cross-site attacks
- ✅ User ownership checks in edit/delete operations (recommended)
- ⚠️ H2 console exposed in development (remove for production)
- ⚠️ No input validation/sanitization (add validation before production)
- ⚠️ No rate limiting on registration/login (add to prevent abuse)

---

## Debugging Tips

1. **Enable logging:** Add to application.properties:
   ```properties
   logging.level.org.springframework.security=DEBUG
   logging.level.org.springframework.web=DEBUG
   ```

2. **Check H2 Console:** View actual database state
3. **Check SecurityContext:** Add `@Autowired SecurityContext` to inspect authentication
4. **Test endpoints with Postman:** Send raw requests to test without HTML forms
5. **Review Spring Boot console output:** Shows authentication attempts, URL patterns matched

---

## Future Enhancements

- [ ] User profiles with bio/avatar
- [ ] Follow/unfollow users
- [ ] Direct messaging
- [ ] Post categories/tags
- [ ] Search functionality
- [ ] Email notifications
- [ ] Rate limiting
- [ ] Input validation & sanitization
- [ ] Admin dashboard
- [ ] Post deletion/editing
- [ ] Reply threads (nested comments)
