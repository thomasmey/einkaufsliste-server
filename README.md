Wildfly-Swam backend component
==============================

This project is the backend component that is able to run in
an cloud foundry environemnt, like IBM Bluemix.

This maven project creates an jar which contains everything needed for
starting the application, by command:
<code>mvn package</code>

Example push command:
<code>cf push appname -p target/einkaufsliste-xxx-swarm.jar -b java_buildpack</code>
