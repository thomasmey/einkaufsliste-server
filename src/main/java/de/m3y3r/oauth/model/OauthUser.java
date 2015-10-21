package de.m3y3r.oauth.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="oauth_user")
@NamedQueries( {
	@NamedQuery(name="OauthUser.byUsername", query="select o from OauthUser o where o.username = :username")
})
public class OauthUser {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private Integer id;

	@NotNull
	@Column(nullable = false, unique = true)
	private String username;

	@Column
	private byte[] hashedPassword;

	@Column
	private byte[] salt;

	@Column
	private boolean active;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public byte[] getHashedPassword() {
		return hashedPassword;
	}

	public void setHashedPassword(byte[] hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	public byte[] getSalt() {
		return salt;
	}

	public void setSalt(byte[] salt) {
		this.salt = salt;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
