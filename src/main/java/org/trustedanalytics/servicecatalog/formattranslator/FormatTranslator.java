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
package org.trustedanalytics.servicecatalog.formattranslator;

import org.trustedanalytics.cloud.cc.api.CcApp;
import org.trustedanalytics.cloud.cc.api.CcServiceInstance;
import org.trustedanalytics.cloud.cc.api.CcSummary;
import org.trustedanalytics.servicecatalog.service.model.App;
import org.trustedanalytics.servicecatalog.service.model.ServiceInstance;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class FormatTranslator {

    private FormatTranslator() {
    }

    public static Collection<ServiceInstance> getServiceInstancesFromPlainSummary(
        CcSummary ccSpaceSummary, UUID acceptedServiceGuid) {

        Collection<CcApp> apps = ccSpaceSummary.getApps();
        Collection<CcServiceInstance> servInstances = ccSpaceSummary.getServiceInstances();

        Map<String, Collection<App>> appsGroupedByBoundServiceName =
            groupAppGuidsByBoundServiceNames(apps);

        CaseIgnoringUUIDFilter serviceFilter =
            new CaseIgnoringUUIDFilter(acceptedServiceGuid);

        return getFilteredServiceInstancesWithBoundApps(servInstances, appsGroupedByBoundServiceName,
                serviceFilter);
    }

    private static Collection<ServiceInstance> getFilteredServiceInstancesWithBoundApps(
        Collection<CcServiceInstance> services,
        Map<String, Collection<App>> appsGroupedByBoundServiceName,
        CaseIgnoringUUIDFilter serviceFilter) {
        return services.stream()
            .filter(s -> serviceFilter
                .isSatisfiedBy(Optional.of(s).map(CcServiceInstance::getServiceGuid)
                    .map(Object::toString).orElse(null)))
            .map(s -> {
                ServiceInstance instance =
                    new ServiceInstance(s, appsGroupedByBoundServiceName.get(s.getName()));
                instance.setDashboardUrl(s.getDashboardUrl());
                return instance;
            })
            .collect(Collectors.toList());
    }

    private static Map<String, Collection<App>> groupAppGuidsByBoundServiceNames(
        Collection<CcApp> apps) {

        Map<String, Collection<App>> groupedApps = new HashMap<>();

        for (CcApp app : apps) {
            UUID guid = app.getGuid();
            String name = app.getName();
            Collection<String> serviceNames = app.getServiceNames();

            if (serviceNames != null) {
                for (String service : serviceNames) {
                    addAppToMap(groupedApps, new App(guid, name), service);
                }
            }
        }

        return groupedApps;
    }

    private static void addAppToMap(Map<String, Collection<App>> groupedApps, App app,
        String serviceName) {
        if (groupOfThisServiceExists(groupedApps, serviceName)) {
            addAppToExistingGroup(groupedApps, app, serviceName);
        } else {
            addAppToNewGroup(groupedApps, app, serviceName);
        }
    }

    private static boolean groupOfThisServiceExists(Map<String, Collection<App>> groupedApps,
        String serviceName) {
        return groupedApps.containsKey(serviceName);
    }

    private static void addAppToExistingGroup(Map<String, Collection<App>> groupedApps,
        App appToAdd,
        String serviceName) {
        Collection<App> apps = groupedApps.get(serviceName);
        apps.add(appToAdd);
    }

    private static void addAppToNewGroup(Map<String, Collection<App>> groupedApps, App appToAdd,
        String service) {
        List<App> apps;
        apps = new LinkedList<>();
        apps.add(appToAdd);
        groupedApps.put(service, apps);
    }
}
