package de.m3y3r.oauth.authserver;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import de.m3y3r.oauth.model.OauthClient;

@ApplicationScoped
public class OauthClientManager {

	private Logger log;

	@Inject
	PersistenceHelper helper;

	public OauthClientManager() {
		log = Logger.getLogger(OauthClientManager.class.getName());
	}

	public OauthClient getClientByClientId(String clientId) {
		TypedQuery<OauthClient> q = helper.getEntityManager().createNamedQuery("OauthClient.byClientId", OauthClient.class);
		q.setParameter("clientId", clientId);
		try {
			return q.getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
	}
}
