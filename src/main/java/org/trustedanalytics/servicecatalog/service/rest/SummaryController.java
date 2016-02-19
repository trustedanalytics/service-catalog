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

import io.swagger.annotations.ApiOperation;
import org.trustedanalytics.cloud.cc.api.CcOperations;
import org.trustedanalytics.cloud.cc.api.CcSummary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
public class SummaryController {
    public static final String GET_SPACE_SUMMARY_URL = "/rest/summaries";
    public static final String GET_SPACE_FILTERED_SUMMARY_URL = "/rest/summaries?space={space}";

    private final CcOperations ccClient;

    @Autowired
    public SummaryController(CcOperations ccClient) {
        this.ccClient = ccClient;
    }

    @ApiOperation("Get summary for apps and service deployed in space")
    @RequestMapping(value = GET_SPACE_SUMMARY_URL, method = GET, produces = APPLICATION_JSON_VALUE)
    public CcSummary getSpaceSummary(@RequestParam(required = false) UUID space) {
        if(space != null) {
            return ccClient.getSpaceSummary(space);
        }
        throw new UnsupportedOperationException("Handling not filtered request, not implemented yet");
    }
}
