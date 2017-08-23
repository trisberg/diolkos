/*
 * Copyright 2017 the original author or authors.
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

package io.diolkos.stream.controller;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.diolkos.core.stream.StreamResource;
import io.diolkos.core.resource.ResourceEvent;
import io.diolkos.core.resource.ResourceWatcher;
import io.diolkos.core.stream.StreamResourceEvent;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * @author Mark Fisher
 * @author Thomas Risberg
 */
@Configuration
@EnableBinding
@EnableConfigurationProperties(StreamControllerProperties.class)
public class StreamControllerConfiguration {

	@Bean
	public ResourceWatcher<ResourceEvent<StreamResource>> watcher(StreamResourceEventHandler topicCreatingHandler) {
		return new ResourceWatcher(StreamResourceEvent.class, "streams", topicCreatingHandler);
	}

	@Bean
	public StreamResourceEventHandler topicCreatingHandler(KubernetesClient kubernetesClient) {
		return new StreamResourceEventHandler(kubernetesClient);
	}

	@Bean
	public KubernetesClient kubernetesClient() {
		return new DefaultKubernetesClient().inNamespace("default");
	}

}
