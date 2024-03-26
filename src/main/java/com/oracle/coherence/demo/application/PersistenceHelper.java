/*
 * File: PersistenceHelper.java
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
import com.tangosol.net.management.MBeanServerProxy;
import com.tangosol.net.management.Registry;

import com.tangosol.util.Base;

import java.util.Arrays;

/**
 * Various helper methods for running Persistence commands.
 *
 * @author Tim Middleton
 */
public class PersistenceHelper {

    /**
     * JMX operation to create a snapshot.
     */
    public static final String CREATE_SNAPSHOT = "createSnapshot";

    /**
     * JMX operation to recover a snapshot.
     */
    public static final String RECOVER_SNAPSHOT = "recoverSnapshot";

    /**
     * JMX operation to remove a snapshot.
     */
    public static final String REMOVE_SNAPSHOT = "removeSnapshot";

    /**
     * JMX operation to archive a snapshot.
     */
    @SuppressWarnings("unused")
    public static final String ARCHIVE_SNAPSHOT = "archiveSnapshot";

    /**
     * JMX operation to retrieve an archived snapshot.
     */
    @SuppressWarnings("unused")
    public static final String RETRIEVE_ARCHIVED_SNAPSHOT = "retrieveArchivedSnapshot";

    /**
     * JMX operation to remove an archived snapshot.
     */
    @SuppressWarnings("unused")
    public static final String REMOVE_ARCHIVED_SNAPSHOT = "removeArchivedSnapshot";

    /**
     * JMX operation to suspend a service.
     */
    public static final String SUSPEND_SERVICE = "suspendService";

    /**
     * JMX operation to resume a service.
     */
    public static final String RESUME_SERVICE = "resumeService";

    /**
     * Sleep time between checking operation completion.
     */
    private static final long SLEEP_TIME = 500L;

    /**
     * Signifies no snapshots were found.
     */
    private static final String[] NO_SNAPSHOTS = new String[0];

    /**
     * MBean server proxy for JMX operations and attribute retrieval.
     */
    private final MBeanServerProxy mbsProxy;

    /**
     * Management Registry for the cluster.
     */
    private final Registry registry;

    /**
     * Construct a new PersistenceHelper which can be used to issue
     * persistence related commands for the examples.
     */
    public PersistenceHelper() {
        Cluster cluster = CacheFactory.ensureCluster();

        registry = cluster.getManagement();

        if (registry == null) {
            throw new RuntimeException("Unable to retrieve Registry from cluster");
        }

        mbsProxy = registry.getMBeanServerProxy();

        // wait for registration of Cluster as the registration is done
        // async and may not be complete before our first call after ensureCluster()
        try {
            waitForRegistration(registry, Registry.CLUSTER_TYPE);
        }
        catch (InterruptedException e) {
            throw Base.ensureRuntimeException(e, "Unable to find MBean");
        }
    }

    /**
     * Obtain a list the snapshots for the specified service.
     *
     * @param serviceName   the name of the service to list snapshots for
     *
     * @return the snapshots for the specified service
     */
    public String[] listSnapshots(String serviceName) {
        String[] snapshots = (String[]) getAttribute(ensureGlobalName(getMBeanName(serviceName)), "Snapshots");

        return snapshots == null ? NO_SNAPSHOTS : snapshots;
    }

    /**
     * Obtain a list of archived snapshots for a given service.
     *
     * @param serviceName   the name of the service to query
     *
     * @return a {@link String}[] of archived snapshots for the given service
     */
    @SuppressWarnings("unused")
    public String[] listArchivedSnapshots(String serviceName) {
        return (String[]) mbsProxy.invoke(ensureGlobalName(getMBeanName(serviceName)),
                "listArchivedSnapshots",
                new String[0],
                new String[0]);
    }

    /**
     * Resume a given service.
     *
     * @param serviceName the service to resume
     */
    @SuppressWarnings("unused")
    public void resumeService(String serviceName) {
        mbsProxy.invoke(Registry.CLUSTER_TYPE,
                RESUME_SERVICE,
                new String[] {serviceName},
                new String[] {"java.lang.String"});
    }

