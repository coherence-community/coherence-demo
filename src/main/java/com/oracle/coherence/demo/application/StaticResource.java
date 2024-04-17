/*
 * File: StaticResource.java
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

import com.oracle.coherence.common.base.Logger;

import com.tangosol.util.Base;
import com.tangosol.util.Resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import jakarta.ws.rs.core.Response;

import java.io.IOException;

import java.net.URL;

/**
 * Serves static resources for an application from the class-path.
 *
 * @author Brian Oliver
 */
@Path("{resource: .*}")
public class StaticResource {

    /**
     * The base folder containing static resources on the class path.
     */
    private static final String BASE_FOLDER = "web";

    /**
     * Serve static web content.
     *
     * @param resource the static resource
     *
     * @return a successful response if the resource is found, a 404 otherwise, and an error if the resource is
     *         found but cannot be served
     */
    @GET
    public Response getResource(@PathParam("resource") String resource) {
        // construct the resource path relative to the base folder
        String resourcePath = BASE_FOLDER + '/' + resource;

        // construct a URL to the resource (using the class loader)
        URL url = Resources.findFileOrResource(resourcePath, Base.ensureClassLoader(null));

        try {
            return url == null
                   ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(url.openStream()).build();

        }
        catch (IOException e) {
            Logger.info("Unexpected error service static resource " + resourcePath);
            Logger.info(e);
            return Response.serverError().build();
        }
    }
}
