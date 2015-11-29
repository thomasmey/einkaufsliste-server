package de.m3y3r.ekl.api.model;

import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ResourceId {

	private UUID id;

	public ResourceId() {}

	public ResourceId(UUID uuidItem) {
		setId(uuidItem);
	}

	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
}
