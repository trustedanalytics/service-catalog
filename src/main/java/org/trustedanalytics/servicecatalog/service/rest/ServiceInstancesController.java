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
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.trustedanalytics.cloud.cc.api.CcExtendedServiceInstance;
import org.trustedanalytics.cloud.cc.api.CcNewServiceInstance;
import org.trustedanalytics.cloud.cc.api.CcOperations;
import org.trustedanalytics.cloud.cc.api.CcSummary;
import org.trustedanalytics.cloud.cc.api.CcServiceInstance;
import org.trustedanalytics.servicecatalog.formattranslator.FormatTranslator;
import org.trustedanalytics.servicecatalog.service.model.Service;
import org.trustedanalytics.servicecatalog.service.model.ServiceInstance;
import org.trustedanalytics.servicecatalog.service.model.ServiceKey;
import rx.Observable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController public class ServiceInstancesController {

    public static final String GET_ALL_SERVICE_INSTANCES_URL = "/rest/service_instances";
    public static final String GET_FILTERED_SERVICE_INSTANCES_URL =
        "/rest/service_instances?space={space}";
    public static final String CREATE_SERVICE_INSTANCE_URL = "/rest/service_instances";
    public static final String DELETE_SERVICE_INSTANCE_URL = "/rest/service_instances/{instance}";
    public static final String SERVICE_INSTANCES_SUMMARY_URL = "/rest/service_instances/summary";

    private final CcOperations ccClient;
    private final ServiceInstancesControllerHelpers helpers;

    @Autowired public ServiceInstancesController(CcOperations ccClient,
        ServiceInstancesControllerHelpers helpers) {
        this.ccClient = ccClient;
        this.helpers = helpers;
    }

    @ApiOperation("Get services instances filtering by broker in space")
    @RequestMapping(value = GET_ALL_SERVICE_INSTANCES_URL, method = GET,
        produces = APPLICATION_JSON_VALUE)
    public Collection<ServiceInstance> getAllServiceInstances(
        @RequestParam(required = false) UUID space, @RequestParam(required = false) UUID broker) {
        if (space == null) {
            throw new UnsupportedOperationException(
                "Handling not filtered request, not implemented yet");
        }

        CcSummary summary = ccClient.getSpaceSummary(space);
        return FormatTranslator.getServiceInstancesFromPlainSummary(summary, broker);
    }

    @ApiOperation("Creates service instance")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 409, message = "Request was malformed when service name is already in use")
    })
    @RequestMapping(value = CREATE_SERVICE_INSTANCE_URL, method = POST,
        produces = APPLICATION_JSON_VALUE) public CcExtendedServiceInstance createServiceInstance(
        @RequestBody CcNewServiceInstance serviceInstance) {
        CcSummary summary = ccClient.getSpaceSummary(serviceInstance.getSpaceGuid());
        Boolean isNameAlreadyUsed= summary.getServiceInstances()
                .stream()
                .filter(o -> o.getName().equals(serviceInstance.getName()))
                .findAny()
                .isPresent();
        if(isNameAlreadyUsed) {
            throw new NameAlreadyInUseException("Provided name " + serviceInstance.getName() + " is already in use by other instance.");
        }
        return ccClient.createServiceInstance(serviceInstance).toBlocking().single();
    }

    @ApiOperation("Removes service instance")
    @RequestMapping(value = DELETE_SERVICE_INSTANCE_URL, method = DELETE)
    public void deleteServiceInstance(@PathVariable UUID instance) {
        ccClient.deleteServiceInstance(instance);
    }

    @ApiOperation("Get services instances summary")
    @RequestMapping(value = SERVICE_INSTANCES_SUMMARY_URL, method = GET,
        produces = APPLICATION_JSON_VALUE)
    public Collection<Service> getServiceKeysSummary(@RequestParam("space") UUID spaceId,
        @RequestParam(value = "service_keys", required = false) boolean fetchKeys) {
        return getSpaceSummary(spaceId, fetchKeys);
    }

    private List<Service> getSpaceSummary(UUID spaceId, boolean fetchKeys) {
        List<ServiceInstance> instances = helpers.getServiceInstances(spaceId);

        if(fetchKeys) {
            Observable<ServiceKey> serviceKeys = helpers.getServiceKeys();
            helpers.mergeServiceKeys(instances, serviceKeys);
        }

        List<Service> brokers = helpers.getServices();
        helpers.mergeInstances(brokers, instances);

        brokers = brokers.stream()
            .filter(b -> !b.getInstances().isEmpty())
            .collect(Collectors.toList());

        return brokers;
    }
}
