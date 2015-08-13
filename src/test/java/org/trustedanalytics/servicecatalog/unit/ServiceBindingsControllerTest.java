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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.trustedanalytics.cloud.cc.api.CcNewServiceBinding;
import org.trustedanalytics.cloud.cc.api.CcOperations;
import org.trustedanalytics.cloud.cc.api.CcServiceBinding;
import org.trustedanalytics.cloud.cc.api.CcServiceBindingList;
import org.trustedanalytics.servicecatalog.service.rest.ServiceBindingsController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ServiceBindingsControllerTest {

    private ServiceBindingsController sut;

    @Mock
    private CcOperations ccClient;

    @Before
    public void setUp() {
        sut = new ServiceBindingsController(ccClient);
    }

    @Test
    public void getBindingsOfApp_appGuidSpecified_returnAppBindingsFromCc() {

        String app = "68b9c96a-2a92-4f5d-9860-475d2223c6f5";
        UUID appGuid = UUID.fromString(app);
        CcServiceBindingList expectedAppBindings = new CcServiceBindingList();

        when(ccClient.getAppBindings(any(UUID.class))).thenReturn(expectedAppBindings);

        CcServiceBindingList appBindings = sut.getBindingsOfApp(appGuid);
        assertEquals(expectedAppBindings, appBindings);

        verify(ccClient).getAppBindings(appGuid);
    }

    @Test
    public void createServiceBinding_createBindingAndReturnIt() {
        ArgumentCaptor<CcNewServiceBinding> captor = ArgumentCaptor.forClass(CcNewServiceBinding.class);
        UUID app = UUID.fromString("9ed144a6-b550-49b3-8058-903587016300");
        UUID service = UUID.fromString("1d308c90-33e9-4aff-a51b-ff4bfc5d5275");
        CcNewServiceBinding input = new CcNewServiceBinding(null, service);
        CcServiceBinding returned = mock(CcServiceBinding.class);
        when(ccClient.createServiceBinding(any(CcNewServiceBinding.class))).thenReturn(returned);

        CcServiceBinding result = sut.createServiceBinding(app, input);

        verify(ccClient).createServiceBinding(captor.capture());
        assertEquals(returned, result);
    }

    @Test
    public void deleteServiceBinding_deleteInstanceInCloudFoudry() {
        doNothing().when(ccClient).deleteServiceBinding(any(UUID.class));
        UUID bindingGuid = UUID.randomUUID();

        sut.deleteServiceBinding(bindingGuid);

        verify(ccClient).deleteServiceBinding(bindingGuid);
    }
}
