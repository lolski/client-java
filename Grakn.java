/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package grakn.client;

import grakn.client.api.GraknClient;
import grakn.client.cluster.ClusterClient;
import grakn.client.core.CoreClient;

import java.util.Set;

import static grakn.common.collection.Collections.set;

public class Grakn {

    public static final String DEFAULT_ADDRESS = "localhost:1729";

    public static GraknClient coreClient(String address) {
        return CoreClient.create(address);
    }

    public static GraknClient coreClient(String address, int parallelisation) {
        return CoreClient.create(address, parallelisation);
    }

    public static GraknClient.Cluster clusterClient(String address, boolean tlsEnabled) {
        return ClusterClient.create(set(address), tlsEnabled, null);
    }

    public static GraknClient.Cluster clusterClient(String address, boolean tlsEnabled, String tlsRootCA) {
        return ClusterClient.create(set(address), tlsEnabled, tlsRootCA);
    }

    public static GraknClient.Cluster clusterClient(String address, boolean tlsEnabled, int parallelisation) {
        return ClusterClient.create(set(address), tlsEnabled, null, parallelisation);
    }

    public static GraknClient.Cluster clusterClient(String address, boolean tlsEnabled, String tlsRootCA, int parallelisation) {
        return ClusterClient.create(set(address), tlsEnabled, tlsRootCA, parallelisation);
    }

    public static GraknClient.Cluster clusterClient(Set<String> addresses, boolean tlsEnabled) {
        return ClusterClient.create(addresses, tlsEnabled, null);
    }

    public static GraknClient.Cluster clusterClient(Set<String> addresses, boolean tlsEnabled, String tlsRootCA) {
        return ClusterClient.create(addresses, tlsEnabled, tlsRootCA);
    }

    public static GraknClient.Cluster clusterClient(Set<String> addresses, boolean tlsEnabled, int parallelisation) {
        return ClusterClient.create(addresses, tlsEnabled, null, parallelisation);
    }

    public static GraknClient.Cluster clusterClient(Set<String> addresses, boolean tlsEnabled, String tlsRootCA, int parallelisation) {
        return ClusterClient.create(addresses, tlsEnabled, tlsRootCA, parallelisation);
    }
}
