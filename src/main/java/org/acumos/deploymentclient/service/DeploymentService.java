package org.acumos.deploymentclient.service;

import org.acumos.cds.domain.MLPTask;
import org.acumos.deploymentclient.bean.DeployBean;
import org.acumos.deploymentclient.bean.DeploymentBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public interface DeploymentService {
	
	public boolean deployValidation(DeployBean deployBean);
	//void getSolutionZip(SolutionBean solutionBean);
	public String getSolutionCode(String solutionId,String datasource,String userName,String dataPd);
	public String getSingleImageData(String solutionId,String revisionId,String datasource,String userName,
			String dataPd)throws Exception;
	public byte[] singleSolutionDetails(DeploymentBean dBean,String imageTag,String singleModelPort)throws Exception;
	public byte[] compositeSolutionDetails(DeploymentBean dBean)throws Exception;
	public MLPTask getTaskDetails(String datasource,String userName,String dataPd, long taskIdNum,
			DeploymentBean dBean) throws Exception;
	 public void updateTaskDetails(String datasource,String userName,String dataPd, long taskIdNum,
			 String status,String reason,MLPTask mlpTask)throws Exception;
	 public MLPTask createTaskDetails(DeployBean deployBean,DeploymentBean dBean) throws Exception;
	 public void createJenkinTask(DeploymentBean dBean,String taskId,String jobName) throws Exception;
	 public void setDeploymentBeanProperties(DeploymentBean dBean,Environment env)throws Exception;
	 

}
