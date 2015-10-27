package de.m3y3r.ekl.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import de.m3y3r.common.model.User;

@Entity
@NamedQueries( {
	@NamedQuery(name="Einkaufsliste.byOwner", query="select o from Einkaufsliste o where o.owner = :owner")
})
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

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Item> getItems() {
		return items;
	}
	public void setItems(List<Item> items) {
		this.items = items;
	}
}
