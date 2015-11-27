package de.m3y3r.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.m3y3r.ekl.EklApp;
import de.m3y3r.ekl.api.model.ShoppingListGet;
import de.m3y3r.oauth.authserver.TokenResponse;

public class ShoppingListIT {

	private static final String INTEGRATION_TEST_ENV = "/integration-test-env.properties";

	private static String baseUrl = "http://localhost:";

	// oauth
	private static final String ENDPOINT_TOKEN = "/token";

	//api
	private static final String buildEndpoint = "/buildinfo";
	private static final String ENDPOINT_LIST = "/list";

	private static final String appPathOauth = "/oauth";
	private static final String appPathEkl = "/ekl";

	@BeforeClass
	public static void setup() throws IOException, InterruptedException {
		Map<String,String> env = readEnvs();
		baseUrl = baseUrl + env.get("PORT");
		waitForServerStartup();
	}

	private static void waitForServerStartup() throws IOException, InterruptedException {
		long maxWaitTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(baseUrl + appPathEkl);
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

		WebTarget targetOauth = client.target(baseUrl + appPathOauth);

		// establish oauth token
		Map<String, String> p = readEnvs();

		BasicAuthentication basicAuthFeature = new BasicAuthentication(p.get("clientId"), p.get("clientSecret"));
		WebTarget tokenEndpoint = targetOauth.path(ENDPOINT_TOKEN).register(basicAuthFeature);

		Form form = new Form();
		form.param("grant_type", "password");
		form.param("username", p.get("userName"));
		form.param("password", p.get("userPass"));
		TokenResponse tokenResponse = tokenEndpoint.request()
				.header("X-Forwarded-Proto", "https")
				.accept(MediaType.APPLICATION_JSON).post(Entity.form(form), TokenResponse.class);

		Assert.assertNotNull(tokenResponse);
		Assert.assertNotNull(tokenResponse.getAccesToken());

		WebTarget targetApp = client.target(baseUrl + appPathEkl);
		WebTarget listEndpoint = targetApp.path(ENDPOINT_LIST);

		List<ShoppingListGet> list = listEndpoint.request().header("Authorization", "Bearer " + tokenResponse.getAccesToken())
		.accept(MediaType.APPLICATION_JSON)
		.get(new GenericType<List<ShoppingListGet>>() {});

		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 0);
	}
}
