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
package org.trustedanalytics.servicecatalog.atk.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.trustedanalytics.cloud.cc.api.CcNewServiceBinding;
import org.trustedanalytics.cloud.cc.api.CcNewServiceInstance;
import org.trustedanalytics.cloud.cc.api.CcOperations;
import org.trustedanalytics.cloud.cc.api.CcSummary;
import org.trustedanalytics.cloud.cc.api.queries.Filter;
import org.trustedanalytics.cloud.cc.api.queries.FilterOperator;
import org.trustedanalytics.cloud.cc.api.queries.FilterQuery;
import org.trustedanalytics.servicecatalog.atk.Atk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
public class AtkController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtkController.class);

    private final CcOperations client;

    @Autowired
    public AtkController(CcOperations client) {
        this.client = client;
    }

    @RequestMapping(value = "/rest/atk/scoring-engine", method = POST, produces = APPLICATION_JSON_VALUE)
    public void createScoringEngine(@RequestBody ScoringEngineRequest request) {

        final CcNewServiceInstance newSEInstance = new CcNewServiceInstance("se-" + request.getInstanceName(),
            request.getOrgGuid(),
            request.getSpaceGuid(),
            request.getServicePlanGuid());

        LOGGER.info("Creating Scoring Engine: {}", newSEInstance.getName());
        client.createServiceInstance(newSEInstance);

        final CcSummary ccSummary = client.getSpaceSummary(newSEInstance.getSpaceGuid());

        final Atk server = new Atk(request.getInstanceName(), Atk.Mode.SERVER, ccSummary);
        LOGGER.info("Atk Server: {}", server);

        final Atk scoringEngine = new Atk(newSEInstance.getName(), Atk.Mode.SCORING_ENGINE, ccSummary);
        LOGGER.info("Atk Scoring Engine: {}", scoringEngine);

        rebindService(scoringEngine.getApp(), scoringEngine.getPostgres(), server.getPostgres());

        LOGGER.info("Restaging application: {}", scoringEngine.getApp());
        client.restageApp(scoringEngine.getApp());
    }

    private void rebindService(UUID app, UUID from, UUID to) {
        LOGGER.info("Rebinding apps {} service {} to {}", app, from, to);

        final FilterQuery filter =
            FilterQuery.from(Filter.SERVICE_INSTANCE_GUID, FilterOperator.EQ, from);

        final UUID binding = client.getAppBindings(app, filter).getBindings().stream()
            .findAny()
            .orElseThrow(() -> new IllegalStateException("Binding not found for app: " + app))
            .getMetadata().getGuid();

        // remove existing binding
        client.deleteServiceBinding(binding);
        // create new biding
        client.createServiceBinding(new CcNewServiceBinding(app, to));
        // delete unused service instance
        client.deleteServiceInstance(from);
    }
}
