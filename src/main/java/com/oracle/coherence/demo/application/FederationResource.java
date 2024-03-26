/*
 * File: FederationResource.java
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
import com.tangosol.net.Cluster;
import com.tangosol.net.NamedCache;

import com.tangosol.net.management.MBeanServerProxy;
import com.tangosol.net.management.Registry;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import jakarta.ws.rs.core.Response;

import java.util.Map;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * A JAX-RS resource providing the ability to control Federation by issuing the
 * following commands:
 * <ul>
 *     <li>start - start a secondary cluster</li>
 *     <li>stop - stop a secondary cluster</li>
 *     <li>replicateAll - Issue a replicateAll command</li>
 *     <li>reportStat - Report the state of federation</li>
 * </ul>
 * <p>
 * <strong>Note:</strong> This is an example only and does not include security
 * capabilities to protect REST end-points. <p>Adding security via supported
 * methods would be highly recommended if you were to utilize this pattern.
 *
 * @author Tim Middleton
 */
@Path("/federation")
public class FederationResource {
    /**
     * Argument types for invocations against an MBean server.
     */
    private static final String[] STRING_ARG = new String[] {"java.lang.String"};

    /**
     * Name of primary cluster.
     */
    private static final String PRIMARY_CLUSTER = System.getProperty(Launcher.PRIMARY_CLUSTER_PROPERTY);

    /**
     * Name of secondary cluster.
     */
    private static final String SECONDARY_CLUSTER = System.getProperty(Launcher.SECONDARY_CLUSTER_PROPERTY);

    /**
     * Invoke the specified federated resource command.
     * <p>
     * Available commands are:
     * <ul>
     *     <li>
     *         start {@code ->} starts replication
     *     </li>
     *     <li>
     *         stop {@code ->} stops replication
     *     </li>
     *     <li>
     *         replicateAll {@code ->} replicate all caches
     *     </li>
     *     <li>
     *         reportState {@code ->} report current federation state
     *     </li>
     *     <li>
     *         hostname {@code ->}  returns the current value of the {@code http.hostname} system property
     *     </li>
     *     <li>
     *         clusterNames {@code ->} returns a composite of the two federated demo clusters in the
     *         format of {@code <PRIMARY_CLUSTER_NAME>:<SECONDARY_CLUSTER_NAME>}
     *     </li>
     * </ul>
     *
     * @param command  the command to invoke
     *
     * @return {@link Response#ok}, a {@code 404} if the command isn't found
     */
    @GET
    @Path("{command}")
    @Produces( {TEXT_PLAIN})
    public Response federationCommand(@PathParam("command") String command) {
        Cluster                   cluster  = CacheFactory.getCluster();
        Registry                  registry = cluster.getManagement();
        NamedCache<String, Trade> trades   = Utilities.getTradesCache();

        if (registry != null) {
            MBeanServerProxy proxy  = registry.getMBeanServerProxy();
            String           sMBean = getFederationMBean(trades.getCacheService().getInfo().getServiceName());

            if (registry.isRegistered(sMBean)) {
                Object response = "OK";
                String target = cluster.getClusterName().equals(PRIMARY_CLUSTER) ? SECONDARY_CLUSTER
                                                                                 : PRIMARY_CLUSTER;

                switch (command) {
                    case "start":
                    case "stop":
                    case "replicateAll":
                    case "pause":
                        proxy.invoke(sMBean, command, new String[] {target}, STRING_ARG);
                        break;

                    case "reportState":
                        Map<?, ?> statusMap = (Map<?, ?>) proxy.invoke(sMBean,
                                command,
                                new String[] {target},
                                STRING_ARG);
                        StringBuilder sb = new StringBuilder();

                        // key = state and value = percent of members in that state
                        statusMap.forEach((key, value)->sb.append(key).append(' '));

                        // just take the first status if we have multiple values as it is just transitive anyway
                        String status = sb.toString();

                        response = status.substring(0, status.indexOf(' '));
                        break;

                    default:
                        return Response.status(Response.Status.NOT_FOUND).build();
                }

                return Response.ok(response).build();
            }
        }

        return Response.serverError().build();
    }

    /**
     * Obtain the name of the FederationMBean for a given service.
     *
     * @param sServiceName  the service name used to construct the MBean reference
     *
     * @return the name
     */
    private String getFederationMBean(String sServiceName) {
        return Registry.FEDERATION_TYPE + ",service=" + sServiceName + "," + Registry.KEY_RESPONSIBILITY
               + "Coordinator";
    }
}
