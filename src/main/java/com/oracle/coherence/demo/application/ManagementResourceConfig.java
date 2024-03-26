/*
 * File: ManagementResourceConfig.java
 *
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates.
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

import com.tangosol.coherence.management.RestManagement;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * The Coherence Demo Web Rest Management {@link ResourceConfig}.
 *
 * @author Tim Middleton
 */
public class ManagementResourceConfig
        extends ResourceConfig {

    public ManagementResourceConfig() {
        RestManagement.configure(this);
    }
}