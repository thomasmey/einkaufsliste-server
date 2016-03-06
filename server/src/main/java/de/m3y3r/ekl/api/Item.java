package de.m3y3r.ekl.api;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import de.m3y3r.ekl.filter.NoBearerTokenNeeded;

@NoBearerTokenNeeded
@Path("item")
public class Item {

	@Path("{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@PUT
	public String replaceItem(JsonObject item) {
		return null;
	}

	@Path("{id}")
	@DELETE
	public String deleteItem() {
		return null;
	}
}
