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

package io.diolkos.core.stream;

import java.util.List;

import io.diolkos.core.stream.StreamResource.StreamSpec;
import io.diolkos.core.resource.Resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Mark Fisher
 */
// todo: add these to the model (can be empty)
@JsonIgnoreProperties({ "status", "message" })
public class StreamResource extends Resource<StreamSpec> {

	public class StreamSpec {

		private String dsl;

		private List<App> apps;

		public String getDsl() {
			return dsl;
		}

		public void setDsl(String dsl) {
			this.dsl = dsl;
		}

		public List<App> getApps() {
			return apps;
		}

		public void setApps(List<App> apps) {
			this.apps = apps;
		}

		@Override
		public String toString() {
			return "StreamSpec [" + dsl + "] + Apps " + apps;
		}
	}
}
