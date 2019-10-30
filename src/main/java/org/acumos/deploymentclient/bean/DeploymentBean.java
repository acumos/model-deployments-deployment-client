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

import java.util.ArrayList;
import java.util.List;

public class DeploymentBean {

  private String bluePrintjson;
  private String probePort;
  private String bluePrintImage;
  private String bluePrintPort;
  private String dataBrokerTargetPort;
  private String probeTargetPort;
  private String incrementPort;
  private String bluePrintNodePort;
  private String dataBrokerNodePort;
  private String probeNodePort;
  private String dataBrokerModelPort;
  private String probeModelPort;
  private String nexusEndPointURL;
  private String probeSchemaPort;
  private String probeApiPort;
  private String probeExternalPort;
  private String probeImageName;
  private String folderPath;
  private String singleModelPort;
  private String singleNodePort;
  private String singleTargetPort;
  private String taskId;
  private String solutionId;
  private String revisionId;
  private String envId;
  private String userId;
  private String trackingId;
  private String datasource;
  private String dataUserName;
  private String dataPd;
  private String jenkinUrl;
  private String jenkinUserName;
  private String jenkinPassword;
  private String jenkinJobSimple;
  private String jenkinJobComposite;
  private String jenkinJobNifi;
  private String logstashHost;
  private String logstashIP;
  private String logstashPort;

  private String nexusUrl;
  private String nexusUserName;
  private String nexusPd;
  private String nginxImageName;

  private String dataBrokerJson;
  private String dockerInfoJson;
  private String solutionYml;

  /*private String dockerProxyHost;
  private String dockerProxyPort;*/
  private String mlTargetPort;
  private String templateYmlDirectory;

  private String acumosRegistryName;
  private String acumosRegistryUser;
  private String acumosRegistryPd;
  private String deploymentClientApiBaseUrl;
  private String solutionName;
  private List<ArrayList> kubernetesClusterList;

  public List<ArrayList> getKubernetesClusterList() {
    return kubernetesClusterList;
  }

  public void setKubernetesClusterList(List<ArrayList> kubernetesClusterList) {
    this.kubernetesClusterList = kubernetesClusterList;
  }

  public String getSolutionName() {
    return solutionName;
  }

  public void setSolutionName(String solutionName) {
    this.solutionName = solutionName;
  }

  public String getDeploymentClientApiBaseUrl() {
    return deploymentClientApiBaseUrl;
  }

  public void setDeploymentClientApiBaseUrl(String deploymentClientApiBaseUrl) {
    this.deploymentClientApiBaseUrl = deploymentClientApiBaseUrl;
  }

  public String getLogstashIP() {
    return logstashIP;
  }

  public void setLogstashIP(String logstashIP) {
    this.logstashIP = logstashIP;
  }

  public String getAcumosRegistryName() {
    return acumosRegistryName;
  }

  public void setAcumosRegistryName(String acumosRegistryName) {
    this.acumosRegistryName = acumosRegistryName;
  }

  public String getAcumosRegistryUser() {
    return acumosRegistryUser;
  }

  public void setAcumosRegistryUser(String acumosRegistryUser) {
    this.acumosRegistryUser = acumosRegistryUser;
  }

  public String getAcumosRegistryPd() {
    return acumosRegistryPd;
  }

  public void setAcumosRegistryPd(String acumosRegistryPd) {
    this.acumosRegistryPd = acumosRegistryPd;
  }

  public String getTemplateYmlDirectory() {
    return templateYmlDirectory;
  }

  public void setTemplateYmlDirectory(String templateYmlDirectory) {
    this.templateYmlDirectory = templateYmlDirectory;
  }

  public String getLogstashHost() {
    return logstashHost;
  }

  public void setLogstashHost(String logstashHost) {
    this.logstashHost = logstashHost;
  }

  public String getLogstashPort() {
    return logstashPort;
  }

