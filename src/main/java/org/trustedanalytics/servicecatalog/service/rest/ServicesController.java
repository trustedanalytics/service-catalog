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

import com.google.common.base.Preconditions;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.trustedanalytics.cloud.auth.OAuth2TokenRetriever;
import org.trustedanalytics.cloud.cc.api.CcExtendedService;
import org.trustedanalytics.cloud.cc.api.CcExtendedServicePlan;
import org.trustedanalytics.cloud.cc.api.CcOperations;
import org.trustedanalytics.cloud.cc.api.CcOrg;
import org.trustedanalytics.cloud.cc.api.queries.Filter;
import org.trustedanalytics.cloud.cc.api.queries.FilterOperator;
import org.trustedanalytics.cloud.cc.api.queries.FilterQuery;
import org.trustedanalytics.servicecatalog.service.CatalogOperations;
import org.trustedanalytics.servicecatalog.service.model.ServiceBroker;
import org.trustedanalytics.servicecatalog.service.model.ServiceDetails;
import org.trustedanalytics.servicecatalog.service.model.ServicePlanResponse;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
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
    public static final String CLONED_APPLICATION = "/rest/marketplace/application/{service}";

    private final CcOperations ccClient;
    private final CcOperations privilegedClient;
    private final CatalogOperations catalogClient;

    private static final String ADMIN_ROLE = "console.admin";

    @Autowired
    private OAuth2TokenRetriever tokenRetriever;

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
    public ServiceDetails getService(@PathVariable UUID service) {
        CcExtendedService ccService = ccClient.getService(service)
                .toBlocking().single();

        return catalogClient.getCatalog().getServices().stream()
                .filter(item -> item.getId().toString().equals(ccService.getEntity().getUniqueId()))
                .findFirst().map(item -> new ServiceDetails(ccService, canDeleteOffering(ccService.getMetadata().getGuid())))
                .orElse(new ServiceDetails(ccService, false));
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
    public CcExtendedService registerApplication(@RequestBody ServiceRegistrationRequest data) {
        Preconditions.checkNotNull(data.getOrganizationGuid());

        CcOrg org = ccClient.getOrgs().filter(o -> data.getOrganizationGuid().equals(o.getGuid()))
                .toBlocking().firstOrDefault(null);
        if(org == null) {
            throw new AccessDeniedException("Permission denied to access organization: " + data.getOrganizationGuid());
        }

        catalogClient.register(data);

        Observable<CcExtendedService> extendedService = privilegedClient.getExtendedServices().
                firstOrDefault(null, service -> data.getName().equals(service.getEntity().getLabel()));

        extendedService
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

        return extendedService.toBlocking().single();
    }


    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "No services for delete"),
            @ApiResponse(code = 403, message = "Your are not authorize to delete this service")
    })
    @RequestMapping(value = CLONED_APPLICATION, method = DELETE)
    public void deregisterApplication(@PathVariable UUID service) {

        ServiceBroker catalogResult = catalogClient.getCatalog();
        if (catalogResult.getServices().isEmpty() || catalogResult.getServices().size() < 2) {
            throw new NoItemInCatalogException("Catalog is empty or have only 1 item left.");
        }

        CcExtendedService ccService = ccClient.getService(service)
                .toBlocking().single();

        if (!canDeleteOffering(ccService.getMetadata().getGuid())) {
            throw new AccessDeniedException("User not authorize to delete this service");
        }

        for (ServiceRegistrationRequest item : catalogResult.getServices()) {
            if(item.getId().toString().equals(ccService.getEntity().getUniqueId())){
                catalogClient.deregister(ccService.getEntity().getUniqueId());
            }
        }

    }

    @RequestMapping(value = CLONED_APPLICATION, method = GET)
    public Collection<CcExtendedService> getClonedApplications(@PathVariable UUID service) {
        ServiceBroker catalogResult = catalogClient.getCatalog();
        Collection<CcExtendedService> result = new LinkedList<>();

        for (ServiceRegistrationRequest item : catalogResult.getServices()) {
            if(item.getApp().getMetadata().getGuid().equals(service)){
                final FilterQuery filter =
                        FilterQuery.from(Filter.LABEL, FilterOperator.EQ, item.getName());
                result.add(ccClient.getExtendedServices(filter).toBlocking().single());
            }
        }

        return result;
    }

    public boolean canDeleteOffering(UUID serviceGuid) {
        final FilterQuery filter =
                FilterQuery.from(Filter.SERVICE_PLAN_GUID, FilterOperator.EQ, serviceGuid);
        boolean isPublic = privilegedClient.getExtendedServicePlans(serviceGuid)
                .exists(plan -> plan.getEntity().getPublicStatus())
                .toBlocking().single();

        boolean inAnotherOrg = privilegedClient.getExtendedServicePlanVisibility(filter)
                .distinct(visibility -> visibility.getEntity().getOrgGuid())
                        .count().toBlocking().single() > 1;

        if (!isPublic && !inAnotherOrg) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = Optional
                .ofNullable(auth.getAuthorities()).orElse(new ArrayList<>());
        return authorities.stream()
                .map(GrantedAuthority::getAuthority).anyMatch(ADMIN_ROLE::equalsIgnoreCase);
    }

}
