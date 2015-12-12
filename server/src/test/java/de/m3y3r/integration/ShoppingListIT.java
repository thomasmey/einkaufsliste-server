package de.m3y3r.integration;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.m3y3r.ekl.api.model.ItemPost;
import de.m3y3r.ekl.api.model.ResourceId;
import de.m3y3r.ekl.api.model.ShoppingListGet;
import de.m3y3r.ekl.api.model.ShoppingListPost;
import de.m3y3r.oauth.authserver.TokenResponse;
import de.m3y3r.oauth.filter.RateLimitFilter;

public class ShoppingListIT {

	//api
	private static final String APP_PATH_EKL = "ekl";
	private static final String ENDPOINT_READY = "ready";
	private static final String ENDPOINT_LIST = "list";

	@BeforeClass
	public static void setup() throws IOException, InterruptedException {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(Util.getBaseUrl());
		WebTarget path = target.path(APP_PATH_EKL).path(ENDPOINT_READY);
		Util.waitForServerStartup(path);
		client.close();
	}

	@Test
	public void testReadShoppingList() throws IOException, InterruptedException {

		Client client = ClientBuilder.newClient();
		BearerToken bearerToken = new BearerToken();
		WebTarget targetApp = client.target(Util.getBaseUrl()).path(APP_PATH_EKL).register(bearerToken);
		WebTarget listEndpoint = targetApp.path(ENDPOINT_LIST);

		List<ShoppingListGet> list = listEndpoint.request().accept(MediaType.APPLICATION_JSON).get(new GenericType<List<ShoppingListGet>>() {});
		client.close();

		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 0);
	}

	@Test
	public void testCreateShoppingList() throws IOException, InterruptedException {

		Client client = ClientBuilder.newClient();
		BearerToken bearerToken = new BearerToken();
		WebTarget targetApp = client.target(Util.getBaseUrl()).path(APP_PATH_EKL).register(bearerToken);
		WebTarget listEndpoint = targetApp.path(ENDPOINT_LIST);

		ShoppingListPost sl = new ShoppingListPost();
		ResourceId rid = listEndpoint.request().post(Entity.entity(sl, MediaType.APPLICATION_JSON), ResourceId.class);
		client.close();

//		Assert.assertNotNull(rid);
//		Assert.assertNotNull(rid.getId());
	}

	@Test
	public void testAddItem() throws IOException, InterruptedException {

		Client client = ClientBuilder.newClient();
		BearerToken bearerToken = new BearerToken();
		WebTarget targetApp = client.target(Util.getBaseUrl()).path(APP_PATH_EKL).register(bearerToken);
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

		client.close();
		Assert.assertNotNull(shoppingListGet);
	}

}

class BearerToken implements ClientRequestFilter {

	private String accessToken;

	private static long lastTokenRequest = System.currentTimeMillis();

	// oauth
	private static final String ENDPOINT_TOKEN = "token";
	private static final String appPathOauth = "oauth";

	private TokenResponse getToken() throws InterruptedException {

		synchronized (BearerToken.class) {
			long ct = System.currentTimeMillis();
			long diff = ct - lastTokenRequest;
			if(diff < RateLimitFilter.getMinMillisPerRequest()) {
				System.out.println("sleeping for " + diff);
				//FIXME!
				Thread.sleep(diff * 3);
			}
			lastTokenRequest = ct;
		}

		Client client = ClientBuilder.newClient();

		WebTarget targetOauth = client.target(Util.getBaseUrl()).path(appPathOauth);

		BasicAuthentication basicAuthFeature = new BasicAuthentication(Util.getConfig("clientId"), Util.getConfig("clientSecret"));
		WebTarget tokenEndpoint = targetOauth.path(ENDPOINT_TOKEN).register(basicAuthFeature);

		Form form = new Form();
		form.param("grant_type", "password");
		form.param("username", Util.getConfig("userName"));
		form.param("password", Util.getConfig("userPass"));
		TokenResponse tokenResponse = null;
		tokenResponse = tokenEndpoint.request()
			.header("X-Forwarded-Proto", "https")
			.accept(MediaType.APPLICATION_JSON).post(Entity.form(form), TokenResponse.class);
		client.close();
		return tokenResponse;
	}

	public BearerToken() throws InterruptedException {
		this.accessToken = getToken().getAccesToken();
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		requestContext.getHeaders().add("Authorization", "Bearer " + accessToken);
	}

}