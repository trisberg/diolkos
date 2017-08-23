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

import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.diolkos.core.stream.App;
import io.diolkos.core.stream.Property;
import io.diolkos.core.stream.StreamResource;
import io.diolkos.core.streamapp.StreamAppResource;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.diolkos.core.resource.ResourceEventHandler;

import org.springframework.util.StringUtils;

/**
 * @author Mark Fisher
 * @author Thomas Risberg
 */
public class StreamResourceEventHandler implements ResourceEventHandler<StreamResource> {

	private static Log logger = LogFactory.getLog(StreamResourceEventHandler.class);

	private final KubernetesClient kubernetesClient;

	private final ObjectMapper objectMapper = new ObjectMapper();

	public StreamResourceEventHandler(KubernetesClient kubernetesClient) {
		this.kubernetesClient = kubernetesClient;
	}

	@Override
	public void resourceAdded(StreamResource resource) {
		String streamName = resource.getMetadata().get("name").toString();
		logger.info("ADD event for stream: " + streamName);
		logger.info("in namespace: " + kubernetesClient.getNamespace());
		for (App app : resource.getSpec().getApps()) {
			StreamAppResource r = getApp(app.getName() + "-" + app.getType());
			logger.info("with app " + app.getName() + "-" + app.getType() + " using image " + r.getSpec().getImage());
			createApp(streamName, app.getName() + "-" + app.getType(), r.getSpec(), streamName + ".data", app);
		}
	}

	@Override
	public void resourceDeleted(StreamResource resource) {
		String streamName = resource.getMetadata().get("name").toString();
		logger.info("DELETE event for stream: " + streamName);
		deleteStream(streamName);
	}

	private StreamAppResource getApp(String name) {
		// TODO: use this.handlers (local cache from a watch)
		OkHttpClient httpClient = HttpClientUtils.createHttpClient(kubernetesClient.getConfiguration());
		Response response = null;
		try {
			// TODO: replace this code
			URL url = new URL(kubernetesClient.getMasterUrl() + "apis/extensions.diolkos.io/v1/namespaces/default/streamapps/" + name);
			Request.Builder requestBuilder = new Request.Builder().get().url(url);
			response = httpClient.newCall(requestBuilder.build()).execute();
			BufferedSource source = response.body().source();
			while (!source.exhausted()) {
				String line = source.readUtf8LineStrict();
				if (!StringUtils.hasText(line)) {
					break;
				}
				try {
					return this.objectMapper.readValue(line, StreamAppResource.class);
				}
				catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
		catch (SocketTimeoutException e) {
			// reconnect
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				response.close();
			}
			catch (Exception e) {
				// ignore
			}
		}
		return null;
	}

	private void createApp(String streamName, String name, StreamAppResource.StreamAppSpec spec, String topic, App app) {

		/*
		RabbitMQ service:
		  helm install --name diolkos --set rabbitmqPassword=rabbit stable/rabbitmq
		 */

		Map<String, String> labelMap = new HashMap<>();
		labelMap.put("app", name);
		labelMap.put("group", streamName);
		labelMap.put("topic", topic);
		ServiceSpecBuilder svcBuilder = new ServiceSpecBuilder();
		ServicePort servicePort = new ServicePort();
		servicePort.setPort(8080);
		svcBuilder.withSelector(labelMap)
				.addNewPortLike(servicePort).endPort();

		kubernetesClient.services().inNamespace(kubernetesClient.getNamespace()).createNew()
				.withNewMetadata()
				  .withName(name)
				  .addToLabels(labelMap)
				.endMetadata()
				.withSpec(svcBuilder.build())
				.done();

		PodSpecBuilder podSpec = new PodSpecBuilder();

		List<EnvVar> envVars = new ArrayList<>();
		String appJson;
		if (name.contains("source")) {
			appJson = "{\"spring.cloud.stream.bindings.output.producer.requiredGroups\": \"" + streamName +
					"\", \"spring.cloud.stream.bindings.output.destination\": \"" + topic + "\"}";
		}
		else {
			appJson = "{\"spring.cloud.stream.bindings.input.group\": \"" + streamName +
					"\", \"spring.cloud.stream.bindings.input.destination\": \"" + topic + "\"}";
		}
		envVars.add(new EnvVar("SPRING_APPLICATION_JSON", appJson, null));
		envVars.add(new EnvVar("SPRING_CLOUD_CONFIG_ENABLED", "false", null));
		envVars.add(new EnvVar("ENDPOINTS_SHUTDOWN_ENABLED", "true", null));
		envVars.add(new EnvVar("SPRING_RABBITMQ_HOST", "${DIOLKOS_RABBITMQ_SERVICE_HOST}", null));
		envVars.add(new EnvVar("SPRING_RABBITMQ_PORT", "5672", null));
		envVars.add(new EnvVar("SPRING_RABBITMQ_USERNAME", "user", null));
		envVars.add(new EnvVar("SPRING_RABBITMQ_PASSWORD", "rabbit", null));

		List<String> args = new ArrayList<>();
		if (app.getProperties() != null) {
			for (Property prop : app.getProperties()) {
				String arg = "--" + prop.getName() + "=" + prop.getValue();
				args.add(arg);
			}
		}

		ContainerBuilder containerBuilder = new ContainerBuilder();
		containerBuilder.withName(name)
				.withImage(spec.getImage())
				.withEnv(envVars)
				.withArgs(args);
		containerBuilder.addNewPort()
				  .withContainerPort(8080)
				.endPort();
		podSpec.addToContainers(containerBuilder.build());

		Deployment deployment = new DeploymentBuilder()
				.withNewMetadata()
				  .withName(name)
				  .withLabels(labelMap)
				.endMetadata()
				.withNewSpec()
				  .withReplicas(1)
				  .withNewTemplate()
				    .withNewMetadata()
				      .withLabels(labelMap)
				    .endMetadata()
				    .withSpec(podSpec.build())
				  .endTemplate()
				.endSpec()
				.build();

		kubernetesClient.extensions().deployments().create(deployment);
	}

	private void deleteStream(String name) {
		kubernetesClient.services().withLabel("group=" + name).delete();
		kubernetesClient.extensions().deployments().withLabel("group=" + name).delete();
	}
}
