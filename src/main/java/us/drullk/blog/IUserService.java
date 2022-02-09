package us.drullk.blog;

import java.util.List;
import java.util.Optional;

public interface IUserService {

	List<User> findAll();

	User registerUser(String name, String email, String hash);

	User registerUserViaGithub(String name, String email, Long id);

	User updateEmail(User user, String email);

	User updatePassword(User user, String hash);

	User updateGithubID(User user, Long id);

	User updateSession(User user, String session);

	Optional<User> getUser(Integer id);

	Optional<User> getUserFromEmail(String email);

	Optional<User> getUserFromGithubID(Long id);

	Optional<User> getUserFromSession(String session);

}
