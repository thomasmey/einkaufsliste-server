package de.m3y3r.ekl.api.mapper;

import java.util.UUID;

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

	public String idtoUuid(Integer id, String type) {
		EntityManager em = persistenceHelper.getEntityManager();
		TypedQuery<IdMapping> q = em.createNamedQuery("IdMapping.getByIdIntern", IdMapping.class);
		q.setParameter("idIntern", id);
		q.setParameter("objectName", type);
		IdMapping idMapping = q.getSingleResult();
		return idMapping.getIdExtern().toString();
	}

	public Integer uuidToId(UUID uuid, String type) {
		EntityManager em = persistenceHelper.getEntityManager();
		TypedQuery<IdMapping> q = em.createNamedQuery("IdMapping.getByIdExtern", IdMapping.class);
		q.setParameter("idExtern", uuid);
		q.setParameter("objectName", type);
		IdMapping idMapping = q.getSingleResult();
		return idMapping.getIdIntern();
	}
}
