/*
 * File: DeveloperResource.java
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

import com.oracle.coherence.demo.model.Trade;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * A JAX-RS resource providing Provides developer-related commands.
 * <p>
 * <strong>Note:</strong> This is an example only and does not include security
 * capabilities to protect REST end-points. <p>Adding security via supported
 * methods would be highly recommended if you were to utilize this pattern.
 *
 * @author Tim Middleton
 */
@Path("/developer")
public class DeveloperResource
{
    /**
     * Name of primary cluster.
     */
    private static final String PRIMARY_CLUSTER = System.getProperty(Launcher.PRIMARY_CLUSTER_PROPERTY);

    /**
     * Name of secondary cluster.
     */
    private static final String SECONDARY_CLUSTER = System.getProperty(Launcher.SECONDARY_CLUSTER_PROPERTY);

    /**
     * Return the environment information for this Coherence cluster.
     *
     * @return the result as JSON
     */
    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML, TEXT_PLAIN})
    @Path("environment")
    public Response getEnvironmentInfo()
    {
        Map<String, Object> mapEnv =  new HashMap<>();

        String clusterName = CacheFactory.ensureCluster().getClusterName();
        String edition     = CacheFactory.getEdition();

        mapEnv.put("runningInKubernetes",       Utilities.isRunningInKubernetes());
        mapEnv.put("metricsEnabled",            Utilities.isMetricsEnabled());
        mapEnv.put("coherenceVersion",          Utilities.getCoherenceVersion());
        mapEnv.put("coherenceVersionAsInt",     Utilities.getCoherenceVersionAsInt());
        mapEnv.put("primaryCluster",            clusterName.equals(PRIMARY_CLUSTER));
        mapEnv.put("federationConfiguredInK8s", Utilities.isFederationConfiguredInK8s());
        mapEnv.put("thisClusterName",           clusterName);
        mapEnv.put("coherenceEdition",          edition);
        mapEnv.put("coherenceEditionFull",      ("CE".equals(edition) ? "Community" : "Grid") + " Edition");
        mapEnv.put("javaVersion",               System.getProperty("java.version") + " " +
                                                System.getProperty("java.vendor"));
        // properties for limiting resource usage
        mapEnv.put("maxServers",                System.getProperty("max.servers", "1000"));
        mapEnv.put("maxCacheEntries",           System.getProperty("max.cache.entries", "99999999999"));
        mapEnv.put("disableShutdown",           Boolean.valueOf(System.getProperty("disable.shutdown", "false")));

        return Response.status(Response.Status.OK).entity(mapEnv).build();
    }

    /**
     * Insert a number of positions based on the input argument.
     *
     * @param count  the number of positions to create
     *
     * @return {@link Response#ok}
     */
    @GET
    @Path("insert/{count}")
    public Response getResourceInsert(@PathParam("count") int count)
    {
        Utilities.createPositions(count);

        return Response.ok().build();
    }


     /**
      * Adds or removes indexes based in input arguments.
      *
      * @param enabled  flag determining whether to add or remove the indexes
      *
      * @return {@link Response#ok}
      */
    @GET
    @Path("indexes/{enabled}")
    public Response getResourceIndexes(@PathParam("enabled") boolean enabled)
    {
        if (enabled)
        {
            Utilities.addIndexes();
        }
        else
        {
            Utilities.removeIndexes();
        }

        return Response.ok().build();
    }


    /**
     * Invoke the specified developer resource command.
     * <p>
     * Available commands are:
     * <ul>
     *     <li>
     *         populate {@code ->} populates the stock positions
     *     </li>
     *     <li>
     *         clear {@code ->} clears the trade cache
     *     </li>
     *     <li>
     *         shutdown {@code ->} terminates the cluster
     *     </li>
     *     <li>
     *         hostname {@code ->} returns the current value of the {@code http.hostname} system property
     *     </li>
     *     <li>
     *         clusterNames {@code ->} returns a composite of the two federated demo clusters in the
     *         format of {@code <PRIMARY_CLUSTER_NAME>:<SECONDARY_CLUSTER_NAME>}
     *     </li>
     * </ul>
     *
     * @param command  the command to invoke
     *
     * @return {@link Response#ok}, a {@code 404} if the command isn't found, or an error response
     *         if an exception is raised
     */
    @GET
    @Path("{command}")
    @Produces({TEXT_PLAIN})
    public Response getResource(@PathParam("command") String command)
    {
        Object response = null;

        try
        {
            NamedCache<String, Trade> trades = Utilities.getTradesCache();

            switch (command)
            {
            case "clear" :
                trades.clear();
                break;

            case "populate" :
                Utilities.createPositions();
                break;

            case "shutdown" :
                System.out.println("Coherence Demo has been shutdown. Please close any browsers.");
                System.exit(0);
                break;

            case "hostname" :
                String lbrHostname = System.getProperty("lbr.hostname");
                // check for an overriding load balancer hostname first
                if (lbrHostname != null)
                {
                    response = lbrHostname;
                }
                else
                {
                    response = System.getProperty("http.hostname", "127.0.0.1");
                }

                break;

            case "clusterNames" :
                response = PRIMARY_CLUSTER + ':' + SECONDARY_CLUSTER;
                break;

            default :
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();

            return Response.serverError().build();
        }

        if (response != null)
        {
            return Response.ok(response).build();
        }

        return Response.ok().build();
    }
}