    /**
     * Suspend a given service.
     *
     * @param serviceName the service to suspend
     */
    @SuppressWarnings("unused")
    public void suspendService(String serviceName) {
        mbsProxy.invoke(Registry.CLUSTER_TYPE,
                SUSPEND_SERVICE,
                new String[] {serviceName},
                new String[] {"java.lang.String"});
    }

    /**
     * Issue an operation and wait for the operation to be complete by
     * polling the "Idle" attribute of the PersistenceCoordinator for the service.
     * This method will poll continuously until an "Idle" status has been reached
     * or until timeout set by a calling thread has been raised. e.g.<br>
     * <pre>
     * try (Timeout t = Timeout.after(120, TimeUnit.SECONDS))
     * {
     *     helper.invokeOperationWithWait("createSnapshot", "snapshot", "Service");
     * }
     * </pre>
     *
     * @param operation    the operation to execute
     * @param snapshot     the snapshot name
     * @param serviceName  the name of the service to execute operation on
     */
    public void invokeOperationWithWait(String operation,
                                        String snapshot,
                                        String serviceName) {
        try {
            String beanName = ensureGlobalName(getMBeanName(serviceName));

            mbsProxy.invoke(beanName, operation, new String[] {snapshot}, new String[] {"java.lang.String"});

            while (true) {
                Base.sleep(SLEEP_TIME);

                if ((boolean) getAttribute(beanName, "Idle")) {
                    // idle means the operation has completed as we are guaranteed an up-to-date
                    // attribute value just after an operation was called
                    return;
                }
            }
        }
        catch (Exception e) {
            throw Base.ensureRuntimeException(e, "Unable to complete operation " + operation + " for service "
                                                 + serviceName);
        }
    }

    /**
     * Validate that a snapshot exists for a given service.
     *
     * @param serviceName   the service name to check
     * @param snapshotName  the snapshot name to check
     *
     * @return true if the snapshot exists for the service
     */
    public boolean snapshotExists(String serviceName,
                                  String snapshotName) {
        try {
            String[] aSnapshots = listSnapshots(serviceName);

            return aSnapshots != null && Arrays.asList(aSnapshots).contains(snapshotName);
        }
        catch (Exception e) {
            throw Base.ensureRuntimeException(e, "Error listing snapshots");
        }
    }

    /**
     * Wait for the given MBean to be registered.
     *
     * @param registry  registry to use
     * @param beanName  the MBean to wait for
     *
     * @throws InterruptedException if the MBean is not registered
     */
    public static void waitForRegistration(Registry registry,
                                           String beanName) throws InterruptedException {
        int nMaxRetries = 100;

        while (!registry.getMBeanServerProxy().isMBeanRegistered(beanName)) {
            Base.sleep(100L);

            if (--nMaxRetries == 0) {
                throw new RuntimeException("Unable to find registered MBean " + beanName);
            }
        }
    }

    /**
     * Obtain the PersistenceManagerMBean name for a given service.
     *
     * @param service  the service name
     *
     * @return the MBean name
     */
    public static String getMBeanName(String service) {
        return Registry.PERSISTENCE_SNAPSHOT_TYPE + ",service=" + service + "," + Registry.KEY_RESPONSIBILITY
               + "PersistenceCoordinator";
    }

    /**
     * Return a global name for the given MBean Name.
     *
     * @param name  the MBean to get the global name for
     *
     * @return the global name.
     */
    private String ensureGlobalName(String name) {
        return registry.ensureGlobalName(name);
    }

    /**
     * Return an attribute name from an MBean.
     *
     * @param objectName  object name to query
     * @param attribute   attribute to retrieve from object name
     *
     * @return the value of the attribute
     */
    private Object getAttribute(String objectName,
                                String attribute) {
        return mbsProxy.getAttribute(objectName, attribute);
    }
}
