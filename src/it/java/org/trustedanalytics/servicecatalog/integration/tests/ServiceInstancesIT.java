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
package org.trustedanalytics.servicecatalog.integration.tests;

import static org.trustedanalytics.servicecatalog.integration.utils.PlatformVerifiers.verifySetTokenThenGetForEntity;
import static org.trustedanalytics.servicecatalog.integration.utils.PlatformVerifiers.verifySetTokenThenPostForEntity;
import static org.trustedanalytics.servicecatalog.integration.utils.RestOperationsHelpers.getForEntityWithToken;
import static org.trustedanalytics.servicecatalog.integration.utils.RestOperationsHelpers.postForEntityWithToken;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.trustedanalytics.cloud.auth.OAuth2TokenRetriever;
import org.trustedanalytics.cloud.cc.api.CcNewServiceInstance;
import org.trustedanalytics.cloud.cc.api.CcSummary;
import org.trustedanalytics.servicecatalog.Application;
import org.trustedanalytics.servicecatalog.service.model.ServiceInstance;
import org.trustedanalytics.servicecatalog.service.rest.ServiceInstancesController;
import org.trustedanalytics.servicecatalog.utils.ServiceInstancesTestsResources;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestOperations;
import org.trustedanalytics.servicecatalog.integration.utils.PlatformVerifiers;
import org.trustedanalytics.servicecatalog.integration.utils.RestOperationsHelpers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("integration-test")
public class ServiceInstancesIT {

    @Value("http://localhost:${local.server.port}")
    private String BASE_URL;

    @Autowired
    private String TOKEN;

    private CcSummary spaceSummaryReturnedByCc;

    @Autowired
    private RestOperations restTemplate;

    @Autowired
    private OAuth2TokenRetriever tokenRetriever;

    @Value("${cf.resource:/}")
    private String cfApiBaseUrl;

    @Before
    public void setUp() {
        spaceSummaryReturnedByCc =
            ServiceInstancesTestsResources.spaceSummaryReturnedByCcAdapter();
        when(tokenRetriever.getAuthToken(any(Authentication.class))).thenReturn(TOKEN);
    }

    @Test
    public void getAllServiceInstances_shouldAskCloudFoundryForSummaryAndReturnOnlyServiceInstances() {

        String CF_SPACE_SUMMARY_URL =
            cfApiBaseUrl + "/v2/spaces/{space}/summary?inline-relations-depth=1";

        final String SPACE = "dda93601-8894-4400-8d5a-8b942ef72da2";
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("space", SPACE);

        final ServiceInstance[] EXPECTED_BODY =
            ServiceInstancesTestsResources.allNotFilteredServiceInstances()
                .toArray(new ServiceInstance[0]);

        when(restTemplate.getForEntity(any(String.class), eq(CcSummary.class), any(Map.class)))
            .thenReturn(new ResponseEntity<>(spaceSummaryReturnedByCc, HttpStatus.OK));

        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<ServiceInstance[]> response = RestOperationsHelpers.getForEntityWithToken(testRestTemplate, TOKEN,
                BASE_URL + ServiceInstancesController.GET_FILTERED_SERVICE_INSTANCES_URL,
                ServiceInstance[].class, pathVars);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(EXPECTED_BODY));

        PlatformVerifiers.verifySetTokenThenGetForEntity(restTemplate, TOKEN, CF_SPACE_SUMMARY_URL, CcSummary.class,
                pathVars);
    }

    @Test
    public void createNewServiceInstance_shouldCreateNewInstanceInCloudFoundry() {

        String CF_SERVICE_INSTANCES_URL = cfApiBaseUrl + "/v2/service_instances";

        final CcNewServiceInstance INSTANCE_PASSED =
            new CcNewServiceInstance("passed", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        final CcNewServiceInstance INSTANCE_RETRIEVED =
            new CcNewServiceInstance("retrieved", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        when(restTemplate.postForEntity(any(String.class), any(CcNewServiceInstance.class),
            eq(CcNewServiceInstance.class)))
            .thenReturn(new ResponseEntity<>(INSTANCE_RETRIEVED, HttpStatus.OK));

        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<CcNewServiceInstance> response = RestOperationsHelpers.postForEntityWithToken(testRestTemplate,
                TOKEN, BASE_URL + ServiceInstancesController.CREATE_SERVICE_INSTANCE_URL,
                INSTANCE_PASSED, CcNewServiceInstance.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(INSTANCE_RETRIEVED, response.getBody());

        PlatformVerifiers.verifySetTokenThenPostForEntity(restTemplate, TOKEN, CF_SERVICE_INSTANCES_URL,
                INSTANCE_PASSED, CcNewServiceInstance.class);
    }

    @Test
    public void deleteServiceInstance_shouldDeleteInstanceInCloudFoundry() {

        final String CF_SERVICE_INSTANCE_URL =
            cfApiBaseUrl + "/v2/service_instances/{instance}";

        final String INSTANCE_ID = UUID.randomUUID().toString();
        ImmutableMap<String, String> pathVars = ImmutableMap.of("instance", INSTANCE_ID);

        doNothing().when(restTemplate).delete(any(String.class), any(Map.class));

        TestRestTemplate testRestTemplate = new TestRestTemplate();
        RestOperationsHelpers.deleteWithToken(testRestTemplate, TOKEN,
                BASE_URL + ServiceInstancesController.DELETE_SERVICE_INSTANCE_URL, pathVars);

        PlatformVerifiers.verifySetTokenThenDelete(restTemplate, TOKEN, CF_SERVICE_INSTANCE_URL, pathVars);
    }
}
