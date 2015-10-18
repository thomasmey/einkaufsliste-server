package de.m3y3r.cloud;

import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.datasources.Datasource;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.datasources.Driver;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

/**
 * Start an embedded Wildfly server for usage in an cloudfoundry environment
 * @author thomas
 */
public class App implements Runnable {

	private static final String CF_ENV_PORT = "PORT";
	private static final String CF_VCAP_SERVICES = "VCAP_SERVICES";

	private Logger log;

	public static void main(String... args) {
		App app = new App();
		app.run();
	}

	public App() {
//		System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
		/* needs:
		 *  <dependency>
			  <groupId>org.jboss.logmanager</groupId>
			  <artifactId>jboss-logmanager</artifactId>
			  <version>2.0.0.Final</version>
			</dependency>
		 */
		/* be careful to not initialize JDK logging here as wildfly needs jboss logmanager! */
//		log = Logger.getLogger(App.class.getName());
	}

	public void run() {

		/* for environment variables see:
		 * http://docs.run.pivotal.io/devguide/deploy-apps/environment-variable.html
		 */

		String port = System.getenv(CF_ENV_PORT);

		try {
			Container container = new Container();

			// we want ssl only!
//			container.subsystem(new UndertowFraction() {
//				@Override
//				public void initialize(Container.InitContext initContext) {
//					initContext.socketBinding(new SocketBinding("https").port(port));
//				}
//			});
			DatasourcesFraction dsf = new DatasourcesFraction();
			Map<String, Datasource> dataSourceFromVcapService = getDataSourceFromVcapService();
			Set<String> drivers = new HashSet<>();
			for(Datasource ds: dataSourceFromVcapService.values()) {
				dsf.datasource(ds);
				drivers.add(ds.driver());
			}
			//add relevant drivers
			for(String driver: drivers) {
				Driver dr = new Driver(driver);
				switch(driver) {
				case "postgres":
					dr.module("??");
					dr.datasourceClassName("org.postgresql.Driver");
					dr.xaDatasourceClassName("org.postgresql.Driver");
					break;
				}
				dsf.driver(dr);
			}
			container.subsystem(dsf);

			// Prevent JPA Fraction from installing it's default datasource fraction
//			container.fraction(new JPAFraction()
//					.inhibitDefaultDatasource()
//					.defaultDatasourceName("MyDS")
//					);

			container.start();

			// deploy authorization server
			JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
			deployment.addPackages(true, "de/m3y3r/oauth");
			deployment.addAsWebInfResource(
					new ClassLoaderAsset("META-INF/persistence.xml", App.class.getClassLoader()),
					"classes/META-INF/persistence.xml");
			deployment.addAllDependencies();
			container.deploy(deployment);

		} catch(Exception e) {
			log.log(Level.SEVERE, "server failed", e);
		}
	}

	/**
	 * this expects VCAP_SERVICES v2 style json
	 * @return
	 */
	private Map<String, Datasource> getDataSourceFromVcapService() {

		Map<String, Datasource> datasources = new HashMap<>();

		// http://docs.run.pivotal.io/devguide/deploy-apps/environment-variable.html
		String vcapService = System.getenv(CF_VCAP_SERVICES);
		if(vcapService == null) {
			return datasources;
		}

		StringReader reader = new StringReader(vcapService);
		JsonReader jsonReader = Json.createReader(reader);

		JsonStructure js = jsonReader.read();
		assert js.getValueType() == ValueType.OBJECT;

		// extract database services
		JsonObject jo = (JsonObject) js;
		for(String serviceType: jo.keySet()) {

			JsonArray jaSqldb = jo.getJsonArray(serviceType);

			switch(serviceType) {
			case "elephantsql":
				for(JsonValue entry: jaSqldb) {
					assert entry.getValueType() == ValueType.OBJECT;
					JsonObject dbEntry = (JsonObject) entry;

					JsonObject jsCred = dbEntry.getJsonObject("credentials");
					String pUri = jsCred.getString("uri");

					String name = dbEntry.getString("name");
					Datasource ds = pgDataSourceFromUrl(pUri, name);
					datasources.put(name, ds);
				}
				break;

			case "sqldb":
				break;
			}
		}

		return datasources;
	}

	public static Datasource pgDataSourceFromUrl(String pUri, String name) {

		/* sadly the postgres jdbc driver has no convert utility
		 * to convert a connection string to a jdbc url :-(
		 */
		Pattern pattern = Pattern.compile("postgres://(.+):(.+)@(.+):(\\d+)/(.+)");
		Matcher matcher = pattern.matcher(pUri);
		if(!matcher.matches())
			return null;

		String username = matcher.group(1);
		String password = matcher.group(2);
		String hostname = matcher.group(3);
		String port = matcher.group(4);
		String databaseName = matcher.group(5);

		Datasource datasource = new Datasource(name);
		datasource.authentication(username, password);
		datasource.driver("postgres");
		datasource.connectionURL("jdbc:postgresql://"+ hostname + ':' + port +'/' + databaseName);
		return datasource;
	}
}
