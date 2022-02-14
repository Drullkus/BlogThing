package us.drullk.blog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import us.drullk.blog.post.IPostService;
import us.drullk.blog.post.Post;
import us.drullk.blog.user.IUserService;
import us.drullk.blog.user.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@Scope("session")
public class ApplicationRestController {

	public static final Pattern EMAIL_REGEX = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

	private final IUserService userService;
	private final IPostService postService;

	@Autowired
	public ApplicationRestController(IUserService userService, IPostService postService) {
		this.userService = userService;
		this.postService = postService;
	}

	/**
	 * Output: List of Users
	 */
	@GetMapping("/api/users")
	public ResponseEntity<List<User>> findUsers() {
		return ResponseEntity.ok(userService.findAll());
	}

	/**
	 * Registers a User
	 * Input: [String: name, String email, String password]
	 * Output: [String success, User user]
	 */
	@PostMapping("/api/user/register")
	public ResponseEntity<JsonNode> registerUser(HttpServletResponse response, @RequestBody JsonNode payload) {
		if (payload.hasNonNull("name") && payload.hasNonNull("email") && payload.hasNonNull("password")) {
			final String email = payload.get("email").asText();
			if(!EMAIL_REGEX.matcher(email).matches())
				return ResponseEntity.badRequest().body(jsonObject().put("error", "Invalid Email Address"));
			Optional<User> existing = userService.getUserFromEmail(email);
			if (existing.isPresent() && existing.get().getHash() != null && !existing.get().getHash().isEmpty())
				return ResponseEntity.badRequest().body(jsonObject().put("error", "User already registered"));
			final String hash = new BCryptPasswordEncoder(12, new SecureRandom()).encode(payload.get("password").asText());
			User user = existing.isPresent() ? userService.updatePassword(existing.get(), hash) : userService.registerUser(payload.get("name").asText(), email, hash);
			userService.updateSession(user, ApplicationController.newSession(response));
			return ResponseEntity.ok(jsonObject().
					put("success", "User successfully created").
					set("user", new ObjectMapper().
							valueToTree(user)));
		}
		return ResponseEntity.badRequest().body(jsonObject().put("error", "Missing Data"));
	}

	/**
	 * Login as a User, assigns a Session Cookie
	 * Input: [String email, String password]
	 * Output: [String success, User user] or [String error]
	 */
	@PostMapping("/api/user/login")
	public ResponseEntity<JsonNode> loginUser(HttpServletResponse response, @RequestBody JsonNode payload) {
		if (payload.hasNonNull("email") && payload.hasNonNull("password")) {
			final String email = payload.get("email").asText();
			if(!EMAIL_REGEX.matcher(email).matches())
				return ResponseEntity.badRequest().body(jsonObject().put("error", "Invalid Email Address"));
			Optional<User> user = userService.getUserFromEmail(email);
			if (user.isEmpty())
				return ResponseEntity.badRequest().body(jsonObject().put("error", "Unknown Email Address"));
			if(!new BCryptPasswordEncoder(12, new SecureRandom()).matches(payload.get("password").asText(), user.get().getHash()))
				return ResponseEntity.badRequest().body(jsonObject().put("error", "Wrong password"));
			userService.updateSession(user.get(), ApplicationController.newSession(response));
			return ResponseEntity.ok(jsonObject().
					put("success", "User successfully logged in").
					set("user", new ObjectMapper().valueToTree(user.get())));
		}
		return ResponseEntity.badRequest().body(jsonObject().put("error", "Missing Data"));
	}

