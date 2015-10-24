package de.m3y3r.ekl.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import de.m3y3r.common.model.User;

@Entity
public class Einkauf implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private Integer id;

	@Column(nullable=false)
	@NotNull
	private String name;

	@ManyToOne(optional=false)
	@NotNull
	private User einkäufer;

	@Column(nullable=false, name="start_ts")
	@NotNull
	private Timestamp startT;

	@Column(name="end_ts")
	private Timestamp end;

	@ManyToOne(optional=false)
	private Einkaufsliste ekl;

	@ManyToOne
	private Location location;

	@OneToMany
	private List<Item> boughtItems;
}
