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

package org.trustedanalytics.servicecatalog.service;

import org.trustedanalytics.cloud.cc.api.CcApp;
import org.trustedanalytics.cloud.cc.api.CcAppState;
import org.trustedanalytics.cloud.cc.api.CcAppStatus;
import org.trustedanalytics.cloud.cc.api.CcAppSummary;
import org.trustedanalytics.cloud.cc.api.CcOperations;
import org.trustedanalytics.cloud.cc.api.CcServiceInstance;
import org.trustedanalytics.cloud.cc.api.CcSummary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service for performing application related operations.
 */
@Service
public class ApplicationsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationsService.class);
    private final CcOperations ccOperations;
    private final Predicate<CcServiceInstance> orphanServices = service -> service.getBoundAppCount() == 1;

    @Autowired
    public ApplicationsService(CcOperations ccOperations) {
        this.ccOperations = Objects.requireNonNull(ccOperations);
    }

    /**
     * @param space unique space identifier
     * @return applications within space
     */
    public Collection<CcApp> getSpaceApps(UUID space) {
        Collection<CcApp> apps = ccOperations.getSpaceSummary(space).getApps();
        apps.forEach(this::fixAppInfo);
        return apps;
    }

    /**
     * @param space unique space identifier
     * @param serviceFilter service filter
     * @return applications within space matching given space filter
     */
    public Collection<CcApp> getSpaceAppsByService(UUID space,
        Predicate<CcServiceInstance> serviceFilter) {
        final CcSummary summary = ccOperations.getSpaceSummary(space);

        final Set<String> services = summary.getServiceInstances().stream()
                .filter(serviceFilter)
            .map(CcServiceInstance::getName)
            .collect(Collectors.toSet());

        return summary.getApps().stream()
            .filter(app -> app.getServiceNames().stream().anyMatch(services::contains))
            .collect(Collectors.toList());
    }

    /**
     * @param app unique application identifier
     * @return application summary
     */
    public CcAppSummary getAppSummary(UUID app) {
        CcAppSummary summary = ccOperations.getAppSummary(app);
        fixAppInfo(summary);
        return summary;
    }

    /**
     * @param app unique application identifier
     */
    public void restageApp(UUID app) {
        ccOperations.restageApp(app);
    }

    /**
     * @param app unique application identifier
     */
    public void deleteApp(UUID app) {
        LOGGER.info("DeleteApp");
        ccOperations.deleteApp(app);
    }

    /**
     * @param app unique application identifier
     */
    public void switchApp(UUID app, CcAppStatus appStatus) {
        LOGGER.info("SwitchApp");
        ccOperations.switchApp(app, appStatus);
    }

    /**
     * Delete application along with services that are not bound to any other application.
     * @param app unique application identifier
     */
    public void deleteAppCascade(UUID app) {
        LOGGER.info("DeleteAppCascade");

        final Collection<CcServiceInstance> orphans = getAppServices(app, orphanServices);
        deleteApp(app);
        orphans.forEach(service -> ccOperations.deleteServiceInstance(service.getGuid()));
    }

    /**
     * Fix info from cloud foundry to be more useful in the console
     */
    private void fixAppInfo(CcAppSummary summary) {
        if(summary.getRunningInstances() != null) {
            //Cloud foundry sometimes returns running_instances=-1 when the app is staging
            summary.setRunningInstances(Math.max(0, summary.getRunningInstances()));

            //cloud foundry returns state STARTED even if there are no running instances
            if (summary.getRunningInstances() == 0 && CcAppState.STARTED.toString().equals(summary.getState())) {
                summary.setState(CcAppState.STOPPED.toString());
            }
        }
    }

    /**
     * Fix info from cloud foundry to be more useful in the console
     */
    private void fixAppInfo(CcApp app) {
        //Cloud foundry sometimes returns running_instances=-1 when the app is staging
        app.setRunningInstances(Math.max(0, app.getRunningInstances()));

        //cloud foundry returns state STARTED even if there are no running instances
        if (app.getRunningInstances() == 0 && CcAppState.STARTED == app.getState()) {
            app.setState(CcAppState.STOPPED);
        }

    }

    /**
     * @param app unique application identifier
     * @param serviceFilter predicate that allow you to filter desired services
     * @return service instance collection
     */
    public Collection<CcServiceInstance> getAppServices(UUID app, Predicate<CcServiceInstance> serviceFilter) {
        return ccOperations.getAppSummary(app).getServices().stream()
                .filter(serviceFilter)
                .collect(Collectors.toList());
    }
}
