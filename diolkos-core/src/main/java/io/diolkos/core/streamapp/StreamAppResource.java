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

package io.diolkos.core.streamapp;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.diolkos.core.resource.Resource;


/**
 * @author Mark Fisher
 */
// todo: add these to the model (can be empty)
@JsonIgnoreProperties({ "status", "message" })
public class StreamAppResource extends Resource<StreamAppResource.StreamAppSpec> {

	public class StreamAppSpec {

		private String image;

		@JsonProperty("app-properties")
		private List<Map<String, Object>> appProperties;

		public String getImage() {
			return image;
		}

		public void setImage(String image) {
			this.image = image;
		}

		public List<Map<String, Object>> getAppProperties() {
			return appProperties;
		}

		public void setAppProperties(List<Map<String, Object>> appProperties) {
			this.appProperties = appProperties;
		}
	}
}
