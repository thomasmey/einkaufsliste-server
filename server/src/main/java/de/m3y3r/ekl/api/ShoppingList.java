package de.m3y3r.ekl.api;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import de.m3y3r.ekl.api.mapper.EinkaufslisteMapper;
import de.m3y3r.ekl.api.mapper.IdMapper;
import de.m3y3r.ekl.api.mapper.ItemMapper;
import de.m3y3r.ekl.api.model.ItemPost;
import de.m3y3r.ekl.api.model.ResourceId;
import de.m3y3r.ekl.api.model.ShoppingListGet;
import de.m3y3r.ekl.api.model.ShoppingListPost;
import de.m3y3r.ekl.filter.BearerTokenFilter;
import de.m3y3r.ekl.model.Item;
import de.m3y3r.ekl.model.ItemStatus;
import de.m3y3r.ekl.model.Einkaufsliste;
import de.m3y3r.ekl.model.IdMapping;
import de.m3y3r.oauth.authserver.PersistenceHelper;
import de.m3y3r.oauth.model.Token;

@Path("list")
public class ShoppingList {

	@Inject
	PersistenceHelper helper;

	@Inject
	EinkaufslisteMapper eklMapper;

	@Inject
	ItemMapper itemMapper;

	@Inject
	IdMapper idMapper;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ShoppingListGet> getLists(@Context HttpServletRequest request) {
		Token token = (Token) request.getAttribute(BearerTokenFilter.REQ_ATTRIB_TOKEN);
		EntityManager em = helper.getEntityManager();
		TypedQuery<Einkaufsliste> q = em.createNamedQuery("Einkaufsliste.byOwner", Einkaufsliste.class);
		q.setParameter("owner", token.getContext().getUser());
		List<Einkaufsliste> eklen = q.getResultList();

		return eklMapper.map(eklen);
	}

	@Transactional
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public ResourceId create(ShoppingListPost ekl) {
		return null;
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ShoppingListGet getList(
			@PathParam("id") @NotNull String uuid,
			@Context HttpServletRequest request) throws NotAuthorisedException {
		Token token = (Token) request.getAttribute(BearerTokenFilter.REQ_ATTRIB_TOKEN);

		Integer id = idMapper.uuidToId(UUID.fromString(uuid), IdMapping.ON_EKL);
		EntityManager em = helper.getEntityManager();
		Einkaufsliste ekl = em.find(Einkaufsliste.class, id);
		if(!ekl.getOwner().equals(token.getContext().getUser()))
			throw new NotAuthorisedException();

		return eklMapper.map(ekl);
	}

	@Transactional
	@POST
	@Path("{id}/item")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ResourceId addItem(
			@NotNull ItemPost itemPost,
			@PathParam("id") @NotNull String uuidEkl,
			@Context HttpServletRequest request) throws NotAuthorisedException {
		Token token = (Token) request.getAttribute(BearerTokenFilter.REQ_ATTRIB_TOKEN);

		Integer id = idMapper.uuidToId(UUID.fromString(uuidEkl), IdMapping.ON_EKL);
		EntityManager em = helper.getEntityManager();
		Einkaufsliste ekl = em.find(Einkaufsliste.class, id);
		if(!ekl.getOwner().equals(token.getContext().getUser()))
			throw new NotAuthorisedException();

		Item item = itemMapper.map(itemPost);
		item.setEkl(ekl);
		item.setStatus(ItemStatus.NEEDED);
		ekl.getItems().add(item);
		em.persist(item);
		em.flush();
		IdMapping idm = new IdMapping();
		UUID uuidItem = UUID.randomUUID();
		idm.setIdExtern(uuidItem);
		idm.setIdIntern(item.getId());
		idm.setObjectName(IdMapping.ON_ITEM);
		em.persist(idm);

		return new ResourceId(uuidItem);
	}
}
