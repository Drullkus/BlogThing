package us.drullk.blog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
public class ApplicationRestController {

	private static final Pattern EMAIL_REGEX = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

	private final IUserService service;

	@Autowired
	public ApplicationRestController(IUserService service) {
		this.service = service;
	}

	@GetMapping("/api/users")
	public ResponseEntity<List<JsonNode>> findUsers() {
		List<JsonNode> users = new ArrayList<>();
		service.findAll().forEach(user -> users.add(JsonNodeFactory.instance.objectNode().
				put("id", user.getId()).
				put("name", user.getName()).
				put("email", user.getEmail())));
		return ResponseEntity.ok(users);
	}

	@PostMapping("/api/user/register")
	public ResponseEntity<JsonNode> registerUser(@RequestBody JsonNode payload) {
		if (payload.hasNonNull("name") && payload.hasNonNull("email") && payload.hasNonNull("password")) {
			final String email = payload.get("email").asText();
			if(!EMAIL_REGEX.matcher(email).matches())
				return ResponseEntity.badRequest().body(JsonNodeFactory.instance.objectNode().put("error", "Invalid Email Address"));
			final String hash = new BCryptPasswordEncoder(12, new SecureRandom()).encode(payload.get("password").asText());
			service.registerUser(payload.get("name").asText(), email, hash);
			return ResponseEntity.ok(JsonNodeFactory.instance.objectNode().put("success", "User successfully created"));
		}
		return ResponseEntity.badRequest().body(JsonNodeFactory.instance.objectNode().put("error", "Missing Data"));
	}

	@PostMapping("/api/user/login")
	public ResponseEntity<JsonNode> loginUser(@RequestBody JsonNode payload) {
		if (payload.hasNonNull("email") && payload.hasNonNull("password")) {
			final String email = payload.get("email").asText();
			if(!EMAIL_REGEX.matcher(email).matches())
				return ResponseEntity.badRequest().body(JsonNodeFactory.instance.objectNode().put("error", "Invalid Email Address"));
			Optional<User> user = service.getUser(email);
			if (user.isEmpty())
				return ResponseEntity.badRequest().body(JsonNodeFactory.instance.objectNode().put("error", "Unknown Email Address"));
			if(!new BCryptPasswordEncoder(12, new SecureRandom()).matches(payload.get("password").asText(), user.get().getHash()))
				return ResponseEntity.badRequest().body(JsonNodeFactory.instance.objectNode().put("error", "Wrong password"));
			return ResponseEntity.ok(JsonNodeFactory.instance.objectNode().
					put("success", "User successfully logged in!").
					set("user", JsonNodeFactory.instance.objectNode().
							put("id", user.get().getId()).
							put("name", user.get().getName()).
							put("email", user.get().getEmail())));
		}
		return ResponseEntity.badRequest().body(JsonNodeFactory.instance.objectNode().put("error", "Missing Data"));
	}

	@GetMapping("/api/users/{id}")
	public ResponseEntity<User> getUser(@PathVariable Integer id) {
		return service.getUser(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
	}

}
