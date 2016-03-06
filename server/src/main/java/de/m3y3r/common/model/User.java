package de.m3y3r.common.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="users")
@NamedQueries( {
	@NamedQuery(name="User.byUsername", query="select o from User o where o.username = :username")
})
public class User implements Serializable {

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

	@ManyToMany
	private List<Role> roles;

}
