/*
 * File: Launcher.java
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates.
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

import com.tangosol.net.DefaultCacheServer;

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
 * To change the cluster names from defaults  use:
 * <pre>
 *     java -Dprimary.cluster=Boston -Dsecondary.cluster=NewYork -jar target/coherence-demo-3.0.0-SNAPSHOT.jar.
 * </pre>
 * <p>
 *
 * @author Brian Oliver
 */
public class Launcher
{
    /**
     * System property to override primary name.
     */
    public static final String PRIMARY_CLUSTER_PROPERTY = "primary.cluster";

    /**
     * System property to override secondary name.
     */
    public static final String SECONDARY_CLUSTER_PROPERTY = "secondary.cluster";

    /**
     * Cluster port for primary cluster.
     */
    public static final int PRIMARY_PORT = 7574;

    /**
     * Cluster port for secondary cluster.
     */
    public static final int SECONDARY_PORT = 7575;

    /**
     * Default cluster name for primary cluster
     */
    private static String PRIMARY_DEFAULT;

    /**
     *
     * Default cluster name for secondary cluster.
     */
    private static String SECONDARY_DEFAULT;

    /**
     * Map containing defaults for cluster names based upon Timezone.
     */
    private static Map<String, Pair<String, String>> mapZones;


    static
    {
        // initialize the list of default cluster names
        mapZones = new HashMap<>();

        mapZones.put("Australia/Melbourne", new Pair<>("Melbourne", "Perth"));
        mapZones.put("Australia/Sydney", new Pair<>("Sydney", "Singapore"));
        mapZones.put("Australia/Perth", new Pair<>("Perth", "Sydney"));

        mapZones.put("Asia/Singapore", new Pair<>("Singapore", "London"));
        mapZones.put("Singapore", new Pair<>("Singapore", "London"));
        mapZones.put("Asia/Shanghai", new Pair<>("Shanghai", "Beijing"));
        mapZones.put("Asia/Dubai", new Pair<>("Dubai", "Beijing"));
        mapZones.put("Asia/Hong_Kong", new Pair<>("Hong Kong", "Sydney"));
        mapZones.put("Asia/Kuala_Lumpur", new Pair<>("Kuala Lumpur", "London"));
        mapZones.put("Asia/Tokyo", new Pair<>("Tokyo", "London"));
        mapZones.put("Asia/Kolkata", new Pair<>("Bangalore", "New Delhi"));

        mapZones.put("America/Buenos_Aires", new Pair<>("Buenos Aires", "London"));
        mapZones.put("America/Denver", new Pair<>("Denver", "New York"));
        mapZones.put("America/Los_Angeles", new Pair<>("Los Angeles", "New York"));
        mapZones.put("America/New_York", new Pair<>("New York", "London"));
        mapZones.put("America/Vancouver", new Pair<>("Vancouver", "Montreal"));

        mapZones.put("US/Central", new Pair<>("Chicago", "New York"));
        mapZones.put("US/Eastern", new Pair<>("Boston", "London"));

        mapZones.put("Europe/London", new Pair<>("London", "New York"));
        mapZones.put("Europe/Moscow", new Pair<>("Moscow", "London"));
        mapZones.put("Europe/Paris", new Pair<>("Paris", "New York"));
        mapZones.put("Europe/Istanbul", new Pair<>("Istanbul", "London"));
        mapZones.put("Europe/Rome", new Pair<>("Italy", "London"));
        mapZones.put("Europe/Madrid", new Pair<>("Madrid", "Singapore"));

        mapZones.put("Japan", new Pair<>("Tokyo", "London"));
    }


    public static void main(String[] args) throws Exception
    {
        // set JVisualVM refresh time to 5 seconds for demo purposes only
        System.setProperty("com.oracle.coherence.jvisualvm.refreshtime", "5");

        // set the cache configuration
        System.setProperty("coherence.cacheconfig", "cache-config.xml");

        System.setProperty("coherence.role", "CoherenceDemoLauncher");

        // specify to ignore new 2 server SE strategy
        System.setProperty("coherence.distribution.2server", "false");

        // use WKA
        System.setProperty("coherence.wka", "127.0.0.1");
        System.setProperty("coherence.ttl", "0");

        // enable http serving
        System.setProperty("with.http", "true");

        // setup some reasonable defaults for ClusterNames based upon timezone
        chooseDefaults();

        // set properties for cluster names so they are passed to other processes
        System.setProperty(PRIMARY_CLUSTER_PROPERTY, System.getProperty(PRIMARY_CLUSTER_PROPERTY, PRIMARY_DEFAULT));
        System.setProperty(SECONDARY_CLUSTER_PROPERTY,
                           System.getProperty(SECONDARY_CLUSTER_PROPERTY, SECONDARY_DEFAULT));

        // set cluster name
        System.setProperty("coherence.cluster", System.getProperty(PRIMARY_CLUSTER_PROPERTY));

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

        System.out.println("*** Locale: " + locale + ", Country=" + locale.getCountry() + ", Lang="
                           + locale.getLanguage() + ", Zone: " + zone);

        // try direct matches first
        Pair<String, String> entry = mapZones.get(zone);

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
