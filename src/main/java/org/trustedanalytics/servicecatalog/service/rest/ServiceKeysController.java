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
package org.trustedanalytics.servicecatalog.service.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.trustedanalytics.cloud.cc.api.CcNewServiceKey;
import org.trustedanalytics.cloud.cc.api.CcOperations;
import org.trustedanalytics.servicecatalog.service.model.ServiceKey;

import java.util.Collection;
import java.util.UUID;

@RestController public class ServiceKeysController {

    public static final String GET_ALL_SERVICE_KEYS_URL = "/rest/service_keys";
    public static final String SERVICE_KEY_URL = "/rest/service_keys/{guid}";

    private final CcOperations ccClient;

    @Autowired public ServiceKeysController(CcOperations ccClient) {
        this.ccClient = ccClient;
    }

    @RequestMapping(value = GET_ALL_SERVICE_KEYS_URL, method = GET,
        produces = APPLICATION_JSON_VALUE)
    public Collection<ServiceKey> getAllServiceKeys() {
        return ccClient.getServiceKeys().map(ServiceKey::from)
            .toList().toBlocking().single();
    }

    @RequestMapping(value = GET_ALL_SERVICE_KEYS_URL, method = POST,
        produces = APPLICATION_JSON_VALUE)
    public ServiceKey createServiceKey(@RequestBody CcNewServiceKey serviceKey) {
        return ServiceKey.from(ccClient.createServiceKey(serviceKey).toBlocking().first());
    }

    @RequestMapping(value = SERVICE_KEY_URL, method = DELETE,
            produces = APPLICATION_JSON_VALUE)
    public void deleteServiceKey(@PathVariable UUID guid) {
        ccClient.deleteServiceKey(guid);
    }
}
