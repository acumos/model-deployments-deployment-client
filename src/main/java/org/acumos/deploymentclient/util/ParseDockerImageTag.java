/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2020 Nordix Foundation
 * ===================================================================================
 * This Acumos software file is distributed by Nordix Foundation
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

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acumos.cds.client.CommonDataServiceRestClientImpl;
import org.acumos.cds.domain.MLPSolutionRevision;
import org.acumos.deploymentclient.bean.ContainerBean;
import org.acumos.deploymentclient.bean.DeploymentBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseDockerImageTag {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Parse the given image path and extract values. Following format based on
   * docker image path is expected: host:port/modelName_solutionId:version For
   * example,
   * acumos-aio-1:30883/face_privacy_filter_detect_96fc199b-eb96-4162-b33e-b1fc629b28c5:1
   * 
   * @param imageName - image name
   * @return map with key/value pair for each parsed item with key as -
   *         DockerKubeConstants.DOCKER_HOST - DockerKubeConstants.DOCKER_PORT -
   *         DockerKubeConstants.MODEL_NAME - DockerKubeConstants.SOLUTION_ID -
   *         DockerKubeConstants.VERSION - returns empty map, if cannot derived
   *         from the expected imageName format
   */
  public static Map<String, String> parseImageToken(String imageName) throws Exception {
    logger.debug("Start-parseImageToken: imgName:" + imageName);
    Map<String, String> map = new HashMap<String, String>();
    if (imageName != null) {
      // imageName=acumos-aio-host:30883/face_privacy_filter_detect_96fc199b-eb96-4162-b33e-b1fc629b28c5:1
      String[] imageArr = imageName.split("/");
      if (imageArr.length >= 2) {

        // extract docker host:port info
        // String[] dockerInfoArr = imageArr[0].split(":");
        // if (dockerInfoArr.length > 0) {
        // map.put(DeployConstants.DOCKER_HOST, dockerInfoArr[0]);
        // if (dockerInfoArr.length > 1) {
        // map.put(DockerKubeConstants.DOCKER_PORT, dockerInfoArr[1]);
        // }
        // }

        // extract modelName:solutionId:version info
        String[] imageNameArr = imageArr[1].split(":");
        if (imageNameArr.length > 0) {
          if (imageNameArr.length > 1) {
            map.put(DeployConstants.VERSION, imageNameArr[1]);
          }
          // modelSolTkn derived as
          // face_privacy_filter_detect_96fc199b-eb96-4162-b33e-b1fc629b28c5
          String modelSolTkn = imageNameArr[0];
          int underscoreLastIndex = modelSolTkn.lastIndexOf("_");
          if (underscoreLastIndex == -1) {
            throw new Exception("cannot find the solutionId match from input imagePath " + imageName);
          } else {
            String modelName = modelSolTkn.substring(0, underscoreLastIndex);
            String solutionId = modelSolTkn.substring(underscoreLastIndex + 1);

            // make dns-compliant i.e. replace '_' with '-'
            modelName = modelName.replaceAll("_", "-");

            map.put(DeployConstants.NAME, modelName);
            map.put(DeployConstants.SOLUTION_ID, solutionId);
          }
        }
      }
    }

    logger.debug(" End-parseImageToken " + map.get("dockerHost") + "::" + map.get("dockerPort") + "::"
        + map.get("modelName") + "::" + map.get("solutionId") + "::" + map.get("version"));
    return map;
  }

  public CommonDataServiceRestClientImpl getClient(String datasource, String userName, String dataPd) {
    System.out.println("getClient start");
    CommonDataServiceRestClientImpl client = new CommonDataServiceRestClientImpl(datasource, userName, dataPd, null);
    System.out.println("getClient End");
    return client;
  }


  public void fetchSolutionRevisionMap(DeploymentBean dBean) throws Exception {
    logger.debug("getSolutionRevisionMap - start");
    // ACUMOS-2782 - create map of solutionId and revisionId to export
    // (deploy_env.sh)
    Map<String, String> solRevMap = new HashMap<String, String>();
    solRevMap.put(dBean.getSolutionId(), dBean.getRevisionId());
    List<ContainerBean> containerBeans = dBean.getContainerBeanList();

    if (containerBeans != null) {
      CommonDataServiceRestClientImpl cmnDataService = getClient(dBean.getDatasource(), dBean.getDataUserName(),
      dBean.getDataPd());
      for (ContainerBean containerBean : containerBeans) {
        String image = containerBean.getImage();
        Map<String, String> imageMetaMap = ParseDockerImageTag.parseImageToken(image);
        String solutionId = imageMetaMap.get(DeployConstants.SOLUTION_ID);
        String solVersion = imageMetaMap.get(DeployConstants.VERSION);
        List<MLPSolutionRevision> revisions = cmnDataService.getSolutionRevisions(solutionId);
        for (MLPSolutionRevision revision : revisions) {
          if (revision.getVersion().equals(solVersion)) {
            solRevMap.put(solutionId, revision.getRevisionId());
            break;
          }
        }
      }
    }
    dBean.setSolutionRevisionIdMap(solRevMap);
    logger.debug("getSolutionRevisionMap - end");
  }

  public String getEnvFileDetails(DeploymentBean dBean) throws Exception {
    logger.debug("getEnvFileDetails Start ");

    Map<String, String> solRevIdMap = dBean.getSolutionRevisionIdMap();
    if(solRevIdMap==null){
      this.fetchSolutionRevisionMap(dBean);
      solRevIdMap = dBean.getSolutionRevisionIdMap();
    }
    String solId = "";
    String solRevId = "";
    String compSolId = "";
    String compRevId = "";
    if (solRevIdMap != null) {
      for (String solIdKey : solRevIdMap.keySet()) {
        if (solRevId.length() > 0) {
          solId += ",";
          solRevId += ",";
        }
        solId += solIdKey;
        solRevId += solIdKey + ":" + solRevIdMap.get(solIdKey);
      }

      // more than one entry for composite case
      if (solRevIdMap.keySet().size() > 1) {
        compSolId = dBean.getSolutionId();
        compRevId = dBean.getRevisionId();
      }
    }

    // # Single model
    // - SOLUTION_ID=<SINGLE_MDL_SOL_ID>
    // - SOL_REVISION_ID=<SINGLE_MDL_REVISION_ID>
    // # Composite model
    // - SOLUTION_ID=<COMP_MDL_SOL_ID>,<MDL_1_SOL_ID>,<MDL_2_SOL_ID>,...
    // -
    // SOL_REVISION_ID=<COMP_MDL_SOL_ID>:<COMP_MDL_REVISION_ID>,<MDL_1_SOL_ID>:<MDL_1_REVISION_ID>,<MDL_2_SOL_ID>:<MDL_2_REVISION_ID>,...
    StringBuilder setEnvDeploy = new StringBuilder();
    setEnvDeploy.append("export SOLUTION_ID=").append(solId).append("\n").append("export SOL_REVISION_ID=")
      .append(solRevId + "\n");


    if (compSolId != null && compSolId.length() > 0) {
      setEnvDeploy.append("export COMP_SOLUTION_ID=" + compSolId + "\n" )
      .append("export COMP_REVISION_ID=" + compRevId + "\n");
    }

    logger.debug("getEnvFileDetails End " + setEnvDeploy);
    return setEnvDeploy.toString();
  }
}