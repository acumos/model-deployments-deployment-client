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
package org.acumos.deploymentclient.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.acumos.cds.domain.MLPSiteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.invoke.MethodHandles;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;
import org.acumos.cds.client.CommonDataServiceRestClientImpl;
import org.acumos.cds.client.ICommonDataServiceRestClient;

@Component
public class LoadDataOnStartUp
{   
	private static final Logger logger =LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());	
 @Autowired private Environment env;
 
	
    @EventListener(ApplicationReadyEvent.class)
    public void loadData()
    {
    	 logger.debug("start loadData");
    	String cmnDataUrl =
    	        (env.getProperty(DeployConstants.CMNDATASVC_CMNDATASVCENDPOINTURL_PROP) != null)
    	            ? env.getProperty(DeployConstants.CMNDATASVC_CMNDATASVCENDPOINTURL_PROP)
    	            : "";
    	    String cmnDataUser =
    	        (env.getProperty(DeployConstants.CMNDATASVC_CMNDATASVCUSER_PROP) != null)
    	            ? env.getProperty(DeployConstants.CMNDATASVC_CMNDATASVCUSER_PROP)
    	            : "";
    	    String cmnDataPd =
    	        (env.getProperty(DeployConstants.CMNDATASVC_CMNDATASVCPD_PROP) != null)
    	            ? env.getProperty(DeployConstants.CMNDATASVC_CMNDATASVCPD_PROP)
    	            : "";
    	    try {        
    	    CommonDataServiceRestClientImpl client =new CommonDataServiceRestClientImpl(cmnDataUrl, cmnDataUser, cmnDataPd, null);
    	    logger.debug("Database connected"); 
    	    createSiteConfig(client);
    	    }catch(Exception e) {
    	    	logger.error("Error in loadData site config ", e);	
    	    }
    	    logger.debug("End loadData");    
    }


public  String createSiteConfig(CommonDataServiceRestClientImpl client) throws Exception {
    logger.debug("Inside createSiteConfig");
    MLPSiteConfig mlpSiteConfig = null;
    try {
      mlpSiteConfig = client.getSiteConfig(DeployConstants.SITE_VERIFICATION_KEY);
      logger.debug("mlpSiteConfig "+mlpSiteConfig);
    } catch (RestClientResponseException ex) {
      logger.error("getSiteConfig failed, server reports: {}", ex.getResponseBodyAsString());
      throw ex;
    }
    if (StringUtils.isEmpty(mlpSiteConfig)) {
      logger.debug("createSiteConfig: no siteConfig verification key in database");
      String siteConfigJsonFromConfiguration = env.getProperty(DeployConstants.SITECONFIG);
      logger.debug("siteConfig.verification env: {} ", siteConfigJsonFromConfiguration);
      MLPSiteConfig config = new MLPSiteConfig();
      config.setConfigKey(DeployConstants.SITE_VERIFICATION_KEY);
      config.setConfigValue(siteConfigJsonFromConfiguration);
      try {
    	logger.debug("createSiteConfig: Key value {}", config.getConfigKey());
        logger.debug("createSiteConfig: setting value {}", config.getConfigValue());
        mlpSiteConfig = client.createSiteConfig(config);
        logger.debug("createSiteConfig: result {}", mlpSiteConfig.getConfigValue());
        return mlpSiteConfig.getConfigValue();
      } catch (RestClientResponseException ex) {
        logger.error("createSiteConfig failed, server reports: {}", ex.getResponseBodyAsString());
        throw ex;
      }
    } else {
      return mlpSiteConfig.getConfigValue();
    }
  }

}