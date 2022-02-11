package us.drullk.blog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Random;

@Controller
@Scope("session")
public class ApplicationController {

	private final RestService restService;
	private final IUserService userService;

	@Autowired
	public ApplicationController(RestService restService, IUserService userService) {
		this.restService = restService;
		this.userService = userService;
	}

	@GetMapping("/")
	public String index(HttpServletRequest request) {
		return "index";
	}

	public static String newSession(HttpServletRequest request) {
		String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder session = new StringBuilder();
		Random rnd = new Random();
		while (session.length() < 18) {
			int index = (int) (rnd.nextFloat() * chars.length());
			session.append(chars.charAt(index));
		}
		request.getSession().setAttribute("session", session);
		return session.toString();

	}

	@GetMapping("/oauth/github")
	public String oauthGithub(HttpServletRequest request, @RequestParam String code) {
		JsonNode data = restService.post("https://github.com/login/oauth/access_token", headers -> {}, JsonNodeFactory.instance.objectNode().
				put("code", code).
				put("client_id", System.getenv("GITHUB_CLIENT_ID")).
				put("client_secret", System.getenv("GITHUB_CLIENT_SECRET")).
				put("redirect_uri", "http://localhost:8080/oauth/github"));
		final String token = data.get("access_token").asText();
		JsonNode github = restService.post("https://api.github.com/user", headers -> headers.add("Authorization", "token " + token), JsonNodeFactory.instance.objectNode());
		String email = github.get("email").isNull() ? null : github.get("email").asText();
		Long id = github.get("id").asLong();
		Optional<User> user = email == null ? Optional.empty() : userService.getUserFromEmail(email);
		user.ifPresent(u -> u.setGithubID(id));
		User u = user.orElseGet(() -> userService.getUserFromGithubID(id).orElseGet(() -> userService.registerUserViaGithub(github.get("name").asText(), email == null ? "" : email, id)));
		userService.updateSession(u, newSession(request));
		return u.getEmail().isEmpty() ? "oauth/email" : "redirect";
	}

	@GetMapping("/oauth/update")
	public String updateEmail(HttpServletRequest request, @RequestParam String email) {
		if (ApplicationRestController.EMAIL_REGEX.matcher(email).matches())
			userService.getUserFromSession(request.getSession().getAttribute("session").toString()).ifPresent(user -> userService.updateEmail(user, email));
		return "redirect";
	}

}
