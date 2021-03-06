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
import lombok.NoArgsConstructor;

import org.trustedanalytics.cloud.cc.api.CcServiceKey;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceKey {

    private UUID guid;
    private String name;
    private Object credentials;

    @JsonProperty("service_instance_guid")
    private UUID serviceInstanceGuid;

    public static ServiceKey from(CcServiceKey input) {
        return new ServiceKey(input.getMetadata().getGuid(),
            input.getEntity().getName(),
            input.getEntity().getCredentials(),
            input.getEntity().getServiceInstanceGuid());
    }
}
