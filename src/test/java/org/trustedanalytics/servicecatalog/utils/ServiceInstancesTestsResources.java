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
package org.trustedanalytics.servicecatalog.utils;

import org.trustedanalytics.cloud.cc.api.CcApp;
import org.trustedanalytics.cloud.cc.api.CcAppState;
import org.trustedanalytics.cloud.cc.api.CcServiceInstance;
import org.trustedanalytics.cloud.cc.api.CcSummary;
import org.trustedanalytics.servicecatalog.service.model.App;
import org.trustedanalytics.servicecatalog.service.model.ServiceInstance;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ServiceInstancesTestsResources {

    private static final String instance1Guid = "a673034c-cd1e-42ec-af1d-c95677fb3fcd";
    private static final String instance2Guid = "07a63269-8258-4a6b-8bd0-a115f708dd12";
    private static final String instance3Guid = "2de08669-9380-40fc-9b08-8e9c9f849cbf";
    private static final String instance4Guid = "46c6b675-19d0-4df0-829c-f050c4c3f63c";
    private static final String instance1Name = "zerviceInstance1 of serviceFirst";
    private static final String instance2Name = "zerviceInstance2 of serviceFirst";
    private static final String instance3Name = "zerviceInstance1 of serviceSecond";
    private static final String instance4Name = "serviceInstance4 of user provided instance";
    private static final String serviceFirstGuid = "003b90dc-fc87-4b38-bbfd-4df5ced7eb16";
    private static final String serviceSecondGuid = "1ef2f0bd-abdf-4c3a-be1b-53c532f5bd5e";
    private static final String app1Guid = "36a52a5b-e065-4261-a37f-d9043e6893e4";
    private static final String app2Guid = "87467feb-ce80-4840-b3c3-2e78dd8f758e";
    private static final String app3Guid = "06dc0d8f-6233-4123-ad9e-f968d7e41539";
    private static final String app1Name = "app1";
    private static final String app2Name = "app2";
    private static final String app3Name = "app3";

    public static Collection<ServiceInstance> allNotFilteredServiceInstances() {
        // @formatter:off
        Collection<App> instanceEmptyList = new LinkedList<>();
        Collection<App> instance2List = new LinkedList<>();
            instance2List.add(new App(UUID.fromString(app1Guid), app1Name));
            instance2List.add(new App(UUID.fromString(app3Guid), app3Name));
        Collection<App> instance3List = new LinkedList<>();
            instance3List.add(new App(UUID.fromString(app3Guid), app3Name));

        Collection<ServiceInstance> allNotFilteredServiceInstances = new LinkedList<>();
            allNotFilteredServiceInstances.add(
                new ServiceInstance(UUID.fromString(instance1Guid),
                    instance1Name, "shared", UUID.fromString(serviceFirstGuid), instanceEmptyList));
            allNotFilteredServiceInstances.add(
                new ServiceInstance(UUID.fromString(instance2Guid),
                    instance2Name, "free", UUID.fromString(serviceFirstGuid), instance2List));
            allNotFilteredServiceInstances.add(
                new ServiceInstance(UUID.fromString(instance3Guid),
                    instance3Name, "shared", UUID.fromString(serviceSecondGuid), instance3List));
            allNotFilteredServiceInstances.add(
                new ServiceInstance(UUID.fromString(instance4Guid),
                    instance4Name, null, null, instanceEmptyList));
        // @formatter:on

        return allNotFilteredServiceInstances;
    }

    public static Collection<ServiceInstance> allServiceInstancesOfServiceFirst() {
        // @formatter:off
        Collection<App> instanceEmptyList = new LinkedList<>();
        Collection<App> instance2List = new LinkedList<>();
            instance2List.add(new App(UUID.fromString(app1Guid), app1Name));
            instance2List.add(new App(UUID.fromString(app3Guid), app3Name));

        List<ServiceInstance> allFilteredServiceInstances = new LinkedList<>();
            allFilteredServiceInstances.add(
                new ServiceInstance(UUID.fromString(instance1Guid),
                    instance1Name, "shared", UUID.fromString(serviceFirstGuid), instanceEmptyList));
            allFilteredServiceInstances.add(
                new ServiceInstance(UUID.fromString(instance2Guid),
                    instance2Name, "free", UUID.fromString(serviceFirstGuid), instance2List));
        // @formatter:on

        return allFilteredServiceInstances;
    }

    public static CcSummary spaceSummaryReturnedByCcAdapter() {
        CcSummary summary = new CcSummary();

        List<CcApp> apps = Arrays.asList(
            new CcApp(UUID.fromString(app1Guid), null, 0, Arrays.asList(instance2Name), app1Name,
                CcAppState.STARTED),
            new CcApp(UUID.fromString(app2Guid), null, 0, null, app2Name, CcAppState.STARTED),
            new CcApp(UUID.fromString(app3Guid), null, 0,
                Arrays.asList(instance2Name, instance3Name), app3Name, CcAppState.STARTED)
        );
        summary.setApps(apps);

        List<CcServiceInstance> serviceInstances = Arrays.asList(
            new CcServiceInstance(UUID.fromString(instance1Guid), instance1Name, "shared",
                UUID.fromString(serviceFirstGuid)),
            new CcServiceInstance(UUID.fromString(instance2Guid), instance2Name, "free",
                UUID.fromString(serviceFirstGuid)),
            new CcServiceInstance(UUID.fromString(instance3Guid), instance3Name, "shared",
                UUID.fromString(serviceSecondGuid)),
            new CcServiceInstance(UUID.fromString(instance4Guid), instance4Name, null, null)
        );
        summary.setServiceInstances(serviceInstances);
        return summary;
    }
}
