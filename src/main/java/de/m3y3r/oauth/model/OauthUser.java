package de.m3y3r.oauth.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity(name="oauth_client")
public class OauthUser {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private Integer id;

	@NotNull
	@Column(nullable = false, unique = true)
	private String clientId;

	@Column
	private byte[] hashedPassword;

	@Column
	private byte[] salt;

	@Column
	private boolean active;

}
