/*
 *  Copyright 2018 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.gcp.autoconfigure.datastore;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gcp.data.datastore.core.DatastoreOperations;
import org.springframework.cloud.gcp.data.datastore.core.DatastoreTemplate;
import org.springframework.cloud.gcp.data.datastore.core.convert.*;
import org.springframework.cloud.gcp.data.datastore.core.mapping.DatastoreMappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Provides Spring Data classes to use with Cloud Datastore.
 *
 * @author Chengyuan Zhao
 * @since 1.1
 */
@Configuration
@ConditionalOnProperty(value = "spring.cloud.gcp.datastore.enabled", matchIfMissing = true)
@ConditionalOnClass({DatastoreOperations.class, Datastore.class})
@EnableConfigurationProperties(GcpDatastoreProperties.class)
public class GcpDatastoreAutoConfiguration {


    private final String namespace;


    GcpDatastoreAutoConfiguration(GcpDatastoreProperties gcpDatastoreProperties) throws IOException {
        this.namespace = gcpDatastoreProperties.getNamespace();
    }

    @Bean
    @ConditionalOnMissingBean
    public Datastore datastore() {
		DatastoreOptions.Builder builder = DatastoreOptions.newBuilder();
//				.setProjectId(this.projectId)
//				.setHeaderProvider(new UsageTrackingHeaderProvider(this.getClass()))
//				.setCredentials(this.credentials);
		if (this.namespace != null) {
			builder.setNamespace(this.namespace);
		}
        return builder.build().getService();
    }

    @Bean
    @ConditionalOnMissingBean
    public DatastoreCustomConversions datastoreCustomConversions() {
        return new DatastoreCustomConversions();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReadWriteConversions datastoreReadWriteConversions(DatastoreCustomConversions customConversions,
                                                              ObjectToKeyFactory objectToKeyFactory) {
        return new TwoStepsConversions(customConversions, objectToKeyFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public DatastoreMappingContext datastoreMappingContext() {
        return new DatastoreMappingContext();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectToKeyFactory objectToKeyFactory(Datastore datastore) {
        return new  DatastoreServiceObjectToKeyFactory(datastore);
    }

    @Bean
    @ConditionalOnMissingBean
    public DatastoreEntityConverter datastoreEntityConverter(DatastoreMappingContext datastoreMappingContext,
                                                             ReadWriteConversions conversions) {
        return new DefaultDatastoreEntityConverter(datastoreMappingContext, conversions);
    }

    @Bean
    @ConditionalOnMissingBean
    public DatastoreTemplate datastoreTemplate(Datastore datastore, DatastoreMappingContext datastoreMappingContext,
                                               DatastoreEntityConverter datastoreEntityConverter, ObjectToKeyFactory objectToKeyFactory) {
        return new DatastoreTemplate(datastore, datastoreEntityConverter, datastoreMappingContext, objectToKeyFactory);
    }
}
