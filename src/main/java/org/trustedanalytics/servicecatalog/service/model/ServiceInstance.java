/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.servicecatalog.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import org.trustedanalytics.cloud.cc.api.CcLastOperation;
import org.trustedanalytics.cloud.cc.api.CcServiceInstance;
import org.trustedanalytics.cloud.cc.api.CcServicePlan;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

@Data
@JsonInclude(Include.NON_NULL)
public class ServiceInstance {
    private UUID guid;
    private String name;
    private UUID service;

    @JsonProperty("metadata")
    private ServiceInstanceMetadata metadata;

    @JsonProperty("service_plan")
    private CcServicePlan servicePlan;

    @JsonProperty("bound_apps")
    private Collection<App> boundApps;

    @JsonProperty("dashboard_url")
    @JsonInclude(Include.NON_NULL)
    private String dashboardUrl;

    @JsonProperty("service_keys")
    private Collection<ServiceKey> serviceKeys;

    @JsonProperty("last_operation")
    private CcLastOperation lastOperation;

    public ServiceInstance() {
    }

    public ServiceInstance(UUID guid, String name, CcServicePlan servicePlan, UUID service,
        Collection<App> boundApps) {
        this.guid = guid;
        this.name = name;
        this.servicePlan = servicePlan;
        this.service = service;
        this.boundApps = boundApps;
    }

    public ServiceInstance(CcServiceInstance ccServInst, Collection<App> boundApps) {
        guid = ccServInst.getGuid();
        name = ccServInst.getName();
        lastOperation = ccServInst.getLastOperation();
        servicePlan = ccServInst.getServicePlan();
        service = ccServInst.getServiceGuid();
        this.boundApps = boundApps != null ? boundApps : new LinkedList<>();
    }

    public static ServiceInstance from(CcServiceInstance input) {
        return new ServiceInstance(input, null);
    }

}
