package de.m3y3r.ekl.api.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import de.m3y3r.ekl.model.IdMapping;
import de.m3y3r.oauth.authserver.PersistenceHelper;

@ApplicationScoped
public class IdMapper {

	@Inject
	PersistenceHelper persistenceHelper;

	public String idtoUuid(Integer id, String idType) {
		EntityManager em = persistenceHelper.getEntityManager();
		TypedQuery<IdMapping> q = em.createNamedQuery("IdMapping.getByIdIntern", IdMapping.class);
		q.setParameter("idIntern", id);
		q.setParameter("objectName", idType);
		IdMapping idMapping = q.getSingleResult();
		return idMapping.getIdExtern().toString();
	}
}
