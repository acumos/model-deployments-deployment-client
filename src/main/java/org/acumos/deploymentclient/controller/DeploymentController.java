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

package org.acumos.deploymentclient.controller;

import java.lang.invoke.MethodHandles;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.acumos.cds.domain.MLPTask;
import org.acumos.deploymentclient.bean.DeployBean;
import org.acumos.deploymentclient.bean.DeploymentBean;
import org.acumos.deploymentclient.bean.StatusBean;
import org.acumos.deploymentclient.service.DeploymentService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeploymentController {
  @Autowired private Environment env;

  @Autowired(required = true)
  DeploymentService deploymentService;

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @RequestMapping(
      value = "/deploy",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public String deploy(
      HttpServletRequest request, @RequestBody DeployBean deployBean, HttpServletResponse response)
      throws Exception {
    log.debug("Start deploy API ");
    JSONObject jsonOutput = new JSONObject();
    DeploymentBean dBean = new DeploymentBean();
    String taskId="";
    String trackingId="";
    try {
      log.debug("solutionId " + deployBean.getSolutionId());
      log.debug("revisionId " + deployBean.getRevisionId());
      log.debug("envId " + deployBean.getEnvId());
      log.debug("userId " + deployBean.getUserId());
      boolean valid = deploymentService.deployValidation(deployBean);
      if (!valid) {
        jsonOutput.put("status", "solutionId, revisionId, envId, or userId is not found");
        response.setStatus(404);
        return jsonOutput.toString();
      }
      dBean.setEnvId(deployBean.getEnvId());
      dBean.setSolutionId(deployBean.getSolutionId());
      dBean.setRevisionId(deployBean.getRevisionId());
      dBean.setUserId(deployBean.getUserId());
      deploymentService.setDeploymentBeanProperties(dBean, env);
      log.debug("Spring Env properties");
      MLPTask mlpTask = deploymentService.createTaskDetails(deployBean, dBean);
      log.debug("mlpTask created taskId" + mlpTask.getTaskId());
      if(mlpTask.getTaskId()!=null) {
    	  taskId=String.valueOf(mlpTask.getTaskId()); 
      }
      if(mlpTask.getTrackingId()!=null) {
    	  trackingId=String.valueOf(mlpTask.getTrackingId()); 
      }
      log.debug("taskId: " + taskId);
      log.debug("trackingId: " + trackingId);
      String solutionToolKitType =
          deploymentService.getSolutionCode(
              dBean.getSolutionId(),
              dBean.getDatasource(),
              dBean.getDataUserName(),
              dBean.getDataPd());
      log.debug("solutionToolKitType " + solutionToolKitType);
      if (solutionToolKitType != null
          && !"".equals(solutionToolKitType)
          && "CP".equalsIgnoreCase(solutionToolKitType)) {
        deploymentService.createJenkinTask(
            dBean, String.valueOf(mlpTask.getTaskId()), env.getProperty("jenkins.job.composite"));
      } else {
        deploymentService.createJenkinTask(
            dBean, String.valueOf(mlpTask.getTaskId()), env.getProperty("jenkins.job.simple"));
      }

      jsonOutput.put("status", "SUCCESS");
      jsonOutput.put("taskId", taskId);
      jsonOutput.put("trackingId", trackingId);
      response.setStatus(202);
    } catch (Exception e) {
      log.error("deploy API failed", e);
      jsonOutput.put("status", "FAIL");
      jsonOutput.put("taskId", "");
      jsonOutput.put("trackingId", "");
      response.setStatus(400);
    }
    log.debug("End deploy API ");
    return jsonOutput.toString();
  }

  @RequestMapping(
      value = "/getSolutionZip/{taskId}",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public String getSolutionZip(
      HttpServletRequest request,
      @PathVariable("taskId") String taskId,
      HttpServletResponse response)
      throws Exception {
    log.debug("Start getSolutionZip ");
    JSONObject jsonOutput = new JSONObject();
    DeploymentBean dBean = new DeploymentBean();
    String singleModelPort = "";
    byte[] solutionZip = null;
    try {
      log.debug("taskId " + taskId);
      if (taskId == null || "".equalsIgnoreCase(taskId.trim())) {
        jsonOutput.put("status", "taskId not found");
        response.setStatus(404);
        return jsonOutput.toString();
      }
      long taskIdNum = Long.parseLong(taskId);
      log.debug("taskIdNum " + taskIdNum);
      deploymentService.setDeploymentBeanProperties(dBean, env);
      deploymentService.getTaskDetails(
          dBean.getDatasource(), dBean.getDataUserName(), dBean.getDataPd(), taskIdNum, dBean);
      String solutionToolKitType =
          deploymentService.getSolutionCode(
              dBean.getSolutionId(),
              dBean.getDatasource(),
              dBean.getDataUserName(),
              dBean.getDataPd());
      log.debug("solutionToolKitType " + solutionToolKitType);
      if (solutionToolKitType != null
          && !"".equals(solutionToolKitType)
          && "CP".equalsIgnoreCase(solutionToolKitType)) {
        log.debug("Composite Solution Details Start");
        solutionZip = deploymentService.compositeSolutionDetails(dBean);
        log.debug("Composite Solution Deployment End");
      } else {
        log.debug("Single Solution Details Start");
        String imageTag =
            deploymentService.getSingleImageData(
                dBean.getSolutionId(),
                dBean.getRevisionId(),
                dBean.getDatasource(),
                dBean.getDataUserName(),
                dBean.getDataPd());
        if(imageTag!=null && !"".equals(imageTag)) {
        	solutionZip = deploymentService.singleSolutionDetails(dBean, imageTag, singleModelPort);
            log.debug("Single Solution Details End");
        }else {
        	log.debug("imageTag : "+imageTag);
        	log.error("imageTag : "+imageTag);
        	throw new Exception("Simple Solution Image is not available");
        }
        
      }
      jsonOutput.put("status", "OK");
      response.setStatus(200);

    } catch (Exception e) {
      log.error("getSolutionZip failed", e);
      jsonOutput.put("status", "FAIL");
      response.setStatus(400);
    }
    response.setHeader("Content-Disposition", "attachment; filename=" + taskId + ".zip");
    response.getOutputStream().write(solutionZip);
    log.debug("End getSolutionZip");
    return jsonOutput.toString();
  }

  @RequestMapping(
      value = "/status/{taskId}",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public String status(
      HttpServletRequest request,
      @RequestBody StatusBean statusBean,
      @PathVariable("taskId") String taskId,
      HttpServletResponse response)
      throws Exception {
    log.debug("Start status API ");
    JSONObject jsonOutput = new JSONObject();
    DeploymentBean dBean = new DeploymentBean();
    MLPTask mlpTask = null;
    try {
      log.debug("taskId " + taskId);
      if (taskId == null || "".equalsIgnoreCase(taskId.trim())) {
        jsonOutput.put("status", "taskId not found");
        response.setStatus(404);
        return jsonOutput.toString();
      }
      long taskIdNum = Long.parseLong(taskId);
      log.debug("taskIdNum " + taskIdNum);
      log.debug("Status " + statusBean.getStatus());
      log.debug("Reason " + statusBean.getReason());
      log.debug("Ingress " + statusBean.getIngress());
      deploymentService.setDeploymentBeanProperties(dBean, env);
      mlpTask =
          deploymentService.getTaskDetails(
              dBean.getDatasource(), dBean.getDataUserName(), dBean.getDataPd(), taskIdNum, null);
      log.debug("TrackingId " + mlpTask.getTrackingId());
      deploymentService.updateTaskDetails(
          dBean.getDatasource(),
          dBean.getDataUserName(),
          dBean.getDataPd(),
          taskIdNum,
          statusBean.getStatus(),
          statusBean.getReason(),
          statusBean.getIngress(),
          mlpTask,dBean);

      jsonOutput.put("status", "OK");
      response.setStatus(200);

    } catch (Exception e) {
      log.error("status API failed", e);
      jsonOutput.put("status", "FAIL");
      response.setStatus(400);
    }
    log.debug("End status API ");
    return jsonOutput.toString();
  }
}
