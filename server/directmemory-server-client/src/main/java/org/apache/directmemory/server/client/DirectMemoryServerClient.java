package org.apache.directmemory.server.client;
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

import org.apache.directmemory.server.commons.DirectMemoryCacheException;
import org.apache.directmemory.server.commons.DirectMemoryCacheRequest;
import org.apache.directmemory.server.commons.DirectMemoryCacheResponse;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * @author Olivier Lamy
 */
public interface DirectMemoryServerClient
{

    DirectMemoryCacheResponse retrieve( DirectMemoryCacheRequest directMemoryCacheRequest )
        throws DirectMemoryCacheException, IOException, ClassNotFoundException, InstantiationException,
        IllegalAccessException;

    Future<DirectMemoryCacheResponse> asyncRetrieve( DirectMemoryCacheRequest directMemoryCacheRequest )
        throws DirectMemoryCacheException;

    void put( DirectMemoryCacheRequest directMemoryCacheRequest )
        throws DirectMemoryCacheException;

    Future<Void> asyncPut( DirectMemoryCacheRequest directMemoryCacheRequest )
        throws DirectMemoryCacheException;

    DirectMemoryCacheResponse delete( DirectMemoryCacheRequest directMemoryCacheRequest )
        throws DirectMemoryCacheException;

    Future<DirectMemoryCacheResponse> asyncDelete( DirectMemoryCacheRequest directMemoryCacheRequest )
        throws DirectMemoryCacheException;
}
