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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.trustedanalytics.cloud.auth.OAuth2TokenRetriever;
import org.trustedanalytics.cloud.cc.api.CcSummary;
import org.trustedanalytics.servicecatalog.Application;
import org.trustedanalytics.servicecatalog.integration.utils.RestOperationsHelpers;
import org.trustedanalytics.servicecatalog.service.rest.SummaryController;

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

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("integration-test")
public class SummaryIT {

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
    public void getSpaceSummary_spaceGuidSpecified_shouldAskCcForSummary() {

        final String CF_SPACE_SUMMARY_URL =
            cfApiBaseUrl + "/v2/spaces/{space}/summary?inline-relations-depth=1";

        final CcSummary SPACE_SUMMARY_FROM_CF = new CcSummary();

        final String SPACE = "280e97b1-edb4-45e9-988b-b4b8081ce912";
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("space", SPACE);

        when(restTemplate.getForEntity(any(String.class), eq(CcSummary.class), any(Map.class)))
            .thenReturn(new ResponseEntity<>(SPACE_SUMMARY_FROM_CF, HttpStatus.OK));

        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<CcSummary> response =
            RestOperationsHelpers.getForEntityWithToken(testRestTemplate, TOKEN,
                    BASE_URL + SummaryController.GET_SPACE_FILTERED_SUMMARY_URL, CcSummary.class, pathVars);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(SPACE_SUMMARY_FROM_CF));

        PlatformVerifiers.verifySetTokenThenGetForEntity(restTemplate, TOKEN, CF_SPACE_SUMMARY_URL, CcSummary.class,
                pathVars);
    }
}
