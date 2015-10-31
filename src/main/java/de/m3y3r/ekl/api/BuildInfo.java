package de.m3y3r.ekl.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("buildinfo")
public class BuildInfo {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getBuildInfo() throws IOException {
		InputStream inStream = this.getClass().getResourceAsStream("/build.properties");
		Properties p = new Properties();
		p.load(inStream);
		JsonObjectBuilder job = Json.createObjectBuilder();
		for(Entry e : p.entrySet()) {
			job.add(e.getValue().toString(), e.getKey().toString());
		}
		return job.build();
	}
}
