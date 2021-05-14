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

package grakn.client.core;

import com.google.protobuf.ByteString;
import grakn.client.api.GraknClient;
import grakn.client.api.GraknOptions;
import grakn.client.api.GraknSession;
import grakn.client.common.exception.GraknClientException;
import grakn.client.common.rpc.ManagedChannelFactory;
import grakn.client.common.rpc.GraknStub;
import grakn.client.stream.RequestTransmitter;
import grakn.common.concurrent.NamedThreadFactory;
import io.grpc.ManagedChannel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static grakn.client.common.exception.ErrorMessage.Internal.ILLEGAL_CAST;
import static grakn.common.util.Objects.className;

public class CoreClient implements GraknClient {

    private static final String GRAKN_CLIENT_RPC_THREAD_NAME = "grakn-client-rpc";

    private final ManagedChannel channel;
    private final GraknStub.Core stub;
    private final RequestTransmitter transmitter;
    private final CoreDatabaseManager databaseMgr;
    private final ConcurrentMap<ByteString, CoreSession> sessions;

    private CoreClient(String address, ManagedChannelFactory managedChannelFactory) {
        this(address, managedChannelFactory, calculateParallelisation());
    }

    protected CoreClient(String address, ManagedChannelFactory managedChannelFactory, int parallelisation) {
        NamedThreadFactory threadFactory = NamedThreadFactory.create(GRAKN_CLIENT_RPC_THREAD_NAME);
        channel = managedChannelFactory.forAddress(address);
        stub = GraknStub.core(channel);
        transmitter = new RequestTransmitter(parallelisation, threadFactory);
        databaseMgr = new CoreDatabaseManager(this);
        sessions = new ConcurrentHashMap<>();
    }

    public static CoreClient create(String address) {
        return new CoreClient(address, new ManagedChannelFactory.PlainText());
    }

    public static CoreClient create(String address, int parallelisation) {
        return new CoreClient(address, new ManagedChannelFactory.PlainText(), parallelisation);
    }

    public static int calculateParallelisation() {
        int cores = Runtime.getRuntime().availableProcessors();
        if (cores <= 4) return 2;
        else if (cores <= 9) return 3;
        else if (cores <= 16) return 4;
        else return (int) Math.ceil(cores / 4.0);
    }

    @Override
    public CoreSession session(String database, GraknSession.Type type) {
        return session(database, type, GraknOptions.core());
    }

    @Override
    public CoreSession session(String database, GraknSession.Type type, GraknOptions options) {
        CoreSession session = new CoreSession(this, database, type, options);
        assert !sessions.containsKey(session.id());
        sessions.put(session.id(), session);
        return session;
    }

    @Override
    public CoreDatabaseManager databases() {
        return databaseMgr;
    }

    @Override
    public boolean isOpen() {
        return !channel.isShutdown();
    }

    @Override
    public boolean isCluster() {
        return false;
    }

    @Override
    public Cluster asCluster() {
        throw new GraknClientException(ILLEGAL_CAST, className(GraknClient.Cluster.class));
    }

    public ManagedChannel channel() {
        return channel;
    }

    GraknStub.Core stub() {
        return stub;
    }

    RequestTransmitter transmitter() {
        return transmitter;
    }

    void removeSession(CoreSession session) {
        sessions.remove(session.id());
    }

    @Override
    public void close() {
        try {
            sessions.values().forEach(CoreSession::close);
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
            transmitter.close();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
