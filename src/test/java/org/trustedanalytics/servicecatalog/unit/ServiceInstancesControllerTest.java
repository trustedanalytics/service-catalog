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
import static org.mockito.Mockito.*;
import static org.trustedanalytics.servicecatalog.unit.ServiceInstancesTestHelpers.getServiceInstances;
import static org.trustedanalytics.servicecatalog.unit.ServiceInstancesTestHelpers.getServiceKeys;
import static org.trustedanalytics.servicecatalog.unit.ServiceInstancesTestHelpers.getServices;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.trustedanalytics.cloud.cc.api.*;
import org.trustedanalytics.cloud.uaa.UserIdNamePair;
import org.trustedanalytics.servicecatalog.service.model.Service;
import org.trustedanalytics.servicecatalog.service.model.ServiceInstance;
import org.trustedanalytics.servicecatalog.service.model.ServiceInstanceMetadata;
import org.trustedanalytics.servicecatalog.service.rest.ServiceInstancesController;
import org.trustedanalytics.servicecatalog.service.rest.ServiceInstancesControllerHelpers;
import org.trustedanalytics.servicecatalog.storage.ServiceInstanceRegistry;
import org.trustedanalytics.servicecatalog.utils.ServiceInstancesTestsResources;
import rx.Observable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class ServiceInstancesControllerTest {

    private static final String SPACE_GUID_STR = "AC5E9498-526B-4ED7-93BC-D2D7412BCCA5";
    private static final UUID SPACE_GUID = UUID.fromString(SPACE_GUID_STR);

    private CcSummary spaceSummaryReturnedByCcAdapter;
    private ServiceInstancesController sut;

    @Mock
    private CcOperations ccClient;

    @Mock
    private ServiceInstancesControllerHelpers controllerHelpers;

    @Mock
    private ServiceInstanceRegistry serviceInstanceRegistry;

    @Before
    public void setUp() {
        spaceSummaryReturnedByCcAdapter =
            ServiceInstancesTestsResources.spaceSummaryReturnedByCcAdapter();
        when(ccClient.getSpaceSummary(any(UUID.class))).thenReturn(spaceSummaryReturnedByCcAdapter);
        sut = new ServiceInstancesController(ccClient, controllerHelpers, serviceInstanceRegistry);
    }

    @Test
    public void getAllServiceInstances_withoutServiceFilter_returnAllServiceInstancesFromCloudfoundry() {
        UUID service = null;
        ServiceInstanceMetadata metadata = new ServiceInstanceMetadata(UUID.randomUUID(), "test-user");
        when(serviceInstanceRegistry.getInstanceCreator(any(UUID.class))).thenReturn(metadata);

        Collection<ServiceInstance> allNotFilteredServiceInstances =
            ServiceInstancesTestsResources.allNotFilteredServiceInstances();

        Collection<ServiceInstance> serviceInstances =
            sut.getAllServiceInstances(SPACE_GUID, null);

        List<UUID> expectedGuids =
            allNotFilteredServiceInstances.stream().map(i -> i.getGuid()).collect(
                Collectors.toList());
        List<UUID> resultGuids =
            serviceInstances.stream().map(i -> i.getGuid()).collect(Collectors.toList());
        assertEquals(expectedGuids, resultGuids);
        verify(ccClient).getSpaceSummary(SPACE_GUID);
    }

    @Test
    public void getAllServiceInstances_getInstancesCreatorsFromStore() {
        ServiceInstanceMetadata metadata = new ServiceInstanceMetadata(UUID.randomUUID(), "test-user");
        when(serviceInstanceRegistry.getInstanceCreator(any(UUID.class))).thenReturn(metadata);

        Collection<ServiceInstance> serviceInstances =
                sut.getAllServiceInstances(SPACE_GUID, null);

        verify(serviceInstanceRegistry, times(serviceInstances.size())).getInstanceCreator(any(UUID.class));
        serviceInstances.stream().allMatch(i -> i.getMetadata().getCreatorName() != null);
        serviceInstances.stream().allMatch(i -> i.getMetadata().getCreatorUUID() != null);
    }

    @Test
    public void createServiceInstance_createInstanceAndReturnIt() {
        ArgumentCaptor<CcNewServiceInstance> captor = ArgumentCaptor.forClass(
            CcNewServiceInstance.class);
        CcNewServiceInstance passed = mock(CcNewServiceInstance.class);
        CcExtendedServiceInstance returned = mock(CcExtendedServiceInstance.class);
        CcMetadata instanceMetadata = mock(CcMetadata.class);
        UserIdNamePair creator = UserIdNamePair.of(UUID.randomUUID(), "test-user");
        when(returned.getMetadata()).thenReturn(instanceMetadata);
        when(instanceMetadata.getGuid()).thenReturn(UUID.randomUUID());
        when(ccClient.createServiceInstance(any(CcNewServiceInstance.class))).thenReturn(Observable.just(returned));
        when(controllerHelpers.findUserName(any(Authentication.class))).thenReturn(creator.getUserName());
        when(controllerHelpers.findUserId(any(Authentication.class))).thenReturn(creator.getGuid());

        CcExtendedServiceInstance result = sut.createServiceInstance(passed, null);

        verify(ccClient).createServiceInstance(captor.capture());
        assertEquals(passed, captor.getValue());
        assertEquals(returned, result);
    }

    @Test
    public void createServiceInstance_addInstanceCreatorToStore() {
        ArgumentCaptor<CcNewServiceInstance> captor = ArgumentCaptor.forClass(
                CcNewServiceInstance.class);
        final UserIdNamePair creator = UserIdNamePair.of(UUID.randomUUID(), "test-user");
        final UUID instanceUUID = UUID.randomUUID();
        final String instanceName = "test-instance";
        CcNewServiceInstance passed = new CcNewServiceInstance(instanceName, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID());
        CcExtendedServiceInstance returned = new CcExtendedServiceInstance();
        CcMetadata returnedMetadata = new CcMetadata();
        returnedMetadata.setGuid(instanceUUID);
        returned.setMetadata(returnedMetadata);

        when(ccClient.createServiceInstance(any(CcNewServiceInstance.class))).thenReturn(Observable.just(returned));
        when(controllerHelpers.findUserName(any(Authentication.class))).thenReturn(creator.getUserName());
        when(controllerHelpers.findUserId(any(Authentication.class))).thenReturn(creator.getGuid());

        CcExtendedServiceInstance result = sut.createServiceInstance(passed, null);

        verify(serviceInstanceRegistry).addInstanceCreator(instanceUUID,
                new ServiceInstanceMetadata(creator.getGuid(), creator.getUserName()));
    }

    @Test
    public void deleteServiceInstance_deleteInstanceInCloudFoudry() {
        doNothing().when(ccClient).deleteServiceInstance(any(UUID.class));
        UUID serviceGuid = UUID.randomUUID();

        sut.deleteServiceInstance(serviceGuid);

        verify(ccClient).deleteServiceInstance(serviceGuid);
    }

    @Test
    public void deleteServiceInstance_removeInstanceCreatorFromStore() {
        doNothing().when(ccClient).deleteServiceInstance(any(UUID.class));
        UUID serviceGuid = UUID.randomUUID();

        sut.deleteServiceInstance(serviceGuid);

        verify(serviceInstanceRegistry).deleteInstanceCreator(serviceGuid);
    }

    @Test
    public void getServiceInstancesSummary_fetchKeysFalse_getSummaryWithoutKeys() {
        when(controllerHelpers.getServiceKeys()).thenReturn(getServiceKeys(3));
        List<ServiceInstance> instances = getServiceInstances();
        when(controllerHelpers.getServiceInstances(any(UUID.class))).thenReturn(instances);
        List<Service> services = getServices();
        when(controllerHelpers.getServices()).thenReturn(services);
        UUID spaceId = UUID.randomUUID();

        Collection<Service> result = sut.getServiceKeysSummary(spaceId, false);

        verify(controllerHelpers, never()).getServiceKeys();
        verify(controllerHelpers).getServices();
        verify(controllerHelpers).getServiceInstances(spaceId);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getServiceInstancesSummary_fetchKeysTrue_getSummaryWithKeys() {
        when(controllerHelpers.getServiceKeys()).thenReturn(getServiceKeys(3));
        List<ServiceInstance> instances = getServiceInstances();
        when(controllerHelpers.getServiceInstances(any(UUID.class))).thenReturn(instances);
        List<Service> services = getServices();
        when(controllerHelpers.getServices()).thenReturn(services);
        UUID spaceId = UUID.randomUUID();

        Collection<Service> result = sut.getServiceKeysSummary(spaceId, true);

        verify(controllerHelpers).getServiceKeys();
        verify(controllerHelpers).getServices();
        verify(controllerHelpers).getServiceInstances(spaceId);
        Assert.assertEquals(2, result.size());
    }
}
