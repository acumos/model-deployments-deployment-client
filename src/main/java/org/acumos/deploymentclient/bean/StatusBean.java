/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * Modifications Copyright (C) 2020 Nordix Foundation.
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

public class StatusBean implements Serializable {


  private static final long serialVersionUID = 3513274641235816829L;
  
  private String status;
  private String reason;
  private String ingress;
  private String nodePortUrl;
  private Boolean continuousTrainingEnabled;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getReason() {
  	return reason;
  }

  public void setReason(String reason) {
  	this.reason = reason;
  }

  public String getIngress() {
  	return ingress;
  }

  public void setIngress(String ingress) {
  	this.ingress = ingress;
  }

  public String getNodePortUrl() {
    return nodePortUrl;
  }

  public void setNodePortUrl(String nodePortUrl) {
    this.nodePortUrl = nodePortUrl;
  }

  public Boolean getContinuousTrainingEnabled() {
    return continuousTrainingEnabled;
  }

  public void setContinuousTrainingEnabled(Boolean continuousTrainingEnabled) {
    this.continuousTrainingEnabled = continuousTrainingEnabled;
  }
}
