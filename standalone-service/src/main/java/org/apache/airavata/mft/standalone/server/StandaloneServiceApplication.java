/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.mft.standalone.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan(basePackages = {"org.apache.airavata.mft.resource.*", "org.apache.airavata.mft.secret.*",
		"org.apache.airavata.mft.api.*", "org.apache.airavata.mft.api.handler", "org.apache.airavata.mft.controller"})
@EntityScan(basePackages = {"org.apache.airavata.mft.resource.server.backend.sql.entity"})
@EnableJpaRepositories(basePackages = {"org.apache.airavata.mft.resource.server.backend.sql.repository",
		"org.apache.airavata.mft.secret.server.backend.sql.repository"})
@PropertySource(value = "classpath:api-service-application.properties")
@PropertySource(value = "classpath:resource-service-application.properties")
@PropertySource(value = "classpath:secret-service-application.properties")
@PropertySource(value = "classpath:agent-application.properties")
@PropertySource(value = "classpath:controller-application.properties")
@Import(org.apache.airavata.mft.api.AppConfig.class)
public class StandaloneServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(StandaloneServiceApplication.class, args);
	}

}
