# Oracle Coherence Demonstration

## Overview

This document describes how to build and run the Coherence Demonstration application. This application 
showcases Coherence general features, scalability capabilities as well as new 12.2.1 features including:

* Cache Persistence
* Federation
* Java 8 Support

This application can either be run `locally`, or via `Kubernetes` using 
the [Coherence Operator](https://github.com/oracle/coherence-operator).

When running locally, the application results in a single self-contained jar as well as javadoc and source.

The demonstration uses AngularJS 1.7.5, Bootstrap 3.3.4 as well as a number of other
frameworks. The UI interacts with Coherence using REST.

> **Note:** This demonstration requires Coherence version 12.2.1.3.0 to run locally.

## Table of Contents

* [Prerequisites](#prerequisites)
  * [General Prerequisites](#general-prerequisites)
  * [Kubernetes Prerequisites](#kubernetes-prerequisites)
* [Running the Demo](#running-the-coherence-demonstration)
  * [Running Locally](#running-locally)
  * [Running on Kubernetes (Coherence 12.2.1.3.X)](#running-on-kubernetes-coherence-12213x)
  * [Enabling Federation on Kubernetes](#enabling-federation-on-kubernetes)
* [References](#references)  

## Prerequisites

### General Prerequisites

In order to run the demonstration you must have the following installed:

1. Java 8 or 11 SE Development Kit or Runtime environment.
   You can download JDK8 software below:
   - [Java SE Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
   - [JAVA SE Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
   
   You can download JDK 11 software below:
   - [Java SE Development Kit 11 Downloads](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)

2. Maven version 3.5.4 or above installed and configured.

3. Coherence 12.2.1.3.0 or above installed - http://www.oracle.com/technetwork/middleware/coherence/downloads/index.html.
   If you wish to demonstrate the the Coherence JVisualVM Plug-in, follow the instructions below to install:
   https://docs.oracle.com/middleware/12213/coherence/manage/using-jmx-manage-oracle-coherence.htm#COHMG5583

4. You must use a browser that supports AngularJS to run this application. As of
   writing this, the following are supported:
   * Safari, Chrome, Firefox, Opera 15, IE9 and mobile browsers (Android, Chrome Mobile, iOS Safari).

> **Note**: All code compiles to JDK8 bytecode for compatibility with Coherence releases. 

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

```bash
export PATH=$JAVA_HOME/bin:$PATH
```

For Windows:

```bash
set PATH=%JAVA_HOME%\bin;%PATH%
```

You must have Coherence and Coherence-REST installed into your local maven repository. If you
do not, then carry out the following, replacing the version number with the version
of Coherence you have installed.

E.g. for Linux/UNIX/Mac:

```bash
mvn install:install-file -Dfile=$COHERENCE_HOME/lib/coherence.jar      -DpomFile=$COHERENCE_HOME/plugins/maven/com/oracle/coherence/coherence/12.2.1/coherence.12.2.1.pom
mvn install:install-file -Dfile=$COHERENCE_HOME/lib/coherence-rest.jar -DpomFile=$COHERENCE_HOME/plugins/maven/com/oracle/coherence/coherence-rest/12.2.1/coherence-rest.12.2.1.pom
```

> If you are using Coherence 12.2.1.4.0 and above, please also run the following:
> ```bash
> $ mvn install:install-file -Dfile=$COHERENCE_HOME/lib/coherence-http-grizzly.jar -DpomFile=$COHERENCE_HOME/plugins/maven/com/oracle/coherence/coherence-http-grizzly/12.2.1/coherence-http-grizzly.12.2.1.pom
> ```

E.g. for Windows:

```bash
mvn install:install-file -Dfile=%COHERENCE_HOME%\lib\coherence.jar      -DpomFile=%COHERENCE_HOME%\plugins\maven\com\oracle\coherence\coherence\12.2.1\coherence.12.2.1.pom
mvn install:install-file -Dfile=%COHERENCE_HOME%\lib\coherence-rest.jar -DpomFile=%COHERENCE_HOME%\plugins\maven\com\oracle\coherence\coherence-rest\12.2.1\coherence-rest.12.2.1.pom
```


> **Note:** You may need to specify your settings.xml file by adding the following to download required dependencies.

```
$ mvn -s /path/to/settings.xml ...
```

### Kubernetes Prerequisites

Additionally if you wish to run the application in Kubernetes using the `Coherence Operator`, you must
ensure you meet the following:

* `Software and Runtime Prerequisites` in the [Coherence Operator Quickstart Guide](https://oracle.github.io/coherence-operator/docs/quickstart.html#prerequisites)

* Add the Helm repository and retrieve the Coherence image as described in the [Quickstart Guide](https://oracle.github.io/coherence-operator/docs/quickstart.html#1-environment-configuration)

## Running the application

The Coherence demo can be run locally, or via Kubernetes using 
the [Coherence Operator](https://github.com/oracle/coherence-operator).

### Running Locally

1. Build the application

   ```bash
   $ mvn clean install
   ```
   
 The `target` directory will contain a number of files:

 * coherence-demo-{version}-SNAPSHOT.jar          - Executable JAR file, see instructions below
 * coherence-demo-{version}-SNAPSHOT-javadoc.jar  - javadoc
 * coherence-demo-{version}-SNAPSHOT-sources.jar  - sources
   
Ensuring you have Java 8 in the PATH for your operating system, simply run the following:

```bash
$ java -jar target/coherence-demo-3.0.0-SNAPSHOT.jar
```

This command will startup a Coherence cache server as well as HTTP server on port 8080 for
serving REST and application data.  Once the cache server starts, the default browser
will be opened up to http://127.0.0.1:8080/application/index.html and the application will
load. (If you wish to change the port used, see below.)

The following features are available to demonstrate:

* Dynamically add/ remove cluster members and observing the data repartition and recover automatically.
* Create and Recover snapshots via the "Persistence" menu.
* Enable Real-Time price updates.
* Enable/ Disable indexes for queries.
* Add additional data, clear the cache or populate the cache from the "Tools" menu.
* Start JVisualVM from the "Tools" menu.
* Start a secondary cluster via the "Federation" menu.
* Pause and resume replication to secondary cluster.
* Issue replicate all to secondary cluster.
* Open secondary cluster dashboard to observe changes being replicated.
* Stop Federation and shutdown secondary cluster.

> **Note:** If you recover a snapshot on a cluster you must replicate all to re-sync.

To shutdown the application use the "Shutdown" option from the "Tools" menu.
This will shutdown all processes including the secondary cluster if started.

> **Note:** Secondary cluster may not form if you are running on a VPN due to security restrictions.

#### Modifying the Defaults

*HTTP Ports and hostname*

The default HTTP hostname is 127.0.0.1 and default port is 8080. To modify these you can
add the http.hostname or http.port properties on startup:

```bash
$ java -Dhttp.hostname=myhostname -Dhttp.port=9000 -jar coherence-demo-3.0.0-SNAPSHOT.jar
```

By changing the http.hostname you will be able to access the application outside of
your local machine.

*Default Cluster Names*

When starting up the application the timezone is analyzed and some sensible defaults
for primary and secondary cluster names are chosen (see Launcher.java). If you wish to
sepcify your own, you can do the following:

```bash
$ java -Dprimary.cluster=NewYork -Dsecondary.cluster=Boston -jar coherence-demo-3.0.0-SNAPSHOT.jar
```

If you wish to use a cluster name with a space you must enclose it in quotes.

### Running on Kubernetes (Coherence 12.2.1.3.X)

> **Note:** If you wish you enable Federation when running on Kubernetes, please
> follow steps 1,2 & 3 below and continue with instructions [Here](#enabling-federation-on-kubernetes).
   
The following will install and run the application using Coherence Operator in a namespace
called `coherence-demo-ns`.

1. Create the demonstration namespace

   ```bash
   $ kubectl create namespace coherence-demo-ns

   namespace/sample-coherence-ns created
   ```
   
   > **Note**: You should only need to carry out the following the first time you run the application.

1. Create a secret for pulling images from private repositories

   If you are pulling images from private repositories, you must create a secret
   which will be used for this. For this application we are assuming you have created a secret 
   called `coehrence-demo-secret` in your namespace `coherence-demo-ns`.
   
   ```bash
   $ kubectl create secret docker-registry coherence-demo-secret \
        --namespace coherence-demo-ns \
        --docker-server=your-docker-server \
        --docker-username=your-docker-username \
        --docker-password=your-docker-password
   ```

   See [https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/](https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/) for more information.

   
1. Build and (optionally) push the sidecar Docker image  

   The Coherence Operator requires a sidecar Docker image to be built container the classes and
   configuration files required by this application.
   
   Ensure that you have Docker running locally and issue the following:
   
   ```bash
   $ mvn clean install -P docker
   ```
   
   This will create an image called `coherence-demo-sidecar:3.0.0-SNAPSHOT` which will contain
   cache configuration and Java classes to be added to the classpath at runtime. 
   
   > Note: If you are running against a remote Kubernetes cluster you will need to
   > push the above image to your repository accessible to that cluster. You will also need to 
   > prefix the image name in your `helm` commands below.
   
1. Install the Coherence Operator chart

   ```bash
   $ helm install \
      --namespace coherence-demo-ns \
      --set imagePullSecrets=coherence-demo-secret \
      --name coherence-operator \
      --set "targetNamespaces={coherence-demo-ns}" \
      coherence/coherence-operator
   ```

   Confirm the creation of the chart.
   
   ```bash
   $ helm ls
   NAME              	REVISION	UPDATED                 	STATUS  	CHART                   	APP VERSION	NAMESPACE               
   coherence-operator	1       	Fri May 24 14:39:15 2019	DEPLOYED	coherence-operator-0.9.4	0.9.4      	coherence-demo-ns
   
   $ kubectl get pods -n coherence-demo-ns
   NAME                                  READY   STATUS    RESTARTS   AGE
   coherence-operator-5d4dc4546c-4c925   1/1     Running   0          50s
   ```
   
   If you wish to enable log capture or metrics, please see [here](https://oracle.github.io/coherence-operator/docs/samples/#list-of-samples)
   for the list of samples.
      
1. Install the Coherence Cluster
   
   ```bash
   $ helm install \
      --namespace coherence-demo-ns \
      --name coherence-demo \
      --set clusterSize=1 \
      --set cluster=primary-cluster \
      --set imagePullSecrets=coherence-demo-secret \
      --set store.cacheConfig=cache-config.xml \
      --set store.pof.config=pof-config.xml \
      --set store.javaOpts="-Dprimary.cluster=primary-cluster"  \
      --set userArtifacts.image=coherence-demo-sidecar:3.0.0-SNAPSHOT \
      coherence/coherence
   ```
   
   > **Note:** By default, the latest version of the chart will be used.  You can add `--version 1.0.0` to 
   > choose a specific version. E.g. 1.0.0 in the above case.
   
   You can also choose a specific version of the Coherence Docker image by specifying the following in the
   above `helm install` command:
   
   ```bash
   --set coherence.image=store/oracle/coherence:12.2.1.3.2
   ```

   Because we use stateful sets, the coherence cluster will start one pod at a time.
    
   Use `kubectl get pods -n sample-coherence-ns` to ensure that all pods are running.
   All 3 storage-coherence-0/1/2 pods should be running and ready, as below:

   ```bash
   NAME                 READY   STATUS    RESTARTS   AGE
   coherence-demo-0     1/1     Running   0          4m
   ``` 
   
1. Port forward the HTTP port

   ```bash
   $ kubectl port-forward --namespace coherence-demo-ns coherence-demo-0 8080:8080
   ```  
   
1. Access the application

   Open the following URL to access the application home page.
   
   [http://127.0.0.1:8080/application/index.html](http://127.0.0.1:8080/application/index.html)  

1. Scaling the application using `kubectl`

   When running the application in Kubernates, the `Add Server` and `Remove Server` buttons are not available.
   You need to use kubectl to scale the application.
   
   Scale the application to 2 nodes using:
   
   ```bash
   $ kubectl scale statefulsets coherence-demo --namespace coherence-demo-ns --replicas=2
   ```
   
### Enabling Federation on Kubernetes

You must use Coherence 12.2.1.4.0 or above for Federation to work within Kubernetes.

The setup for this example uses 2 Coherence clusters in the same Kubernetes cluster. If you wish 
to use Federation across Kubernetes cluster please see the [Coherence Operator Samples](https://oracle.github.io/coherence-operator/docs/samples/#list-of-samples).

* Primary Cluster
  * Release name: cluster-1
  * Cluster name: PrimaryCluster
* Secondary Cluster
  * Release name: cluster-2
  * Cluster name: SecondaryCluster

1. Build the Sidecar image

   ```bash
   $ mvn clean install -P coherence12214,docker -Dcoherence.version=12.2.1-4-0
   ```
   
1. Install the **Primary** cluster

   ```bash
   $ helm install \
      --namespace coherence-demo-ns \
      --name cluster-1 \
      --set clusterSize=1 \
      --set cluster=PrimaryCluster \
      --set imagePullSecrets=coherence-demo-secret \
      --set store.cacheConfig=cache-config-12214.xml \
      --set store.overrideConfig=tangosol-coherence-override-12214.xml \
      --set store.pof.config=pof-config.xml \
      --set store.javaOpts="-Dprimary.cluster=PrimaryCluster -Dprimary.cluster.port=40000 -Dprimary.cluster.host=cluster-1-coherence-headless -Dsecondary.cluster=SecondaryCluster -Dsecondary.cluster.port=40000 -Dsecondary.cluster.host=cluster-2-coherence-headless"  \
      --set store.ports.federation=40000 \
      --set userArtifacts.image=coherence-demo-sidecar:3.0.0-SNAPSHOT \
      --set coherence.image=your-12.2.1.4.0-Coherence-image \
      coherence/coherence
   ```   
   
1. Port Forward the Primary Cluster - Port **8088**

   ```bash
   $ kubectl port-forward --namespace coherence-demo-ns cluster-1-coherence-0  8080:8080
   ```

   Open the following URL to access the application home page.
   
   [http://127.0.0.1:8080/application/index.html](http://127.0.0.1:8080/application/index.html)  

1. Install the **Secondary** cluster

   ```bash
   $ helm install \
      --namespace coherence-demo-ns \
      --name cluster-2 \
      --set clusterSize=1 \
      --set cluster=SecondaryCluster \
      --set imagePullSecrets=coherence-demo-secret \
      --set store.cacheConfig=cache-config-12214.xml \
      --set store.overrideConfig=tangosol-coherence-override-12214.xml \
      --set store.pof.config=pof-config.xml \
      --set store.javaOpts="-Dwith.data=false -Dprimary.cluster=PrimaryCluster -Dprimary.cluster.port=40000 -Dprimary.cluster.host=cluster-1-coherence-headless -Dsecondary.cluster=SecondaryCluster -Dsecondary.cluster.port=40000 -Dsecondary.cluster.host=cluster-2-coherence-headless"  \
      --set store.ports.federation=40000 \
      --set userArtifacts.image=coherence-demo-sidecar:3.0.0-SNAPSHOT \
      --set coherence.image=your-12.2.1.4.0-Coherence-image \
      coherence/coherence
   ```   
   
1. Port Forward the Secondary Cluster - Port **8090**

   ```bash
   $ kubectl port-forward --namespace coherence-demo-ns cluster-2-coherence-0  8090:8080
   ```

   Open the following URL to access the application home page.
   
   [http://127.0.0.1:8090/application/index.html](http://127.0.0.1:8090/application/index.html)  

   > You should see that there is no data in the Secondary cluster, as we have not yet started Federation.
   
1. Start Federation on the Primary Cluster

   On the Primary Cluster use the `Federation` menu to `Start Federation`.
   
   Access the Secondary Cluster dashboard and you should see the data appearing from the Primary Cluster.   


### Uninstalling the Charts

Carry out the following commands to delete the chart installed in this sample.

**Without Federation**

```bash
$ helm delete coherence-operator coherence-demo --purge
```

**With Federation**

```bash
$ helm delete coherence-operator cluster-1 cluster-2 --purge
```


Before starting another sample, ensure that all the pods are gone from previous sample.

If you wish to remove the `coherence-operator`, then include it in the `helm delete` command above.


## References

For more information on Oracle Coherence, please see the following links:

* Download Coherence - [http://www.oracle.com/technetwork/middleware/coherence/downloads/index.html](http://www.oracle.com/technetwork/middleware/coherence/downloads/index.html)
* Coherence Documentation - [https://docs.oracle.com/middleware/12213/coherence/docs.htm](https://docs.oracle.com/middleware/12213/coherence/docs.htm)
* Coherence Community - [http://coherence.oracle.com/](http://coherence.oracle.com/)
* Coherence Operator GitHub Page - [https://github.com/oracle/coherence-operator](https://github.com/oracle/coherence-operator)
* Coherence Operator Documentation - [https://oracle.github.io/coherence-operator/](https://oracle.github.io/coherence-operator/)
