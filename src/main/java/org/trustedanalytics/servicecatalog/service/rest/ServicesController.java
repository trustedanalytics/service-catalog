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
import feign.Feign;
import feign.RequestLine;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonEncoder;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.*;

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

    @RequestMapping(value = GET_ALL_SERVICES_URL, method = GET, produces = APPLICATION_JSON_VALUE)
    public Collection<CcExtendedService> getServices(@RequestParam(required = false) UUID space) {
        if(space == null) {
            throw new UnsupportedOperationException("Handling not filtered request, not implemented yet");
        }
        return ccClient.getServices(space)
            .toList().toBlocking().single();
    }

    @RequestMapping(value = GET_SERVICE_DETAILS_URL, method = GET,
        produces = APPLICATION_JSON_VALUE)
    public CcExtendedService getService(@PathVariable UUID service) {
        return ccClient.getService(service)
            .toBlocking().single();
    }

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
