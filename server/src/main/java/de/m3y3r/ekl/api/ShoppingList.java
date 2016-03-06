package de.m3y3r.ekl.api;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

@RequestScoped
@Path("list")
public class ShoppingList {

	private static class Mapper {
		public static JsonObject shoppingListToExternal(JsonObject s) {
			// filter fields
			List<String> directMapping = Arrays.asList("id", "name");
			JsonObjectBuilder t = transferDirectMappings(s, directMapping);

			//map id

			return t.build();
		}

		/**
		 * transfer 1:1 mappings
		 * @param s
		 * @param directMappings
		 * @return
		 */
		private static JsonObjectBuilder transferDirectMappings(JsonObject s, List<String> directMappings) {
			JsonObjectBuilder t = Json.createObjectBuilder();
			for(String key: s.keySet()) {
				if(directMappings.contains(key))
					switch(s.getValueType()) {
					case NUMBER:
						t.add(key, s.getBoolean(key)); break;
					case STRING:
						t.add(key, s.getString(key)); break;
					case TRUE: case FALSE:
						t.add(key, s.getBoolean(key));
					}
			}
			return t;
		}

		public static JsonObject postItemToInternal(JsonObject itemPost) {
			return itemPost;
		}
	}

	private class IdMapper {
		public JsonObject extIdToIntId(String uuid) {
			return dbUtil.getAsJson("select * from id_mapping t where t.id_extern = ? and object_name = ?", UUID.fromString(uuid),"einkaufsliste");
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
	public JsonObject create(JsonObject ekl) {
		return null;
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getList(
			@PathParam("id") @NotNull String uuid
			) throws NotAuthorisedException {
		Token token = (Token) request.getAttribute(BearerTokenFilter.REQ_ATTRIB_TOKEN);

		JsonObject id = new IdMapper().extIdToIntId(uuid);
		JsonObject ekl = dbUtil.getAsJson("select * from einkaufsliste t where t.id = ?", id.getInt("idIntern")); 
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
			@PathParam("id") @NotNull String uuidEkl
			) throws NotAuthorisedException {
		Token token = (Token) request.getAttribute(BearerTokenFilter.REQ_ATTRIB_TOKEN);

		JsonObject id = new IdMapper().extIdToIntId(uuidEkl);
		JsonObject ekl = dbUtil.getAsJson("select * from einkaufsliste t where t.id = ?", id.getInt("idIntern")); 
		if(ekl.getInt("ownerId") != token.getContext().getUser().getInt("id"))
			throw new NotAuthorisedException();

		JsonObject item = Mapper.postItemToInternal(itemPost);
//		Item item = itemMapper.map(itemPost);
//		item.setEkl(ekl);
//		item.setStatus(ItemStatus.NEEDED);
//		ekl.getItems().add(item);
//		em.persist(item);
//		em.flush();
//		IdMapping idm = new IdMapping();
		UUID uuidItem = UUID.randomUUID();
//		idm.setIdExtern(uuidItem);
//		idm.setIdIntern(item.getId());
//		idm.setObjectName(IdMapping.ON_ITEM);
//		em.persist(idm);

		return Json.createObjectBuilder().add("id", uuidItem.toString()).build();
	}
}

