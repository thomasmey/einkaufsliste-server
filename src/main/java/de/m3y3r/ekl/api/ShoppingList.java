package de.m3y3r.ekl.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import de.m3y3r.common.model.User;
import de.m3y3r.ekl.api.model.ShoppingListGet;
import de.m3y3r.ekl.api.model.ShoppingListPost;
import de.m3y3r.ekl.filter.BearerTokenFilter;
import de.m3y3r.ekl.model.Einkaufsliste;
import de.m3y3r.oauth.authserver.PersistenceHelper;
import de.m3y3r.oauth.model.Token;

@Path("list")
public class ShoppingList {

	@Inject
	PersistenceHelper helper;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ShoppingListGet> getLists(@Context HttpServletRequest request) {
		Token token = (Token) request.getAttribute(BearerTokenFilter.REQ_ATTRIB_TOKEN);
		EntityManager em = helper.getEntityManager();
		TypedQuery<Einkaufsliste> q = em.createNamedQuery("Einkaufsliste.byOwner", Einkaufsliste.class);
		q.setParameter("owner", token.getContext().getUser());
		List<Einkaufsliste> eklen = q.getResultList();

		return new ArrayList<>();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public String create(ShoppingListPost ekl) {
		return null;
	}

}
