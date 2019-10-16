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

package org.acumos.deploymentclient.test.serviceimpl;


import java.io.File;
import org.acumos.deploymentclient.bean.DeploymentBean;
import org.acumos.deploymentclient.serviceimpl.DeploymentServiceImpl;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
  public void createCompositeSolutionYMLTest() throws Exception {
    logger.info("createCompositeSolutionZipTest Start");
    DeploymentBean dBean = new DeploymentBean();
    DeploymentServiceImpl impl = new DeploymentServiceImpl();
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
    assertNotNull(dBean.getSolutionYml());
    logger.info("createCompositeSolutionZipTest End");
  }
}
