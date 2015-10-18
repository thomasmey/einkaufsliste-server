package de.m3y3r.oauth.authserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.m3y3r.oauth.authserver.ErrorResponse.ErrorType;
import de.m3y3r.oauth.model.OauthClient;

/**
 * rfc6749 - section 4.3.  Resource Owner Password Credentials Grant
 * @author thomas
 *
 */
@Path(value = "/token")
public class AccessToken {

	private Logger log;

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
		// ensure TLS
//		if(!request.isSecure()) {
//			ErrorResponse errorMsg = new ErrorResponse(ErrorType.INVALID_REQUEST);
//			return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
//		}

		if(!"password".equals(grantType)) {
			ErrorResponse errorMsg = new ErrorResponse(ErrorType.UNSUPPORTED_GRANT_TYPE);
			return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
		}

		//FIXME/TODO: Implement rate-limit and/or alerting to prevent brute force attacks
		/* code must go here */

		// require client authentication for confidential clients and authenticate it
		if(!isClientOkay(clientAuthorization)) {
			ErrorResponse errorMsg = new ErrorResponse(ErrorType.INVALID_CLIENT);
			return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
		}

		//validate the resource owner password credentials
		if(!isUserOkay(username, password)) {
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
//	     {
//	         "access_token":"2YotnFZFEjr1zCsicMWpAA",
//	         "token_type":"example",
//	         "expires_in":3600,
//	         "refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",
//	         "example_parameter":"example_value"
//	       }
		TokenResponse tokenMsg = null;
		return Response.ok(tokenMsg).build();
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

		byte[] clientId = Arrays.copyOfRange(decodedClientAuth, 0, iSep);
		//FIXME: check bounds iSep + 1 !!!
		byte[] clientSecret = Arrays.copyOfRange(decodedClientAuth, iSep + 1, n);

		OauthClient client = new OauthClientManager().getClient(clientId);
		return false;
	}

	private String[] checkScopes(String scopes) {
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

	private boolean isUserOkay(String username, String password) {
		// TODO Auto-generated method stub
		return false;
	}
}
