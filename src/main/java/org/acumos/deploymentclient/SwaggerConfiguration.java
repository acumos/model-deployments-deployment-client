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

import org.acumos.deploymentclient.controller.DeploymentController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {
  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select() //
        .apis(
            RequestHandlerSelectors.basePackage(
                DeploymentController.class.getPackage().getName())) //
        .paths(PathSelectors.any()) //
        .build() //
        .apiInfo(apiInfo());
  }

  private ApiInfo apiInfo() {
    final String version =
        DeploymentClientApplication.class.getPackage().getImplementationVersion();
    ApiInfo apiInfo =
        new ApiInfo(
            "Acumos deployment client REST API",
            "Operations for deployment client.", // description
            version == null ? "version not available" : version, // version
            "Terms of service", // TOS
            new Contact(
                "Acumos Team", // name
                "https://acumos.org/to-be-determined", // URL
                "contact@acumos.org"), // email
            "Apache 2.0", // License
            "https://www.apache.org/licenses/LICENSE-2.0"); // License URL
    return apiInfo;
  }
}
