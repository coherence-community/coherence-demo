/*
 * File: ServiceResourceConfig.java
 *
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates.
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

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Defined the configured REST services for the Coherence Demo application.
 *
 * @author Brian Oliver
 */
@ApplicationPath("service")
public class ServiceResourceConfig
        extends ResourceConfig {
    
    /**
     * Constructs a {@link ServiceResourceConfig}.
     */
    public ServiceResourceConfig() {
        register(MemberInfoResource.class);
        register(StartMemberResource.class);
        register(StopMemberResource.class);
        register(ChartDataResource.class);
        register(FederationResource.class);
        register(EventsResource.class);
        register(StartSecondaryResource.class);
        register(DeveloperResource.class);
        register(PersistenceResource.class);
        register(ManagementResource.class);
        register(TracingFilter.class);
    }
}
