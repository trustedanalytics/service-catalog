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
package org.trustedanalytics.servicecatalog.atk;

import org.trustedanalytics.cloud.cc.api.CcApp;
import org.trustedanalytics.cloud.cc.api.CcServiceInstance;
import org.trustedanalytics.cloud.cc.api.CcSummary;

import com.google.common.base.MoreObjects;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class Atk {

    public enum Mode {
        SERVER("atk"), SCORING_ENGINE("se");

        private final String name;

        Mode(String name) {
            this.name = name;
        }
    }

    private final UUID app;
    private final UUID postgres;
    private final UUID cdh;
    private final UUID zooKeeper;

    public Atk(String name, Mode mode, CcSummary ccSummary) {
        final String commonPart = getCommonPart(ccSummary, name);

        this.app = getAppByName(ccSummary, mode.name + "-" + commonPart);
        this.postgres = getServiceByName(ccSummary, "postgresql93-" + mode.name + "-" + commonPart);
        this.cdh = getServiceByName(ccSummary, "cdh-" + mode.name + "-" + commonPart);
        this.zooKeeper = getServiceByName(ccSummary, "zookeeper-" + mode.name + "-" + commonPart);
    }

    public UUID getApp() {
           return app;
    }

    public UUID getPostgres() {
        return postgres;
    }

    public UUID getCdh() {
        return cdh;
    }

    public UUID getZooKeeper() {
        return zooKeeper;
    }

    private UUID getAppByName(CcSummary ccSummary, String name) {
        return ccSummary.getApps().stream()
            .filter(application -> application.getName().equalsIgnoreCase(name))
            .map(CcApp::getGuid)
            .findAny()
            .orElseThrow(() -> new IllegalStateException("Application not found: " + name));
    }

    private UUID getServiceByName(CcSummary ccSummary, String name) {
        return ccSummary.getServiceInstances().stream()
            .filter(service -> name.equalsIgnoreCase(service.getName()))
            .map(CcServiceInstance::getGuid)
            .findAny()
            .orElseThrow(() -> new IllegalStateException("Service not found: " + name));
    }

    private String getCommonPart(CcSummary ccSummary, String name) {
        return Arrays.asList(getServiceByName(ccSummary, name).toString().split("-")).stream()
            .limit(4)
            .collect(Collectors.joining("-"));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("app", getApp())
            .add("postgres", getPostgres())
            .add("cdh", getCdh())
            .add("zookeeper", getZooKeeper())
            .toString();
    }
}
