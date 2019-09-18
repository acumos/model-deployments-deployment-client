package org.acumos.deploymentclient.service;

import org.acumos.deploymentclient.bean.DeployBean;
import org.acumos.deploymentclient.bean.DeploymentBean;
import org.acumos.deploymentclient.bean.SolutionBean;
import org.springframework.stereotype.Service;

@Service
public interface DeploymentService {
	
	public boolean deployValidation(DeployBean deployBean);

}
