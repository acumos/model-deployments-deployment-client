package org.acumos.deploymentclient.controller;

import java.lang.invoke.MethodHandles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.acumos.deploymentclient.bean.DeployBean;
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
	@Autowired
	private Environment env;
	
	@Autowired(required=true)
	DeploymentService deploymentService;
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@RequestMapping(value = "/deploy", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String deploy(HttpServletRequest request,@RequestBody DeployBean deployBean,HttpServletResponse response) throws Exception {
		 log.debug("Start deploy API ");
		 JSONObject  jsonOutput = new JSONObject();
		 try {
			 log.debug("solutionId "+deployBean.getSolutionId());
			 log.debug("revisionId "+deployBean.getRevisionId());
			 log.debug("envId "+deployBean.getEnvId());
			 log.debug("userId "+deployBean.getUserId());
			 boolean valid=deploymentService.deployValidation(deployBean);
			 if(!valid) {
				 jsonOutput.put("status", "solutionId, revisionId, envId, or userId is not found");
				 response.setStatus(404);
				 return jsonOutput.toString();
			 }
			 jsonOutput.put("status", "SUCCESS");
			 response.setStatus(202);
		 }catch(Exception e){
			log.error("deploy API failed", e);
			jsonOutput.put("status", "FAIL");
			response.setStatus(400);
		 }
		 log.debug("End deploy API ");
		 return jsonOutput.toString();
	}
	
	
	@RequestMapping(value = "/getSolutionZip/{trackingId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public String getSolutionZip(HttpServletRequest request, @PathVariable("trackingId") String trackingId,HttpServletResponse response) throws Exception {
		 log.debug("Start getSolutionZip ");
		 JSONObject  jsonOutput = new JSONObject();
		 try {
		   log.debug("trackingId "+trackingId);
		   if(trackingId == null || "".equalsIgnoreCase(trackingId.trim())) {
			   jsonOutput.put("status", "trackingId not found");
			   response.setStatus(404);
			   return jsonOutput.toString();
		   }
		   jsonOutput.put("status", "OK");
			 response.setStatus(200);
		 
		 }catch(Exception e){
				log.error("getSolutionZip failed", e);
				jsonOutput.put("status", "FAIL");
				response.setStatus(400);
			 }
		 log.debug("End getSolutionZip ");
		 return jsonOutput.toString();
	}
	
	@RequestMapping(value = "/status/{trackingId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String status(HttpServletRequest request,@RequestBody StatusBean statusBean, @PathVariable("trackingId") String trackingId,
			HttpServletResponse response) throws Exception {
		 log.debug("Start status API ");
		 JSONObject  jsonOutput = new JSONObject();
		 try {
			   log.debug("trackingId "+trackingId);
			   if(trackingId == null || "".equalsIgnoreCase(trackingId.trim())) {
				   jsonOutput.put("status", "trackingId not found");
				   response.setStatus(404);
				   return jsonOutput.toString();
			   }
			   jsonOutput.put("status", "OK");
				 response.setStatus(200);
			 
			 }catch(Exception e){
					log.error("status API failed", e);
					jsonOutput.put("status", "FAIL");
					response.setStatus(400);
				 }
		 log.debug("End status API ");
		 return jsonOutput.toString();
	}
	
}
