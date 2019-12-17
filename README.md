# gatling-dse-examples
A few examples for using the DataStax gatling-dse plugin.  The examples here are not intended to be a complete demonstration of the functionality or features available in the driver; at the moment the goal is to illustrate some changes being made in [JAVA-2562](https://datastax-oss.atlassian.net/browse/JAVA-2562)

# Dependencies
I've tried to keep the dependencies here to a minimum.  I'm assuming you've built the DataStax Gatling DSE plugin (availalable [here](https://github.com/datastax/gatling-dse-plugin)).  "master" in this repo assumes the latest released plugin version (1.3.4) while the "135-snapshot" branch assumes an install of the 1.3.5-SNAPSHOT as defined in the [PR for JAVA-2563](https://github.com/datastax/gatling-dse-plugin/pull/20).

# Running Gatling simulations
Assuming the dependencies above are satisfied you should be able to run the sample scenario with the following:

`./gradlew gatlingRun-SampleSimulation`
