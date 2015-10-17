package de.m3y3r.oauth.authserver;

import java.util.logging.Logger;

import de.m3y3r.oauth.model.OauthClient;

public class OauthClientManager {

	private Logger log;

	public OauthClientManager() {
		log = Logger.getLogger(OauthClientManager.class.getName());
	}

	public OauthClient getClient(byte[] clientId) {
		return null;
	}

}
