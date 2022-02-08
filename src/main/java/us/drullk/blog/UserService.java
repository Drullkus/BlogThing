package us.drullk.blog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements IUserService {

	private final UserRepository repository;

	@Autowired
	public UserService(UserRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<User> findAll() {
		return repository.findAll();
	}

	@Override
	public User registerUser(String name, String email, String hash) {
		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setHash(hash);
		repository.save(user);
		return user;
	}

	@Override
	public Optional<User> getUser(Integer id) {
		return repository.findById(id);
	}

	@Override
	public Optional<User> getUser(String email) {
		User user = new User();
		user.setEmail(email);
		return repository.findOne(Example.of(user, ExampleMatcher.matchingAll().withIgnoreCase()));
	}
}
