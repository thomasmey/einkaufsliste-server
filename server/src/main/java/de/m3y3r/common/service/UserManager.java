package de.m3y3r.common.service;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import de.m3y3r.common.model.User;
import de.m3y3r.oauth.authserver.PersistenceHelper;

@ApplicationScoped
public class UserManager {

	private Logger log;

	@Inject
	PersistenceHelper helper;

	public UserManager() {
		log = Logger.getLogger(UserManager.class.getName());
	}

	public User getUserByUsername(String username) {
		TypedQuery<User> q = helper.getEntityManager().createNamedQuery("User.byUsername", User.class);
		q.setParameter("username", username);
		try {
			return q.getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
	}
}
