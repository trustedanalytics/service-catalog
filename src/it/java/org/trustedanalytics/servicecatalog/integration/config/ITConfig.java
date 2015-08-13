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
package org.trustedanalytics.servicecatalog.integration.config;

import static org.mockito.Mockito.mock;

import org.trustedanalytics.cloud.auth.OAuth2TokenRetriever;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("integration-test")
public class ITConfig {

    @Bean
    protected RestOperations restTemplate() {
        return mock(RestTemplate.class);
    }

    @Bean
    protected OAuth2TokenRetriever tokenRetriever() {
        return mock(OAuth2TokenRetriever.class);
    }

    @Bean
    protected String TOKEN() {
        return "jhksdf8723kjhdfsh4i187y91hkajl";
    }

}
