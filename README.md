Oracle Coherence Demonstration
==============================

Overview
--------

This document describes how to build and run the Coherence Demonstration application.
Building the application results in a single self-contained jar as well as javadoc and source.
The demonstration showcases Coherence general features, scalability capabilities as well as new 12.2.1
features including:

* Cache Persistence
* Federation
* Java 8 Support

The demonstration uses AngularJS 1.4.1, Bootstrap 3.3.4 as well as a number of other
frameworks. The UI interacts with Coherence using REST.

*Note:* This demonstration requires 12.2.1.1.0 of Coherence. Please see Prerequisites section below.

Prerequisites
-------------
In order to run the demonstration you must have the following installed:

1. Java 8 SE Development Kit or Runtime environment.
   You can download the software from:
   - Java SE Development Kit - http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
   - JAVA SE Runtime Environment - http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html

2. Maven version 3.2.5 or above installed and configured.
3. Coherence 12.2.1.1.0 or above installed - http://www.oracle.com/technetwork/middleware/coherence/downloads/index.html.
   If you wish to demonstrate the the Coherence JVisualVM Plug-in, follow the instructions below to install:
   https://docs.oracle.com/middleware/1221/coherence/manage/jmx.htm#COHMG5582
4. You must use a browser that supports AngularJS to run this application. As of
   writing this, the following are supported:
   * Safari, Chrome, Firefox, Opera 15, IE9 and mobile browsers (Android, Chrome Mobile, iOS Safari).

For more information on browser compatibility see https://code.angularjs.org/1.4.1/docs/misc/faq.

Ensure the following environment variables are set:

* `JAVA_HOME` -- Make sure that the `JAVA_HOME` environment variable points to the location of a JDK supported by the
Oracle Coherence version you are using.

* `COHERENCE_HOME` -- Make sure `COHERENCE_HOME` is set to point to your 'coherence' directory under your 
   install location. I.e. the directory containing the bin, lib, doc directories.
This is only required for the Maven install-file commands.

* `MAVEN_HOME` -- If mvn command is not in your path then you should set `MAVEN_HOME` and then add `MAVEN_HOME\bin` to your PATH
in a similar way to Java being added to the path below.

You must also ensure the java command is in the path.

E.g. for Linux/UNIX:
```
export PATH=$JAVA_HOME/bin:$PATH
```

For Windows:

```
set PATH=%JAVA_HOME%\bin;%PATH%
```

You must have Coherence and Coherence-REST installed into your local maven repository. If you
do not, then carry out the following, replacing the version number with the version
of Coherence you have installed.

E.g. for Linux/UNIX/Mac:

```
mvn install:install-file -Dfile=$COHERENCE_HOME/lib/coherence.jar      -DpomFile=$COHERENCE_HOME/plugins/maven/com/oracle/coherence/coherence/12.2.1/coherence.12.2.1.pom
mvn install:install-file -Dfile=$COHERENCE_HOME/lib/coherence-rest.jar -DpomFile=$COHERENCE_HOME/plugins/maven/com/oracle/coherence/coherence-rest/12.2.1/coherence-rest.12.2.1.pom
```

E.g. for Windows:

```
mvn install:install-file -Dfile=%COHERENCE_HOME%\lib\coherence.jar      -DpomFile=%COHERENCE_HOME%\plugins\maven\com\oracle\coherence\coherence\12.2.1\coherence.12.2.1.pom
mvn install:install-file -Dfile=%COHERENCE_HOME%\lib\coherence-rest.jar -DpomFile=%COHERENCE_HOME%\plugins\maven\com\oracle\coherence\coherence-rest\12.2.1\coherence-rest.12.2.1.pom
```

**Note:** You may need to specify your settings.xml file by adding the following to download required dependencies.

```
mvn -s /path/to/settings.xml ...
```

Build Instructions
------------------

Build the Coherence Demonstration by using:

```
mvn clean install
```

The target directory will contain a number of files:

- coherence-demo-2.0.1-SNAPSHOT.jar          - Executable JAR file, see instructions below
- coherence-demo-2.0.1-SNAPSHOT-javadoc.jar  - javadoc
- coherence-demo-2.0.1-SNAPSHOT-sources.jar  - sources

Run Instructions
----------------

Ensuring you have Java 8 in the PATH for your operating system, simply run the following:

```
      java -jar target/coherence-demo-2.0.1-SNAPSHOT.jar
```

This command will startup a Coherence cache server as well as HTTP server on port 8080 for
serving REST and application data.  Once the cache server starts, the default browser
will be opened up to http://127.0.0.1:8080/application/index.html and the application will
load. (If you wish to change the port used, see below.)

The following features are available to demonstrate:

- Dynamically add/ remove cluster members and observing the data repartition and recover automatically.
- Create and Recover snapshots via the "Persistence" menu.
- Enable Real-Time price updates.
- Enable/ Disable indexes for queries.
- Add additional data, clear the cache or populate the cache from the "Tools" menu.
- Start JVisualVM from the "Tools" menu.
- Start a secondary cluster via the "Federation" menu.
- Pause and resume replication to secondary cluster.
- Issue replicate all to secondary cluster.
- Open secondary cluster dashboard to observe changes being replicated.
- Stop Federation and shutdown secondary cluster.

**Note:** If you recover a snapshot on a cluster you must replicate all to re-sync.

To shutdown the application use the "Shutdown" option from the "Tools" menu.
This will shutdown all processes including the secondary cluster if started.

**Note:** Secondary cluster may not form if you are running on a VPN due to security restrictions.

Modifying the Defaults
----------------------

*HTTP Ports and hostname*

The default HTTP hostname is 127.0.0.1 and default port is 8080. To modify these you can
add the http.hostname or http.port properties on startup:

```
java -Dhttp.hostname=myhostname -Dhttp.port=9000 -jar coherence-demo-2.0.1-SNAPSHOT.jar
```

By changing the http.hostname you will be able to access the application outside of
your local machine.

*Default Cluster Names*

When starting up the application the timezone is analyzed and some sensible defaults
for primary and secondary cluster names are chosen (see Launcher.java). If you wish to
sepcify your own, you can do the following:

```
java -Dprimary.cluster=NewYork -Dsecondary.cluster=Boston -jar coherence-demo-2.0.1-SNAPSHOT.jar
```

If you wish to use a cluster name with a space you must enclose it in quotes.

References
----------

For more information on Oracle Coherence, please see the following links:

- Download Coherence - http://www.oracle.com/technetwork/middleware/coherence/downloads/index.html
- Coherence Documentation - http://docs.oracle.com/middleware/1221/coherence/index.html
- Coherence Community - http://coherence.java.net/
