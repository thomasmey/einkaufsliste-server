package de.m3y3r.oauth.authserver;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ApplicationScoped
public class PersistenceHelper {

	@PersistenceContext(name="authserverPU")
	private EntityManager em;

	public EntityManager getEntityManager() {
		return em;
	}
}
