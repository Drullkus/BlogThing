package us.drullk.blog.user;

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
		return repository.save(user);
	}

	@Override
	public User registerUserViaGithub(String name, String email, Long id) {
		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setGithubID(id);
		return repository.save(user);
	}

	@Override
	public User updateEmail(User user, String email) {
		user.setEmail(email);
		return repository.save(user);
	}

	@Override
	public User updatePassword(User user, String hash) {
		user.setHash(hash);
		return repository.save(user);
	}

	@Override
	public User updateGithubID(User user, Long id) {
		user.setGithubID(id);
		return repository.save(user);
	}

	@Override
	public User updateSession(User user, String session) {
		user.setSession(session);
		return repository.save(user);
	}

	@Override
	public User updateAdminStatus(User user, boolean status) {
		user.setAdmin(status);
		return repository.save(user);
	}

	@Override
	public Optional<User> getUser(Integer id) {
		return repository.findById(id);
	}

	@Override
	public Optional<User> getUserFromEmail(String email) {
		if (email == null || email.isEmpty())
			return Optional.empty();
		User user = new User();
		user.setEmail(email);
		return repository.findOne(Example.of(user, ExampleMatcher.matchingAll().withIgnorePaths("admin").withIgnoreCase()));
	}

	@Override
	public Optional<User> getUserFromGithubID(Long id) {
		if (id == null)
			return Optional.empty();
		User user = new User();
		user.setGithubID(id);
		return repository.findOne(Example.of(user, ExampleMatcher.matchingAll().withIgnorePaths("admin").withIgnoreCase()));
	}

	@Override
	public Optional<User> getUserFromSession(String session) {
		if (session == null || session.isEmpty())
			return Optional.empty();
		User user = new User();
		user.setSession(session);
		return repository.findOne(Example.of(user, ExampleMatcher.matchingAll().withIgnorePaths("admin").withIgnoreCase()));
	}

	@Override
	public boolean isAdmin(User user) {
		return user.getAdmin();
	}
}
