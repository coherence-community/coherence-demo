/*
 * Copyright (c) 2020, 2025, Oracle and/or its affiliates.
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

import java.net.URI;

import io.opentelemetry.api.GlobalOpenTelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

import io.opentelemetry.context.Scope;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import jakarta.ws.rs.ext.Provider;

/**
 * Simple {@code JAX-RS} request/response filter to enable tracing of {@code JAX-RS} operations.
 */
@Provider
public class TracingFilter
        implements ContainerRequestFilter, ContainerResponseFilter {

    /**
     * Value for OpenTelemetry attribute key for JAX-RS.
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
        Tracer tracer = GlobalOpenTelemetry.getTracer("coherence.demo");
        Span span = tracer.spanBuilder(getOperationName())
                           .setSpanKind(SpanKind.SERVER)
                           .setAttribute("component", JAXRS)
                           .setAttribute("http.method", context.getMethod())
                           .setAttribute("http.url", getURL(context)).startSpan();

        store(context, SPAN_KEY, span);
        store(context, SCOPE_KEY, span.makeCurrent());
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        Span  span  = load(requestContext, SPAN_KEY);
        Scope scope = load(requestContext, SCOPE_KEY);

        if (responseContext.getStatusInfo().getFamily() == Response.Status.Family.SERVER_ERROR) {
            span.setStatus(StatusCode.ERROR);
        }

        span.setAttribute("http.status_code", responseContext.getStatus());

        span.end();
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
     * Store the specified key/value within the specified {@link ContainerRequestContext}.
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
