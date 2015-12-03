package de.m3y3r.ekl.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import de.m3y3r.ekl.filter.NoBearerTokenNeeded;

@Path("ready")
public class Ready {
	@NoBearerTokenNeeded
	@GET
	public Response resourceReady() {
		return Response.ok().build();
	}
}
