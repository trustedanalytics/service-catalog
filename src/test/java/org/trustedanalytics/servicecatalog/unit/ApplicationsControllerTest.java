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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.trustedanalytics.cloud.cc.api.CcApp;
import org.trustedanalytics.cloud.cc.api.CcAppState;
import org.trustedanalytics.cloud.cc.api.CcAppStatus;
import org.trustedanalytics.cloud.cc.api.CcAppSummary;
import org.trustedanalytics.cloud.cc.api.CcOperations;
import org.trustedanalytics.cloud.cc.api.CcServiceInstance;
import org.trustedanalytics.cloud.cc.api.CcSummary;
import org.trustedanalytics.servicecatalog.service.ApplicationsService;
import org.trustedanalytics.servicecatalog.service.rest.ApplicationsController;
import org.trustedanalytics.servicecatalog.utils.ApplicationsTestsResources;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationsControllerTest {

    @Mock
    private CcOperations ccClient;

    private ApplicationsController sut;

    @Before
    public void setup() {
        sut = new ApplicationsController(new ApplicationsService(ccClient));
    }

    @Test
    public void getAllApplications_withoutFilter_returnAllApplicationsFromCloudfoundry() {

        String space = "AC5E9498-526B-4ED7-93BC-D2D7412BCCA5";
        UUID spaceGuid = UUID.fromString(space);

        CcSummary sampleSummaryFromCc = ApplicationsTestsResources.appSummaryReturnedByCcAdapter();
        Collection<CcApp> allNotFilteredApplications =
            ApplicationsTestsResources.allNotFilteredApps();

        when(ccClient.getSpaceSummary(any(UUID.class))).thenReturn(sampleSummaryFromCc);

        Collection<CcApp> applications = sut.getFilteredApplications(spaceGuid, Optional.empty());
        assertEquals(allNotFilteredApplications, applications);

        verify(ccClient).getSpaceSummary(spaceGuid);
    }

    @Test
    public void getAppsDetails_appGuidSpecified_returnAppDetailsFromCc() {

        String app = "68b9c96a-2a92-4f5d-9860-475d2223c6f5";
        UUID appGuid = UUID.fromString(app);
        CcAppSummary expectedAppDetails = new CcAppSummary();

        when(ccClient.getAppSummary(any(UUID.class))).thenReturn(expectedAppDetails);

        CcAppSummary appDetails = sut.getAppsDetails(appGuid);
        assertEquals(expectedAppDetails, appDetails);

        verify(ccClient).getAppSummary(appGuid);
    }

    @Test
    public void postStatus_restaging_postRestageToCc() {
        String app = "68b9c96a-2a92-4f5d-9860-475d2223c6f5";
        UUID appGuid = UUID.fromString(app);

        doNothing().when(ccClient).restageApp(any(UUID.class));

        sut.restageApp(appGuid, new CcAppStatus(CcAppState.RESTAGING));

        verify(ccClient).restageApp(appGuid);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void postStatus_restarting_throwUnsupportedOperation() {
        String app = "68b9c96a-2a92-4f5d-9860-475d2223c6f5";
        UUID appGuid = UUID.fromString(app);
        doNothing().when(ccClient).restageApp(any(UUID.class));

        sut.restageApp(appGuid, new CcAppStatus(CcAppState.RESTARTING));
    }

    @Test(expected = IllegalArgumentException.class)
    public void postStatus_empty_throwUnsupportedOperation() {
        String app = "68b9c96a-2a92-4f5d-9860-475d2223c6f5";
        UUID appGuid = UUID.fromString(app);
        doNothing().when(ccClient).restageApp(any(UUID.class));

        sut.restageApp(appGuid, new CcAppStatus());
    }

    @Test
    public void deleteApp_appGuidSpecified_shouldAskCcForDeletingApp() {
        UUID app = UUID.randomUUID();
        doNothing().when(ccClient).deleteApp(any(UUID.class));

        sut.deleteApp(app, Optional.of(false));

        verify(ccClient).deleteApp(app);
    }

    @Test
    public void deleteAppCascade_appGuidSpecified_shouldAskCcForDeletingAppAndServices() {
        // given
        final UUID app = UUID.randomUUID();

        final CcServiceInstance instanceToDelete = new CcServiceInstance();
        instanceToDelete.setGuid(UUID.randomUUID());
        instanceToDelete.setBoundAppCount(1);

        final CcServiceInstance instanceToLeave = new CcServiceInstance();
        instanceToLeave.setGuid(UUID.randomUUID());
        instanceToLeave.setBoundAppCount(2);

        final CcAppSummary summary = new CcAppSummary();
        summary.setServices(ImmutableList.of(instanceToDelete, instanceToLeave));

        // when
        doNothing().when(ccClient).deleteApp(any(UUID.class));
        when(ccClient.getAppSummary(app)).thenReturn(summary);
        sut.deleteApp(app, Optional.of(true));

        // then
        verify(ccClient).getAppSummary(app);
        verify(ccClient).deleteApp(app);
        verify(ccClient).deleteServiceInstance(instanceToDelete.getGuid());
        verify(ccClient, never()).deleteServiceInstance(instanceToLeave.getGuid());
    }

    @Test
    public void switchApp_appGuidSpecified_shouldSwitchOffApp() {
        // given
        UUID app = UUID.randomUUID();
        CcAppState state = CcAppState.STOPPED;
        CcAppStatus status = new CcAppStatus(state);

        // when
        doNothing().when(ccClient).switchApp(any(UUID.class), any(CcAppStatus.class));
        sut.restageApp(app, status);

        // then
        verify(ccClient).switchApp(app, status);
    }

    @Test
    public void switchApp_appGuidSpecified_shouldSwitchOnApp() {
        // given
        UUID app = UUID.randomUUID();
        CcAppState state = CcAppState.STARTED;
        CcAppStatus status = new CcAppStatus(state);

        // when
        doNothing().when(ccClient).switchApp(any(UUID.class), any(CcAppStatus.class));
        sut.restageApp(app, status);

        // then
        verify(ccClient).switchApp(app, status);
    }

    @Test
    public void getAppOrphanServices_oneOrphanServiceSpecified_shouldReturnOneUniqueService(){
        // given
        final UUID app = UUID.randomUUID();

        final CcServiceInstance instanceOrphan = new CcServiceInstance();
        instanceOrphan.setGuid(UUID.randomUUID());
        instanceOrphan.setBoundAppCount(1);

        final CcServiceInstance instanceNonOrphan = new CcServiceInstance();
        instanceNonOrphan.setGuid(UUID.randomUUID());
        instanceNonOrphan.setBoundAppCount(2);

        final CcAppSummary summary = new CcAppSummary();
        summary.setServices(ImmutableList.of(instanceOrphan, instanceNonOrphan));

        Collection<CcServiceInstance> expectedOutput = new ArrayList<CcServiceInstance>(){ { add(instanceOrphan); } };

        //when
        when(ccClient.getAppSummary(app)).thenReturn(summary);
        Collection<CcServiceInstance> actualOutput = sut.getAppOrphanServices(app);

        //then
        verify(ccClient).getAppSummary(app);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void getAppOrphanServices_lackOfOrphanServiceInstancesSpecified_shouldReturnEmptyCollection(){
        // given
        final UUID app = UUID.randomUUID();

        final CcServiceInstance instanceOrphan = new CcServiceInstance();
        instanceOrphan.setGuid(UUID.randomUUID());
        instanceOrphan.setBoundAppCount(2);

        final CcServiceInstance instanceNonOrphan = new CcServiceInstance();
        instanceNonOrphan.setGuid(UUID.randomUUID());
        instanceNonOrphan.setBoundAppCount(2);

        final CcAppSummary summary = new CcAppSummary();
        summary.setServices(ImmutableList.of(instanceOrphan, instanceNonOrphan));

        Collection<CcServiceInstance> expectedOutput = new ArrayList<CcServiceInstance>(){ { } };

        //when
        when(ccClient.getAppSummary(app)).thenReturn(summary);
        Collection<CcServiceInstance> actualOutput = sut.getAppOrphanServices(app);

        //then
        verify(ccClient).getAppSummary(app);
        assertEquals(expectedOutput, actualOutput);
    }
}
