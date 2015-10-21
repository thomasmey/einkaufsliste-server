package de.m3y3r.oauth.authserver;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import de.m3y3r.oauth.model.OauthUser;

@ApplicationScoped
public class OauthUserManager {

	private Logger log;

	@Inject
	PersistenceHelper helper;

	public OauthUserManager() {
		log = Logger.getLogger(OauthUserManager.class.getName());
	}

	public OauthUser getUserByUsername(String username) {
		TypedQuery<OauthUser> q = helper.getEntityManager().createNamedQuery("OauthUser.byUsername", OauthUser.class);
		q.setParameter("username", username);
		try {
			return q.getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
	}
}
