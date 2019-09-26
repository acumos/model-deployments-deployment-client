package org.acumos.deploymentclient.bean;

public class StatusBean {

  private String status;
  private String reason;
  private String ingress;

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
}
