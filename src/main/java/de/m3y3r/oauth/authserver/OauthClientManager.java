package de.m3y3r.oauth.authserver;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import de.m3y3r.oauth.model.OauthClient;

public class OauthClientManager {

	private Logger log;

	@Inject
	private EntityManager em;

	public OauthClientManager() {
		log = Logger.getLogger(OauthClientManager.class.getName());
	}

	public OauthClient getClient(byte[] clientId) {
		return em.find(OauthClient.class, clientId);
	}
}
