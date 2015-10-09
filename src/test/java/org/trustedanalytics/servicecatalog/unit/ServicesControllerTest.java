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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.json.JSONException;
import org.json.JSONObject;

import org.trustedanalytics.cloud.cc.api.*;
import org.trustedanalytics.servicecatalog.service.rest.ServicesController;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import rx.Observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ServicesControllerTest {

    private ServicesController sut;

    @Mock
    private CcOperations ccClient;

    @Before
    public void setUp() {
        sut = new ServicesController(ccClient);
    }

    @Test
    public void getPlanGuid_labelAndPlanSpecified_returnPlanGuidFromCloudfoundry()
        throws JSONException {

        UUID guid = UUID.randomUUID();

        Collection<CcExtendedService> expectedExtendedServices =
            new ArrayList<CcExtendedService>() {{
            }};
        CcExtendedService extendedService = new CcExtendedService();
        extendedService.setMetadata(new CcMetadata());
        extendedService.getMetadata().setGuid(guid);
        extendedService.setEntity(new CcExtendedServiceEntity());
        extendedService.getEntity().setLabel("label");
        expectedExtendedServices.add(extendedService);

        Collection<CcExtendedServicePlan> expectedExtendedServicePlan =
            new ArrayList<CcExtendedServicePlan>() {{
            }};
        CcExtendedServicePlan extendedServicePlan = new CcExtendedServicePlan();
        extendedServicePlan.setEntity(new CcExtendedServicePlanEntity());
        extendedServicePlan.getEntity().setName("plan");
        extendedServicePlan.setMetadata(new CcMetadata());
        extendedServicePlan.getMetadata().setGuid(guid);
        expectedExtendedServicePlan.add(extendedServicePlan);

        when(ccClient.getExtendedServices()).thenReturn(Observable.from(expectedExtendedServices));
        when(ccClient.getExtendedServicePlans(any()))
            .thenReturn(Observable.from(expectedExtendedServicePlan));

        JSONObject json = new JSONObject(sut.getPlanGuid("label", "plan"));

        assertEquals(json.get("guid"), guid.toString());
    }

    @Test
    public void getServices_spaceSpecified_returnServicesFromCloudfoundry() {
        UUID spaceId = UUID.fromString("8efd7c5c-d83c-4786-b399-b7bd548839e1");
        List<CcExtendedService> expectedServices =
            Arrays.asList(new CcExtendedService(), new CcExtendedService());
        when(ccClient.getServices(any(UUID.class))).thenReturn(Observable.from(expectedServices));

        Collection<CcExtendedService> services = sut.getServices(spaceId);

        assertEquals(expectedServices, services);

        verify(ccClient).getServices(spaceId);
    }

    @Test
    public void getService_returnServiceFromCloudfoundry() {
        UUID serviceId = UUID.fromString("8efd7c5c-d83c-4786-b399-b7bd548839e2");
        CcExtendedService expectedService = new CcExtendedService();
        when(ccClient.getService(any(UUID.class))).thenReturn(Observable.just(expectedService));

        CcExtendedService serviceData = sut.getService(serviceId);

        assertEquals(expectedService, serviceData);
    }

    @Test
    public void getServicePlans_ExistingLabelSpecified_returnServicePlansFromCloudFoundry() {
        // given
        final String expectedLabel = "expected";
        final String notExpectedLabel = "notExpectedLabel";
        final CcExtendedService expectedService = createService(expectedLabel);
        final CcExtendedService notExpectedService = createService(notExpectedLabel);

        final CcExtendedServicePlan expectedServicePlan =
            createServicePlan(expectedLabel, expectedService.getMetadata().getGuid());
        final CcExtendedServicePlan notExpectedServicePlan =
            createServicePlan(notExpectedLabel, notExpectedService.getMetadata().getGuid());

        final List<CcExtendedService> services =
            ImmutableList.of(expectedService, notExpectedService);

        // when
        when(ccClient.getExtendedServices()).thenReturn(Observable.from(services));
        when(ccClient.getExtendedServicePlans(expectedService.getMetadata().getGuid()))
            .thenReturn(Observable.just(expectedServicePlan));
        when(ccClient.getExtendedServicePlans(notExpectedService.getMetadata().getGuid()))
            .thenReturn(Observable.just(notExpectedServicePlan));

        // then
        final Collection<CcExtendedServicePlan> plans = sut.getServicePlans(expectedLabel);
        assertThat(plans, hasSize(1));
        assertThat(Iterables.getOnlyElement(plans).getEntity().getName(), is(expectedLabel));
    }

    private CcExtendedService createService(String label) {
        final CcExtendedServiceEntity entity = new CcExtendedServiceEntity();
        entity.setLabel(label);

        final CcMetadata metadata = new CcMetadata();
        metadata.setGuid(UUID.randomUUID());

        final CcExtendedService service = new CcExtendedService();
        service.setEntity(entity);
        service.setMetadata(metadata);
        return service;
    }

    private CcExtendedServicePlan createServicePlan(String name, UUID serviceGuid) {
        final CcExtendedServicePlanEntity entity = new CcExtendedServicePlanEntity();
        entity.setName(name);
        entity.setServiceGuid(serviceGuid);

        final CcMetadata metadata = new CcMetadata();
        metadata.setGuid(UUID.randomUUID());

        final CcExtendedServicePlan plan = new CcExtendedServicePlan();
        plan.setEntity(entity);
        plan.setMetadata(metadata);
        return plan;
    }
}
