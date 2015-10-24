package de.m3y3r.ekl.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import de.m3y3r.common.model.User;

@Entity
public class Einkaufsliste implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private Integer id;

	@Column
	private String name;

	@ManyToOne(optional=false)
	@NotNull
	private User owner;

	@OneToMany(mappedBy="ekl")
	private List<Item> items;
}