  public void setLogstashPort(String logstashPort) {
    this.logstashPort = logstashPort;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getJenkinJobSimple() {
    return jenkinJobSimple;
  }

  public void setJenkinJobSimple(String jenkinJobSimple) {
    this.jenkinJobSimple = jenkinJobSimple;
  }

  public String getJenkinJobComposite() {
    return jenkinJobComposite;
  }

  public void setJenkinJobComposite(String jenkinJobComposite) {
    this.jenkinJobComposite = jenkinJobComposite;
  }

  public String getJenkinJobNifi() {
    return jenkinJobNifi;
  }

  public void setJenkinJobNifi(String jenkinJobNifi) {
    this.jenkinJobNifi = jenkinJobNifi;
  }

  public String getJenkinUrl() {
    return jenkinUrl;
  }

  public void setJenkinUrl(String jenkinUrl) {
    this.jenkinUrl = jenkinUrl;
  }

  public String getJenkinUserName() {
    return jenkinUserName;
  }

  public void setJenkinUserName(String jenkinUserName) {
    this.jenkinUserName = jenkinUserName;
  }

  public String getJenkinPassword() {
    return jenkinPassword;
  }

  public void setJenkinPassword(String jenkinPassword) {
    this.jenkinPassword = jenkinPassword;
  }

  public String getSolutionId() {
    return solutionId;
  }

  public void setSolutionId(String solutionId) {
    this.solutionId = solutionId;
  }

  public String getRevisionId() {
    return revisionId;
  }

  public void setRevisionId(String revisionId) {
    this.revisionId = revisionId;
  }

  public String getEnvId() {
    return envId;
  }

  public void setEnvId(String envId) {
    this.envId = envId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getTrackingId() {
    return trackingId;
  }

  public void setTrackingId(String trackingId) {
    this.trackingId = trackingId;
  }

  public String getDatasource() {
    return datasource;
  }

  public void setDatasource(String datasource) {
    this.datasource = datasource;
  }

  public String getDataUserName() {
    return dataUserName;
  }

  public void setDataUserName(String dataUserName) {
    this.dataUserName = dataUserName;
  }

  public String getDataPd() {
    return dataPd;
  }

  public void setDataPd(String dataPd) {
    this.dataPd = dataPd;
  }

  public String getSingleModelPort() {
    return singleModelPort;
  }

  public void setSingleModelPort(String singleModelPort) {
    this.singleModelPort = singleModelPort;
  }

  public String getSingleNodePort() {
    return singleNodePort;
  }

  public void setSingleNodePort(String singleNodePort) {
    this.singleNodePort = singleNodePort;
  }

  public String getSingleTargetPort() {
    return singleTargetPort;
  }

  public void setSingleTargetPort(String singleTargetPort) {
    this.singleTargetPort = singleTargetPort;
  }

  public String getProbeImageName() {
    return probeImageName;
  }

  public void setProbeImageName(String probeImageName) {
    this.probeImageName = probeImageName;
  }

  public String getFolderPath() {
    return folderPath;
  }

  public void setFolderPath(String folderPath) {
    this.folderPath = folderPath;
  }

  public String getNginxImageName() {
    return nginxImageName;
  }

  public void setNginxImageName(String nginxImageName) {
    this.nginxImageName = nginxImageName;
  }

  public String getNexusEndPointURL() {
    return nexusEndPointURL;
  }

  public void setNexusEndPointURL(String nexusEndPointURL) {
    this.nexusEndPointURL = nexusEndPointURL;
  }

  public String getProbeSchemaPort() {
    return probeSchemaPort;
  }

  public void setProbeSchemaPort(String probeSchemaPort) {
    this.probeSchemaPort = probeSchemaPort;
  }

  public String getProbeApiPort() {
    return probeApiPort;
  }

  public void setProbeApiPort(String probeApiPort) {
    this.probeApiPort = probeApiPort;
  }

  public String getProbeExternalPort() {
    return probeExternalPort;
  }

  public void setProbeExternalPort(String probeExternalPort) {
    this.probeExternalPort = probeExternalPort;
  }
  /*public String getDockerProxyHost() {
  	return dockerProxyHost;
  }
  public void setDockerProxyHost(String dockerProxyHost) {
  	this.dockerProxyHost = dockerProxyHost;
  }
  public String getDockerProxyPort() {
  	return dockerProxyPort;
  }
  public void setDockerProxyPort(String dockerProxyPort) {
  	this.dockerProxyPort = dockerProxyPort;
  }*/
  public String getIncrementPort() {
    return incrementPort;
  }

  public void setIncrementPort(String incrementPort) {
    this.incrementPort = incrementPort;
  }

  public String getBluePrintNodePort() {
    return bluePrintNodePort;
  }

  public void setBluePrintNodePort(String bluePrintNodePort) {
    this.bluePrintNodePort = bluePrintNodePort;
  }

  public String getDataBrokerNodePort() {
    return dataBrokerNodePort;
  }

  public void setDataBrokerNodePort(String dataBrokerNodePort) {
    this.dataBrokerNodePort = dataBrokerNodePort;
  }

  public String getProbeNodePort() {
    return probeNodePort;
  }

  public void setProbeNodePort(String probeNodePort) {
    this.probeNodePort = probeNodePort;
  }

  public String getDataBrokerModelPort() {
    return dataBrokerModelPort;
  }

  public void setDataBrokerModelPort(String dataBrokerModelPort) {
    this.dataBrokerModelPort = dataBrokerModelPort;
  }

  public String getProbeModelPort() {
    return probeModelPort;
  }

  public void setProbeModelPort(String probeModelPort) {
    this.probeModelPort = probeModelPort;
  }

  public String getMlTargetPort() {
    return mlTargetPort;
  }

  public void setMlTargetPort(String mlTargetPort) {
    this.mlTargetPort = mlTargetPort;
  }

  public String getProbeTargetPort() {
    return probeTargetPort;
  }

  public void setProbeTargetPort(String probeTargetPort) {
    this.probeTargetPort = probeTargetPort;
  }

  private List<ContainerBean> containerBeanList;

  public String getDataBrokerTargetPort() {
    return dataBrokerTargetPort;
  }

  public void setDataBrokerTargetPort(String dataBrokerTargetPort) {
    this.dataBrokerTargetPort = dataBrokerTargetPort;
  }

  public String getProbePort() {
    return probePort;
  }

  public void setProbePort(String probePort) {
    this.probePort = probePort;
  }

  public String getBluePrintImage() {
    return bluePrintImage;
  }

  public void setBluePrintImage(String bluePrintImage) {
    this.bluePrintImage = bluePrintImage;
  }

  public String getBluePrintPort() {
    return bluePrintPort;
  }

  public void setBluePrintPort(String bluePrintPort) {
    this.bluePrintPort = bluePrintPort;
  }

  public List<ContainerBean> getContainerBeanList() {
    return containerBeanList;
  }

  public void setContainerBeanList(List<ContainerBean> containerBeanList) {
    this.containerBeanList = containerBeanList;
  }

  public String getBluePrintjson() {
    return bluePrintjson;
  }

  public void setBluePrintjson(String bluePrintjson) {
    this.bluePrintjson = bluePrintjson;
  }

  public String getNexusUrl() {
    return nexusUrl;
  }

  public void setNexusUrl(String nexusUrl) {
    this.nexusUrl = nexusUrl;
  }

  public String getNexusUserName() {
    return nexusUserName;
  }

  public void setNexusUserName(String nexusUserName) {
    this.nexusUserName = nexusUserName;
  }

  public String getNexusPd() {
    return nexusPd;
  }

  public void setNexusPd(String nexusPd) {
    this.nexusPd = nexusPd;
  }

  public String getDataBrokerJson() {
    return dataBrokerJson;
  }

  public void setDataBrokerJson(String dataBrokerJson) {
    this.dataBrokerJson = dataBrokerJson;
  }

  public String getDockerInfoJson() {
    return dockerInfoJson;
  }

  public void setDockerInfoJson(String dockerInfoJson) {
    this.dockerInfoJson = dockerInfoJson;
  }

  public String getSolutionYml() {
    return solutionYml;
  }

  public void setSolutionYml(String solutionYml) {
    this.solutionYml = solutionYml;
  }
}
