package de.m3y3r.ekl.filter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import de.m3y3r.oauth.authserver.TokenManager;
import de.m3y3r.oauth.model.Token;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class BearerTokenFilter implements ContainerRequestFilter {

	public static final String REQ_ATTRIB_TOKEN = "token";

	private Logger log;

	@Inject
	TokenManager tokenManager;

	public BearerTokenFilter() {
		log = Logger.getLogger(BearerTokenFilter.class.getName());
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		if(log.isLoggable(Level.FINE))
			log.log(Level.FINE, "Processing request path {0}", requestContext.getUriInfo().getPath());

		if(requestContext.getProperty(NoBearerTokenFilter.OKAY_WITH_NO_BEARER_TOKEN) != null)
			return;

		String authHeader = requestContext.getHeaderString("Authorization");
		String bearer = "Bearer ";
		if(authHeader == null || !authHeader.startsWith(bearer)) {
			if(log.isLoggable(Level.FINE))
				log.log(Level.FINE, "Abort because of missing Auth header!");
			requestContext.abortWith(Response.status(Status.FORBIDDEN).build());
			return;
		}

		String id = authHeader.substring(bearer.length());
		byte[] uuid = Base64.getDecoder().decode(id);
		ByteBuffer bb = ByteBuffer.wrap(uuid);
		UUID u = new UUID(bb.getLong(), bb.getLong());

		if(log.isLoggable(Level.FINE))
			log.log(Level.FINE, "Token UUID {0}", u);

		Token token = tokenManager.getTokenByUuid(u);
		if(token == null || token.getExpiresIn() <= 0) {
			if(log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "Token with UUID {0} not found!", u);
				log.log(Level.FINE, "Abort because of missing token!");
			}
			requestContext.abortWith(Response.status(Status.FORBIDDEN).build());
			return;
		}

		if(log.isLoggable(Level.FINE))
			log.log(Level.FINE, "Processing request with token {0}", token.getId());
		requestContext.setProperty(REQ_ATTRIB_TOKEN, token);
	}
}
