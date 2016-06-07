/*
 * File: BootstrapInterceptor.java
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

import com.tangosol.net.CacheFactory;
import com.tangosol.net.events.EventInterceptor;
import com.tangosol.net.events.application.LifecycleEvent;

import java.awt.*;
import java.net.URI;

/**
 * An {@link EventInterceptor} for bootstrapping the Coherence Demo Application.
 *
 * @author Brian Oliver
 */
public class BootstrapInterceptor implements EventInterceptor<LifecycleEvent>
{
    @Override
    public void onEvent(LifecycleEvent event)
    {
        if (event.getType() == LifecycleEvent.Type.ACTIVATED)
        {
            if (CacheFactory.getCluster().getLocalMember().getId() == 1)
            {
                // only load if with.data=true, which is defaulted to true
                // with.data is set to false on startup of secondary cluster for federation
                if ("true".equals(System.getProperty("with.data", "true")))
                {
                    // create initial data for the demo
                    Utilities.addIndexes();
                    Utilities.populatePrices();
                    Utilities.createPositions();
                }

                // cater for case where user has overridden default port via -Dhttp.port=xxxx
                String sPort = System.getProperty("http.port");
                String sHost = System.getProperty("http.hostname");
                String sURL  = "http://127.0.0.1:" + (sPort == null ? "8080" : sPort) + "/application/index.html";

                // open the default web browser to start the front-end
                try
                {
                    if (Desktop.isDesktopSupported())
                    {
                        Desktop.getDesktop().browse(new URI(sURL));
                    }
                    else
                    {
                        System.out.println("Open: " + sURL + " to start the demo");
                    }
                }
                catch (Exception e)
                {
                    System.out.println("Open: " + sURL + " to start the demo");
                }

                System.out.println("***\n*** Default cluster names chosen are Primary: "
                                   + System.getProperty(Launcher.PRIMARY_CLUSTER_PROPERTY) + ", Secondary: "
                                   + System.getProperty(Launcher.SECONDARY_CLUSTER_PROPERTY) + "\n***");
            }
        }
    }
}
