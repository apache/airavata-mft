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
