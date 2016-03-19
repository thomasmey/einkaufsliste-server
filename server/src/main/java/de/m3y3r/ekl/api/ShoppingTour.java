package de.m3y3r.ekl.api;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Path("tour")
public class ShoppingTour {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<JsonObject> getTours() {
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public String create(JsonObject ekl) {
		return null;
	}

	@POST
	public String startShoppingTour(JsonObject tour) {
		return null;
	}
}
