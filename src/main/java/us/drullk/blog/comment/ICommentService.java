package us.drullk.blog.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import us.drullk.blog.post.Post;

import java.util.Optional;

public interface ICommentService {

	Page<Comment> find(int page, int size, Sort sort);

	Page<Comment> findForPost(Post post, int page, int size, Sort sort);

	Page<Comment> findForAuthor(Integer author, int page, int size, Sort sort);

	Page<Comment> findForPostAndAuthor(Post post, Integer author, int page, int size, Sort sort);

	Optional<Comment> get(Integer id);

	Comment create(Post post, Integer author, String text);

	Comment edit(Comment comment, String text);

	void delete(Integer id);

}
