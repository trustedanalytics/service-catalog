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
package org.trustedanalytics.servicecatalog.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.trustedanalytics.servicecatalog.service.model.ServiceInstanceMetadata;

public class StorageConfig {

    private StorageConfig(){
    };

    @Configuration
    public static class RedisStorageConfig {

        @Bean
        public RedisOperations<String, ServiceInstanceMetadata> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
            return CommonConfiguration.redisTemplate(redisConnectionFactory,
                    new JacksonJsonRedisSerializer<ServiceInstanceMetadata>(ServiceInstanceMetadata.class));
        }

        @Bean
        KeyValueStore<ServiceInstanceMetadata> redisServiceInstancesMetadataStore(RedisOperations<String, ServiceInstanceMetadata> redisTemplate) {
            return new RedisStore<>(redisTemplate, "service-instances-metadata");
        }

        @Bean
        protected ServiceInstanceRegistry serviceInstanceRegistry(KeyValueStore<ServiceInstanceMetadata> redisCreatedApplicationsStore) {
            return new ServiceInstanceRegistry(redisCreatedApplicationsStore);
        }

    }

    private static class CommonConfiguration {
        private CommonConfiguration() {
        }

        private static <T> RedisOperations<String, T>
        redisTemplate(RedisConnectionFactory redisConnectionFactory, RedisSerializer<T> valueSerializer) {
            RedisTemplate<String, T> template = new RedisTemplate<String, T>();
            template.setConnectionFactory(redisConnectionFactory);

            RedisSerializer<String> stringSerializer = new StringRedisSerializer();
            template.setKeySerializer(stringSerializer);
            template.setValueSerializer(valueSerializer);
            template.setHashKeySerializer(stringSerializer);
            template.setHashValueSerializer(valueSerializer);

            return template;
        }
    }
}
