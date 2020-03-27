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

public class DeploymentDetailBean implements Serializable{
	
	
	private static final long serialVersionUID = -4596110210757617811L;
	
	private String jenkinUrl;
	 private String deploymentUrl;
	 private String nodePortUrl;
	 private Boolean continuousTrainingEnabled;

	 public String getJenkinUrl() {
			return jenkinUrl;
	 }
	 public void setJenkinUrl(String jenkinUrl) {
		this.jenkinUrl = jenkinUrl;
	 }
	 public String getDeploymentUrl() {
		return deploymentUrl;
	 }
	 public void setDeploymentUrl(String deploymentUrl) {
		this.deploymentUrl = deploymentUrl;
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
