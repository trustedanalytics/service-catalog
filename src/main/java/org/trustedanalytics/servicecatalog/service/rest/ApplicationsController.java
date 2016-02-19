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
import org.trustedanalytics.cloud.cc.api.CcApp;
import org.trustedanalytics.cloud.cc.api.CcAppStatus;
import org.trustedanalytics.cloud.cc.api.CcAppSummary;
import org.trustedanalytics.cloud.cc.api.CcServiceInstance;
import org.trustedanalytics.servicecatalog.service.ApplicationsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@RestController
public class ApplicationsController {

    public static final String GET_ALL_APPS_URL = "/rest/apps";
    public static final String GET_FILTERED_APPS_URL = "/rest/apps?space={space}";
    public static final String GET_APP_DETAILS_URL = "/rest/apps/{app}";
    public static final String RESTAGE_APP_URL = "/rest/apps/{app}/status";
    public static final String DELETE_APP_URL = "/rest/apps/{app}";
    public static final String GET_APP_ORPHAN_SERVICES = "/rest/apps/{app}/orphan_services";

    private final ApplicationsService applicationsService;

    @Autowired
    public ApplicationsController(ApplicationsService applicationsService) {
        this.applicationsService = applicationsService;
    }

    @ApiOperation("Get applications from given space")
    @RequestMapping(value = GET_ALL_APPS_URL, method = GET, produces = APPLICATION_JSON_VALUE)
    public Collection<CcApp> getFilteredApplications(@RequestParam(required = false, value = "space") UUID space,
        @RequestParam(value = "service_label") Optional<String> serviceLabel) {
        return serviceLabel
            .map(label ->
                applicationsService.getSpaceAppsByService(space, service ->
                    (service.getServicePlan() != null) && label
                        .equals(service.getServicePlan().getService().getLabel())))
            .orElse(applicationsService.getSpaceApps(space));
    }

    @ApiOperation("Get application details")
    @RequestMapping(value = GET_APP_DETAILS_URL, method = GET, produces = APPLICATION_JSON_VALUE)
    public CcAppSummary getAppsDetails(@PathVariable UUID app) {
        return applicationsService.getAppSummary(app);
    }

    @ApiOperation("Get service instances bounded only to given application")
    @RequestMapping(value = GET_APP_ORPHAN_SERVICES, method = GET, produces = APPLICATION_JSON_VALUE)
    public Collection<CcServiceInstance> getAppOrphanServices(@PathVariable UUID app){
        return applicationsService.getAppServices(app, service -> service.getBoundAppCount() == 1);
    }

    @ApiOperation("Restages application")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Request was malformed when application status is null")
    })
    @RequestMapping(value = RESTAGE_APP_URL, method = POST)
    public void restageApp(@PathVariable UUID app, @RequestBody CcAppStatus status) {
        if (status.getState() == null) {
            throw new IllegalArgumentException();
        }

        switch (status.getState()) {
            case RESTAGING:
                applicationsService.restageApp(app);
                break;
            case RESTARTING:
                throw new UnsupportedOperationException();
            case STARTED:
                applicationsService.switchApp(app, status);
                break;
            case STOPPED:
                applicationsService.switchApp(app, status);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @ApiOperation("Removes application, cascade option allows removing bounded service instances for given application")
    @RequestMapping(value = DELETE_APP_URL, method = DELETE)
    public void deleteApp(@PathVariable UUID app, @RequestParam(value = "cascade") Optional<Boolean> cascade) {
        if(cascade.orElse(false)) {
            applicationsService.deleteAppCascade(app);
        } else {
            applicationsService.deleteApp(app);
        }
    }
}
