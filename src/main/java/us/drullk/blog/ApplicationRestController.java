package us.drullk.blog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class ApplicationRestController {

	private final IUserService service;

	@Autowired
	public ApplicationRestController(IUserService service) {
		this.service = service;
	}

	@GetMapping("/api/users")
	public ResponseEntity<List<User>> findUsers() {
		return ResponseEntity.ok(service.findAll());
	}

	@GetMapping("/api/user/add")
	public ResponseEntity<User> addUser(@RequestParam("firstname") Optional<String> firstname, @RequestParam("lastname") Optional<String> lastname) {
		if (firstname.isPresent() && lastname.isPresent())
			return ResponseEntity.ok(service.addUser(firstname.get(), lastname.get()));
		return ResponseEntity.badRequest().build();
	}

	@GetMapping("/api/users/{id}")
	public ResponseEntity<User> getUser(@PathVariable Integer id) {
		return service.getUser(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
	}

}
