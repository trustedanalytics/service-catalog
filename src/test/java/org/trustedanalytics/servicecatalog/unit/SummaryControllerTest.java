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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.trustedanalytics.cloud.cc.api.CcOperations;
import org.trustedanalytics.cloud.cc.api.CcSummary;
import org.trustedanalytics.servicecatalog.service.rest.SummaryController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class SummaryControllerTest {

    private SummaryController sut;

    @Mock private CcOperations ccClient;

    @Before
    public void setUp() {
        sut = new SummaryController(ccClient);
    }

    @Test
    public void getSpaceSummary_spaceGuidSpecified_returnSummaryFromCc() {

        String spaceGuidPlain = "39604b6f-50bc-4446-938d-4c8320007636";
        UUID spaceGuid = UUID.fromString(spaceGuidPlain);

        CcSummary summaryReturnedByCcAdapter = mock(CcSummary.class);
        when(ccClient.getSpaceSummary(any(UUID.class))).thenReturn(summaryReturnedByCcAdapter);

        CcSummary summary = sut.getSpaceSummary(spaceGuid);
        assertEquals(summaryReturnedByCcAdapter, summary);

        verify(ccClient).getSpaceSummary(spaceGuid);
    }

}
