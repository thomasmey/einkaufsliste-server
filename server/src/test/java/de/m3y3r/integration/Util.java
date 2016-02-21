package de.m3y3r.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class Util {

	private static final String INTEGRATION_TEST_ENV = "/integration-test-env.properties";
	private static final String BASE_URL = "http://localhost:9080/ekl";
	private static Map<String,String> env = readEnvs();

	public static String getBaseUrl() {
		return BASE_URL /*+ env.get("PORT") */ ;
	}

	private static Map<String, String> readEnvs() {
		try(InputStream inStream = ShoppingListIT.class.getResourceAsStream(INTEGRATION_TEST_ENV)) {;
			Properties p = new Properties();
			p.load(inStream);
			return (Map)p;
		} catch (IOException e) {
			Logger.getLogger(Util.class.getName()).log(Level.SEVERE,"config file not found!", e);
			return Collections.emptyMap();
		}
	}


	public static void waitForServerStartup(WebTarget path) throws IOException, InterruptedException {
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

	public static String getConfig(String configKey) {
		return env.get(configKey);
	}

}
