/*
 * File: ManagementResource.java
 *
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates.
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

import com.tangosol.net.management.MBeanHelper;

import java.util.Objects;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import jakarta.ws.rs.core.Response;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * A JAX-RS resource providing a REST interface to JMX infrastructure.
 * <ul>
 * <li>Query via - /jmx/query/Coherence:type=Cluster/ClusterName,ClusterSize</li>
 * <li>Query via - /jmx/query/Coherence:type=Service,* &#47;*</li>
 * <li>Run a report - /jmx/run-report/report-name</li>
 * </ul>
 * When you run a report using http://127.0.0.1:8080/service/jmx/run-report/report-cache-size
 * The report name gets a prefix of reports/ and also xml added, so effectively you run
 * "reports/report-cache-size.xml"
 * <p>
 * <strong>Note:</strong> This is an example only and does not include
 * security capabilities to protect REST end-points. <p>Adding security via supported
 * methods would be highly recommended if you were to utilize this pattern.
 *
 * @author Tim Middleton
 */
@Path("/jmx")
public class ManagementResource {

    /**
     * Run a jmx query and return the results as JSON.
     *
     * @param queryArgs          the arguments to query, see MbeanServerConnection.queryNames
     * @param displayAttributes  the attributes to display, either * for all attributes or comma
     *                           separated list of attributes
     *
     * @return the results in JSON format
     */
    @GET
    @Produces( {APPLICATION_JSON})
    @Path("query/{queryArgs}/{displayAttributes}")
    public Response getQuery(@PathParam("queryArgs") String queryArgs,
                             @PathParam("displayAttributes") String displayAttributes) {
        Set<QueryResult>      setResults = new HashSet<>();
        MBeanServerConnection mbs        = MBeanHelper.findMBeanServer();

        try {
            if (mbs != null) {
                Set<ObjectName> setObjects    = mbs.queryNames(new ObjectName(queryArgs), null);
                Set<String>     setAttributes = new HashSet<>();

                if (!"*".equals(displayAttributes)) {
                    String[] attributeList = displayAttributes.split(",");
                    Collections.addAll(setAttributes, attributeList);
                }

                for (ObjectName objectName : setObjects) {
                    Map<String, String>  mapKeys       = new TreeMap<>(objectName.getKeyPropertyList());
                    Map<String, Object>  mapAttributes = new TreeMap<>();
                    MBeanInfo            info          = mbs.getMBeanInfo(objectName);
                    MBeanAttributeInfo[] attrInfo      = info.getAttributes();

                    String[] attrsToRetrieve = new String[setAttributes.size() == 0
                                                          ? attrInfo.length
                                                          : setAttributes.size()];
                    int i = 0;

                    // add the attributes if they are in the attributes or if attributes is "*"
                    for (MBeanAttributeInfo attributeInfo : attrInfo) {
                        String attrName = attributeInfo.getName();

                        if (setAttributes.size() == 0 || setAttributes.contains(attrName)) {
                            attrsToRetrieve[i++] = attrName;
                        }
                    }

                    // validate that all attributes exist
                    for (String attribute : attrsToRetrieve) {
                        if (attribute == null) {
                            throw new RuntimeException("One or more invalid attributes in list: " + setAttributes);
                        }
                    }

                    Arrays.sort(attrsToRetrieve);

                    List<Attribute> lstAttr = mbs.getAttributes(objectName, attrsToRetrieve).asList();

                    // add the attribute values
                    for (Attribute attr : lstAttr) {
                        Object value = attr.getValue();
                        if (value instanceof Object[]) {
                            value = Arrays.toString((Object[]) value);
                        }
                        mapAttributes.put(attr.getName(), value);
                    }

                    setResults.add(new QueryResult(mapKeys, mapAttributes));
                }
            }
            else {
                return Response.serverError().build();
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();
        }

        return Response.ok(setResults).build();
    }

    /**
     * Execute a reporter report and return the report output as JSON.
     *
     * @param reportName  the report name to execute. This is a name of a report in the "reports/"
     *                    path in coherence.jar. The report is appended with .xml to find the report.
     *                    E.g. report-cache-size is translated to "reports/report-cache-size.xml.
     *
     * @return the report output in JSON format
     */
    @GET
    @Produces( {APPLICATION_JSON})
    @Path("run-report/{reportName}")
    public Response getReport(@PathParam("reportName") String reportName) {
        MBeanServerConnection mbs            = MBeanHelper.findMBeanServer();
        String                fullReportName = "reports/" + reportName + ".xml";

        Set<Map<String, Object>> results = new HashSet<>();

        try {
            TabularData reportData = (TabularData) mbs.invoke(
                    new ObjectName(Objects.requireNonNull(getReporterObjectName(mbs))),
                    "runTabularReport", new Object[] {fullReportName},
                    new String[] {"java.lang.String"});

            //noinspection unchecked
            Collection<CompositeData> values = (Collection<CompositeData>) reportData.values();
            for (CompositeData compositeData : values) {
                Set<String>         keys      = compositeData.getCompositeType().keySet();
                Map<String, Object> mapValues = new HashMap<>();

                keys.forEach((k)->mapValues.put(k, compositeData.get(k)));
                results.add(mapValues);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();
        }

        return Response.ok(results).build();
    }

    /**
     * Class to return results of query, e.g. keys and attributes.
     */
    @XmlRootElement(name = "query-results")
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class QueryResult {
        /**
         * The result keys.
         */
        private final Map<String, String> mapKey;

        /**
         * The result attributes.
         */
        private final Map<String, Object> mapAttributes;

        /**
         * Construct a new {@code QueryResult}.
         *
         * @param mapKey         the result keys
         * @param mapAttributes  the result attributes
         */
        public QueryResult(Map<String, String> mapKey, Map<String, Object> mapAttributes) {
            this.mapKey = mapKey;
            this.mapAttributes = mapAttributes;
        }

        /**
         * Obtain the key.
         *
         * @return the key
         */
        public Map<String, String> getKey() {
            return mapKey;
        }

        /**
         * Obtain the attributes.
         *
         * @return the attributes.
         */
        public Map<String, Object> getAttributes() {
            return mapAttributes;
        }
    }


    /**
     * Retrieve the Reporter MBean for the local member ID. A JMX query is performed to get the object
     * as it may have additional key values due to a container environment.
     *
     * @param server  the {@link MBeanServerConnection} used for querying
     *
     * @return the reporter for the local member ID
     */
    private String getReporterObjectName(MBeanServerConnection server) {
        int localMember = CacheFactory.getCluster().getLocalMember().getId();
        String query =
                "Coherence:type=Reporter,nodeId=" + localMember + ",*";
        try {
            Set<ObjectName> setResult = server.queryNames(new ObjectName(query),
                    null);
            for (Object oResult : setResult) {
                return oResult.toString();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(
                    "Unable to obtain reporter for nodeId=" + localMember + ": " + e.getMessage());
        }
        return null;
    }
}



