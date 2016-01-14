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
package org.trustedanalytics.servicecatalog.unit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.cloud.cc.api.CcApp;
import org.trustedanalytics.cloud.cc.api.CcAppState;
import org.trustedanalytics.cloud.cc.api.CcAppSummary;
import org.trustedanalytics.cloud.cc.api.CcOperations;
import org.trustedanalytics.cloud.cc.api.CcSummary;
import org.trustedanalytics.servicecatalog.service.ApplicationsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;



@RunWith(MockitoJUnitRunner.class)
public class ApplicationsServiceTest {

    @Mock
    private CcOperations ccClient;

    private ApplicationsService sut;

    private String guid = "68b9c96a-2a92-4f5d-9860-475d2223c6f5";

    @Before
    public void setup() {
        sut = new ApplicationsService(ccClient);
    }

    @Test
    public void getApplicationSummaryWith0Instances_statusesUnchanged(){
        UUID appGuid = UUID.fromString(guid);
        Long runningInstances = 0l;

        CcAppSummary mockedSummary = new CcAppSummary();
        mockedSummary.setRunningInstances(runningInstances);
        mockedSummary.setState(CcAppState.STARTED.toString());

        when(ccClient.getAppSummary(any(UUID.class))).thenReturn(mockedSummary);

        CcAppSummary appSummary = sut.getAppSummary(appGuid);
        assertEquals(CcAppState.STARTED.toString(), appSummary.getState());
        assertEquals(runningInstances, appSummary.getRunningInstances());
    }

    @Test
    public void getApplicationSummaryWithNegativeInstances_instancesChangedToZero(){
        UUID appGuid = UUID.fromString(guid);
        CcAppSummary mockedSummary = new CcAppSummary();
        mockedSummary.setRunningInstances(-1l);

        when(ccClient.getAppSummary(any(UUID.class))).thenReturn(mockedSummary);

        CcAppSummary appSummary = sut.getAppSummary(appGuid);
        assertEquals((Long)0l, appSummary.getRunningInstances());
    }

    @Test
    public void getSpaceAppsEmpty_EmptyReturned(){
        UUID appGuid = UUID.fromString(guid);

        CcSummary mockedSummary = new CcSummary();
        mockedSummary.setApps(new ArrayList<>());

        when(ccClient.getSpaceSummary(any(UUID.class))).thenReturn(mockedSummary);

        List<CcApp> ccApps = new ArrayList<>(sut.getSpaceApps(appGuid));

        assertEquals(0, ccApps.size());
    }

    @Test
    public void getSpaceAppsWith0Instances_StatusesUnchanged(){
        UUID appGuid = UUID.fromString(guid);

        CcSummary mockedSummary = new CcSummary();
        CcApp appData = new CcApp();
        appData.setRunningInstances(0);
        appData.setState(CcAppState.STARTED);
        mockedSummary.setApps(Arrays.asList(appData));


        when(ccClient.getSpaceSummary(any(UUID.class))).thenReturn(mockedSummary);

        List<CcApp> ccApps = new ArrayList<>(sut.getSpaceApps(appGuid));

        assertEquals(1, ccApps.size());
        assertEquals(CcAppState.STARTED, ccApps.get(0).getState());
        assertEquals(0, ccApps.get(0).getRunningInstances());
    }

    @Test
    public void getSpaceAppsWithNegativeInstances_instancesChangedToZero(){
        UUID appGuid = UUID.fromString(guid);

        CcSummary mockedSummary = new CcSummary();
        CcApp appData = new CcApp();
        appData.setRunningInstances(-1);
        mockedSummary.setApps(Arrays.asList(appData));

        when(ccClient.getSpaceSummary(any(UUID.class))).thenReturn(mockedSummary);

        List<CcApp> ccApps = new ArrayList<>(sut.getSpaceApps(appGuid));

        assertEquals(1, ccApps.size());
        assertEquals(0, ccApps.get(0).getRunningInstances());
    }



}
