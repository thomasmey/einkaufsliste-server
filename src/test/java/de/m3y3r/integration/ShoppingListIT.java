package de.m3y3r.integration;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.token.OAuthToken;
import org.junit.BeforeClass;
import org.junit.Test;

public class ShoppingListIT {

	private static final String INTEGRATION_TEST_ENV = "/integration-test-env.properties";

	private static String baseUrl = "http://localhost:";

	// api
	private static String pingEndpoint = "/ekl/buildinfo";
	private String shoppingListEndpoint = "/ekl/list";

	// oauth
	private String tokenEndpoint =  "/oauth/token";

	@BeforeClass
	public static void setup() throws IOException, InterruptedException {
		Map<String,String> env = readEnvs();
		baseUrl = baseUrl + env.get("PORT");
		waitForServerStartup();
	}

	private static void waitForServerStartup() throws IOException, InterruptedException {
		long maxWaitTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);
		URL url = new URL(baseUrl + pingEndpoint);
		System.out.println("url=" + url);

		boolean okay = false;
		System.out.println("waiting for server: ");
		while(System.currentTimeMillis() < maxWaitTime) {
			try {
				URLConnection uc = url.openConnection();
				HttpURLConnection huc = (HttpURLConnection) uc;
				int responseCode = huc.getResponseCode();
				if(responseCode == 200) {
					okay = true;
					break;
				}
			} catch(ConnectException e) {
//				System.err.println(e);
			}
			Thread.sleep(TimeUnit.SECONDS.toMillis(1));
			System.out.print(".");
		}
		System.out.println();

		if(!okay) {
			throw new IOException("Timeout to wait for server startup!");
		}
		System.out.println("wait okay!");
	}

	private static Map<String, String> readEnvs() throws IOException {
		InputStream inStream = ShoppingListIT.class.getResourceAsStream(INTEGRATION_TEST_ENV);
		Properties p = new Properties();
		p.load(inStream);
		inStream.close();
		return (Map)p;
	}

	@Test
	public void testCreateShoppingList() throws OAuthSystemException, OAuthProblemException, IOException {

		// establish oauth token
		Map<String, String> p = readEnvs();

		/* the Apache Oltu library seems to be completely unusable/broken!
		 * look at this below code! it's totally not intuitive!
		 * Even a bug was submitted but was resolved: https://issues.apache.org/jira/browse/OLTU-159
		 * But the comment about the abilities doesn't meet the RFC!
		 */
		String idSecret = p.get("clientId") + ':' + p.get("clientSecret");
		String basicAuth = Base64.getEncoder().encodeToString(idSecret.getBytes("UTF-8"));
		OAuthClientRequest request = OAuthClientRequest.tokenLocation(baseUrl + tokenEndpoint)
				.setGrantType(GrantType.PASSWORD)
				.setUsername(p.get("userName"))
				.setPassword(p.get("userPass"))
				.buildBodyMessage();
		request.setHeader("Authorization", "Basic " + basicAuth);
		request.setHeader("X-Forwarded-Proto", "https");

		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

		OAuthJSONAccessTokenResponse accessTokenResp = oAuthClient.accessToken(request);

		OAuthToken oauthToken = accessTokenResp.getOAuthToken();

		OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(baseUrl + shoppingListEndpoint).setAccessToken(oauthToken.getAccessToken()).buildHeaderMessage();
		OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
	}
}
