package de.m3y3r.ekl.filter;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
@NoBearerTokenNeeded
@Priority(Priorities.AUTHORIZATION - 1) // ensure run before real filter
public class NoBearerTokenFilter implements ContainerRequestFilter {

	public static final String OKAY_WITH_NO_BEARER_TOKEN = "NO_BEARER_TOKEN_NEEDED";

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		requestContext.setProperty(OKAY_WITH_NO_BEARER_TOKEN, Integer.valueOf(0));
	}
}
