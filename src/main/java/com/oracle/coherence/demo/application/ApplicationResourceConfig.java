/*
 * File: ApplicationResourceConfig.java
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

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * The Coherence Demo Web Application {@link ResourceConfig}.
 *
 * @author Brian Oliver
 */
@ApplicationPath("application")
public class ApplicationResourceConfig extends ResourceConfig
{
    /**
     * Constructs the {@link ApplicationResourceConfig}.
     */
    public ApplicationResourceConfig()
    {
        register(StaticResource.class);
    }
}
