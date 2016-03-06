package de.m3y3r.common.service;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import de.m3y3r.oauth.util.DbUtil;

@RequestScoped
public class UserManager {

	@Inject
	private DbUtil dbUtil;

	public JsonObject getUserByUsername(String username) {
		return dbUtil.getAsJson("select * from users u where u.username = ?", username);
	}
}
