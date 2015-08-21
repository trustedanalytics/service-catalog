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

import org.trustedanalytics.cloud.cc.api.CcExtendedService;
import org.trustedanalytics.cloud.cc.api.CcExtendedServicePlan;
import org.trustedanalytics.cloud.cc.api.CcOperationsServices;
import org.trustedanalytics.servicecatalog.service.model.ServicePlanResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ServicesController {

    public static final String GET_ALL_SERVICES_URL = "/rest/services";
    public static final String GET_SERVICE_PLAN_GUID_URL = "/rest/service_plan_guid";
    public static final String GET_FILTERED_SERVICES_URL = "/rest/services?space={space}";
    public static final String GET_SERVICE_DETAILS_URL = "/rest/services/{service}";

    private final CcOperationsServices ccClient;

    @Autowired
    public ServicesController(CcOperationsServices ccClient) {
        this.ccClient = ccClient;
    }

    @RequestMapping(value = GET_SERVICE_PLAN_GUID_URL, method = GET, produces = APPLICATION_JSON_VALUE)
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

    @RequestMapping(value = GET_ALL_SERVICES_URL, method = GET, produces = APPLICATION_JSON_VALUE)
    public String getServices(@RequestParam(required = false) UUID space) {
        if(space == null) {
            throw new UnsupportedOperationException("Handling not filtered request, not implemented yet");
        }
        return ccClient.getServices(space);
    }

    @RequestMapping(value = GET_SERVICE_DETAILS_URL, method = GET,
        produces = APPLICATION_JSON_VALUE)
    public String getService(@PathVariable UUID service) {
        return ccClient.getService(service);
    }

}
