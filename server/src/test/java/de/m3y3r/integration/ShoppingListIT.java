package de.m3y3r.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.oauth2.OAuth2ClientSupport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.m3y3r.ekl.api.model.ShoppingListGet;

public class ShoppingListIT {

	private static final String INTEGRATION_TEST_ENV = "/integration-test-env.properties";

	private static String baseUrl = "http://localhost:";

	// oauth
	private final String tokenEndpoint = "/token";

	//api
	private static final String buildEndpoint = "/buildinfo";

	@BeforeClass
	public static void setup() throws IOException, InterruptedException {
		Map<String,String> env = readEnvs();
		baseUrl = baseUrl + env.get("PORT");
		waitForServerStartup();
	}

	private static void waitForServerStartup() throws IOException, InterruptedException {
		long maxWaitTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(baseUrl + "/ekl");
		WebTarget path = target.path(buildEndpoint);

		boolean okay = false;
		System.out.println("waiting for server: ");
		while(System.currentTimeMillis() < maxWaitTime) {
			try {
				Response response = path.request(MediaType.APPLICATION_JSON).get();
				if(response.getStatus() == 200) {
					okay = true;
					break;
				}
			} catch(ProcessingException e) {
				System.err.println(e);
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
	public void testCreateShoppingList() throws IOException {

		Client client = ClientBuilder.newClient();
		WebTarget targetOauth = client.target(baseUrl + "/oauth");

		// establish oauth token
		Map<String, String> p = readEnvs();

		HttpAuthenticationFeature basicAuthFeature = HttpAuthenticationFeature.basic(p.get("clientId"), p.get("clientSecret"));
		WebTarget tokenEndpoint = targetOauth.path(this.tokenEndpoint).register(basicAuthFeature);

		Form form = new Form();
		form.param("grant_type", "password");
		form.param("username", p.get("userName"));
		form.param("password", p.get("userPass"));
		Response tokenResponse = tokenEndpoint.request().post(Entity.form(form));

		// no exception hit, everything seems to be okay!
		Assert.assertTrue(true);
	}
}
