package us.drullk.blog.comment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import us.drullk.blog.post.Post;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "comments")
public class Comment {

	@Id
	@Getter
	@Setter
	@Column(nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Getter
	@Setter
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "posts.id")
	private Post parent;

	@Getter
	@Setter
	@Column(nullable = false)
	private Integer author;

	@Getter
	@Setter
	@Column(nullable = false, columnDefinition = "TEXT")
	private String text;

	@Getter
	@Setter
	@Column(nullable = false)
	private Long timestamp;

	@Getter
	@Setter
	private Long editTimestamp;

}
