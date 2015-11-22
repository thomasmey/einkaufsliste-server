package de.m3y3r.ekl.filter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.m3y3r.oauth.authserver.TokenManager;
import de.m3y3r.oauth.model.Token;

@WebFilter("/ekl/*")
public class BearerTokenFilter implements Filter {

	public static final String REQ_ATTRIB_TOKEN = "token";

	@Inject
	TokenManager tokenManager;

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain fc)
			throws IOException, ServletException {

		HttpServletRequest hreq = (HttpServletRequest) sreq;
		HttpServletResponse hresp = (HttpServletResponse) sresp;

		if("/buildinfo".equals(hreq.getPathInfo())) {
			fc.doFilter(sreq, sresp);
			return;
		}
		String authHeader = hreq.getHeader("Authorization");
		String bearer = "Bearer ";
		if(authHeader == null || !authHeader.startsWith(bearer)) {
			hresp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String id = authHeader.substring(bearer.length());
		byte[] uuid = Base64.getDecoder().decode(id);
		ByteBuffer bb = ByteBuffer.wrap(uuid);
		UUID u = new UUID(bb.getLong(), bb.getLong());
		Token token = tokenManager.getTokenByUuid(u);
		if(token == null || token.getExpiresIn() <= 0) {
			hresp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		hreq.setAttribute(REQ_ATTRIB_TOKEN, token);
		fc.doFilter(sreq, sresp);
	}

	@Override
	public void init(FilterConfig fc) throws ServletException {
	}
}
