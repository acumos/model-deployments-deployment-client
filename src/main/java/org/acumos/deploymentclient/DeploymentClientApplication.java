package org.acumos.deploymentclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@ComponentScan(basePackages = {"org.acumos.deploymentclient.*"})
public class DeploymentClientApplication {
	public static final String CONFIG_ENV_VAR_NAME = "SPRING_APPLICATION_JSON";
	public static void main(String[] args)throws Exception {
		final String springApplicationJson = System.getenv(CONFIG_ENV_VAR_NAME);
		if (springApplicationJson != null && springApplicationJson.contains("{")) {
			final ObjectMapper mapper = new ObjectMapper();
			// ensure it's valid
			mapper.readTree(springApplicationJson);
			// logger.info("main: successfully parsed configuration from environment {}",
			// CONFIG_ENV_VAR_NAME);
		} else {
			// logger.warn("main: no configuration found in environment {}",
			// CONFIG_ENV_VAR_NAME);
		}
		SpringApplication.run(DeploymentClientApplication.class, args);
	}

}
