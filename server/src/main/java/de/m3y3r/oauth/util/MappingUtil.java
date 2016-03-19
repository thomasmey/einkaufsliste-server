package de.m3y3r.oauth.util;

import java.util.List;
import java.util.function.Predicate;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class MappingUtil {

	/**
	 * transfer 1:1 mappings
	 * @param s
	 * @param directMappings
	 * @return
	 */
	public static JsonObjectBuilder transferDirectMappings(JsonObject s, List<String> directMappings) {
		return toBuilder(s, (k) -> (directMappings.contains(k)));
	}

	public static JsonObjectBuilder toBuilder(JsonObject s) {
		return toBuilder(s, null);
	}

	private static JsonObjectBuilder toBuilder(JsonObject s, Predicate<String> p) {
		JsonObjectBuilder t = Json.createObjectBuilder();
		for(String key: s.keySet()) {
			if(p.test(key))
				t.add(key, s.get(key));
		}
		return t;
	}

	public static JsonObjectBuilder merge(JsonObject objCurrent, JsonObject objNew) {
		// TODO Auto-generated method stub
		return null;
	}
}
