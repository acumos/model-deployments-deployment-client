/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * ===================================================================================
 * This Acumos software file is distributed by AT&T and Tech Mahindra
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END=========================================================
 */

package org.acumos.deploymentclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.acumos.*"})
public class DeploymentClientApplication {
  public static final String CONFIG_ENV_VAR_NAME = "SPRING_APPLICATION_JSON";

  public static void main(String[] args) throws Exception {
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
