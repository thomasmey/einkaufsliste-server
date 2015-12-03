package de.m3y3r.ekl.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.m3y3r.ekl.api.model.ShoppingListGet;
import de.m3y3r.ekl.api.model.ShoppingListPost;
import de.m3y3r.ekl.api.model.ShoppingTourPost;
import de.m3y3r.ekl.filter.NoBearerTokenNeeded;

@Path("tour")
public class ShoppingTour {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ShoppingListGet> getLists() {
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public String create(ShoppingListPost ekl) {
		return null;
	}

	@POST
	public String startShoppingTour(ShoppingTourPost tour) {
		return null;
	}
}
