package de.m3y3r.oauth.model;

import javax.json.JsonObject;

public class Context {

	private JsonObject user;

	public JsonObject getUser() {
		return user;
	}
	public void setUser(JsonObject user) {
		this.user = user;
	}
}
