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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.trustedanalytics.cloud.cc.api.CcServiceInstance;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

public class ServiceInstance {
    private UUID guid;
    private String name;
    private UUID service;

    @JsonProperty("service_plan")
    private String servicePlan;

    @JsonProperty("bound_apps")
    private Collection<App> boundApps;

    @JsonProperty("dashboard_url")
    @JsonInclude(Include.NON_NULL)
    private String dashboardUrl;

    public ServiceInstance() {
    }

    public ServiceInstance(UUID guid, String name, String servicePlan, UUID service,
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
        servicePlan = ccServInst.getServicePlanName();
        service = ccServInst.getServiceGuid();
        this.boundApps = boundApps != null ? boundApps : new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public UUID getGuid() {
        return guid;
    }

    public String getServicePlan() {
        return servicePlan;
    }

    public UUID getService() {
        return service;
    }

    public Collection<App> getBoundApps() {
        return boundApps;
    }

    public String getDashboardUrl() {
        return dashboardUrl;
    }

    public void setDashboardUrl(String dashboardUrl) {
        this.dashboardUrl = dashboardUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServiceInstance that = (ServiceInstance) o;

        if (boundApps != null ? !boundApps.equals(that.boundApps) : that.boundApps != null) {
            return false;
        }
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (service != null ? !service.equals(that.service) : that.service != null) {
            return false;
        }
        if (servicePlan != null ?
            !servicePlan.equals(that.servicePlan) :
            that.servicePlan != null) {
            return false;
        }

        if (dashboardUrl != null ?
            dashboardUrl.equals(that.dashboardUrl) :
            that.dashboardUrl != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = guid != null ? guid.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (servicePlan != null ? servicePlan.hashCode() : 0);
        result = 31 * result + (service != null ? service.hashCode() : 0);
        result = 31 * result + (boundApps != null ? boundApps.hashCode() : 0);
        result = 31 * result + (dashboardUrl != null ? dashboardUrl.hashCode() : 0);
        return result;
    }
}
