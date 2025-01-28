/*
 * File: Launcher.java
 *
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates.
 *
 * You may not use this file except in compliance with the Universal Permissive
 * License (UPL), Version 1.0 (the "License.")
 *
 * You may obtain a copy of the License at https://opensource.org/licenses/UPL.
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.oracle.coherence.demo.application;

import com.oracle.bedrock.util.Pair;

import com.oracle.coherence.spring.CoherenceContext;
import com.tangosol.net.DefaultCacheServer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.time.ZoneId;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The launcher for the Coherence Demo application.
 * <p>
 * To launch the application issue the following using Java 8 or 11:
 * <pre>
 *     java -jar target/coherence-demo-3.0.0-SNAPSHOT.jar.
 * </pre>
 * <p>
 * To use a different port other than 8080 use:
 * <pre>
 *     java -Dhttp.port=9000 -jar target/coherence-demo-3.0.0-SNAPSHOT.jar.
 * </pre>
 *
 * <p>
 * To use a different hostname other than 127.0.0.1 use:
 * <pre>
 *     java -Dhttp.hostname=my-host-name.com -jar target/coherence-demo-3.0.0-SNAPSHOT.jar.
 * </pre>
 * <p>
 * To change the cluster names from defaults, use:
 * <pre>
 *     java -Dprimary.cluster=Boston -Dsecondary.cluster=NewYork -jar target/coherence-demo-3.0.0-SNAPSHOT.jar.
 * </pre>
 * <p>
 *
 * @author Brian Oliver
 */
public final class Launcher
{
    /**
     * System property to override the primary cluster name.
     */
    public static final String PRIMARY_CLUSTER_PROPERTY = "primary.cluster";

    /**
     * System property to override the secondary cluster name.
     */
    public static final String SECONDARY_CLUSTER_PROPERTY = "secondary.cluster";

    /**
     * Default Jaeger tracing endpoint if not overridden by user.
     */
    public static final String DEFAULT_JAEGER_ENDPOINT = "http://localhost:14268/api/traces";

    /**
     * System property to define the Jaeger service name (as it will be displayed in the UI).
     */
    public static final String JAEGER_SERVICE_NAME_PROPERTY = "JAEGER_SERVICE_NAME";

    /**
     * System property to define the Jaeger tracing endpoint.  If not explicitly overridden,
     * the value will default to {@value DEFAULT_JAEGER_ENDPOINT}.
     */
    public static final String JAEGER_ENDPOINT_PROPERTY = "JAEGER_ENDPOINT";


    /**
     * Cluster port for the primary cluster.
     */
    @SuppressWarnings("unused")
    public static final int PRIMARY_PORT = 7574;

    /**
     * Cluster port for the secondary cluster.
     */
    public static final int SECONDARY_PORT = 7575;

    /**
     * Default cluster name for the primary cluster.
     */
    private static String PRIMARY_DEFAULT;

    /**
     *
     * Default cluster name for the secondary cluster.
     */
    private static String SECONDARY_DEFAULT;

    /**
     * Map containing defaults for cluster names based upon Timezone.
     */
    private static final Map<String, Pair<String, String>> MAP_ZONES;


    static
    {
        // initialize the list of default cluster names
        MAP_ZONES = new HashMap<>();

        MAP_ZONES.put("Australia/Melbourne", new Pair<>("Melbourne", "Perth"));
        MAP_ZONES.put("Australia/Sydney", new Pair<>("Sydney", "Singapore"));
        MAP_ZONES.put("Australia/Perth", new Pair<>("Perth", "Sydney"));

        MAP_ZONES.put("Asia/Singapore", new Pair<>("Singapore", "London"));
        MAP_ZONES.put("Singapore", new Pair<>("Singapore", "London"));
        MAP_ZONES.put("Asia/Shanghai", new Pair<>("Shanghai", "Beijing"));
        MAP_ZONES.put("Asia/Dubai", new Pair<>("Dubai", "Beijing"));
        MAP_ZONES.put("Asia/Hong_Kong", new Pair<>("Hong Kong", "Sydney"));
        MAP_ZONES.put("Asia/Kuala_Lumpur", new Pair<>("Kuala Lumpur", "London"));
        MAP_ZONES.put("Asia/Tokyo", new Pair<>("Tokyo", "London"));
        MAP_ZONES.put("Asia/Kolkata", new Pair<>("Bangalore", "New Delhi"));

        MAP_ZONES.put("America/Buenos_Aires", new Pair<>("Buenos Aires", "London"));
        MAP_ZONES.put("America/Denver", new Pair<>("Denver", "New York"));
        MAP_ZONES.put("America/Los_Angeles", new Pair<>("Los Angeles", "New York"));
        MAP_ZONES.put("America/New_York", new Pair<>("New York", "London"));
        MAP_ZONES.put("America/Vancouver", new Pair<>("Vancouver", "Montreal"));

        MAP_ZONES.put("US/Central", new Pair<>("Chicago", "New York"));
        MAP_ZONES.put("US/Eastern", new Pair<>("Boston", "London"));
        MAP_ZONES.put("UTC", new Pair<>("Boston", "London"));

        MAP_ZONES.put("Europe/London", new Pair<>("London", "New York"));
        MAP_ZONES.put("Europe/Moscow", new Pair<>("Moscow", "London"));
        MAP_ZONES.put("Europe/Paris", new Pair<>("Paris", "New York"));
        MAP_ZONES.put("Europe/Istanbul", new Pair<>("Istanbul", "London"));
        MAP_ZONES.put("Europe/Rome", new Pair<>("Italy", "London"));
        MAP_ZONES.put("Europe/Madrid", new Pair<>("Madrid", "Singapore"));

        MAP_ZONES.put("Japan", new Pair<>("Tokyo", "London"));
    }

