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

/**
 * @author Thomas Risberg
 */
public class App {

	private String name;

	private String type;

	private List<Property> properties;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProperty(String name) {
		for (Property param : properties) {
			if (param.getName().equals(name)) {
				return param.getValue();
			}
		}
		return "";
	}

	public List<Property> getProperties() {
		return properties;
	}

	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return "App{" +
				"name='" + name + '\'' +
				", type='" + type + '\'' +
				", properties=" + properties +
				'}';
	}
}
