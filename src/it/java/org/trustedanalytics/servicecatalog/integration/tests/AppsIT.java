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

import static org.trustedanalytics.servicecatalog.integration.utils.RestOperationsHelpers.postForEntityWithToken;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.trustedanalytics.cloud.auth.OAuth2TokenRetriever;
import org.trustedanalytics.cloud.cc.api.CcApp;
import org.trustedanalytics.cloud.cc.api.CcAppState;
import org.trustedanalytics.cloud.cc.api.CcAppStatus;
import org.trustedanalytics.cloud.cc.api.CcAppSummary;
import org.trustedanalytics.cloud.cc.api.CcSummary;
import org.trustedanalytics.servicecatalog.Application;
import org.trustedanalytics.servicecatalog.service.rest.ApplicationsController;
import org.trustedanalytics.servicecatalog.utils.ApplicationsTestsResources;

import com.google.common.collect.ImmutableMap;
import org.trustedanalytics.servicecatalog.integration.utils.PlatformVerifiers;
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
public class AppsIT {

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
    public void getAllApplications_shouldAskCloudFoundryForSummaryAndReturnOnlyApplications() {

        final String CF_SPACE_SUMMARY_URL =
            cfApiBaseUrl + "/v2/spaces/{space}/summary?inline-relations-depth=1";

        final String SPACE = "dda93601-8894-4400-8d5a-8b942ef72da2";
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("space", SPACE);

        final CcSummary APPS_SUMMARY_RETURNED_BY_CF =
            ApplicationsTestsResources.appSummaryReturnedByCcAdapter();

        final CcApp[] EXPECTED_BODY =
            ApplicationsTestsResources.allNotFilteredApps().toArray(new CcApp[0]);

        when(restTemplate.getForEntity(any(String.class), eq(CcSummary.class), any(Map.class)))
            .thenReturn(new ResponseEntity<>(APPS_SUMMARY_RETURNED_BY_CF, HttpStatus.OK));

        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<CcApp[]> response = RestOperationsHelpers.getForEntityWithToken(testRestTemplate, TOKEN,
                BASE_URL + ApplicationsController.GET_FILTERED_APPS_URL, CcApp[].class, pathVars);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(EXPECTED_BODY));

        PlatformVerifiers.verifySetTokenThenGetForEntity(restTemplate, TOKEN, CF_SPACE_SUMMARY_URL, CcSummary.class,
                pathVars);
    }

    @Test
    public void getAppDetails_guidSpecified_shouldAskCcForAppSummary() {

        final String CF_APP_SUMMARY_URL = cfApiBaseUrl + "/v2/apps/{app}/summary";

        final UUID APP_GUID = UUID.fromString("290f0c0e-3d2e-4222-b7aa-dc1b4870faec");
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("app", APP_GUID.toString());

        final CcAppSummary expected = new CcAppSummary();
        expected.setName("ExpectedApp");

        when(restTemplate.getForEntity(any(String.class), eq(CcAppSummary.class), any(Map.class)))
            .thenReturn(new ResponseEntity<>(expected, HttpStatus.OK));

        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<CcAppSummary> response = RestOperationsHelpers.getForEntityWithToken(testRestTemplate, TOKEN,
                BASE_URL + ApplicationsController.GET_APP_DETAILS_URL, CcAppSummary.class, pathVars);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody().getName(), equalTo(expected.getName()));

        PlatformVerifiers.verifySetTokenThenGetForEntity(restTemplate, TOKEN, CF_APP_SUMMARY_URL, CcAppSummary.class,
                pathVars);
    }

    @Test
    public void restageApp_shouldpostRestageToCloudFoundry() {

        final String CF_APP_RESTAGE_URL = cfApiBaseUrl + "/v2/apps/{app}/restage";

        final String APP_ID = UUID.randomUUID().toString();
        ImmutableMap<String, String> pathVars = ImmutableMap.of("app", APP_ID);

        when(restTemplate.postForEntity(anyString(), any(), any(Class.class), anyMap()))
            .thenReturn(null);

        TestRestTemplate testRestTemplate = new TestRestTemplate();
        RestOperationsHelpers.postForEntityWithToken(testRestTemplate, TOKEN,
                BASE_URL + ApplicationsController.RESTAGE_APP_URL,
                new CcAppStatus(CcAppState.RESTAGING), String.class, pathVars);

        PlatformVerifiers.verifySetTokenThenPostForEntity(restTemplate, TOKEN, CF_APP_RESTAGE_URL, null, String.class,
                pathVars);
    }

    @Test
    public void deleteApps_shouldInvokeDeleteOnCloudFoundry() {

        final String CF_APP_DELETE_URL = cfApiBaseUrl + "/v2/apps/{app}?recursive=true";

        final String APP_ID = UUID.randomUUID().toString();
        ImmutableMap<String, String> pathVars = ImmutableMap.of("app", APP_ID);

        when(restTemplate.postForEntity(anyString(), any(), any(Class.class), anyMap()))
            .thenReturn(null);

        TestRestTemplate testRestTemplate = new TestRestTemplate();
        RestOperationsHelpers.deleteWithToken(testRestTemplate, TOKEN, BASE_URL + ApplicationsController.DELETE_APP_URL,
                pathVars);

        PlatformVerifiers.verifySetTokenThenDelete(restTemplate, TOKEN, CF_APP_DELETE_URL, pathVars);
    }

    @Test
    public void restageApp_shouldInvokeSwitchOnAppOnCloudFoundry() {

        final String CF_APP_SWITCH_APP_URL = cfApiBaseUrl + "/v2/apps/{app}";

        final String APP_ID = UUID.randomUUID().toString();
        ImmutableMap<String, String> pathVars = ImmutableMap.of("app", APP_ID);
        CcAppState state = CcAppState.STARTED;
        CcAppStatus status = new CcAppStatus(state);
        Map<String, Object> serviceRequest = new HashMap<String, Object>();
        serviceRequest.put("state", status.getState());

        doNothing().when(restTemplate).put(anyString(), any(), anyMap());

        TestRestTemplate testRestTemplate = new TestRestTemplate();
        postForEntityWithToken(testRestTemplate, TOKEN,
                BASE_URL + ApplicationsController.RESTAGE_APP_URL,
                status, String.class, pathVars);

        PlatformVerifiers.verifySetTokenThenPut(restTemplate, TOKEN, CF_APP_SWITCH_APP_URL, serviceRequest, pathVars);
    }
}
