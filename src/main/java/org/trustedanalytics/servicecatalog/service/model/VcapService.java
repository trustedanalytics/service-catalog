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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VcapService {

    private String label;
    private Object credentials;
    private String name;
    private String plan;
    private Object tags;

    public static Collection<VcapService> from(ServiceInstance input, Service broker) {
        return Optional.ofNullable(input.getServiceKeys())
            .orElse(new ArrayList<>())
            .stream()
            .map(sk -> new VcapService(broker.getLabel(), sk.getCredentials(),
                input.getName(), input.getServicePlan().getName(), broker.getTags()))
            .collect(Collectors.toList());
    }

}
