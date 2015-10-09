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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.cloud.cc.api.CcOperations;
import org.trustedanalytics.cloud.cc.api.CcService;
import org.trustedanalytics.cloud.cc.api.CcServiceInstance;
import org.trustedanalytics.cloud.cc.api.CcServicePlan;
import org.trustedanalytics.cloud.cc.api.CcSummary;
import org.trustedanalytics.servicecatalog.service.model.Service;
import org.trustedanalytics.servicecatalog.service.model.ServiceInstance;
import org.trustedanalytics.servicecatalog.service.model.ServiceKey;
import org.trustedanalytics.servicecatalog.service.rest.ServiceInstancesControllerHelpers;
import rx.Observable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ServiceInstancesControllerHelpersTest {

    private ServiceInstancesControllerHelpers sut;

    @Mock
    private CcOperations ccClient;

    @Before
    public void setUp() {
        sut = new ServiceInstancesControllerHelpers(ccClient);
    }

    @Test
    public void mergeServiceKeys_returnCorrectStructure() {
        List<UUID> uuids = Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        List<ServiceInstance> instances = Arrays.asList(
            getServiceInstance(uuids.get(0), null),
            getServiceInstance(uuids.get(1), null),
            getServiceInstance(uuids.get(2), null));
        List<ServiceKey> keys = Arrays.asList(
            getServiceKey(uuids.get(0)),
            getServiceKey(uuids.get(0)),
            getServiceKey(uuids.get(1)));

        sut.mergeServiceKeys(instances, Observable.from(keys));
        assertEquals(2, instances.get(0).getServiceKeys().size());
        assertEquals(1, instances.get(1).getServiceKeys().size());
        assertEquals(0, instances.get(2).getServiceKeys().size());
    }

    @Test
    public void mergeInstances_returnCorrectStructure() {
        List<UUID> uuids = Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        List<Service> services = Arrays.asList(
            getService(uuids.get(0)),
            getService(uuids.get(1)),
            getService(uuids.get(2)));
        List<ServiceInstance> instances = Arrays.asList(
            getServiceInstance(null, uuids.get(0)),
            getServiceInstance(null, uuids.get(0)),
            getServiceInstance(null, uuids.get(1)));

        sut.mergeInstances(services, instances);
        assertEquals(2, services.get(0).getInstances().size());
        assertEquals(1, services.get(1).getInstances().size());
        assertEquals(0, services.get(2).getInstances().size());
    }

    @Test
    public void getServiceInstances_getSummaryFromCC() {
        List<UUID> uuids = Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        UUID spaceGuid = UUID.randomUUID();
        CcSummary summary = new CcSummary();
        List<CcServiceInstance> instances = Arrays.asList(
            getCcServiceInstance(null, uuids.get(0)),
            getCcServiceInstance(null, uuids.get(0)),
            getCcServiceInstance(null, null));
        summary.setServiceInstances(instances);
        when(ccClient.getSpaceSummary(any(UUID.class))).thenReturn(summary);

        List<ServiceInstance> result = sut.getServiceInstances(spaceGuid);

        assertEquals(2, result.size());
        verify(ccClient).getSpaceSummary(spaceGuid);
    }

    private ServiceInstance getServiceInstance(UUID uuid, UUID serviceGuid) {
        ServiceInstance instance = new ServiceInstance();
        instance.setGuid(uuid);
        instance.setServicePlan(getPlan(serviceGuid));
        return instance;
    }

    private CcServiceInstance getCcServiceInstance(UUID uuid, UUID serviceGuid) {
        CcServiceInstance instance = new CcServiceInstance();
        instance.setGuid(uuid);
        instance.setServicePlan(getPlan(serviceGuid));
        return instance;
    }

    private CcServicePlan getPlan(UUID serviceGuid) {
        if(serviceGuid == null) {
            return null;
        }
        CcServicePlan plan = new CcServicePlan();
        CcService service = new CcService();
        service.setGuid(serviceGuid);
        plan.setService(service);
        return plan;
    }

    private ServiceKey getServiceKey(UUID instanceGuid) {
        ServiceKey key = new ServiceKey();
        key.setService_instance_guid(instanceGuid);
        return key;
    }

    private Service getService(UUID uuid) {
        Service service = new Service();
        service.setGuid(uuid);
        return service;
    }
}
