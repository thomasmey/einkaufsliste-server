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

@RequestScoped
@Path("list")
public class ShoppingList {

	private static class Mapper {
		public static JsonObject shoppingListToExternal(JsonObject s) {
			List<String> directMapping = Arrays.asList("id", "name");
			JsonObjectBuilder t = MappingUtil.transferDirectMappings(s, directMapping);
			return t.build();
		}

		public static JsonObjectBuilder postItemToInternal(JsonObject s) {
			List<String> directMapping = Arrays.asList("name");
			JsonObjectBuilder t = MappingUtil.transferDirectMappings(s, directMapping);
			return t;
		}

		public static JsonObjectBuilder postEklToInternal(JsonObject s) {
			List<String> directMapping = Arrays.asList("name");
			JsonObjectBuilder t = MappingUtil.transferDirectMappings(s, directMapping);
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

		JsonObject ekl = dbUtil.getAsJson("select * from einkaufsliste t where t.id = ?", uuid); 
		if(ekl.getInt("ownerId") != token.getContext().getUser().getInt("id"))
			throw new NotAuthorisedException();

		return Mapper.shoppingListToExternal(ekl);
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

		JsonObject ekl = dbUtil.getAsJson("select * from einkaufsliste t where t.id = ?", uuidEkl); 
		if(ekl.getInt("ownerId") != token.getContext().getUser().getInt("id"))
			throw new NotAuthorisedException();

		JsonObjectBuilder eklb = MappingUtil.toBuilder(ekl);
		JsonObjectBuilder item = Mapper.postItemToInternal(itemPost);
//		Item item = itemMapper.map(itemPost);
//		item.setEkl(ekl);
//		item.setStatus(ItemStatus.NEEDED);
//		ekl.getItems().add(item);
//		em.persist(item);
//		em.flush();

		UUID uuidItem = UUID.randomUUID();
		return Json.createObjectBuilder().add("id", uuidItem.toString()).build();
	}
}

