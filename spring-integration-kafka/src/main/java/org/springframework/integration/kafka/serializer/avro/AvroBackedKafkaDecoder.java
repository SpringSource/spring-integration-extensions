/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.kafka.serializer.avro;


import kafka.serializer.Decoder;
import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Soby Chacko
 */
public class AvroBackedKafkaDecoder<T> implements Decoder<T> {
    private static final Log LOG = LogFactory.getLog(AvroBackedKafkaDecoder.class);

    private final Class clazz;

    public AvroBackedKafkaDecoder(final Class clazz) {
        this.clazz = clazz;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T fromBytes(final byte[] bytes) {
        final Schema schema = ReflectData.get().getSchema(clazz);
        final AvroSerializer avroSerializer = new AvroSerializer();

        try {
            return (T) avroSerializer.deserialize(bytes, schema);
        } catch (IOException e) {
            LOG.error("Failed to decode byte array for schema: " + schema.getFullName(), e);
        }

        return null;
    }
}

