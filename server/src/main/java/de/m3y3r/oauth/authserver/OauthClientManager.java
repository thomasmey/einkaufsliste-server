package de.m3y3r.oauth.authserver;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import de.m3y3r.oauth.util.DbUtil;

@RequestScoped
public class OauthClientManager {

	@Inject
	private DbUtil dbUtil;

	public JsonObject getClientByClientId(String clientId) {
		return dbUtil.getAsJson("select * from oauth_client o where o.client_id = ?", clientId);
	}
}
