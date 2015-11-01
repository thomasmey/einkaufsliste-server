package de.m3y3r.ekl;

import javax.json.JsonValue;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;

@Provider
public class JacksonJsonpProvider implements ContextResolver<ObjectMapper> {

	@Override
	public ObjectMapper getContext(Class<?> type) {
		if(JsonValue.class.isAssignableFrom(type)) {
			ObjectMapper om = new ObjectMapper();
			om.registerModule(new JSR353Module());
			return om;
		}
		return null;
	}
}
