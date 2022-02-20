package us.drullk.blog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import us.drullk.blog.comment.Comment;
import us.drullk.blog.comment.ICommentService;
import us.drullk.blog.post.IPostService;
import us.drullk.blog.post.Post;
import us.drullk.blog.user.IUserService;
import us.drullk.blog.user.User;

import java.util.List;
import java.util.Optional;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BlogApplicationTests {

	@LocalServerPort
	int port;

	@Autowired
	private RestTemplateBuilder builder;

	private final IUserService userService;
	private final IPostService postService;
	private final ICommentService commentService;

	private static final String PERSON_B_SESSION = "HQZzVijA318CvDdEqW";

	@Autowired
	BlogApplicationTests(IUserService userService, IPostService postService, ICommentService commentService) {
		this.userService = userService;
		this.postService = postService;
		this.commentService = commentService;
	}

	private static ObjectNode jsonObject() {
		return JsonNodeFactory.instance.objectNode();
	}

	@Test
	@SuppressWarnings("rawtypes")
    void isWebOK() {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<List> result = template.exchange(RequestEntity.get("/api/users").build(), List.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
	void registerUser() {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<JsonNode> result = template.exchange(RequestEntity.post("/api/user/register").
				contentType(MediaType.APPLICATION_JSON).
				body(jsonObject().
						put("name", "Person D").
						put("email", "d@person.test").
						put("password", "d")
				), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("success")).isNotNull();
		Assertions.assertThat(result.getBody().get("user")).isNotNull();
		Assertions.assertThat(result.getBody().get("user").get("id").asInt()).isEqualTo(4);
	}

    @Test
	void loginUser() {
		loginUser("a@person.test", "a", 1);
		loginUser("c@person.test", "c", 3);
	}

	private void loginUser(String email, String password, int expectedID) {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<JsonNode> result = template.exchange(RequestEntity.post("/api/user/login").
				contentType(MediaType.APPLICATION_JSON).
				body(jsonObject().
						put("email", email).
						put("password", password)
				), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("success")).isNotNull();
		Assertions.assertThat(result.getBody().get("user")).isNotNull();
		Assertions.assertThat(result.getBody().get("user").get("id").asInt()).isEqualTo(expectedID);
		// Test for the Session Cookie data matching what's in the database
		List<String> cookies = result.getHeaders().get(HttpHeaders.SET_COOKIE);
		Assertions.assertThat(cookies).isNotNull();
		Optional<String> cookie = cookies.stream().filter(c -> c.contains("session=")).findAny();
		Assertions.assertThat(cookie.isPresent()).isTrue();
		Optional<User> user = userService.getUserFromEmail(email);
		Assertions.assertThat(user.isPresent()).isTrue();
		final String session = cookie.get().split("session=")[1].split(";")[0];
		Assertions.assertThat(session).isEqualTo(user.get().getSession());
	}

	@Test
	void logoutUser() {
		Optional<User> user = userService.getUserFromSession(PERSON_B_SESSION);
		Assertions.assertThat(user.isPresent()).isTrue();
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<JsonNode> result = template.exchange(RequestEntity.post("/api/user/logout").
				contentType(MediaType.APPLICATION_JSON).
				header(HttpHeaders.COOKIE, "session=" + PERSON_B_SESSION).
				build(), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("success")).isNotNull();
		userService.updateSession(user.get(), PERSON_B_SESSION);
	}

	@Test
	void getUser() {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<JsonNode> result = template.exchange(RequestEntity.get("/api/users/2").
				build(), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("id").asInt()).isEqualTo(2);
	}

	@Test
	void getSelf() {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<JsonNode> result = template.exchange(RequestEntity.get("/api/users/self").
				header(HttpHeaders.COOKIE, "session=" + PERSON_B_SESSION).
				build(), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("id").asInt()).isEqualTo(2);
	}

	@Test
	void getPosts() {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<JsonNode> result = template.exchange(RequestEntity.post("/api/posts").
				contentType(MediaType.APPLICATION_JSON).
				build(), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("page").asInt()).isEqualTo(0);
		Assertions.assertThat(result.getBody().get("size").asInt()).isEqualTo(5);

		template = builder.rootUri("http://localhost:" + port).build();
		result = template.exchange(RequestEntity.post("/api/posts").
				contentType(MediaType.APPLICATION_JSON).
				body(jsonObject().
						put("author", 3)), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("size").asInt()).isEqualTo(2);

		template = builder.rootUri("http://localhost:" + port).build();
		result = template.exchange(RequestEntity.post("/api/posts").
				contentType(MediaType.APPLICATION_JSON).
				body(jsonObject().
						put("size", 2)), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("pages").asInt()).isEqualTo(3);
	}

	@Test
	void addPost() {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<JsonNode> result = template.exchange(RequestEntity.post("/api/post/submit").
				header(HttpHeaders.COOKIE, "session=" + PERSON_B_SESSION).
				body(jsonObject().
						put("data", "Test")), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("data")).isNotNull();
		Assertions.assertThat(result.getBody().get("data").get("text").asText()).isEqualTo("Test");
		postService.delete(result.getBody().get("data").get("id").asInt());
	}

	@Test
	void getPost() {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<JsonNode> result = template.exchange(RequestEntity.get("/api/posts/4").
				build(), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("timestamp").asInt()).isEqualTo(1645226425);
	}

	@Test
	void editPost() {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<JsonNode> result = template.exchange(RequestEntity.post("/api/post/edit").
				header(HttpHeaders.COOKIE, "session=" + PERSON_B_SESSION).
				body(jsonObject().
						put("id", 5).
						put("data", "Wow!!")), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("data")).isNotNull();
		Assertions.assertThat(result.getBody().get("data").get("author").asInt()).isEqualTo(2);
		Assertions.assertThat(result.getBody().get("data").get("text").asText()).isEqualTo("Wow!!");
	}

	@Test
	void deletePost() {
		Post post = postService.create(2, "Test");
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<JsonNode> result = template.exchange(RequestEntity.get("/api/post/delete/" + post.getId()).
				header(HttpHeaders.COOKIE, "session=" + PERSON_B_SESSION).
				build(), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("success")).isNotNull();
	}

	@Test
	void getComments() {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<JsonNode> result = template.exchange(RequestEntity.post("/api/comments").
				contentType(MediaType.APPLICATION_JSON).
				body(jsonObject().
						put("post", 1)), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("page").asInt()).isEqualTo(0);
		Assertions.assertThat(result.getBody().get("size").asInt()).isEqualTo(2);

		template = builder.rootUri("http://localhost:" + port).build();
		result = template.exchange(RequestEntity.post("/api/comments").
				contentType(MediaType.APPLICATION_JSON).
				body(jsonObject().
						put("post", 1).
						put("author", 3)), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("size").asInt()).isEqualTo(1);

		template = builder.rootUri("http://localhost:" + port).build();
		result = template.exchange(RequestEntity.post("/api/comments").
				contentType(MediaType.APPLICATION_JSON).
				body(jsonObject().
						put("post", 1).
						put("size", 1)), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("pages").asInt()).isEqualTo(2);
	}

	@Test
	void addComment() {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<JsonNode> result = template.exchange(RequestEntity.post("/api/comment/submit").
				header(HttpHeaders.COOKIE, "session=" + PERSON_B_SESSION).
				body(jsonObject().
						put("post", 1).
						put("data", "Test")), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("data")).isNotNull();
		Assertions.assertThat(result.getBody().get("data").get("text").asText()).isEqualTo("Test");
		commentService.delete(result.getBody().get("data").get("id").asInt());
	}

	@Test
	void getComment() {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<JsonNode> result = template.exchange(RequestEntity.get("/api/comments/2").
				build(), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("timestamp").asInt()).isEqualTo(1645226534);
		Assertions.assertThat(result.getBody().get("editTimestamp").asInt()).isEqualTo(1645226539);
	}

	@Test
	void editComment() {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<JsonNode> result = template.exchange(RequestEntity.post("/api/comment/edit").
				header(HttpHeaders.COOKIE, "session=" + PERSON_B_SESSION).
				body(jsonObject().
						put("id", 1).
						put("data", "First!!!")), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("data")).isNotNull();
		Assertions.assertThat(result.getBody().get("data").get("author").asInt()).isEqualTo(2);
		Assertions.assertThat(result.getBody().get("data").get("text").asText()).isEqualTo("First!!!");
	}

	@Test
	void deleteComment() {
		Optional<Post> post = postService.get(2);
		Assertions.assertThat(post.isPresent()).isTrue();
		Comment comment = commentService.create(post.get(), 2, "Test");
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<JsonNode> result = template.exchange(RequestEntity.get("/api/comment/delete/" + comment.getId()).
				header(HttpHeaders.COOKIE, "session=" + PERSON_B_SESSION).
				build(), JsonNode.class);
		Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		Assertions.assertThat(result.getBody()).isNotNull();
		Assertions.assertThat(result.getBody().get("success")).isNotNull();
	}

}
