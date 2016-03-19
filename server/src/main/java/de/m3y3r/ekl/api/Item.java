package de.m3y3r.ekl.api;

import static de.m3y3r.util.StructUtil.l;

import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.m3y3r.ekl.filter.BearerTokenFilter;
import de.m3y3r.oauth.model.Token;
import de.m3y3r.oauth.util.ApiUtil;
import de.m3y3r.oauth.util.DbUtil;
import de.m3y3r.oauth.util.MappingUtil;

@RequestScoped
@Path("item")
public class Item {

	private static class Mapper {
		public static JsonObject itemToExternal(JsonObject s) {
			JsonObjectBuilder t = MappingUtil.transferDirectMappings(s, l("name", "menge", "unit", "status"));
			return t.build();
		}
		public static JsonObjectBuilder putItemToInternal(JsonObject s) {
			JsonObjectBuilder t = MappingUtil.transferDirectMappings(s, l("name", "menge", "unit"));
			return t;
		}
	}

	@Inject
	private DbUtil dbUtil;

	@Context
	private HttpServletRequest request;

	@Path("{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@PUT
	@Transactional
	public Response replaceItem(
			@PathParam("id") @NotNull UUID uuid,
			JsonObject itemPut
			) throws NotAuthorisedException {

		Token token = (Token) request.getAttribute(BearerTokenFilter.REQ_ATTRIB_TOKEN);
		JsonObject itemCurrent = dbUtil.getAsJsonFrom("readItem", uuid);
		JsonObject ekl = dbUtil.getAsJsonFrom("readEkl", itemCurrent.get("eklId"));
		ApiUtil.checkAuthorisation(token, ekl.getInt("ownerId"));

		JsonObjectBuilder itemMerged = MappingUtil.merge(itemCurrent, Mapper.putItemToInternal(itemPut).build());

		JsonObject io = itemMerged.build();
		if(!ApiUtil.checkMandatoryAttributes(io, l("menge", "unit", "name")))
			throw new IllegalArgumentException();

		dbUtil.updateFromJson("item", io);

		return Response.ok().build();
	}

	@Path("{id}")
	@DELETE
	@Transactional
	public Response deleteItem(
			@PathParam("id") @NotNull UUID uuid
			) throws NotAuthorisedException {
		Token token = (Token) request.getAttribute(BearerTokenFilter.REQ_ATTRIB_TOKEN);
		JsonObject item = dbUtil.getAsJsonFrom("readItem", uuid);
		JsonObject ekl = dbUtil.getAsJsonFrom("readEkl", item.get("eklId"));
		ApiUtil.checkAuthorisation(token, ekl.getInt("ownerId"));

		if(dbUtil.deleteFromJson("item", item)) {
			return Response.ok().build();
		}
		return Response.serverError().build();
	}

	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getItem(
			@PathParam("id") @NotNull UUID uuid
			) throws NotAuthorisedException {
		Token token = (Token) request.getAttribute(BearerTokenFilter.REQ_ATTRIB_TOKEN);

		JsonObject item = dbUtil.getAsJsonFrom("readItem", uuid);
		JsonObject ekl = dbUtil.getAsJsonFrom("readEkl", item.get("eklId"));
		ApiUtil.checkAuthorisation(token, ekl.getInt("ownerId"));
		return Mapper.itemToExternal(item);
	}
}
