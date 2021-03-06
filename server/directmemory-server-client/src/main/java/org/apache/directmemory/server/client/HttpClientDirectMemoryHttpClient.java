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
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Olivier Lamy
 */
public class HttpClientDirectMemoryHttpClient
    extends AbstractDirectMemoryHttpClient
    implements DirectMemoryHttpClient
{
    private Logger log = LoggerFactory.getLogger( getClass() );

    public static HttpClientDirectMemoryHttpClient instance( DirectMemoryServerClientConfiguration configuration )
    {
        return new HttpClientDirectMemoryHttpClient( configuration );
    }

    private HttpClient httpClient;


    public HttpClientDirectMemoryHttpClient( DirectMemoryServerClientConfiguration configuration )
    {
        super( configuration );
    }

    @Override
    public void configure( DirectMemoryServerClientConfiguration configuration )
        throws DirectMemoryCacheException
    {
        this.configuration = configuration;
        ThreadSafeClientConnManager threadSafeClientConnManager = new ThreadSafeClientConnManager();
        threadSafeClientConnManager.setDefaultMaxPerRoute( configuration.getMaxConcurentConnection() );
        this.httpClient = new DefaultHttpClient( threadSafeClientConnManager );
    }

    @Override
    public void put( DirectMemoryCacheRequest request )
        throws DirectMemoryCacheException
    {
        String uri = buildRequestWithKey( request );
        if ( log.isDebugEnabled() )
        {
            log.debug( "put request to: {}", uri.toString() );
        }
        HttpPut httpPut = new HttpPut( uri.toString() );
        httpPut.addHeader( "Content-Type", getRequestContentType( request ) );
        httpPut.setEntity( new ByteArrayEntity( getPutContent( request ) ) );

        try
        {
            HttpResponse response = httpClient.execute( httpPut );
            StatusLine statusLine = response.getStatusLine();
            if ( statusLine.getStatusCode() != 200 )
            {
                throw new DirectMemoryCacheException(
                    "put cache content return http code:'" + statusLine.getStatusCode() + "', reasonPhrase:"
                        + statusLine.getReasonPhrase() );
            }
        }
        catch ( IOException e )
        {
            throw new DirectMemoryCacheException( e.getMessage(), e );
        }
    }

    @Override
    public Future<Void> asyncPut( final DirectMemoryCacheRequest request )
        throws DirectMemoryCacheException
    {
        return Executors.newSingleThreadExecutor().submit( new Callable<Void>()
        {
            @Override
            public Void call()
                throws Exception
            {
                put( request );
                return null;
            }
        } );
    }

    @Override
    public DirectMemoryCacheResponse get( DirectMemoryCacheRequest request )
        throws DirectMemoryCacheException
    {
        String uri = buildRequestWithKey( request );
        if ( log.isDebugEnabled() )
        {
            log.debug( "get request to: {}", uri.toString() );
        }

        HttpGet httpGet = new HttpGet( uri );

        httpGet.addHeader( "Accept", getAcceptContentType( request ) );

        try
        {
            HttpResponse httpResponse = this.httpClient.execute( httpGet );

            // handle no content response
            StatusLine statusLine = httpResponse.getStatusLine();
            if ( statusLine.getStatusCode() == 204 )
            {
                return new DirectMemoryCacheResponse().setFound( false );
            }

            if ( request.isDeleteRequest() )
            {
                return new DirectMemoryCacheResponse().setFound( true ).setDeleted( true );
            }

            return buildResponse( httpResponse.getEntity().getContent() ).setFound( true );
        }
        catch ( IOException e )
        {
            throw new DirectMemoryCacheException( e.getMessage(), e );
        }
    }

    @Override
    public Future<DirectMemoryCacheResponse> asyncGet( final DirectMemoryCacheRequest request )
        throws DirectMemoryCacheException
    {
        return Executors.newSingleThreadExecutor().submit( new Callable<DirectMemoryCacheResponse>()
        {
            @Override
            public DirectMemoryCacheResponse call()
                throws Exception
            {
                return get( request );
            }
        } );
    }

    @Override
    public DirectMemoryCacheResponse delete( DirectMemoryCacheRequest request )
        throws DirectMemoryCacheException
    {
        String uri = buildRequestWithKey( request );
        if ( log.isDebugEnabled() )
        {
            log.debug( "get request to: {}", uri.toString() );
        }

        HttpDelete httpDelete = new HttpDelete( uri );

        try
        {
            HttpResponse httpResponse = this.httpClient.execute( httpDelete );

            // handle no content response
            StatusLine statusLine = httpResponse.getStatusLine();
            if ( statusLine.getStatusCode() == 204 )
            {
                return new DirectMemoryCacheResponse().setFound( false ).setDeleted( false );
            }

            return new DirectMemoryCacheResponse().setFound( true ).setDeleted( true );

        }
        catch ( IOException e )
        {
            throw new DirectMemoryCacheException( e.getMessage(), e );
        }
    }

    @Override
    public Future<DirectMemoryCacheResponse> asyncDelete( final DirectMemoryCacheRequest request )
        throws DirectMemoryCacheException
    {
        return Executors.newSingleThreadExecutor().submit( new Callable<DirectMemoryCacheResponse>()
        {
            @Override
            public DirectMemoryCacheResponse call()
                throws Exception
            {
                return delete( request );
            }
        } );
    }
}
