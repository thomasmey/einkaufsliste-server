package de.m3y3r.ekl.api.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ShoppingListPost {

	private String name;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
