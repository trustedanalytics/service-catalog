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
package org.trustedanalytics.servicecatalog.storage;

import org.trustedanalytics.servicecatalog.service.model.ServiceInstanceMetadata;

import java.util.UUID;

public class ServiceInstanceRegistry {

    private KeyValueStore<ServiceInstanceMetadata> serviceInstancesMetadataStore;

    public ServiceInstanceRegistry(KeyValueStore<ServiceInstanceMetadata> store) {
        this.serviceInstancesMetadataStore = store;
    }

    public void addInstanceCreator(UUID instanceUUID, ServiceInstanceMetadata metadata) {
        serviceInstancesMetadataStore.put(instanceUUID.toString(), metadata);
    }

    public void deleteInstanceCreator(UUID instanceUUID) {
        serviceInstancesMetadataStore.remove(instanceUUID.toString());
    }

    public ServiceInstanceMetadata getInstanceCreator(UUID instanceUUID) {
        return serviceInstancesMetadataStore.get(instanceUUID.toString());
    }
}
