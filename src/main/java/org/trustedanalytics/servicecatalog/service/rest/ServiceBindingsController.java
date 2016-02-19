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

import io.swagger.annotations.ApiOperation;
import org.trustedanalytics.cloud.cc.api.CcNewServiceBinding;
import org.trustedanalytics.cloud.cc.api.CcOperationsApps;
import org.trustedanalytics.cloud.cc.api.CcServiceBinding;
import org.trustedanalytics.cloud.cc.api.CcServiceBindingList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController public class ServiceBindingsController {

    public static final String GET_APP_BINDINGS_URL = "/rest/apps/{app}/service_bindings";
    public static final String CREATE_SERVICE_BINDING_URL = "/rest/apps/{app}/service_bindings";
    public static final String DELETE_SERVICE_BINDING_URL = "/rest/service_bindings/{binding}";

    private final CcOperationsApps ccClient;

    @Autowired public ServiceBindingsController(CcOperationsApps ccClient) {
        this.ccClient = ccClient;
    }

    @ApiOperation("Get service bindings for the application")
    @RequestMapping(value = GET_APP_BINDINGS_URL, method = GET, produces = APPLICATION_JSON_VALUE)
    public CcServiceBindingList getBindingsOfApp(@PathVariable UUID app) {
        return ccClient.getAppBindings(app);
    }

    @ApiOperation("Creates service binding for the application")
    @RequestMapping(value = CREATE_SERVICE_BINDING_URL, method = POST,
        produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public CcServiceBinding createServiceBinding(@PathVariable UUID app,
        @RequestBody CcNewServiceBinding service) {
        return ccClient.createServiceBinding(new CcNewServiceBinding(app, service.getServiceInstanceGuid()));
    }

    @ApiOperation("Removes service binding for the application")
    @RequestMapping(value = DELETE_SERVICE_BINDING_URL, method = DELETE)
    public void deleteServiceBinding(@PathVariable UUID binding) {
        ccClient.deleteServiceBinding(binding);
    }
}
