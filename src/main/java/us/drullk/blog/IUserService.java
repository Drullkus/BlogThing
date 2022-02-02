package us.drullk.blog;

import java.util.List;
import java.util.Optional;

public interface IUserService {

	List<User> findAll();

	User addUser(String firstname, String lastname);

	Optional<User> getUser(Integer id);

}
