package de.m3y3r.cloud;

import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.datasources.JDBCDriver;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.jpa.JPAFraction;

/**
 * Start an embedded Wildfly server for usage in an cloud foundry environment
 * @author thomas
 */
public class App implements Runnable {

	private static final String CF_ENV_PORT = "PORT";
	private static final String CF_VCAP_SERVICES = "VCAP_SERVICES";

	private static final String ENV_DS_MAP = "DATASOURCE_MAP";
	private static final String ENV_DS_DEFAULT = "DATASOURCE_DEFAULT";

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
		System.setProperty("jboss.http.port", port);

		try {
			Container container = new Container();

			DatasourcesFraction dsf = new DatasourcesFraction();
//			dsf.jdbcDriver(childKey, config)
			Map<String, DataSource> dataSourceFromVcapService = getDataSourceFromVcapService();
			//add relevant drivers
			{
				Set<String> drivers = new HashSet<>();
				for(DataSource ds: dataSourceFromVcapService.values()) {
					drivers.add(ds.driverName());
				}
				for(String driver: drivers) {
					JDBCDriver dr = new JDBCDriver(driver);
					switch(driver) {
					case "postgres":
						dr.driverModuleName("org.postgresql.postgres");
						dr.driverXaDatasourceClassName("org.postgresql.xa.PGXADataSource");
						break;
					}
					dsf.jdbcDriver(dr);
				}
			}
			// add all datasources
			for(DataSource ds: dataSourceFromVcapService.values()) {
				dsf.dataSource(ds);
			}
			container.fraction(dsf);

			container.fraction(new JPAFraction()
					// Prevent JPAFraction from installing it's default datasource fraction
					.inhibitDefaultDatasource()
					.defaultDatasource("jboss/datasources/" + System.getenv(ENV_DS_DEFAULT))
					);

			container.start();

			// deploy app
			JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
			deployment.addPackages(true, "de/m3y3r/ekl");
			deployment.addPackages(true, "de/m3y3r/common");
			deployment.addPackages(true, "de/m3y3r/oauth");
			deployment.addAsWebInfResource(
					new ClassLoaderAsset("META-INF/persistence.xml", App.class.getClassLoader()),
					"classes/META-INF/persistence.xml");
			deployment.addAsWebInfResource(
					new ClassLoaderAsset("META-INF/load.sql", App.class.getClassLoader()),
					"classes/META-INF/load.sql");
			deployment.addAsWebInfResource(
					new ClassLoaderAsset("build.properties", App.class.getClassLoader()),
					"classes/build.properties");
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
	private Map<String, DataSource> getDataSourceFromVcapService() {

		Map<String, DataSource> datasources = new HashMap<>();

		// http://docs.run.pivotal.io/devguide/deploy-apps/environment-variable.html
		String vcapService = System.getenv(CF_VCAP_SERVICES);
		if(vcapService == null) {
			return datasources;
		}

		Map<String, String> dsMap = getDatasourceMapping();

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
					DataSource ds = pgDataSourceFromUrl(pUri, dsMap.get(name));
					datasources.put(name, ds);
				}
				break;

			case "sqldb":
				break;
			}
		}

		return datasources;
	}

	private Map<String, String> getDatasourceMapping() {
		String dsMapping = System.getenv(ENV_DS_MAP);

		StringReader reader = new StringReader(dsMapping);
		JsonReader jsonReader = Json.createReader(reader);

		JsonStructure js = jsonReader.read();
		assert js.getValueType() == ValueType.OBJECT;

		JsonObject jo = (JsonObject) js;

		Map<String, String> dsMap = new HashMap<>();
		for(Entry<String, JsonValue> e: jo.entrySet()) {
			dsMap.put(e.getKey(), ((JsonString)e.getValue()).getString());
		}

		return dsMap;
	}

	public static DataSource pgDataSourceFromUrl(String pUri, String name) {

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

		DataSource datasource = new DataSource(name);
		datasource.userName(username);
		datasource.password(password);
		datasource.driverName("postgres");
		datasource.connectionUrl("jdbc:postgresql://"+ hostname + ':' + port +'/' + databaseName);
		return datasource;
	}
}
