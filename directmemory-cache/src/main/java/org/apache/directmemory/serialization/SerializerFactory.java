package org.apache.directmemory.serialization;

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

import java.util.Iterator;

import static java.util.ServiceLoader.load;

public final class SerializerFactory
{

    public static Serializer createNewSerializer()
    {
        Iterator<Serializer> serializers = load( Serializer.class ).iterator();

        // iterate over all found services
        while ( serializers.hasNext() )
        {
            // try getting the current service and return
            try
            {
                return serializers.next();
            }
            catch ( Throwable t )
            {
                // just ignore, skip and try getting the next
            }
        }

        return new StandardSerializer();
    }

    public static <S extends Serializer> S createNewSerializer( Class<S> serializer )
    {
        Iterator<Serializer> serializers = load( Serializer.class ).iterator();

        // iterate over all found services
        while ( serializers.hasNext() )
        {
            // try getting the current service and return
            try
            {
                Serializer next = serializers.next();
                if ( serializer.isInstance( next ) )
                {
                    return serializer.cast( next );
                }
            }
            catch ( Throwable t )
            {
                // just ignore, skip and try getting the next
            }
        }

        return null;
    }

    public static Serializer createNewSerializer( String serializerClassName )
    {
        return createNewSerializer( serializerClassName, SerializerFactory.class.getClassLoader() );
    }

    public static Serializer createNewSerializer( String serializerClassName, ClassLoader classLoader )
    {
        Class<?> anonSerializerClass;
        try
        {
            anonSerializerClass = classLoader.loadClass( serializerClassName );
        }
        catch ( ClassNotFoundException e )
        {
            return null;
        }

        if ( Serializer.class.isAssignableFrom( anonSerializerClass ) )
        {
            @SuppressWarnings( "unchecked" ) // the assignment is guarded by the previous check
            Class<? extends Serializer> serializerClass = (Class<? extends Serializer>) anonSerializerClass;

            return createNewSerializer( serializerClass );
        }

        return null;
    }

    /**
     * Hidden constructor, this class cannot be instantiated
     */
    private SerializerFactory()
    {
        // do nothing
    }

}
