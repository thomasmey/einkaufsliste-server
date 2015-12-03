package de.m3y3r.oauth.authserver.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("ready")
public class Ready {
	@GET
	public Response resourceReady() {
		return Response.ok().build();
	}
}
