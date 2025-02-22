// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.monitor.query.log.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** An array of requests. */
@Fluent
public final class BatchRequest {
    /*
     * An single request in a batch.
     */
    @JsonProperty(value = "requests")
    private List<LogQueryRequest> requests;

    /**
     * Get the requests property: An single request in a batch.
     *
     * @return the requests value.
     */
    public List<LogQueryRequest> getRequests() {
        return this.requests;
    }

    /**
     * Set the requests property: An single request in a batch.
     *
     * @param requests the requests value to set.
     * @return the BatchRequest object itself.
     */
    public BatchRequest setRequests(List<LogQueryRequest> requests) {
        this.requests = requests;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (getRequests() != null) {
            getRequests().forEach(e -> e.validate());
        }
    }
}
