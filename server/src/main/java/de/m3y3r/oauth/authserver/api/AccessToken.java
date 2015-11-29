package de.m3y3r.oauth.authserver.api;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.m3y3r.common.model.User;
import de.m3y3r.common.service.UserManager;
import de.m3y3r.oauth.authserver.ErrorResponse;
import de.m3y3r.oauth.authserver.OauthClientManager;
import de.m3y3r.oauth.authserver.TokenManager;
import de.m3y3r.oauth.authserver.TokenResponse;
import de.m3y3r.oauth.authserver.TokenResponse.TokenType;
import de.m3y3r.oauth.authserver.ErrorResponse.ErrorType;
import de.m3y3r.oauth.model.OauthClient;
import de.m3y3r.oauth.model.Token;
import de.m3y3r.oauth.util.PasswordUtil;

/**
 * rfc6749 - section 4.3.  Resource Owner Password Credentials Grant
 * @author thomas
 *
 */
@Path(value = "/token")
public class AccessToken {

	private Charset iso8859 = Charset.forName("ISO-8859-1");
	private Charset utf8 = Charset.forName("UTF-8");

	@Inject
	OauthClientManager oauthClientManager;

	@Inject
	UserManager oauthUserManager;

	@Inject
	PasswordUtil passwordUtil;

	@Inject
	TokenManager tokenManager;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response getToken(
			@NotNull @FormParam("grant_type") String grantType,
			@NotNull @FormParam("username") String username,
			@NotNull @FormParam("password") String password,
			@FormParam("scope") String scopes,
			@NotNull @HeaderParam("Authorization") String clientAuthorization,
			@Context HttpServletRequest request
	) {
		// ensure TLS or in bluemix environment that "x-forwarded-proto" is https
		if(!(request.isSecure() || "https".equals(request.getHeader("X-Forwarded-Proto")))) {
			ErrorResponse errorMsg = new ErrorResponse(ErrorType.INVALID_REQUEST);
			return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
		}

		if(!"password".equals(grantType)) {
			ErrorResponse errorMsg = new ErrorResponse(ErrorType.UNSUPPORTED_GRANT_TYPE);
			return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
		}

		// require client authentication for confidential clients and authenticate it
		if(!isClientOkay(clientAuthorization)) {
			ErrorResponse errorMsg = new ErrorResponse(ErrorType.INVALID_CLIENT);
			return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
		}

		//validate the resource owner password credentials
		User user = oauthUserManager.getUserByUsername(username);
		if(!isUserOkay(user, password)) {
			ErrorResponse errorMsg = new ErrorResponse(ErrorType.INVALID_REQUEST);
			return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
		}

		// process scopes
		String[] givenScopes = checkScopes(scopes);
//		if(!isScopesOkay(scopes)) {
//			ErrorResponse errorMsg = new ErrorResponse(ErrorType.INVALID_SCOPE);
//			return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
//		}

		// everything seems to be okay, create token for request

		// does an still valid token exists for the combination clientId/username?
		String clientId = null;
		Token token;
//		synchronized (tokenManager) {
			token = tokenManager.getToken(clientId, username);
			if(token == null) {
				token = tokenManager.newToken(clientId, username);
				token.getContext().setUser(user);
			}
//		}
		assert user.getUsername().equals(token.getContext().getUser().getUsername());

		// convert into TokenResponse
		TokenResponse tokenMsg = new TokenResponse();
		tokenMsg.setTokenType(TokenType.BEARER);
		ByteBuffer bb = ByteBuffer.allocate(16);
		bb.putLong(token.getId().getMostSignificantBits());
		bb.putLong(token.getId().getLeastSignificantBits());
		bb.flip();
		ByteBuffer encodedId = Base64.getEncoder().encode(bb);
		byte[] ba = new byte[encodedId.limit()];
		encodedId.get(ba);

		tokenMsg.setAccesToken(new String(ba, iso8859));
		tokenMsg.setExpiresIn(token.getExpiresIn());

		//FIXME: Also set for error case?!
		CacheControl cacheControl = new CacheControl();
		cacheControl.setNoStore(true);
		cacheControl.setNoCache(true);
		return Response.ok(tokenMsg).cacheControl(cacheControl).build();
	}

	/**
	 * FIXME: check if this can be done via @Context SecurityContext
	 * @param clientAuthorization
	 * @return
	 */
	private boolean isClientOkay(String clientAuthorization) {

		String prefix = "Basic ";
		if(!clientAuthorization.startsWith(prefix)) {
			return false;
		}

		byte[] decodedClientAuth = Base64.getDecoder().decode(clientAuthorization.substring(prefix.length()));

		// username must not include ':', RFC2617
		int iSep = -1;
		final int n = decodedClientAuth.length;
		for(int i = 0; i < n; i++) {
			if(decodedClientAuth[i] == ':') {
				iSep = i;
				break;
			}
		}
		if(iSep < 0) {
			return false;
		}

		// RFC2617, RFC2616, http://stackoverflow.com/a/9056877
		String clientId = new String(decodedClientAuth, 0, iSep, iso8859);

		if(iSep + 1 >= n) {
			//no password after usename!
			return false;
		}
		byte[] clientSecret = Arrays.copyOfRange(decodedClientAuth, iSep + 1, n);
		OauthClient client = oauthClientManager.getClientByClientId(clientId);
		if(client == null)
			return false;
		if(!client.isActive())
			return false;
	
		return passwordUtil.isPasswordOkay(client.getHashedPassword(), client.getSalt(), clientSecret);
	}

	private String[] checkScopes(String scopes) {
		if(scopes == null)
			return new String[0];

		// section 3.3
		String[] scopesA = scopes.split(" ");
		List<String> resultScopes = new ArrayList<>();

		for(String scope: scopesA) {
			boolean okay = true;
			for(int i = 0, n = scope.length(); i < n; i++) {
				char c = scope.charAt(n);
				if(c == 0x21 || c >= 0x23 && c <= 0x5b || c >= 0x5d && c <= 0x7e) {} else {
					okay = false;
					break;
				}
			}
			if(okay) {
				resultScopes.add(scope);
			}
		}

		return resultScopes.toArray(new String[0]);
	}

	private boolean isUserOkay(User user, String password) {

		if(user == null)
			return false;
		if(!user.isActive())
			return false;

		return passwordUtil.isPasswordOkay(user.getHashedPassword(), user.getSalt(), password.getBytes(utf8));
	}
}
