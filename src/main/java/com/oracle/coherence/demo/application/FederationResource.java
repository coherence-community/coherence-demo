/*
 * File: FederationResource.java
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

import com.oracle.coherence.demo.model.Trade;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.NamedCache;
import com.tangosol.net.management.MBeanServerProxy;
import com.tangosol.net.management.Registry;
import com.tangosol.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Map;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

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
 * security capabilities to protect REST end-points. <p>Adding security via supported
 * methods would be highly recommended if you were to utilize this pattern.
 *
 * @author Tim Middleton
 */
@Path("/federation")
public class FederationResource
{
    private static final String[] STRING_ARG = new String[] {"java.lang.String"};

    /**
     * Name of primary cluster.
     */
    private static final String primaryCluster = System.getProperty(Launcher.PRIMARY_CLUSTER_PROPERTY);

    /**
     * Name of secondary cluster.
     */
    private static final String secondaryCluster = System.getProperty(Launcher.SECONDARY_CLUSTER_PROPERTY);


    @GET
    @Path("{command}")
    @Produces({TEXT_PLAIN})
    @SuppressWarnings("unchecked")
    public Response federationCommand(@PathParam("command") String command) throws InterruptedException
    {
        Cluster                 cluster  = CacheFactory.getCluster();
        Registry                registry = cluster.getManagement();

        NamedCache<UUID, Trade> trades   = Utilities.getTradesCache();

        if (registry != null)
        {
            MBeanServerProxy proxy  = registry.getMBeanServerProxy();
            String           sMBean = getFederationMBean(trades.getCacheService().getInfo().getServiceName());

            if (registry.isRegistered(sMBean))
            {
                Object response = "OK";
                String target   = cluster.getClusterName().equals(primaryCluster) ? secondaryCluster : primaryCluster;

                switch (command)
                {
                case "start" :
                case "stop" :
                case "replicateAll" :
                case "pause" :
                    proxy.invoke(sMBean, command, new String[] {target}, STRING_ARG);
                    break;

                case "reportState" :
                    Map           statusMap = (Map) proxy.invoke(sMBean, command, new String[] {target}, STRING_ARG);
                    StringBuilder sb        = new StringBuilder();

                    // key = state and value = percent of members in that state
                    statusMap.forEach((key, value) -> sb.append(key).append(' '));

                    // just take the first status if we multiple values as it is just transitive anyway
                    String status = sb.toString();

                    response = status.substring(0, status.indexOf(' '));
                    break;

                default :
                    return Response.status(404).build();
                }

                return Response.ok(response).build();
            }
        }

        return Response.serverError().build();
    }


    /**
     * Obtain the name of the FederationMBean for a given service.
     *
     * @param sServiceName  the service name to obtain name for
     *
     * @return the name
     */
    private String getFederationMBean(String sServiceName)
    {
        return Registry.FEDERATION_TYPE + ",service=" + sServiceName + "," + Registry.KEY_RESPONSIBILITY
               + "Coordinator";
    }
}