    // ----- constructors ---------------------------------------------------

    /**
     * Instances not allowed.
     */
    private Launcher()
        {
        throw new IllegalStateException("illegal instantiation");
        }

    /**
     * Entry point for the demo application.
     *
     * @param args  unused
     */
    public static void main(String[] args)
    {

        var applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");

        CoherenceContext.setApplicationContext(applicationContext);

        // set JVisualVM refresh time to 5 seconds for demo purposes only
        System.setProperty("com.oracle.coherence.jvisualvm.refreshtime", "5");

        // set the cache configuration
        System.setProperty("coherence.cacheconfig", "cache-config.xml");

        System.setProperty("coherence.role", "CoherenceDemoLauncher");

        // specify to ignore new 2server SE strategy
        System.setProperty("coherence.distribution.2server", "false");

        // use WKA
        System.setProperty("coherence.wka", "127.0.0.1");
        System.setProperty("coherence.ttl", "0");

        // enable http serving
        System.setProperty("with.http", "true");

        // setup some reasonable defaults for ClusterNames based upon timezone
        chooseDefaults();

        // set properties for cluster names, so they are passed to other processes
        System.setProperty(PRIMARY_CLUSTER_PROPERTY, System.getProperty(PRIMARY_CLUSTER_PROPERTY, PRIMARY_DEFAULT));
        System.setProperty(SECONDARY_CLUSTER_PROPERTY,
                           System.getProperty(SECONDARY_CLUSTER_PROPERTY, SECONDARY_DEFAULT));

        // set cluster name
        System.setProperty("coherence.cluster", System.getProperty(PRIMARY_CLUSTER_PROPERTY));

        // set properties necessary for Jaeger to function
        System.setProperty(JAEGER_SERVICE_NAME_PROPERTY,
                           "Coherence Demo (" + System.getProperty(PRIMARY_CLUSTER_PROPERTY) + ')');

        System.setProperty(JAEGER_ENDPOINT_PROPERTY,
                           System.getProperty(JAEGER_ENDPOINT_PROPERTY, DEFAULT_JAEGER_ENDPOINT));

        // start the Default Cache Server
        DefaultCacheServer.main(args);
    }


    /**
     * Chose defaults for primary and secondary cluster based upon your location.
     */
    private static void chooseDefaults()
    {
        String zone   = ZoneId.systemDefault().toString();
        Locale locale = Locale.getDefault();

        System.out.println("*** Locale: " + locale + ", Country=" + locale.getCountry() + ", Language="
                           + locale.getLanguage() + ", Zone: " + zone);

        // try direct matches first
        Pair<String, String> entry = MAP_ZONES.get(zone);

        if (entry != null)
        {
            PRIMARY_DEFAULT   = entry.getX();
            SECONDARY_DEFAULT = entry.getY();
        }
        else
        {
            // no direct match so make some broad assumptions
            if (zone.startsWith("Australia"))
            {
                PRIMARY_DEFAULT   = "Sydney";
                SECONDARY_DEFAULT = "Perth";
            }
            else if (zone.startsWith("Africa"))
            {
                PRIMARY_DEFAULT   = "Cape Town";
                SECONDARY_DEFAULT = "London";
            }
            else if (zone.startsWith("Brazil"))
            {
                PRIMARY_DEFAULT   = "Brasilia";
                SECONDARY_DEFAULT = "London";
            }
            else if (zone.startsWith("Canada"))
            {
                PRIMARY_DEFAULT   = "Vancouver";
                SECONDARY_DEFAULT = "Montreal";
            }
            else if (zone.startsWith("Europe"))
            {
                PRIMARY_DEFAULT   = "London";
                SECONDARY_DEFAULT = "NewYork";
            }
            else if (zone.startsWith("Pacific"))
            {
                PRIMARY_DEFAULT   = "Honolulu";
                SECONDARY_DEFAULT = "San Francisco";
            }
            else if (zone.startsWith("US") || zone.startsWith("America") || locale.getCountry().equalsIgnoreCase("US"))
            {
                PRIMARY_DEFAULT   = "New York";
                SECONDARY_DEFAULT = "San Francisco";
            }
            else
            {
                PRIMARY_DEFAULT   = "Primary";
                SECONDARY_DEFAULT = "Secondary";
            }
        }
    }
}
