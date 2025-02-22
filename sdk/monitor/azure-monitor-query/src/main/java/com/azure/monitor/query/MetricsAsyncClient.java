// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.monitor.query.metrics.implementation.MonitorManagementClientImpl;
import com.azure.monitor.query.metrics.implementation.models.Metric;
import com.azure.monitor.query.metrics.implementation.models.MetricValue;
import com.azure.monitor.query.metrics.implementation.models.MetricsResponse;
import com.azure.monitor.query.metrics.implementation.models.ResultType;
import com.azure.monitor.query.metrics.implementation.models.TimeSeriesElement;
import com.azure.monitor.query.metricsdefinitions.implementation.MetricsDefinitionsClientImpl;
import com.azure.monitor.query.metricsnamespaces.implementation.MetricsNamespacesClientImpl;
import com.azure.monitor.query.models.MetricsDefinition;
import com.azure.monitor.query.models.MetricsNamespace;
import com.azure.monitor.query.models.Metrics;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;
import com.azure.monitor.query.models.MetricsTimeSeriesElement;
import com.azure.monitor.query.models.MetricsValue;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * The asynchronous client for querying Azure Monitor metrics.
 */
@ServiceClient(builder = MetricsClientBuilder.class, isAsync = true)
public final class MetricsAsyncClient {
    private final MonitorManagementClientImpl metricsClient;
    private final MetricsNamespacesClientImpl metricsNamespaceClient;
    private final MetricsDefinitionsClientImpl metricsDefinitionsClient;

    MetricsAsyncClient(MonitorManagementClientImpl metricsClient,
                       MetricsNamespacesClientImpl metricsNamespaceClient,
                       MetricsDefinitionsClientImpl metricsDefinitionsClients) {
        this.metricsClient = metricsClient;
        this.metricsNamespaceClient = metricsNamespaceClient;
        this.metricsDefinitionsClient = metricsDefinitionsClients;
    }

    /**
     * Returns all the Azure Monitor metrics requested for the resource.
     * @param resourceUri The resource URI for which the metrics is requested.
     * @param metricsNames The names of the metrics to query.
     * @return A time-series metrics result for the requested metric names.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<MetricsQueryResult> queryMetrics(String resourceUri, List<String> metricsNames) {
        return queryMetricsWithResponse(resourceUri, metricsNames, new MetricsQueryOptions()).map(Response::getValue);
    }

    /**
     * Returns all the Azure Monitor metrics requested for the resource.
     * @param resourceUri The resource URI for which the metrics is requested.
     * @param metricsNames The names of the metrics to query.
     * @param options Options to filter the query.
     * @return A time-series metrics result for the requested metric names.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<MetricsQueryResult>> queryMetricsWithResponse(String resourceUri, List<String> metricsNames,
                                                                       MetricsQueryOptions options) {
        return withContext(context -> queryMetricsWithResponse(resourceUri, metricsNames, options, context));
    }

    /**
     * Lists all the metrics namespaces created for the resource URI.
     * @param resourceUri The resource URI for which the metrics namespaces are listed.
     * @param startTime The returned list of metrics namespaces are created after the specified start time.
     * @return List of metrics namespaces.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<MetricsNamespace> listMetricsNamespace(String resourceUri, OffsetDateTime startTime) {
        return metricsNamespaceClient
                .getMetricNamespaces()
                .listAsync(resourceUri, startTime.toString());
    }

    /**
     * Lists all the metrics definitions created for the resource URI.
     * @param resourceUri The resource URI for which the metrics definitions are listed.
     * @param metricsNamespace The metrics namespace to which the listed metrics definitions belong.
     * @return List of metrics definitions.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<MetricsDefinition> listMetricsDefinition(String resourceUri, String metricsNamespace) {
        return metricsDefinitionsClient
                .getMetricDefinitions()
                .listAsync(resourceUri, metricsNamespace);
    }

    PagedFlux<MetricsNamespace> listMetricsNamespace(String resourceUri, OffsetDateTime startTime, Context context) {
        return metricsNamespaceClient
                .getMetricNamespaces()
                .listAsync(resourceUri, startTime.toString(), context);
    }

    PagedFlux<MetricsDefinition> listMetricsDefinition(String resourceUri, String metricsNamespace, Context context) {
        return metricsDefinitionsClient.getMetricDefinitions()
                .listAsync(resourceUri, metricsNamespace, context);
    }

    Mono<Response<MetricsQueryResult>> queryMetricsWithResponse(String resourceUri, List<String> metricsNames,
                                                                MetricsQueryOptions options, Context context) {
        String aggregation = null;
        if (!CoreUtils.isNullOrEmpty(options.getAggregation())) {
            aggregation = options.getAggregation()
                    .stream()
                    .map(type -> String.valueOf(type.ordinal()))
                    .collect(Collectors.joining(","));
        }
        return metricsClient
                .getMetrics()
                .listWithResponseAsync(resourceUri, options.getTimespan(), options.getInterval(),
                        String.join(",", metricsNames), aggregation, options.getTop(), options.getOrderby(),
                        options.getFilter(), ResultType.DATA, options.getMetricsNamespace(), context)
                .map(response -> convertToMetricsQueryResult(response));
    }

    private Response<MetricsQueryResult> convertToMetricsQueryResult(Response<MetricsResponse> response) {
        MetricsResponse metricsResponse = response.getValue();
        MetricsQueryResult metricsQueryResult = new MetricsQueryResult(
                metricsResponse.getCost(), metricsResponse.getTimespan(), metricsResponse.getInterval(),
                metricsResponse.getNamespace(), metricsResponse.getResourceregion(), mapMetrics(metricsResponse.getValue()));

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), metricsQueryResult);
    }

    private List<Metrics> mapMetrics(List<Metric> value) {
        return value.stream()
                .map(metric -> new Metrics(metric.getId(), metric.getType(), metric.getUnit(), metric.getName().getValue(),
                        mapTimeSeries(metric.getTimeseries())))
                .collect(Collectors.toList());
    }

    private List<MetricsTimeSeriesElement> mapTimeSeries(List<TimeSeriesElement> timeseries) {
        return timeseries.stream()
                .map(timeSeriesElement -> new MetricsTimeSeriesElement(mapMetricsData(timeSeriesElement.getData())))
                .collect(Collectors.toList());
    }

    private List<MetricsValue> mapMetricsData(List<MetricValue> data) {
        return data.stream()
                .map(metricValue -> new MetricsValue(metricValue.getTimeStamp(),
                        metricValue.getAverage(), metricValue.getMinimum(), metricValue.getMaximum(), metricValue.getTotal(),
                        metricValue.getCount()))
                .collect(Collectors.toList());
    }
}
