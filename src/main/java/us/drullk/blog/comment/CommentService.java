package us.drullk.blog.comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import us.drullk.blog.post.Post;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CommentService implements ICommentService {

	private final CommentRepository repository;

	@Autowired
	public CommentService(CommentRepository repository) {
		this.repository = repository;
	}

	@Override
	public Page<Comment> find(int page, int size, Sort sort) {
		return repository.findAll(PageRequest.of(page, size, sort));
	}

	@Override
	public Page<Comment> findForPost(Post post, int page, int size, Sort sort) {
		if (post == null)
			return new PageImpl<>(new ArrayList<>());
		Comment comment = new Comment();
		comment.setParent(post);
		return repository.findAll(Example.of(comment, ExampleMatcher.matchingAll().withIgnoreCase()), PageRequest.of(page, size, sort));
	}

	@Override
	public Page<Comment> findForAuthor(Integer author, int page, int size, Sort sort) {
		if (author == null)
			return new PageImpl<>(new ArrayList<>());
		Comment comment = new Comment();
		comment.setAuthor(author);
		return repository.findAll(Example.of(comment, ExampleMatcher.matchingAll().withIgnoreCase()), PageRequest.of(page, size, sort));
	}

	@Override
	public Page<Comment> findForPostAndAuthor(Post post, Integer author, int page, int size, Sort sort) {
		if (author == null)
			return new PageImpl<>(new ArrayList<>());
		Comment comment = new Comment();
		comment.setParent(post);
		comment.setAuthor(author);
		return repository.findAll(Example.of(comment, ExampleMatcher.matchingAll().withIgnoreCase()), PageRequest.of(page, size, sort));
	}

	@Override
	public Optional<Comment> get(Integer id) {
		return repository.findById(id);
	}

	@Override
	public Comment create(Post post, Integer author, String text) {
		Comment comment = new Comment();
		comment.setParent(post);
		comment.setAuthor(author);
		comment.setText(text);
		comment.setTimestamp(System.currentTimeMillis() / 1000L);
		return repository.save(comment);
	}

	@Override
	public Comment edit(Comment comment, String text) {
		comment.setText(text);
		comment.setEditTimestamp(System.currentTimeMillis() / 1000L);
		return repository.save(comment);
	}

	@Override
	public void delete(Integer id) {
		repository.deleteById(id);
	}
}
