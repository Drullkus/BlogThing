package us.drullk.blog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

	private final IUserService service;

	@Autowired
	public ApplicationRestController(IUserService service) {
		this.service = service;
	}

	@GetMapping("/api/users")
	public ResponseEntity<List<User>> findUsers() {
		return ResponseEntity.ok(service.findAll());
	}

	@PostMapping("/api/user/register")
	public ResponseEntity<JsonNode> registerUser(HttpServletResponse response, @RequestBody JsonNode payload) {
		if (payload.hasNonNull("name") && payload.hasNonNull("email") && payload.hasNonNull("password")) {
			final String email = payload.get("email").asText();
			if(!EMAIL_REGEX.matcher(email).matches())
				return ResponseEntity.badRequest().body(jsonObject().put("error", "Invalid Email Address"));
			Optional<User> existing = service.getUserFromEmail(email);
			if (existing.isPresent() && existing.get().getHash() != null && !existing.get().getHash().isEmpty())
				return ResponseEntity.badRequest().body(jsonObject().put("error", "User already registered"));
			final String hash = new BCryptPasswordEncoder(12, new SecureRandom()).encode(payload.get("password").asText());
			User user = existing.isPresent() ? service.updatePassword(existing.get(), hash) : service.registerUser(payload.get("name").asText(), email, hash);
			service.updateSession(user, ApplicationController.newSession(response));
			return ResponseEntity.ok(jsonObject().
					put("success", "User successfully created").
					set("user", new ObjectMapper().
							valueToTree(user)));
		}
		return ResponseEntity.badRequest().body(jsonObject().put("error", "Missing Data"));
	}

	@PostMapping("/api/user/login")
	public ResponseEntity<JsonNode> loginUser(HttpServletResponse response, @RequestBody JsonNode payload) {
		if (payload.hasNonNull("email") && payload.hasNonNull("password")) {
			final String email = payload.get("email").asText();
			if(!EMAIL_REGEX.matcher(email).matches())
				return ResponseEntity.badRequest().body(jsonObject().put("error", "Invalid Email Address"));
			Optional<User> user = service.getUserFromEmail(email);
			if (user.isEmpty())
				return ResponseEntity.badRequest().body(jsonObject().put("error", "Unknown Email Address"));
			if(!new BCryptPasswordEncoder(12, new SecureRandom()).matches(payload.get("password").asText(), user.get().getHash()))
				return ResponseEntity.badRequest().body(jsonObject().put("error", "Wrong password"));
			service.updateSession(user.get(), ApplicationController.newSession(response));
			return ResponseEntity.ok(jsonObject().
					put("success", "User successfully logged in").
					set("user", new ObjectMapper().valueToTree(user)));
		}
		return ResponseEntity.badRequest().body(jsonObject().put("error", "Missing Data"));
	}

	@PostMapping("/api/user/logout")
	public ResponseEntity<ObjectNode> logoutUser(HttpServletRequest request, HttpServletResponse response) {
		return ApplicationController.getSessionUser(request, service).map(user -> {
			ApplicationController.killSession(response, service, Optional.of(user));
			return ResponseEntity.ok(jsonObject().put("success", "Successfully Logged Out!"));
		}).orElse(ResponseEntity.badRequest().body(jsonObject().put("error", "Not Logged In!")));
	}

	@GetMapping("/api/users/{id}")
	public ResponseEntity<User> getUser(@PathVariable Integer id) {
		return service.getUser(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
	}
	
	private static ObjectNode jsonObject() {
		return JsonNodeFactory.instance.objectNode();
	}

}
