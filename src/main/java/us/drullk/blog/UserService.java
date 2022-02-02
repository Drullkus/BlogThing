package us.drullk.blog;

import org.springframework.beans.factory.annotation.Autowired;
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
	public User addUser(String firstname, String lastname) {
		User user = new User();
		user.setFirstname(firstname);
		user.setLastname(lastname);
		repository.save(user);
		return user;
	}

	@Override
	public Optional<User> getUser(Integer id) {
		return repository.findById(id);
	}
}
