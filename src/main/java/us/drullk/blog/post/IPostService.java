package us.drullk.blog.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.Optional;

public interface IPostService {

	Page<Post> find(int page, int size, Sort sort);

	Page<Post> findForAuthor(Integer author, int page, int size, Sort sort);

	Optional<Post> get(Integer id);

	Post create(Integer author, String text);

	Post edit(Post post, String text);

	void delete(Integer id);

}
