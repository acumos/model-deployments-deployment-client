package org.acumos.deploymentclient.bean;

public class SolutionBean {

  private String solutionId;
  private String revisionId;
  private String envId;
  private String userId;
  private String trackingId;
  private String datasource;
  private String dataUserName;
  private String dataPd;

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
}
