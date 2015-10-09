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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.trustedanalytics.cloud.cc.api.CcExtendedServiceInstance;
import org.trustedanalytics.cloud.cc.api.CcExtendedServicePlan;
import org.trustedanalytics.cloud.cc.api.CcServiceKey;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtendedServiceInstance {
    private UUID guid;
    private String name;

    @JsonProperty("service_plan_guid")
    private UUID servicePlanGuid;

    @JsonProperty("service_plan")
    private CcExtendedServicePlan servicePlan;

    @JsonProperty("service_keys")
    private Collection<ServiceKey> serviceKeys;

    public static ExtendedServiceInstance from(CcExtendedServiceInstance input) {
        return new ExtendedServiceInstance(
            input.getMetadata().getGuid(),
            input.getEntity().getName(),
            input.getEntity().getServicePlanGuid(),
            input.getEntity().getServicePlan(),
            serviceKeysFrom(input.getEntity().getServiceKeys())
        );
    }

    private static Collection<ServiceKey> serviceKeysFrom(Collection<CcServiceKey> input) {
        if(input == null) {
            return null;
        }
        return input.stream().map(ServiceKey::from).collect(Collectors.toList());
    }
}
