package us.drullk.blog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import us.drullk.blog.user.IUserService;
import us.drullk.blog.user.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
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

	@RequestMapping({"", "/", "/register", "/login", "/profile/{id}", "/post/{id}"})
	public String index(HttpServletRequest request, HttpServletResponse response) {
		if (request.getCookies() != null && getSessionUser(request, userService).isEmpty()) {
			killSession(response, userService, Optional.empty()); // Nuke the cookie if the session is invalid
		}
		return "index";
	}

	public static Optional<User> getSessionUser(HttpServletRequest request, IUserService service) {
		return request.getCookies() == null ? Optional.empty() : Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("session")).findAny().
				flatMap(cookie -> service.getUserFromSession(cookie.getValue()));
	}

	/**
	 * We use a cookie instead of a session so it persists when the App restarts, basically just easier in-dev testing of things. Don't ever do this on a real App.<br>
	 * Since we're using cookies, we take advantage of this in the frontend to see if we're logged in or not.<br>
	 * {@link ApplicationController#index(HttpServletRequest, HttpServletResponse)} will invalidate the cookie for us if need be.
	 */
	public static String newSession(HttpServletResponse response) {
		String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder session = new StringBuilder();
		Random rnd = new Random();
		while (session.length() < 18) {
			int index = (int) (rnd.nextFloat() * chars.length());
			session.append(chars.charAt(index));
		}
		Cookie cookie = new Cookie("session", session.toString());
		cookie.setPath("/");
		cookie.setHttpOnly(false);
		cookie.setMaxAge(60 * 60 * 24); // 1 Day
		response.addCookie(cookie);
		return session.toString();
	}

	public static void killSession(HttpServletResponse response, IUserService service, Optional<User> user) {
		Cookie cookie = new Cookie("session", "");
		cookie.setPath("/");
		cookie.setHttpOnly(false);
		cookie.setMaxAge(0); // Kill the cookie!
		response.addCookie(cookie);
		user.ifPresent(u -> service.updateSession(u, null));
	}



	@GetMapping("/oauth/github")
	public String oauthGithub(HttpServletResponse response, @RequestParam String code) {
		JsonNode data = restService.post("https://github.com/login/oauth/access_token", headers -> {}, JsonNodeFactory.instance.objectNode().
				put("code", code).
				put("client_id", System.getenv("GITHUB_CLIENT_ID")).
				put("client_secret", System.getenv("GITHUB_CLIENT_SECRET")).
				put("redirect_uri", "http://137.184.125.125:8080/oauth/github"));
		final String token = data.get("access_token").asText();
		JsonNode github = restService.post("https://api.github.com/user", headers -> headers.add("Authorization", "token " + token), JsonNodeFactory.instance.objectNode());
		String email = github.get("email").isNull() ? null : github.get("email").asText();
		Long id = github.get("id").asLong();
		Optional<User> user = userService.getUserFromGithubID(id).or(() -> email == null ? Optional.empty() : userService.getUserFromEmail(email));
		user.ifPresent(u -> u.setGithubID(id)); // Link the potential registered Email account
		User u = user.orElseGet(() -> userService.registerUserViaGithub(github.get("name").asText(), email == null ? "" : email, id));
		userService.updateSession(u, newSession(response));
		return "redirect";
	}

}
