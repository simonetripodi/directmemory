package org.apache.directmemory.server.commons;
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


import org.apache.directmemory.serialization.Serializer;
import org.apache.directmemory.serialization.SerializerFactory;
import org.apache.directmemory.test.Wine;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Olivier Lamy
 */
public class DirectMemoryCacheWriterTest
{
    private Logger log = LoggerFactory.getLogger( getClass() );

    @Test
    public void writeRequestWithString()
        throws Exception
    {
        DirectMemoryCacheRequest dmRq =
            new DirectMemoryCacheRequest().setKey( "101" ).setUpdate( true ).setExpiresIn( 123 ).setCacheContent(
                "foo bar".getBytes() );
        String rq = DirectMemoryCacheWriter.instance().generateJsonRequest( dmRq );

        dmRq = DirectMemoryCacheParser.instance().buildRequest( new ByteArrayInputStream( rq.getBytes() ) );
        assertNotNull( dmRq );

        assertEquals( "101", dmRq.getKey() );
        assertEquals( true, dmRq.isUpdate() );
        assertEquals( 123, dmRq.getExpiresIn() );
        assertEquals( "foo bar", new String( dmRq.getCacheContent() ) );
    }

    @Test
    public void writeRequestWithObject()
        throws Exception
    {
        Wine wine = new Wine();
        wine.setName( "Bordeaux" );

        Serializer serializer = SerializerFactory.createNewSerializer();

        DirectMemoryCacheRequest dmRq =
            new DirectMemoryCacheRequest().setKey( "101" ).setUpdate( true ).setExpiresIn( 123 ).setObject(
                wine ).setSerializer( serializer );
        String rq = DirectMemoryCacheWriter.instance().generateJsonRequest( dmRq );
        log.info( "rq:" + rq );

        dmRq = DirectMemoryCacheParser.instance().buildRequest( new ByteArrayInputStream( rq.getBytes() ) );
        assertNotNull( dmRq );

        assertEquals( "101", dmRq.getKey() );
        assertEquals( true, dmRq.isUpdate() );
        assertEquals( 123, dmRq.getExpiresIn() );

        wine = serializer.deserialize( dmRq.getCacheContent(), Wine.class );
        assertEquals( "Bordeaux", wine.getName() );

    }

    @Test
    public void writeResponseWithString()
        throws Exception
    {
        DirectMemoryCacheResponse rs =
            new DirectMemoryCacheResponse().setKey( "101" ).setFound( true ).setUpdated( false ).setCacheContent(
                "foo bar".getBytes() );
        String jsonRs = DirectMemoryCacheWriter.instance().generateJsonResponse( rs );

        rs = DirectMemoryCacheParser.instance().buildResponse( new ByteArrayInputStream( jsonRs.getBytes() ) );
        assertNotNull( rs );

        log.info( "jsonRs:" + jsonRs );

        assertEquals( "101", rs.getKey() );
        assertEquals( true, rs.isFound() );
        assertEquals( false, rs.isUpdated() );
        assertEquals( "foo bar", new String( rs.getCacheContent() ) );
    }
}
