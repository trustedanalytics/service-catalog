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
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.google.common.base.Preconditions;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.trustedanalytics.cloud.cc.api.CcExtendedService;
import org.trustedanalytics.cloud.cc.api.CcExtendedServicePlan;
import org.trustedanalytics.cloud.cc.api.CcOperations;
import org.trustedanalytics.cloud.cc.api.CcOrg;
import org.trustedanalytics.cloud.cc.api.CcPlanVisibility;
import org.trustedanalytics.servicecatalog.service.CatalogOperations;
import org.trustedanalytics.servicecatalog.service.model.ServicePlanResponse;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.trustedanalytics.servicecatalog.service.model.ServiceRegistrationRequest;
import rx.Observable;


@RestController
public class ServicesController {

    public static final String GET_ALL_SERVICES_URL = "/rest/services";
    public static final String GET_SERVICE_PLAN_URL = "/rest/service_plan";
    public static final String GET_SERVICE_PLANS_URL = "/rest/services/{label}/service_plans";
    public static final String GET_FILTERED_SERVICES_URL = "/rest/services?space={space}";
    public static final String GET_SERVICE_DETAILS_URL = "/rest/services/{service}";
    public static final String REGISTER_APPLICATION = "/rest/marketplace/application";

    private final CcOperations ccClient;
    private final CcOperations privilegedClient;
    private final CatalogOperations catalogClient;

    @Autowired
    public ServicesController(CcOperations ccClient, CcOperations ccPrivilegedClient, CatalogOperations catalogClient) {
        this.ccClient = ccClient;
        this.privilegedClient = ccPrivilegedClient;
        this.catalogClient = catalogClient;
    }

    @ApiOperation(
            value = "Get plan guid for service using label",
            notes = "Privilege level: Consumer of this endpoint must have access to space that service belongs to" +
                    " Verification is performed by Cloud Controller using user token"
    )
    @RequestMapping(value = GET_SERVICE_PLAN_URL, method = GET, produces = APPLICATION_JSON_VALUE)
    public ServicePlanResponse getPlanGuid(@RequestParam(required = true) String label, @RequestParam(required = true) String plan) {

        ServicePlanResponse servicePlanResponse = new ServicePlanResponse();
        CcExtendedService service = ccClient.getExtendedServices().
            firstOrDefault(null, ccExtendedService -> ccExtendedService.getEntity().getLabel().equals(label)).
            toBlocking().single();
        if (service == null)
            return servicePlanResponse;

        CcExtendedServicePlan servicePlan = ccClient.getExtendedServicePlans(service.getMetadata().getGuid()).
                firstOrDefault(null, extendedServicePlans -> extendedServicePlans.getEntity().getName().equals(plan)).
                toBlocking().single();
        if (servicePlan == null)
            return servicePlanResponse;

        servicePlanResponse.setGuid(servicePlan.getMetadata().getGuid());
        return servicePlanResponse;
    }

    @ApiOperation(
            value = "Get available plans for service using label",
            notes = "Privilege level: Consumer of this endpoint must have access to space that service belongs to" +
                    " Verification is performed by Cloud Controller using user token"
    )
    @RequestMapping(value = GET_SERVICE_PLANS_URL, method = GET, produces = APPLICATION_JSON_VALUE)
    public Collection<CcExtendedServicePlan> getServicePlans(@PathVariable String label) {
        return ccClient.getExtendedServices()
            .filter(service -> label.equals(service.getEntity().getLabel()))
            .firstOrDefault(null)
            .flatMap(service -> {
                if (service != null) {
                    return ccClient.getExtendedServicePlans(service.getMetadata().getGuid());
                } else {
                    return Observable.empty();
                }
            })
            .toList()
            .toBlocking()
            .single();
    }

    @ApiOperation(
            value = "Get summary for services in space",
            notes = "Privilege level: Consumer of this endpoint must be a member of specified space"
    )
    @RequestMapping(value = GET_ALL_SERVICES_URL, method = GET, produces = APPLICATION_JSON_VALUE)
    public Collection<CcExtendedService> getServices(@RequestParam(required = false) UUID space) {
        if(space == null) {
            throw new UnsupportedOperationException("Handling not filtered request, not implemented yet");
        }
        return ccClient.getServices(space)
            .toList().toBlocking().single();
    }

    @ApiOperation(
            value = "Get service summary",
            notes = "Privilege level: Consumer of this endpoint must have access to space that service belongs to" +
                    " Verification is performed by Cloud Controller using user token"
    )
    @RequestMapping(value = GET_SERVICE_DETAILS_URL, method = GET,
        produces = APPLICATION_JSON_VALUE)
    public CcExtendedService getService(@PathVariable UUID service) {
        return ccClient.getService(service)
            .toBlocking().single();
    }

    @ApiOperation(
            value = "Register application in catalog",
            notes = "Privilege level: Consumer of this endpoint must be a member of organization"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "Request was malformed while registering without organization access")
    })
    @RequestMapping(value = REGISTER_APPLICATION, method = POST,
            produces = APPLICATION_JSON_VALUE)
    public List<CcPlanVisibility> registerApplication(@RequestBody ServiceRegistrationRequest data) {
        Preconditions.checkNotNull(data.getOrganizationGuid());

        CcOrg org = ccClient.getOrgs().filter(o -> data.getOrganizationGuid().equals(o.getGuid()))
                .toBlocking().firstOrDefault(null);
        if(org == null) {
            throw new AccessDeniedException("Permission denied to access organization: " + data.getOrganizationGuid());
        }

        catalogClient.register(data);

        return privilegedClient.getExtendedServices()
                .filter(service -> data.getName().equals(service.getEntity().getLabel()))
                .firstOrDefault(null)
                .flatMap(service -> {
                    if (service != null) {
                        return privilegedClient.getExtendedServicePlans(service.getMetadata().getGuid());
                    } else {
                        return Observable.empty();
                    }
                })
                .flatMap(plan ->
                        privilegedClient.setExtendedServicePlanVisibility(plan.getMetadata().getGuid(),org.getGuid()))
                .toList().toBlocking().single();
    }
}
