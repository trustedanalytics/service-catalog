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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.trustedanalytics.cloud.auth.OAuth2TokenRetriever;
import org.trustedanalytics.cloud.cc.api.CcServiceBinding;
import org.trustedanalytics.cloud.cc.api.CcServiceBindingList;
import org.trustedanalytics.servicecatalog.Application;
import org.trustedanalytics.servicecatalog.service.rest.ServiceBindingsController;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.trustedanalytics.servicecatalog.integration.utils.PlatformVerifiers;
import org.trustedanalytics.servicecatalog.integration.utils.RestOperationsHelpers;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("integration-test")
public class BindingsIT {

    @Value("http://localhost:${local.server.port}")
    private String baseUrl;

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
    public void getBindings_appGuidSpecified_shouldAskCloudfoundryForAllSpaces() {

        final String CF_FILTERED_SERVICE_BINDINGS_URL =
            cfApiBaseUrl + "/v2/apps/{app}/service_bindings";

        final UUID app = UUID.fromString("290f0c0e-3d2e-4222-b7aa-dc1b4870faec");
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("app", app.toString());

        final CcServiceBindingList expectedBody = new CcServiceBindingList();
        expectedBody.setBindings(ImmutableList.of(new CcServiceBinding()));

        when(restTemplate
            .getForEntity(any(String.class), eq(CcServiceBindingList.class), any(Map.class)))
            .thenReturn(new ResponseEntity<>(expectedBody, HttpStatus.OK));

        TestRestTemplate testRestTemplate = new TestRestTemplate();

        ResponseEntity<CcServiceBindingList> response = RestOperationsHelpers.getForEntityWithToken(testRestTemplate, TOKEN,
                baseUrl + ServiceBindingsController.GET_APP_BINDINGS_URL, CcServiceBindingList.class, pathVars);


        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody().getBindings().size(), equalTo(expectedBody.getBindings().size()));

        PlatformVerifiers.verifySetTokenThenGetForEntity(restTemplate, TOKEN, CF_FILTERED_SERVICE_BINDINGS_URL,
                CcServiceBindingList.class, pathVars);
    }

    @Test
    public void deleteServiceBinding_shouldDeleteBindingInCloudFoundry() {

        final String CF_DELETE_SERVICE_BINDING_URL =
            cfApiBaseUrl + "/v2/service_bindings/{binding}";

        final String BINDING_ID = UUID.randomUUID().toString();
        ImmutableMap<String, String> pathVars = ImmutableMap.of("binding", BINDING_ID);

        doNothing().when(restTemplate).delete(any(String.class), any(Map.class));

        TestRestTemplate testRestTemplate = new TestRestTemplate();
        RestOperationsHelpers.deleteWithToken(testRestTemplate, TOKEN,
                baseUrl + ServiceBindingsController.DELETE_SERVICE_BINDING_URL, pathVars);

        PlatformVerifiers.verifySetTokenThenDelete(restTemplate, TOKEN, CF_DELETE_SERVICE_BINDING_URL, pathVars);
    }
}

