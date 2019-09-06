package org.acumos.deploymentclient.test.serviceimpl;

import static org.junit.Assert.assertNotNull;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.acumos.deploymentclient.bean.DeploymentBean;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DeploymentServiceImplTest {
	
	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/*@Test	
	public void getSingleSolutionYMLFileTest() throws Exception{
		logger.info("getSingleSolutionYMLFileTest Start");
		DeploymentBean dBean =new DeploymentBean();
		DeploymentServiceImpl impl=new DeploymentServiceImpl();
		dBean.setSingleNodePort("30333");
		dBean.setSingleModelPort("8556");
		dBean.setSingleTargetPort("30333");
		dBean.setDockerProxyHost("http://host");
		dBean.setDockerProxyPort("4243");
		dBean.setSingleTargetPort("30333");
		impl.getSingleSolutionYMLFile("repo/image:1", "30333", dBean);
		logger.info("getSingleSolutionYMLFileTestt End");
	}*/
	
	@Test	
	public void createCompositeSolutionYMLTest() throws Exception{
		logger.info("createCompositeSolutionZipTest Start");
		
		logger.info("createCompositeSolutionZipTest End");
		
	}

}
