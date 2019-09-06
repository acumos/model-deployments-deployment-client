package org.acumos.deploymentclient.serviceimpl;

import org.acumos.deploymentclient.bean.DeployBean;
import org.acumos.deploymentclient.service.DeploymentService;
import org.springframework.stereotype.Component;

@Component
public class DeploymentServiceImpl implements DeploymentService{


public boolean deployValidation(DeployBean deployBean) {
	boolean valid=true;
	if(deployBean!=null) {
		if(isNullOrEmpty(deployBean.getEnvId())) 
			valid=false;
		if(isNullOrEmpty(deployBean.getRevisionId())) 
			valid=false;
		if(isNullOrEmpty(deployBean.getSolutionId())) 
			valid=false;
		if(isNullOrEmpty(deployBean.getUserId())) 
			valid=false;
		
	}else {
		valid=false;
	}
	
	return valid;
  }
public  boolean isNullOrEmpty(String str) {
    if(str != null && !str.trim().isEmpty())
        return false;
    return true;
}
}
