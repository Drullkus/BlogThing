package us.drullk.blog.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class PostService implements IPostService {

	private final PostRepository repository;

	@Autowired
	public PostService(PostRepository repository) {
		this.repository = repository;
	}

	@Override
	public Page<Post> find(int page, int size, Sort sort) {
		return repository.findAll(PageRequest.of(page, size, sort));
	}

	@Override
	public Page<Post> findForAuthor(Integer author, int page, int size, Sort sort) {
		if (author == null)
			return new PageImpl<>(new ArrayList<>());
		Post post = new Post();
		post.setAuthor(author);
		return repository.findAll(Example.of(post, ExampleMatcher.matchingAll().withIgnoreCase()), PageRequest.of(page, size, sort));
	}

	@Override
	public Optional<Post> get(Integer id) {
		return repository.findById(id);
	}

	@Override
	public Post create(Integer author, String text) {
		Post post = new Post();
		post.setAuthor(author);
		post.setText(text);
		post.setTimestamp(System.currentTimeMillis() / 1000L);
		return repository.save(post);
	}

	@Override
	public Post edit(Post post, String text) {
		post.setText(text);
		post.setEditTimestamp(System.currentTimeMillis() / 1000L);
		return repository.save(post);
	}

	@Override
	public void delete(Integer id) {
		repository.deleteById(id);
	}
}
