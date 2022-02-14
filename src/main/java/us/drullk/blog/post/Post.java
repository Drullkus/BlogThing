package us.drullk.blog.post;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "posts")
public class Post {

	@Id
	@Getter
	@Setter
	@Column(nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

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
