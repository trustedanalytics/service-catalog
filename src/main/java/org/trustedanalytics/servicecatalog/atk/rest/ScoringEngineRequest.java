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
package org.trustedanalytics.servicecatalog.atk.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScoringEngineRequest {
    @JsonProperty("atk_name")
    private String atkName;
    @JsonProperty("instance_name")
    private String instanceName;
    @JsonProperty("organization_guid")
    private UUID orgGuid;
    @JsonProperty("space_guid")
    private UUID spaceGuid;
    @JsonProperty("service_plan_guid")
    private UUID servicePlanGuid;

    public String getAtkName() {
        return atkName;
    }

    public void setAtkName(String atkName) {
        this.atkName = atkName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public UUID getSpaceGuid() {
        return spaceGuid;
    }

    public void setSpaceGuid(UUID spaceGuid) {
        this.spaceGuid = spaceGuid;
    }

    public UUID getServicePlanGuid() {
        return servicePlanGuid;
    }

    public void setServicePlanGuid(UUID servicePlanGuid) {
        this.servicePlanGuid = servicePlanGuid;
    }

    public UUID getOrgGuid() { return orgGuid; }

    public void setOrgGuid(UUID orgGuid) { this.orgGuid = orgGuid; }
}
