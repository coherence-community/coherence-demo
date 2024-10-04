/*
 * File: BootstrapInterceptor.java
 *
 * Copyright (c) 2015, 2024 Oracle and/or its affiliates.
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

import com.oracle.coherence.demo.model.Price;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.events.EventInterceptor;
import com.tangosol.net.events.application.LifecycleEvent;
import com.tangosol.util.Base;

import java.awt.Desktop;

import java.net.URI;
import java.util.Random;

/**
 * An {@link EventInterceptor} for bootstrapping the Coherence Demo Application.
 *
 * @author Brian Oliver
 */
public class BootstrapInterceptor
        implements EventInterceptor<LifecycleEvent> {

    @Override
    public void onEvent(LifecycleEvent event) {
        if (event.getType() == LifecycleEvent.Type.ACTIVATED) {
            int memberId = CacheFactory.getCluster().getLocalMember().getId();
            // check if we are the first member, or we are running in Kubernetes as the
            // first member could be http which is storage-disabled and the data cannot yet be loaded
            if (memberId == 1 || Utilities.isRunningInKubernetes()) {
                // only load if with.data=true, which is defaulted to true
                // with.data is set to false on the start of the secondary cluster for federation
                if ("true".equals(System.getProperty("with.data", "true"))) {
                    // create initial data for the demo if it does not already exist
                    NamedCache<String, Price> pricesCache = Utilities.getPricesCache();
                    boolean                   loadData    = false;

                    // check to see if the data is loaded already if we are in Kubernetes
                    if (Utilities.isRunningInKubernetes()) {
                        if (pricesCache.isEmpty()) {
                            // wait for a short while in case the two storage members start at exact same time
                            // and if the prices cache is still zero then load
                            Base.sleep(new Random().nextInt(4000) + 1000L);
                            if (pricesCache.isEmpty()) {
                                loadData = true;
                            }
                        }
                    }
                    else if (memberId == 1) {
                        loadData = true;  // we are not in Kubernetes so load the data for 1st member
                    }

                    if (loadData) {
                        Utilities.addIndexes();
                        Utilities.populatePrices();
                        Utilities.createPositions();
                    }
                }

                // cater for case where user has overridden default port via -Dhttp.port=xxxx
                String sPort = System.getProperty("http.port");
                String sHost = System.getProperty("http.hostname");
                String sURL = "http://" + (sHost == null ? "127.0.0.1" : sHost) + ':'
                              + (sPort == null ? "8080" : sPort) + "/application/index.html";

                // open the default web browser to start the front-end if we are not running in k8s
                try {
                    if (!Utilities.isRunningInKubernetes() && Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI(sURL));
                    }
                    else {
                        System.out.println("Open: " + sURL + " to start the demo");
                    }
                }
                catch (Exception e) {
                    System.out.println("Open: " + sURL + " to start the demo");
                }

                System.out.println("***\n*** Default cluster names chosen are Primary: "
                                   + System.getProperty(Launcher.PRIMARY_CLUSTER_PROPERTY) + ", Secondary: "
                                   + System.getProperty(Launcher.SECONDARY_CLUSTER_PROPERTY) + "\n***");
            }
        }
    }
}
