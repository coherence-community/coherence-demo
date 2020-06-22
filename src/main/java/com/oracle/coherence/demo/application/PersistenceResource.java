/*
 * File: PersistenceResource.java
 *
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates.
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

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.management.Registry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * A JAX-RS resource providing the ability to control persistence by issuing
 * the following commands:
 * <ul>
 *     <li>
 *         createSnapshot - Create a snapshot with the name SNAPSHOT_NAME. If the snapshot already exists it will
 *         be removed first
 *     </li>
 *     <li>
 *         removeSnapshot - Remove a snapshot with the name SNAPSHOT_NAME.
 *     </li>
 *     <li>
 *         recoverSnapshot - Recover a snapshot with the name SNAPSHOT_NAME.
 *     </li>
 * </ul>
 *
 * @author Tim Middleton
 */
@Path("/persistence")
public class PersistenceResource
{
    /**
     * Name of the federated service we are using.
     */
    private static final String SERVICE_NAME = CacheFactory.getEdition().equals("CE") ? "DistributedCache" : "FederatedCache";


    /**
     * Name of the snapshot to create.
     */
    private static final String SNAPSHOT_NAME = "CoherenceDemoSnapshot";

    /**
     * Invoke the specified persistence resource command.
     * <p>
     * Available commands are:
     * <ul>
     *     <li>
     *         createSnapshot {@code ->} creates a snapshot
     *     </li>
     *     <li>
     *         removeSnapshot {@code ->} removes a snapshot
     *     </li>
     *     <li>
     *         recoverSnapshot {@code ->} recover a cache for an existing snapshot
     *     </li>
     * </ul>
     *
     * @param command  the command to invoke
     *
     * @return {@link Response#ok}, a {@code 404} if the command isn't found
     */
    @GET
    @Path("{command}")
    @Produces({TEXT_PLAIN})
    public Response getResource(@PathParam("command") String command)
    {
        Cluster  cluster  = CacheFactory.getCluster();
        Registry registry = cluster.getManagement();

        if (registry != null)
        {
            PersistenceHelper helper   = new PersistenceHelper();
            Object            response = "OK";

            switch (command)
            {
            case "createSnapshot" :
                removeSnapshot(helper);
                helper.invokeOperationWithWait(PersistenceHelper.CREATE_SNAPSHOT, SNAPSHOT_NAME, SERVICE_NAME);
                break;

            case "removeSnapshot" :
                removeSnapshot(helper);
                break;

            case "recoverSnapshot" :
                if (helper.snapshotExists(SERVICE_NAME, SNAPSHOT_NAME))
                {
                    helper.invokeOperationWithWait(PersistenceHelper.RECOVER_SNAPSHOT, SNAPSHOT_NAME, SERVICE_NAME);
                }
                else
                {
                    response = "Snapshot does not exist";
                }
                break;

            default :
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            return Response.ok(response).build();
        }

        return Response.serverError().build();
    }


    /**
     * Remove the snapshot.
     *
     * @param helper  PersistenceToolsHelper used to remove the snapshot
     */
    private void removeSnapshot(PersistenceHelper helper)
    {
        if (helper.snapshotExists(SERVICE_NAME, SNAPSHOT_NAME))
        {
            helper.invokeOperationWithWait(PersistenceHelper.REMOVE_SNAPSHOT, SNAPSHOT_NAME, SERVICE_NAME);
        }
    }
}
