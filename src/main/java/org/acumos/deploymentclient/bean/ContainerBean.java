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
package org.acumos.deploymentclient.bean;

import java.io.Serializable;

public class ContainerBean implements Serializable {
 
  private static final long serialVersionUID = 3047976826678173113L;
  private String containerName;
  private String nodeType;
  private String image;
  private String protoUriPath;
  private String protoUriDetails;

  public String getContainerName() {
    return containerName;
  }

  public void setContainerName(String containerName) {
    this.containerName = containerName;
  }

  public String getNodeType() {
    return nodeType;
  }

  public void setNodeType(String nodeType) {
    this.nodeType = nodeType;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public String getProtoUriPath() {
    return protoUriPath;
  }

  public void setProtoUriPath(String protoUriPath) {
    this.protoUriPath = protoUriPath;
  }

  public String getProtoUriDetails() {
    return protoUriDetails;
  }

  public void setProtoUriDetails(String protoUriDetails) {
    this.protoUriDetails = protoUriDetails;
  }
}
