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
import org.trustedanalytics.cloud.cc.api.CcSummary;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

public class ApplicationsTestsResources {

    private static CcApp app1;
    private static CcApp app2;

    static {
        app1 = new CcApp(UUID.fromString("31c7781b-416b-446d-bedf-f34ade649f5a"),
            Arrays.asList("url1.sample.com", "url2.sample.com"), 5,
            Arrays.asList("serv1"), "app1", CcAppState.STARTED);
        app2 = new CcApp(UUID.fromString("dc06c4c7-9074-4392-88c7-c9cadd186b63"),
            Arrays.asList("myapp-myurl.sample.com"), 5,
            Arrays.asList("serv1", "serv2", "serv3"), "app2", CcAppState.RESTAGING);
    }

    public static CcSummary appSummaryReturnedByCcAdapter() {
        CcSummary summary = new CcSummary();
        summary.setApps(Arrays.asList(app1, app2));
        return summary;
    }

    public static Collection<CcApp> allNotFilteredApps() {
        Collection<CcApp> apps = new LinkedList<>();
        apps.add(app1);
        apps.add(app2);
        return apps;
    }
}

