package us.drullk.blog.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

	@Id
	@Getter
	@Setter
	@Column(nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Getter
	@Setter
	@Column(nullable = false)
	private String name;

	@Getter
	@Setter
	@Column(nullable = false)
	private String email;

	@Getter
	@Setter
	@JsonIgnore
	private String hash;

	@Getter
	@Setter
	@JsonIgnore
	private Long githubID;

	@Getter
	@Setter
	@JsonIgnore
	private String session;

	@Getter
	@Setter
	@Column(nullable = false)
	private Boolean admin = false;

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + Objects.hashCode(this.id);
		hash = 79 * hash + Objects.hashCode(this.name);
		hash = 79 * hash + Objects.hashCode(this.email);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final User other = (User) obj;
		if (!this.name.equals(other.name)) {
			return false;
		}
		if (!this.email.equals(other.email)) {
			return false;
		}
		return Objects.equals(this.id, other.id);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("User{");
		sb.append("id=").append(id);
		sb.append(", name='").append(name).append('\'');
		sb.append(", email=").append(email);
		sb.append('}');
		return sb.toString();
	}
}
