package de.m3y3r.ekl.api.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ShoppingListGet {

	private String id;

	private String name;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
