/*
 * File: DeveloperResource.java
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

import com.oracle.bedrock.runtime.LocalPlatform;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * A JAX-RS resource providing Provides developer related commands.
 * <p>
 * <strong>Note:</strong> This is an example only and does not include security
 * security capabilities to protect REST end-points. <p>Adding security via supported
 * methods would be highly recommended if you were to utilize this pattern.
 *
 * @author Tim Middleton
 */
@Path("/developer")
public class DeveloperResource
{
    private static final String SEP = File.separator;

    /**
     * Name of primary cluster.
     */
    private static final String primaryCluster = System.getProperty(Launcher.PRIMARY_CLUSTER_PROPERTY);

    /**
     * Name of secondary cluster.
     */
    private static final String secondaryCluster = System.getProperty(Launcher.SECONDARY_CLUSTER_PROPERTY);


    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML, TEXT_PLAIN})
    @Path("environment")
    public Response getEnvironmentInfo()
    {
        Map<String, Object> mapEnv =  new HashMap<>();

        String sClusterName = CacheFactory.ensureCluster().getClusterName();

        mapEnv.put("runningInKubernetes",       Utilities.isRunningInKubernetes());
        mapEnv.put("coherenceVersion",          Utilities.getCoherenceVersion());
        mapEnv.put("coherenceVersionAsInt",     Utilities.getCoherenceVersionAsInt());
        mapEnv.put("primaryCluster",            sClusterName.equals(primaryCluster));
        mapEnv.put("federationConfiguredInK8s", Utilities.isFederationConfiguredInK8s());
        mapEnv.put("thisClusterName",           sClusterName);

        return Response.status(Response.Status.OK).entity(mapEnv).build();
    }


    @GET
    @Path("insert/{count}")
    public Response getResourceInsert(@PathParam("count") int count)
    {
        Utilities.createPositions(count);

        return Response.ok().build();
    }


    @GET
    @Path("indexes/{enabled}")
    public Response getResourceINdexes(@PathParam("enabled") boolean enabled)
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


    @GET
    @Path("{command}")
    @Produces({TEXT_PLAIN})
    public Response getResource(@PathParam("command") String command)
    {
        Object response = null;

        try
        {
            NamedCache<UUID, Trade> trades = Utilities.getTradesCache();

            switch (command)
            {
            case "jvisualvm" :
                // If -Dvisualvm.executable has been set then use the default for the JVM.
                // VisualVM was removed from the JDK in 9
                String sVisualVM = Utilities.VISUALVM.isEmpty() ?
                                   System.getProperty("java.home") + SEP + ".." + SEP + "bin" + SEP + "jvisualvm" :
                                   Utilities.VISUALVM;
                LocalPlatform.get().launch(sVisualVM);
                break;

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
                response = System.getProperty("http.hostname", "127.0.0.1");
                break;

            case "clusterNames" :
                response = new String(primaryCluster + ':' + secondaryCluster);
                break;

            default :
                throw new RuntimeException("Invalid request: " + command);
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
