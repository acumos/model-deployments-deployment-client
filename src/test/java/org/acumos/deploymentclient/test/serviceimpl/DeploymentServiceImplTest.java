package org.acumos.deploymentclient.test.serviceimpl;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.acumos.deploymentclient.bean.DeploymentBean;
import org.acumos.deploymentclient.serviceimpl.DeploymentServiceImpl;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DeploymentServiceImplTest {
	
	private static final Logger logger = LoggerFactory.getLogger(DeploymentServiceImplTest.class);
/*	@Test	
	public void getSingleSolutionYMLFileTest() throws Exception{
		logger.info("getSingleSolutionYMLFileTest Start");
		DeploymentBean dBean =new DeploymentBean();
		DeploymentServiceImpl impl=new DeploymentServiceImpl();
		dBean.setSingleNodePort("30333");
		dBean.setSingleModelPort("8556");
		dBean.setSingleTargetPort("30333");
		dBean.setDockerProxyHost("http://host");
		dBean.setDockerProxyPort("4243");
		//dBean.setSingleTargetPort("30333");
		String solutionYaml=impl.getSingleSolutionYMLFile("repo/image:1", "30333", dBean);
		logger.info("getSingleSolutionYMLFileTestt End"+solutionYaml);
	}*/
	
	@Test	
	public void createCompositeSolutionYMLTest() throws Exception{
		logger.info("createCompositeSolutionZipTest Start");
		DeploymentBean dBean =new DeploymentBean();
		DeploymentServiceImpl impl=new DeploymentServiceImpl();
		dBean.setFolderPath("deploy/private");
		dBean.setBluePrintjson("blueprint.json");
		dBean.setDockerInfoJson("dockerinfo.json");
		dBean.setSolutionYml("solution.yml");
		dBean.setDataBrokerJson("dataBroker.json");
		dBean.setIncrementPort("8557");
		dBean.setBluePrintImage("blueprintimage");
		dBean.setBluePrintPort("8555");
		File file = new File("blueprint.json");
		String jsonString = FileUtils.readFileToString(file);
		impl.getSolutionYMLFile(dBean, jsonString);
		logger.info("createCompositeSolutionZipTest End");
		
	}
	
	

}