	/**
	 * Logout of a session
	 * Required: A Session Cookie
	 * Output: [String success] or [String error]
	 */
	@PostMapping("/api/user/logout")
	public ResponseEntity<ObjectNode> logoutUser(HttpServletRequest request, HttpServletResponse response) {
		return ApplicationController.getSessionUser(request, userService).map(user -> {
			ApplicationController.killSession(response, userService, Optional.of(user));
			return ResponseEntity.ok(jsonObject().put("success", "Successfully Logged Out!"));
		}).orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject().put("error", "Not Logged In!")));
	}

	/**
	 * Get a User by their ID
	 */
	@GetMapping("/api/users/{id}")
	public ResponseEntity<User> getUser(@PathVariable Integer id) {
		return userService.getUser(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
	}

	/**
	 * Get the currently logged in User by the Session Cookie
	 * Output: User object or [String error]
	 */
	@GetMapping("/api/users/self")
	public ResponseEntity<Object> getSelf(HttpServletRequest request) {
		return ApplicationController.getSessionUser(request, userService).
				map(u -> ResponseEntity.ok(new ObjectMapper().valueToTree(u))).
				orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject().put("error", "Invalid Session!")));
	}

	/**
	 * Get Posts sorted by timestamp
	 * Input: [Optional Integer author, Optional Integer page (>= 0), Optional Integer size (> 0, <= 50))]
	 * Output: [Integer page, Integer size, Integer pages, List Post data]
	 */
	@PostMapping("/api/posts")
	public ResponseEntity<JsonNode> getPosts(@RequestBody JsonNode body) {
		JsonNode author = body.get("author");
		JsonNode page = body.get("page");
		JsonNode size = body.get("size");
		int p = page == null || !page.isInt() || page.asInt() < 0 ? 0 : page.asInt();
		int s = size == null || !size.isInt() || size.asInt() <= 0 || size.asInt() > 50 ? 50 : size.asInt();
		Page<Post> post = author != null && author.isInt() ?

				postService.findForAuthor(author.asInt(), p, s, Sort.by("timestamp")) :

				postService.find(p, s, Sort.by("timestamp"));
		ArrayNode nodes = JsonNodeFactory.instance.arrayNode();
		post.toList().forEach(data -> nodes.add(new ObjectMapper().valueToTree(data)));
		return ResponseEntity.ok(jsonObject().
				put("page", p).
				put("size", s).
				put("pages", post.getTotalPages()).
				set("data", nodes));
	}

	/**
	 * Submit a Post
	 * Requires a Session Cookie
	 * Input: [String data]
	 * Output: [Post data]
	 */
	@PostMapping("/api/post/submit")
	public ResponseEntity<?> addPost(HttpServletRequest request, @RequestBody JsonNode body) {
		return ApplicationController.getSessionUser(request, userService).map(user -> {
			JsonNode text = body.get("data");
			if (text == null || !text.isTextual() || text.asText().isEmpty())
				return ResponseEntity.badRequest().body(jsonObject().put("error", "No Text"));
			return ResponseEntity.ok(jsonObject().set("data", new ObjectMapper().valueToTree(postService.create(user.getId(), text.asText()))));
		}).orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject().put("error", "Not Logged In!")));
	}

	/**
	 * Get a Post by its ID
	 * Output: Post object
	 */
	@PostMapping("/api/posts/{id}")
	public ResponseEntity<Post> getPost(@PathVariable Integer id) {
		return postService.get(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
	}

	/**
	 * Edit a Post
	 * Requires a Session Cookie
	 * Input: [String data, Integer id]
	 * Output: [Post data] or [String error]
	 */
	@PostMapping("/api/post/edit")
	public ResponseEntity<?> editPost(HttpServletRequest request, @RequestBody JsonNode body) {
		return ApplicationController.getSessionUser(request, userService).map(user -> {
			JsonNode text = body.get("data");
			JsonNode id = body.get("id");
			if (text == null || !text.isTextual() || text.asText().isEmpty())
				return ResponseEntity.badRequest().body(jsonObject().put("error", "No Text"));
			if (id == null || !id.isInt())
				return ResponseEntity.badRequest().body(jsonObject().put("error", "Invalid ID"));
			return postService.get(id.asInt()).map(post -> {
				if (post.getAuthor().equals(user.getId()))
					return ResponseEntity.ok(jsonObject().set("data", new ObjectMapper().valueToTree(postService.edit(post, text.asText()))));
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject().put("error", "Access Denied"));
			}).orElse(ResponseEntity.badRequest().body(jsonObject().put("error", "Post doesn't exist!")));
		}).orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject().put("error", "Not Logged In!")));
	}

	/**
	 * Delete a Post
	 * Requires a Session Cookie
	 * Output: [String success] or [String error]
	 */
	@PostMapping("/api/post/delete/{id}")
	public ResponseEntity<?> editPost(HttpServletRequest request, @PathVariable Integer id) {
		return ApplicationController.getSessionUser(request, userService).map(user -> postService.get(id).map(post -> {
			if (post.getAuthor().equals(user.getId())) {
				postService.delete(id);
				return ResponseEntity.ok(jsonObject().put("success", "Post Deleted!"));
			}
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject().put("error", "Access Denied"));
		}).orElse(ResponseEntity.badRequest().body(jsonObject().put("error", "Post doesn't exist!")))).
				orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject().put("error", "Not Logged In!")));
	}

	private static ObjectNode jsonObject() {
		return JsonNodeFactory.instance.objectNode();
	}

}
