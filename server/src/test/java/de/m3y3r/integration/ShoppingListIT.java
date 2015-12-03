package de.m3y3r.integration;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.m3y3r.ekl.api.model.ItemPost;
import de.m3y3r.ekl.api.model.ResourceId;
import de.m3y3r.ekl.api.model.ShoppingListGet;
import de.m3y3r.ekl.api.model.ShoppingListPost;
import de.m3y3r.oauth.authserver.TokenResponse;

public class ShoppingListIT {

	private static final String INTEGRATION_TEST_ENV = "/integration-test-env.properties";

	private static String baseUrl = "http://localhost:";

	// oauth
	private static final String ENDPOINT_TOKEN = "token";

	//api
	private static final String ENDPOINT_READY = "ready";
	private static final String ENDPOINT_LIST = "list";

	private static final String appPathOauth = "oauth";
	private static final String appPathEkl = "ekl";

	@BeforeClass
	public static void setup() throws IOException, InterruptedException {
		Map<String,String> env = readEnvs();
		baseUrl = baseUrl + env.get("PORT");

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(baseUrl);
		WebTarget path = target.path(appPathEkl).path(ENDPOINT_READY);
		waitForServerStartup(path);

		path = target.path(appPathOauth).path(ENDPOINT_READY);
		waitForServerStartup(path);
	}

	private static void waitForServerStartup(WebTarget path) throws IOException, InterruptedException {
		System.out.println("wait for path="+path.getUri());
		long maxWaitTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);

		boolean okay = false;
		System.out.println("waiting for server: ");
		while(System.currentTimeMillis() < maxWaitTime) {
			Response response = path.request().get();
			int s = response.getStatus();
			response.close();

			System.out.println("response: " + s);
			if(s == Status.OK.getStatusCode()) {
				okay = true;
				break;
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

	static TokenResponse getToken() throws IOException {
		Client client = ClientBuilder.newClient();

		WebTarget targetOauth = client.target(baseUrl).path(appPathOauth);

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

//		Assert.assertNotNull(tokenResponse);
//		Assert.assertNotNull(tokenResponse.getAccesToken());
		return tokenResponse;
	}

	@Test
	public void testReadShoppingList() throws IOException {

		Client client = ClientBuilder.newClient();
		BearerToken bearerToken = new BearerToken();
		WebTarget targetApp = client.target(baseUrl).path(appPathEkl).register(bearerToken);
		WebTarget listEndpoint = targetApp.path(ENDPOINT_LIST);

		List<ShoppingListGet> list = listEndpoint.request().accept(MediaType.APPLICATION_JSON).get(new GenericType<List<ShoppingListGet>>() {});

		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 0);
	}

	@Test
	public void testCreateShoppingList() throws IOException {

		Client client = ClientBuilder.newClient();
		BearerToken bearerToken = new BearerToken();
		WebTarget targetApp = client.target(baseUrl).path(appPathEkl).register(bearerToken);
		WebTarget listEndpoint = targetApp.path(ENDPOINT_LIST);

		ShoppingListPost sl = new ShoppingListPost();
		ResourceId rid = listEndpoint.request().post(Entity.entity(sl, MediaType.APPLICATION_JSON), ResourceId.class);

//		Assert.assertNotNull(rid);
//		Assert.assertNotNull(rid.getId());
	}

	@Test
	public void testAddItem() throws IOException {

		Client client = ClientBuilder.newClient();
		BearerToken bearerToken = new BearerToken();
		WebTarget targetApp = client.target(baseUrl).path(appPathEkl).register(bearerToken);
		WebTarget listEndpoint = targetApp.path(ENDPOINT_LIST);

		List<ShoppingListGet> list = listEndpoint.request().accept(MediaType.APPLICATION_JSON).get(new GenericType<List<ShoppingListGet>>() {});

		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 0);

		ShoppingListGet shoppingList0 = list.get(0);

		WebTarget itemEndpoint = listEndpoint.path("/{id}/item");
		WebTarget itemEndpointId = itemEndpoint.resolveTemplate("id", list.get(0).getId());
		ItemPost item = new ItemPost();
		item.setName("Walnüsse");
		item.setCount(BigDecimal.valueOf(1));
		item.setUnit("UNIT");

		ResourceId rid = itemEndpointId.request().post(Entity.entity(item, MediaType.APPLICATION_JSON), ResourceId.class);

		Assert.assertNotNull(rid);
		Assert.assertNotNull(rid.getId());

		ShoppingListGet shoppingListGet = listEndpoint.path("/{id}").resolveTemplate("id", shoppingList0.getId()).request()
		.accept(MediaType.APPLICATION_JSON)
		.get(ShoppingListGet.class);

		Assert.assertNotNull(shoppingListGet);
	}

}

class BearerToken implements ClientRequestFilter {

	private String accessToken;
	public BearerToken() throws IOException {
		this.accessToken = ShoppingListIT.getToken().getAccesToken();
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		requestContext.getHeaders().add("Authorization", "Bearer " + accessToken);
	}

}