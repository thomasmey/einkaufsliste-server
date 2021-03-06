package de.m3y3r.oauth.authserver.api;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
import de.m3y3r.ekl.filter.NoBearerTokenNeeded;
import de.m3y3r.oauth.authserver.OauthClientManager;
import de.m3y3r.oauth.authserver.TokenManager;
import de.m3y3r.oauth.filter.RateLimit;
import de.m3y3r.oauth.model.Token;
import de.m3y3r.oauth.util.PasswordUtil;

/**
 * rfc6749 - section 4.3.  Resource Owner Password Credentials Grant
 * @author thomas
 *
 */
@RequestScoped
@Path(value = "/token")
public class AccessToken {

	private Charset iso8859 = Charset.forName("ISO-8859-1");
	private Charset utf8 = Charset.forName("UTF-8");
	private Logger log;

	public AccessToken() {
		log = Logger.getLogger(AccessToken.class.getName());
	}

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
	@NoBearerTokenNeeded
	@RateLimit
	public Response getToken(
			@NotNull @FormParam("grant_type") String grantType,
			@NotNull @FormParam("username") String username,
			@NotNull @FormParam("password") String password,
			@FormParam("scope") String scopes,
			@NotNull @HeaderParam("Authorization") String clientAuthorization,
			@Context HttpServletRequest request
	) {
		log.log(Level.FINE, "entry");

		// ensure TLS or in bluemix environment that "x-forwarded-proto" is https
		if(!(request.isSecure() || "https".equals(request.getHeader("X-Forwarded-Proto")))) {
			JsonObject errorMsg =  createErrorResponse("invalid_request");
			return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
		}

		if(!"password".equals(grantType)) {
			JsonObject errorMsg = createErrorResponse("UNSUPPORTED_GRANT_TYPE");
			return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
		}

		// require client authentication for confidential clients and authenticate it
		Object[] clientIdSecret = parseBasicAuthHeader(clientAuthorization);
		if(!isClientOkay(clientIdSecret)) {
			JsonObject errorMsg = createErrorResponse("INVALID_CLIENT");
			return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
		}

		//validate the resource owner password credentials
		JsonObject user = oauthUserManager.getUserByUsername(username);
		if(!isUserOkay(user, password)) {
			JsonObject errorMsg = createErrorResponse("INVALID_REQUEST");
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
		String clientId = (String) clientIdSecret[0];
		Token token = tokenManager.getToken(clientId, username);
		if(token == null) {
			token = tokenManager.newToken(clientId, username);
			token.getContext().setUser(user);
		}
		assert user.getString("username").equals(token.getContext().getUser().getString("username"));

		// convert into TokenResponse
		JsonObjectBuilder tokenMsg = Json.createObjectBuilder();
		tokenMsg.add("token_type", "Bearer");
		ByteBuffer bb = ByteBuffer.allocate(16);
		bb.putLong(token.getId().getMostSignificantBits());
		bb.putLong(token.getId().getLeastSignificantBits());
		bb.flip();
		ByteBuffer encodedId = Base64.getEncoder().encode(bb);
		byte[] ba = new byte[encodedId.limit()];
		encodedId.get(ba);

		tokenMsg.add("access_token", new String(ba, iso8859));
		tokenMsg.add("expires_in", token.getExpiresIn());

		//FIXME: Also set for error case?!
		CacheControl cacheControl = new CacheControl();
		cacheControl.setNoStore(true);
		cacheControl.setNoCache(true);
		return Response.ok(tokenMsg.build()).cacheControl(cacheControl).build();
	}

	private JsonObject createErrorResponse(String error, String... errorDescUri) {
		JsonObjectBuilder b = Json.createObjectBuilder();
		b.add("error", error);
		if(errorDescUri.length > 0)
			b.add("error_description", errorDescUri[0]);
		if(errorDescUri.length > 1)
			b.add("error_uri", errorDescUri[1]);
		return b.build();
	}

	private Object[] parseBasicAuthHeader(String clientAuthorization) {

		String prefix = "Basic ";
		if(!clientAuthorization.startsWith(prefix)) {
			return null;
		}

		byte[] decodedClientAuth = Base64.getDecoder().decode(clientAuthorization.substring(prefix.length()));

		// username must not include ':' -> RFC2617
		int iSep = -1;
		final int n = decodedClientAuth.length;
		for(int i = 0; i < n; i++) {
			if(decodedClientAuth[i] == ':') {
				iSep = i;
				break;
			}
		}
		if(iSep < 0) {
			return null;
		}

		// RFC2617, RFC2616, http://stackoverflow.com/a/9056877
		String clientId = new String(decodedClientAuth, 0, iSep, iso8859);

		if(iSep + 1 >= n) {
			//no password after usename!
			return null;
		}
		byte[] clientSecret = Arrays.copyOfRange(decodedClientAuth, iSep + 1, n);

		return new Object[] {clientId, clientSecret};
	}

	/**
	 * FIXME: check if this can be done via @Context SecurityContext
	 * @param clientAuthorization
	 * @return
	 */
	private boolean isClientOkay(Object[] clientIdSecret) {

		if(clientIdSecret == null)
			return false;

		byte[] clientSecret = (byte[]) clientIdSecret[1];
		JsonObject client = oauthClientManager.getClientByClientId((String) clientIdSecret[0]);
		if(client == null)
			return false;
		if(!client.getBoolean("active"))
			return false;
	
		byte[] pw = Base64.getDecoder().decode(client.getString("hashedPassword"));
		byte[] salt = Base64.getDecoder().decode(client.getString("salt"));
		return passwordUtil.isPasswordOkay(pw, salt, clientSecret);
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

	private boolean isUserOkay(JsonObject user, String password) {

		if(user == null)
			return false;
		if(!user.getBoolean("active"))
			return false;

		byte[] pw = Base64.getDecoder().decode(user.getString("hashedPassword"));
		byte[] salt = Base64.getDecoder().decode(user.getString("salt"));
		return passwordUtil.isPasswordOkay(pw, salt, password.getBytes(utf8));
	}
}
