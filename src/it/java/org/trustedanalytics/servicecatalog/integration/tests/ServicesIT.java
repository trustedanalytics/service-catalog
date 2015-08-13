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
import static org.trustedanalytics.servicecatalog.integration.utils.RestOperationsHelpers.getForEntityWithToken;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.trustedanalytics.cloud.auth.OAuth2TokenRetriever;
import org.trustedanalytics.servicecatalog.Application;
import org.trustedanalytics.servicecatalog.service.rest.ServicesController;

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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("integration-test")
public class ServicesIT {

    @Value("http://localhost:${local.server.port}")
    private String BASE_URL;

    @Autowired
    private String TOKEN;

    @Autowired
    private RestOperations restTemplate;

    @Autowired
    private OAuth2TokenRetriever tokenRetriever;

    @Value("${cf.resource:/}")
    private String cfApiBaseUrl;

    @Before
    public void setUp() {
        when(tokenRetriever.getAuthToken(any(Authentication.class))).thenReturn(TOKEN);
    }

    @Test
    public void getAllServices_spaceAndTokenSpecified_shouldAskCloudfoundryForServices() {

        final String CF_SERVICES_OF_SPACE_URL =
            cfApiBaseUrl + "/v2/spaces/{space}/services?inline-relations-depth=1";

        final String SPACE = "7a547a5a-de3c-4996-b312-bacd54ef30e1";
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("space", SPACE);
        final String EXPECTED_BODY = "body";

        when(restTemplate.getForEntity(any(String.class), eq(String.class), any(Map.class)))
            .thenReturn(new ResponseEntity<>(EXPECTED_BODY, HttpStatus.OK));

        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<String> response = RestOperationsHelpers.getForEntityWithToken(testRestTemplate, TOKEN,
                BASE_URL + ServicesController.GET_FILTERED_SERVICES_URL, String.class, pathVars);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(EXPECTED_BODY));

        PlatformVerifiers.verifySetTokenThenGetForEntity(restTemplate, TOKEN, CF_SERVICES_OF_SPACE_URL, String.class,
                pathVars);
    }

    @Test
    public void getServiceDetails_serviceGuidSpecified_shouldAskCfForServiceDetails() {

        final String CF_SERVICE_DETAILS_URL =
            cfApiBaseUrl + "/v2/services/{service}?inline-relations-depth=1";

        final String Service = "c5b34629-aff2-4d50-af7b-da011683de6a";
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("service", Service);
        final String EXPECTED_BODY = "service details from cf";

        when(restTemplate.getForEntity(any(String.class), eq(String.class), any(Map.class)))
            .thenReturn(new ResponseEntity<>(EXPECTED_BODY, HttpStatus.OK));

        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<String> response = RestOperationsHelpers.getForEntityWithToken(testRestTemplate, TOKEN,
                BASE_URL + ServicesController.GET_SERVICE_DETAILS_URL, String.class, pathVars);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(EXPECTED_BODY));

        PlatformVerifiers.verifySetTokenThenGetForEntity(restTemplate, TOKEN, CF_SERVICE_DETAILS_URL, String.class,
                pathVars);
    }
}

