package us.drullk.blog;

import java.util.List;
import java.util.Optional;

public interface IUserService {

	List<User> findAll();

	User registerUser(String name, String email, String hash);

	Optional<User> getUser(Integer id);

	Optional<User> getUser(String email);

}
