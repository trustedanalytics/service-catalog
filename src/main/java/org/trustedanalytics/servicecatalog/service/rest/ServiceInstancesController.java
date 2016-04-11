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

import com.google.common.base.Preconditions;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.trustedanalytics.cloud.cc.api.CcExtendedServiceInstance;
import org.trustedanalytics.cloud.cc.api.CcNewServiceInstance;
import org.trustedanalytics.cloud.cc.api.CcOperations;
import org.trustedanalytics.cloud.cc.api.CcSummary;
import org.trustedanalytics.servicecatalog.formattranslator.FormatTranslator;
import org.trustedanalytics.servicecatalog.service.model.Service;
import org.trustedanalytics.servicecatalog.service.model.ServiceInstance;
import org.trustedanalytics.servicecatalog.service.model.ServiceInstanceMetadata;
import org.trustedanalytics.servicecatalog.service.model.ServiceKey;
import org.trustedanalytics.servicecatalog.service.model.Summary;
import org.trustedanalytics.servicecatalog.storage.ServiceInstanceRegistry;
import rx.Observable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController public class ServiceInstancesController {

    public static final String GET_ALL_SERVICE_INSTANCES_URL = "/rest/service_instances";
    public static final String GET_FILTERED_SERVICE_INSTANCES_URL =
        "/rest/service_instances?space={space}";
    public static final String CREATE_SERVICE_INSTANCE_URL = "/rest/service_instances";
    public static final String DELETE_SERVICE_INSTANCE_URL = "/rest/service_instances/{instance}";
    public static final String SERVICE_INSTANCES_SUMMARY_URL = "/rest/service_instances/summary";
    public static final String SPACE_SUMMARY_URL = "/rest/service_instances/extended_summary";

    private static final String CREATING_INSTANCE_ERROR = "Error while creating service instance";

    private final CcOperations ccClient;
    private final ServiceInstancesControllerHelpers helpers;
    private final ServiceInstanceRegistry serviceInstanceRegistry;

    @Autowired
    public ServiceInstancesController(CcOperations ccClient,
                                      ServiceInstancesControllerHelpers helpers,
                                      ServiceInstanceRegistry serviceInstanceRegistry) {
        this.ccClient = ccClient;
        this.helpers = helpers;
        this.serviceInstanceRegistry = serviceInstanceRegistry;
    }

    @ApiOperation(
            value = "Get services instances filtering by broker in space",
            notes = "Privilege level: Consumer of this endpoint must be a member of specified space"
    )
    @RequestMapping(value = GET_ALL_SERVICE_INSTANCES_URL, method = GET,
        produces = APPLICATION_JSON_VALUE)
    public Collection<ServiceInstance> getAllServiceInstances(
        @RequestParam(required = false) UUID space, @RequestParam(required = false) UUID broker) {
        if (space == null) {
            throw new UnsupportedOperationException(
                "Handling not filtered request, not implemented yet");
        }

        CcSummary summary = ccClient.getSpaceSummary(space);

        Collection<ServiceInstance> serviceInstances = FormatTranslator.getServiceInstancesFromPlainSummary(summary, broker);
        for (ServiceInstance i : serviceInstances) {
            Optional<ServiceInstanceMetadata> metadata =
                    Optional.ofNullable(serviceInstanceRegistry.getInstanceCreator(i.getGuid()));
            metadata.ifPresent(m -> i.setMetadata(m));
        }
        return serviceInstances;
    }

    @ApiOperation(
            value = "Creates service instance",
            notes = "Privilege level: Consumer of this endpoint must be a member of space to create service instance"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 409, message = "Request was malformed when service name is already in use")
    })
    @RequestMapping(value = CREATE_SERVICE_INSTANCE_URL, method = POST,
        produces = APPLICATION_JSON_VALUE) public CcExtendedServiceInstance createServiceInstance(
        @RequestBody CcNewServiceInstance serviceInstance, Authentication authentication) {

        CcSummary summary = ccClient.getSpaceSummary(serviceInstance.getSpaceGuid());
        Boolean isNameAlreadyUsed= summary.getServiceInstances()
                .stream()
                .filter(o -> o.getName().equals(serviceInstance.getName()))
                .findAny()
                .isPresent();
        if(isNameAlreadyUsed) {
            throw new NameAlreadyInUseException("Provided name " + serviceInstance.getName() + " is already in use by other instance.");
        }

        CcExtendedServiceInstance createdInstance = ccClient.createServiceInstance(serviceInstance).toBlocking().single();
        Preconditions.checkState(createdInstance != null, CREATING_INSTANCE_ERROR);
        Preconditions.checkState(createdInstance.getMetadata() != null, CREATING_INSTANCE_ERROR);
        Preconditions.checkState(createdInstance.getMetadata().getGuid() != null, CREATING_INSTANCE_ERROR);

        serviceInstanceRegistry.addInstanceCreator(createdInstance.getMetadata().getGuid(),
                new ServiceInstanceMetadata(helpers.findUserId(authentication), helpers.findUserName(authentication)));

        return createdInstance;
    }

    @ApiOperation(
            value = "Removes service instance",
            notes = "Privilege level: Consumer of this endpoint must have access to space that service instance belongs to" +
                    " Verification is performed by Cloud Controller using user token"
    )
    @RequestMapping(value = DELETE_SERVICE_INSTANCE_URL, method = DELETE)
    public void deleteServiceInstance(@PathVariable UUID instance) {
        ccClient.deleteServiceInstance(instance);
        serviceInstanceRegistry.deleteInstanceCreator(instance);
    }

    @ApiOperation(
            value = "Get services instances summary",
            notes = "Privilege level: Consumer of this endpoint must be a member of specified space"
    )
    @RequestMapping(value = SERVICE_INSTANCES_SUMMARY_URL, method = GET,
        produces = APPLICATION_JSON_VALUE)
    public Collection<Service> getServiceKeysSummary(@RequestParam("space") UUID spaceId,
        @RequestParam(value = "service_keys", required = false) boolean fetchKeys) {
        return getSpaceSummary(spaceId, fetchKeys);
    }

    @ApiOperation(
            value = "Get space summary with instance metadata injected",
            notes = "Privilege level: Consumer of this endpoint must be a member of specified space"
    )
    @RequestMapping(value = SPACE_SUMMARY_URL, method = GET, produces = APPLICATION_JSON_VALUE)
    public Summary getExtendedSpaceSummary(@RequestParam("space") UUID spaceId) {
        if (spaceId == null) {
            throw new UnsupportedOperationException(
                    "Handling not filtered request, not implemented yet");
        }
        List<ServiceInstance> instances = helpers.getServiceInstances(spaceId);
        instances.stream().forEach(i -> i.setMetadata(serviceInstanceRegistry.getInstanceCreator(i.getGuid())));
        return new Summary(instances, ccClient.getSpaceSummary(spaceId).getApps());
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
