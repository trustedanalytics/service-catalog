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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.trustedanalytics.cloud.cc.api.CcNewServiceInstance;
import org.trustedanalytics.cloud.cc.api.CcOperations;
import org.trustedanalytics.cloud.cc.api.CcSummary;
import org.trustedanalytics.servicecatalog.service.model.ServiceInstance;
import org.trustedanalytics.servicecatalog.service.rest.ServiceInstancesController;
import org.trustedanalytics.servicecatalog.utils.ServiceInstancesTestsResources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ServiceInstancesControllerTest {

    private static final String SPACE_GUID_STR = "AC5E9498-526B-4ED7-93BC-D2D7412BCCA5";
    private static final UUID SPACE_GUID = UUID.fromString(SPACE_GUID_STR);

    private CcSummary spaceSummaryReturnedByCcAdapter;
    private ServiceInstancesController sut;

    @Mock
    private CcOperations ccClient;

    @Before
    public void setUp() {
        spaceSummaryReturnedByCcAdapter =
            ServiceInstancesTestsResources.spaceSummaryReturnedByCcAdapter();
        when(ccClient.getSpaceSummary(any(UUID.class))).thenReturn(spaceSummaryReturnedByCcAdapter);
        sut = new ServiceInstancesController(ccClient);
    }

    @Test
    public void getAllServiceInstances_withoutServiceFilter_returnAllServiceInstancesFromCloudfoundry() {
        UUID service = null;

        Collection<ServiceInstance> allNotFilteredServiceInstances =
            ServiceInstancesTestsResources.allNotFilteredServiceInstances();

        Collection<ServiceInstance> serviceInstances =
            sut.getAllServiceInstances(SPACE_GUID, null);

        assertEquals(allNotFilteredServiceInstances, serviceInstances);
        verify(ccClient).getSpaceSummary(SPACE_GUID);
    }

    @Test
    public void createServiceInstance_createInstanceAndReturnIt() {
        ArgumentCaptor<CcNewServiceInstance> captor = ArgumentCaptor.forClass(
            CcNewServiceInstance.class);
        CcNewServiceInstance passed = mock(CcNewServiceInstance.class);
        CcNewServiceInstance returned = mock(CcNewServiceInstance.class);
        when(ccClient.createServiceInstance(any(CcNewServiceInstance.class))).thenReturn(returned);

        CcNewServiceInstance result = sut.createServiceInstance(passed);

        verify(ccClient).createServiceInstance(captor.capture());
        assertEquals(passed, captor.getValue());
        assertEquals(returned, result);
    }

    @Test
    public void deleteServiceInstance_deleteInstanceInCloudFoudry() {
        doNothing().when(ccClient).deleteServiceInstance(any(UUID.class));
        UUID serviceGuid = UUID.randomUUID();

        sut.deleteServiceInstance(serviceGuid);

        verify(ccClient).deleteServiceInstance(serviceGuid);
    }
}
