
/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2020 Nordix Foundation
 * ===================================================================================
 * This Acumos software file is distributed by Nordix Foundation
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
package org.acumos.deploymentclient.bean;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"schema", "name", "runtime", "methods"})
public class MLModel {

  /**
   * Identifies the specific model schema used in the json instance, e.g.
   * 'acumos.schema.model:0.5.0'. (Required)
   * 
   */
  @JsonProperty("schema")
  @JsonPropertyDescription("Identifies the specific model schema used in the json instance, e.g. 'acumos.schema.model:0.5.0'.")
  private String schema;
  /**
   * The name of the model being on-boarded to Acumos, e.g. 'my-model'. (Required)
   * 
   */
  @JsonProperty("name")
  @JsonPropertyDescription("The name of the model being on-boarded to Acumos, e.g. 'my-model'.")
  private String name;
  /**
   * Describes the runtime and dependencies required by the model to operate. (Required)
   * 
   */
  @JsonProperty("runtime")
  @JsonPropertyDescription("Describes the runtime and dependencies required by the model to operate.")
  private Object runtime;
  /**
   * The methods that an on-boarded model provides. Can be any number of uniquely named Function or
   * VoidFunction objects. (Required)
   * 
   */
  @JsonProperty("methods")
  @JsonPropertyDescription("The methods that an on-boarded model provides. Can be any number of uniquely named Function or VoidFunction objects.")
  private MLModelMethods methods;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  /**
   * Identifies the specific model schema used in the json instance, e.g.
   * 'acumos.schema.model:0.5.0'. (Required)
   * 
   */
  @JsonProperty("schema")
  public String getSchema() {
    return schema;
  }

  /**
   * Identifies the specific model schema used in the json instance, e.g.
   * 'acumos.schema.model:0.5.0'. (Required)
   * 
   */
  @JsonProperty("schema")
  public void setSchema(String schema) {
    this.schema = schema;
  }

  /**
   * The name of the model being on-boarded to Acumos, e.g. 'my-model'. (Required)
   * 
   */
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  /**
   * The name of the model being on-boarded to Acumos, e.g. 'my-model'. (Required)
   * 
   */
  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Describes the runtime and dependencies required by the model to operate. (Required)
   * 
   */
  @JsonProperty("runtime")
  public Object getRuntime() {
    return runtime;
  }

  /**
   * Describes the runtime and dependencies required by the model to operate. (Required)
   * 
   */
  @JsonProperty("runtime")
  public void setRuntime(Object runtime) {
    this.runtime = runtime;
  }

  /**
   * The methods that an on-boarded model provides. Can be any number of uniquely named Function or
   * VoidFunction objects. (Required)
   * 
   */
  @JsonProperty("methods")
  public MLModelMethods getMethods() {
    return methods;
  }

  /**
   * The methods that an on-boarded model provides. Can be any number of uniquely named Function or
   * VoidFunction objects. (Required)
   * 
   */
  @JsonProperty("methods")
  public void setMethods(MLModelMethods methods) {
    this.methods = methods;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}

