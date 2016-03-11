package de.m3y3r.ekl.api;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
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

import de.m3y3r.ekl.filter.BearerTokenFilter;
import de.m3y3r.oauth.model.Token;
import de.m3y3r.oauth.util.DbUtil;
import de.m3y3r.oauth.util.MappingUtil;

import static de.m3y3r.util.StructUtil.*;

@RequestScoped
@Path("list")
public class ShoppingList {

	private static class Mapper {
		public static JsonObject shoppingListToExternal(JsonObject s) {
			JsonObjectBuilder t = MappingUtil.transferDirectMappings(s, l("id", "name"));
			return t.build();
		}

		public static JsonObjectBuilder postItemToInternal(JsonObject s) {
			JsonObjectBuilder t = MappingUtil.transferDirectMappings(s, l("name", "menge", "unit"));
			return t;
		}

		public static JsonObjectBuilder postEklToInternal(JsonObject s) {
			JsonObjectBuilder t = MappingUtil.transferDirectMappings(s, l("name"));
			return t;
		}
	}

	@Inject
	private DbUtil dbUtil;

	@Context
	private HttpServletRequest request;

	@GET
	@Transactional
	@Produces(MediaType.APPLICATION_JSON)
	public JsonValue getLists() {
		Token token = (Token) request.getAttribute(BearerTokenFilter.REQ_ATTRIB_TOKEN);

		List<JsonObject> lists = dbUtil.getAsJsons("select * from einkaufsliste t where t.owner_id = ?", 100, token.getContext().getUser().getInt("id"));
		return lists.stream().map(Mapper::shoppingListToExternal).collect(
				Json::createArrayBuilder,
				(a, s) -> a.add(s),
				(b1, b2) -> b1.add(b2))
			.build();
	}

	@Transactional
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject create(JsonObject eklPost) {
		Token token = (Token) request.getAttribute(BearerTokenFilter.REQ_ATTRIB_TOKEN);

		JsonObjectBuilder ekl = Mapper.postEklToInternal(eklPost);
		ekl.add("ownerId", token.getContext().getUser().getInt("id"));
		ekl.add("id", UUID.randomUUID().toString());

		JsonObject e = ekl.build();
		dbUtil.insertFromJson("einkaufsliste", e);
		return e;
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getList(
			@PathParam("id") @NotNull UUID uuid
			) throws NotAuthorisedException {
		Token token = (Token) request.getAttribute(BearerTokenFilter.REQ_ATTRIB_TOKEN);

		JsonObject ekl = readEkl(uuid);
		checkAuthorisation(token, ekl.getInt("ownerId"));

		return Mapper.shoppingListToExternal(ekl);
	}

	private static void checkAuthorisation(Token token, int ownerId) throws NotAuthorisedException {
		if(ownerId != token.getContext().getUser().getInt("id"))
			throw new NotAuthorisedException();
	}

	@Transactional
	@POST
	@Path("{id}/item")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject addItem(
			@NotNull JsonObject itemPost,
			@PathParam("id") @NotNull UUID uuidEkl
			) throws NotAuthorisedException {
		Token token = (Token) request.getAttribute(BearerTokenFilter.REQ_ATTRIB_TOKEN);

		JsonObject ekl = readEkl(uuidEkl);
		checkAuthorisation(token, ekl.getInt("ownerId"));

		JsonObjectBuilder item = Mapper.postItemToInternal(itemPost);
		if(checkMandatoryAttributes(item, l("menge", "unit", "name")))
			throw new IllegalArgumentException();

		UUID uuidItem = UUID.randomUUID();
		item.add("status", "NEEDED");
		item.add("id", uuidItem.toString());
		item.add("dataVersion", 1);
		item.add("eklId", uuidEkl.toString());

		dbUtil.insertFromJson("item", item.build());

		return Json.createObjectBuilder().add("id", uuidItem.toString()).build();
	}

	private static boolean checkMandatoryAttributes(JsonObjectBuilder j, List<String> a) {
		JsonObject jb = j.build();
		return a.stream().allMatch((k) -> { return jb.containsKey(k); } );
	}

	private JsonObject readEkl(UUID uuidEkl) {
		return dbUtil.getAsJson("select * from einkaufsliste t where t.id = ?", uuidEkl);
	}
}

