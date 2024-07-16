/*
 * Copyright (c) 2020, 2024 Oracle and/or its affiliates.
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

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;

import io.opentracing.tag.Tags;

import io.opentracing.util.GlobalTracer;

import java.net.URI;

import java.util.Map;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;

import jakarta.ws.rs.core.Context;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Simple {@code JAXRS} request/response filter to enable tracing of {@code JAXRS} operations.
 */
@Provider
public class TracingFilter
        implements ContainerRequestFilter, ContainerResponseFilter {

    /**
     * Value for OpenTracing {@link Tags#COMPONENT} key.
     */
    private static final String JAXRS = "jaxrs";

    /**
     * Key for storing/retrieving a tracing {@link Span} within a {@link ContainerRequestContext}.
     */
    private static final String SPAN_KEY = TracingFilter.class.getName() + "_SPAN";

    /**
     * Key for storing/retrieving a tracing {@link Scope} within a {@link ContainerRequestContext}.
     */
    private static final String SCOPE_KEY = TracingFilter.class.getName() + "_SCOPE";

    /**
     * The {@link ResourceInfo} of the resource associated with this filter.
     */
    @Context
    private ResourceInfo resInfo;

    @Override
    public void filter(ContainerRequestContext context) {
        Tracer tracer = GlobalTracer.get();
        Span span = tracer.buildSpan(getOperationName())
                          .withTag(Tags.COMPONENT, JAXRS)
                          .withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_SERVER)
                          .withTag(Tags.HTTP_METHOD, context.getMethod())
                          .withTag(Tags.HTTP_URL, getURL(context)).start();

        store(context, SPAN_KEY, span);
        store(context, SCOPE_KEY, tracer.activateSpan(span));
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        Span  span  = load(requestContext, SPAN_KEY);
        Scope scope = load(requestContext, SCOPE_KEY);

        if (responseContext.getStatusInfo().getFamily()
            == Response.Status.Family.SERVER_ERROR) {
            Tags.ERROR.set(span, true);
            span.log(Map.of("event", "error"));
        }

        Tags.HTTP_STATUS.set(span, responseContext.getStatus());

        span.finish();
        scope.close();
    }

    /**
     * Return the string form of the URL based on the HTTP host header, otherwise, fallback to returning
     * the {@link URI#toString()}.
     *
     * @param requestContext  the {@link ContainerRequestContext}
     *
     * @return the string form of the URL based on the HTTP host header, otherwise, fallback to returning
     *         the {@link URI#toString()}.
     */
    private static String getURL(ContainerRequestContext requestContext) {
        String hostHeader = requestContext.getHeaderString("host");
        URI    requestUri = requestContext.getUriInfo().getRequestUri();

        if (hostHeader != null) {
            // let us use host header instead of local interface
            return requestUri.getScheme() + "://" + hostHeader + requestUri.getPath();
        }

        return requestUri.toString();
    }

    /**
     * Return a {@link Span} operation name based on the injected {@link ResourceInfo}.
     *
     * @return a {@link Span} operation name based on the injected {@link ResourceInfo}
     */
    private String getOperationName() {
        return resInfo.getResourceClass().getSimpleName() + '.' + resInfo.getResourceMethod().getName();
    }

    /**
     * Store the specified key/value within the specified {@link ContainerRequestContext).
     *
     * @param context  the {@link ContainerRequestContext}
     * @param key      the key
     * @param value    the value
     */
    private static void store(ContainerRequestContext context, String key, Object value) {
        context.setProperty(key, value);
    }

    /**
     * Return the value, if any, associated with the specified {@code key}.
     *
     * @param context  the {@link ContainerRequestContext}
     * @param key      the key
     * @param <T>      the expected type associated with the specified {@code key}
     *
     * @return the value, if any, associated with the specified {@code key}
     */
    @SuppressWarnings("unchecked")
    private static <T> T load(ContainerRequestContext context, String key) {
        return (T) context.getProperty(key);
    }
}
