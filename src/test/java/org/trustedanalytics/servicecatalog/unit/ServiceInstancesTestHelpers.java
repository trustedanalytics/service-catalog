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
package org.trustedanalytics.servicecatalog.unit;

import org.trustedanalytics.cloud.cc.api.CcServicePlan;
import org.trustedanalytics.servicecatalog.service.model.Service;
import org.trustedanalytics.servicecatalog.service.model.ServiceInstance;
import org.trustedanalytics.servicecatalog.service.model.ServiceKey;
import rx.Observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ServiceInstancesTestHelpers {

    private ServiceInstancesTestHelpers() {}

    public static Observable<ServiceKey> getServiceKeys(int count) {
        return Observable.just(getServiceKey()).repeat(count);
    }

    public static ServiceKey getServiceKey() {
        ServiceKey serviceKey = new ServiceKey();
        return serviceKey;
    }

    public static List<Service> getServices() {
        return Arrays.asList(getService(getServiceInstances()),
            getService(new ArrayList<ServiceInstance>()), getService(getServiceInstances()));
    }

    public static Service getService(List<ServiceInstance> instances) {
        Service broker = new Service();
        broker.setInstances(instances);
        broker.setLabel(UUID.randomUUID().toString());
        return broker;
    }

    public static List<ServiceInstance> getServiceInstances() {
        return Arrays.asList(getServiceInstance(getServiceKeys(3)),
            getServiceInstance(getServiceKeys(2)), getServiceInstance(getServiceKeys(0)),
            getServiceInstance(getServiceKeys(1)));
    }

    public static ServiceInstance getServiceInstance(Observable<ServiceKey> keys) {
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceKeys(keys.toList().toBlocking().single());
        serviceInstance.setServicePlan(new CcServicePlan());
        return serviceInstance;
    }
}
