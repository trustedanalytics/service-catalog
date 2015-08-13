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

import org.trustedanalytics.cloud.cc.api.CcOperations;
import org.trustedanalytics.servicecatalog.service.rest.ServicesController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ServicesControllerTest {

    private ServicesController sut;

    @Mock private CcOperations ccClient;

    @Before
    public void setUp() {
        sut = new ServicesController(ccClient);
    }

    @Test
    public void getServices_spaceSpecified_returnServicesFromCloudfoundry() {
        UUID spaceId = UUID.fromString("8efd7c5c-d83c-4786-b399-b7bd548839e1");
        String expectedServices = "list of services returned by ccClient";
        when(ccClient.getServices(any(UUID.class))).thenReturn(expectedServices);

        String services = sut.getServices(spaceId);

        assertEquals(expectedServices, services);

        verify(ccClient).getServices(spaceId);
    }

    @Test
    public void getService_returnServiceFromCloudfoundry() {
        UUID serviceId = UUID.fromString("8efd7c5c-d83c-4786-b399-b7bd548839e2");
        String expectedServiceData = "list of services returned by ccClient";
        when(ccClient.getService(any(UUID.class))).thenReturn(expectedServiceData);

        String serviceData = sut.getService(serviceId);

        assertEquals(expectedServiceData, serviceData);
    }
}
