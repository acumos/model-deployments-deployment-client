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

package org.acumos.deploymentclient.service;

import org.acumos.cds.domain.MLPTask;
import org.acumos.deploymentclient.bean.DeployBean;
import org.acumos.deploymentclient.bean.DeploymentBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public interface DeploymentService {

  public boolean deployValidation(DeployBean deployBean);
  // void getSolutionZip(SolutionBean solutionBean);
  public String getSolutionCode(
      String solutionId, String datasource, String userName, String dataPd);

  public String getSingleImageData(
      String solutionId, String revisionId, String datasource, String userName, String dataPd)
      throws Exception;

  public byte[] singleSolutionDetails(DeploymentBean dBean, String imageTag, String singleModelPort)
      throws Exception;

  public byte[] compositeSolutionDetails(DeploymentBean dBean) throws Exception;

  public MLPTask getTaskDetails(
      String datasource, String userName, String dataPd, long taskIdNum, DeploymentBean dBean)
      throws Exception;

  public void updateTaskDetails(
      String datasource,
      String userName,
      String dataPd,
      long taskIdNum,
      String status,
      String reason,
      String ingress,
      MLPTask mlpTask,DeploymentBean dBean)
      throws Exception;

  public MLPTask createTaskDetails(DeployBean deployBean, DeploymentBean dBean) throws Exception;

  public void createJenkinTask(DeploymentBean dBean, String taskId, String jobName)
      throws Exception;

  public void setDeploymentBeanProperties(DeploymentBean dBean, Environment env) throws Exception;
}
