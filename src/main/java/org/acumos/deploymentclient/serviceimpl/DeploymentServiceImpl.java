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

package org.acumos.deploymentclient.serviceimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.acumos.cds.client.CommonDataServiceRestClientImpl;
import org.acumos.cds.domain.MLPArtifact;
import org.acumos.cds.domain.MLPNotification;
import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.domain.MLPSolutionRevision;
import org.acumos.cds.domain.MLPTask;
import org.acumos.deploymentclient.bean.ContainerBean;
import org.acumos.deploymentclient.bean.DeployBean;
import org.acumos.deploymentclient.bean.DeploymentBean;
import org.acumos.deploymentclient.bean.DeploymentKubeBean;
import org.acumos.deploymentclient.bean.DockerInfo;
import org.acumos.deploymentclient.bean.DockerInfoBean;
import org.acumos.deploymentclient.bean.MLNotification;
import org.acumos.deploymentclient.parsebean.DataBrokerBean;
import org.acumos.deploymentclient.service.DeploymentService;
import org.acumos.deploymentclient.util.DeployConstants;
import org.acumos.deploymentclient.util.JenkinsJobBuilder;
import org.acumos.deploymentclient.util.ParseJSON;
import org.acumos.nexus.client.NexusArtifactClient;
import org.acumos.nexus.client.RepositoryLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DeploymentServiceImpl implements DeploymentService {

  private static final Logger logger = LoggerFactory.getLogger(DeploymentServiceImpl.class);

  @Override
  public boolean deployValidation(DeployBean deployBean) {
    boolean valid = true;
    if (deployBean != null) {
      if (isNullOrEmpty(deployBean.getEnvId())) valid = false;
      if (isNullOrEmpty(deployBean.getRevisionId())) valid = false;
      if (isNullOrEmpty(deployBean.getSolutionId())) valid = false;
      if (isNullOrEmpty(deployBean.getUserId())) valid = false;

    } else {
      valid = false;
    }

    return valid;
  }

  public boolean isNullOrEmpty(String str) {
    if (str != null && !str.trim().isEmpty()) return false;
    return true;
  }

  public CommonDataServiceRestClientImpl getClient(
      String datasource, String userName, String dataPd) {
    logger.debug("getClient start");
    CommonDataServiceRestClientImpl client =
        new CommonDataServiceRestClientImpl(datasource, userName, dataPd, null);
    logger.debug("getClient End");
    return client;
  }

  public NexusArtifactClient nexusArtifactClient(
      String nexusUrl, String nexusUserName, String nexusPd) {
    logger.debug("nexusArtifactClient start");
    RepositoryLocation repositoryLocation = new RepositoryLocation();
    repositoryLocation.setId("1");
    repositoryLocation.setUrl(nexusUrl);
    repositoryLocation.setUsername(nexusUserName);
    repositoryLocation.setPassword(nexusPd);
    NexusArtifactClient nexusArtifactClient = new NexusArtifactClient(repositoryLocation);
    logger.debug("nexusArtifactClient End");
    return nexusArtifactClient;
  }

  public String getSolutionCode(
      String solutionId, String datasource, String userName, String dataPd) {
    logger.debug("getSolution start");
    String toolKitTypeCode = "";
    try {
      CommonDataServiceRestClientImpl cmnDataService = getClient(datasource, userName, dataPd);
      MLPSolution mlpSolution = cmnDataService.getSolution(solutionId);
      if (mlpSolution != null) {
        logger.debug("mlpSolution.getToolkitTypeCode() " + mlpSolution.getToolkitTypeCode());
        toolKitTypeCode = mlpSolution.getToolkitTypeCode();
      }
    } catch (Exception e) {
      logger.debug("Error in get solution " + e.getMessage());
      toolKitTypeCode = "";
    }
    logger.debug("getSolution End toolKitTypeCode " + toolKitTypeCode);
    return toolKitTypeCode;
  }

  public String getSingleImageData(
      String solutionId, String revisionId, String datasource, String userName, String dataPd)
      throws Exception {
    logger.debug("Start getSingleImageData");
    String imageTag = "";
    CommonDataServiceRestClientImpl cmnDataService = getClient(datasource, userName, dataPd);
    List<MLPArtifact> mlpSolutionRevisions = null;
    mlpSolutionRevisions = cmnDataService.getSolutionRevisionArtifacts(solutionId, revisionId);
    if (mlpSolutionRevisions != null) {
      for (MLPArtifact artifact : mlpSolutionRevisions) {
        String[] st = artifact.getUri().split("/");
        String name = st[st.length - 1];
        artifact.setName(name);
        logger.debug("ArtifactTypeCode" + artifact.getArtifactTypeCode());
        logger.debug("URI" + artifact.getUri());
        if (artifact.getArtifactTypeCode() != null
            && artifact.getArtifactTypeCode().equalsIgnoreCase("DI")) {
          imageTag = artifact.getUri();
        }
      }
    }

    logger.debug("End getSingleImageData imageTag" + imageTag);
    return imageTag;
  }

  public byte[] singleSolutionDetails(DeploymentBean dBean, String imageTag, String singleModelPort)
      throws Exception {
    logger.debug("singleSolutionDetails start");
    logger.debug("imageTag " + imageTag + " singleModelPort " + singleModelPort);
    byte[] solutionZip = null;
    String solutionName = getModelName(imageTag, dBean.getSolutionId());

    logger.debug("solutionName " + solutionName);
    dBean.setSolutionName(solutionName);
    String solutionYaml = getSingleSolutionYMLFile(imageTag, singleModelPort, dBean);
    dBean.setSolutionYml(solutionYaml);
    logger.debug("solutionYaml " + solutionYaml);
    solutionZip = createSingleSolutionZip(dBean);
    logger.debug("singleSolutionDetails End");
    return solutionZip;
  }

  public String getSingleSolutionYMLFile(
      String imageTag, String singleModelPort, DeploymentBean dBean) throws Exception {
    logger.debug("getSingleSolutionYMLFile Start");
    String solutionYaml = "";
    String serviceYml = getSingleSolutionService(singleModelPort, dBean);
    String deploymentYml = getSingleSolutionDeployment(imageTag, dBean);
    solutionYaml = serviceYml;
    solutionYaml = solutionYaml + deploymentYml;
    logger.debug("solutionYaml " + solutionYaml);
    logger.debug("getSingleSolutionYMLFile End");
    return solutionYaml;
  }

  public String getSingleSolutionService(String modelPort, DeploymentBean dBean) throws Exception {
    logger.debug("getSingleSolutionService Start");
    String serviceYml = "";
    ObjectMapper objectMapper = new ObjectMapper();
    YAMLMapper yamlMapper =
        new YAMLMapper(new YAMLFactory().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true));
    ObjectNode apiRootNode = objectMapper.createObjectNode();
    apiRootNode.put(DeployConstants.APIVERSION_YML, DeployConstants.V_YML);

    /*ObjectNode kindDataNode = objectMapper.createObjectNode();
    kindDataNode.put(DeployConstants.KIND_YML, DeployConstants.SERVICE_YML);*/
    apiRootNode.put(DeployConstants.KIND_YML, DeployConstants.SERVICE_YML);

    ObjectNode metadataNode = objectMapper.createObjectNode();
    metadataNode.put(DeployConstants.NAMESPACE_YML, DeployConstants.NAMESPACE_VALUE_YML);
    metadataNode.put(
        DeployConstants.NAME_YML,
        dBean.getSolutionName() + "-" + DeployConstants.TRACKINGID_VALUE_YML);

    ObjectNode labelsNode = objectMapper.createObjectNode();
    labelsNode.put(
        DeployConstants.APP_DEP_YML,
        dBean.getSolutionName() + "-" + DeployConstants.TRACKINGID_VALUE_YML);
    labelsNode.put(DeployConstants.TRACKINGID_YML, DeployConstants.TRACKINGID_VALUE_YML);
    metadataNode.put(DeployConstants.LABELS_DEP_YML, labelsNode);

    apiRootNode.set(DeployConstants.METADATA_YML, metadataNode);

    ObjectNode specNode = objectMapper.createObjectNode();

    ObjectNode selectorNode = objectMapper.createObjectNode();
    selectorNode.put(
        DeployConstants.APP_YML,
        dBean.getSolutionName() + "-" + DeployConstants.TRACKINGID_VALUE_YML);
    specNode.set(DeployConstants.SELECTOR_YML, selectorNode);
    specNode.put(DeployConstants.TYPE_YML, DeployConstants.NODE_TYPE_PORT_YML);

    ArrayNode portsArrayNode = specNode.arrayNode();
    ObjectNode portsNode = objectMapper.createObjectNode();

    portsNode.put(DeployConstants.NAME_YML, DeployConstants.PROTOBUF_API_DEP_YML);
    portsNode.put(DeployConstants.PORT_YML, dBean.getSingleModelPort());
    portsNode.put(DeployConstants.TARGETPORT_YML, dBean.getSingleTargetPort());
    // portsNode.put(DeployConstants.NODEPORT_YML, dBean.getSingleNodePort());
    // portsNode.put(DeployConstants.PORT_YML, dBean.getSingleModelPort());
    // portsNode.put(DeployConstants.TARGETPORT_YML, dBean.getSingleTargetPort());
    portsArrayNode.add(portsNode);
    specNode.set(DeployConstants.PORTS_YML, portsArrayNode);

    apiRootNode.set(DeployConstants.SPEC_YML, specNode);

    serviceYml = yamlMapper.writeValueAsString(apiRootNode);
    logger.debug("solutionDeployment " + serviceYml);
    return serviceYml;
  }

  public String getSingleSolutionDeployment(String imageTag, DeploymentBean dBean)
      throws Exception {
    logger.debug("getSingleSolutionDeployment Start");
    ObjectMapper objectMapper = new ObjectMapper();
    // CommonUtil cutil=new CommonUtil();
    YAMLMapper yamlMapper =
        new YAMLMapper(new YAMLFactory().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true));
    ObjectNode kindRootNode = objectMapper.createObjectNode();
    kindRootNode.put(DeployConstants.APIVERSION_DEP_YML, DeployConstants.APPS_V1_DEP_YML);
    kindRootNode.put(DeployConstants.KIND_DEP_YML, DeployConstants.DEPLOYMENT_DEP_YML);

    ObjectNode metadataNode = objectMapper.createObjectNode();
    metadataNode.put(DeployConstants.NAMESPACE_DEP_YML, DeployConstants.NAMESPACE_VALUE_YML);
    metadataNode.put(
        DeployConstants.NAME_DEP_YML,
        dBean.getSolutionName() + "-" + DeployConstants.TRACKINGID_VALUE_YML);

    ObjectNode labelsNode = objectMapper.createObjectNode();
    labelsNode.put(
        DeployConstants.APP_DEP_YML,
        dBean.getSolutionName() + "-" + DeployConstants.TRACKINGID_VALUE_YML);
    labelsNode.put(DeployConstants.TRACKINGID_YML, DeployConstants.TRACKINGID_VALUE_YML);
    metadataNode.put(DeployConstants.LABELS_DEP_YML, labelsNode);

    kindRootNode.set(DeployConstants.METADATA_DEP_YML, metadataNode);

    ObjectNode specNode = objectMapper.createObjectNode();
    specNode.put(DeployConstants.REPLICAS_DEP_YML, 1);

    ObjectNode selectorNode = objectMapper.createObjectNode();
    ObjectNode matchLabelsNode = objectMapper.createObjectNode();
    matchLabelsNode.put(
        DeployConstants.APP_DEP_YML,
        dBean.getSolutionName() + "-" + DeployConstants.TRACKINGID_VALUE_YML);
    selectorNode.set(DeployConstants.MATCHLABELS_DEP_YML, matchLabelsNode);

    specNode.set(DeployConstants.SELECTOR_DEP_YML, selectorNode);

    ObjectNode templateNode = objectMapper.createObjectNode();
    ObjectNode metadataTemplateNode = objectMapper.createObjectNode();
    ObjectNode labelsTemplateNode = objectMapper.createObjectNode();
    labelsTemplateNode.put(
        DeployConstants.APP_DEP_YML,
        dBean.getSolutionName() + "-" + DeployConstants.TRACKINGID_VALUE_YML);
    labelsTemplateNode.put(DeployConstants.TRACKINGID_YML, DeployConstants.TRACKINGID_VALUE_YML);
    metadataTemplateNode.set(DeployConstants.LABELS_DEP_YML, labelsTemplateNode);

    ObjectNode specTempNode = objectMapper.createObjectNode();
    ArrayNode containerArrayNode = templateNode.arrayNode();
    ObjectNode containerNode = objectMapper.createObjectNode();
    containerNode.put(
        DeployConstants.NAME_DEP_YML,
        dBean.getSolutionName() + "-" + DeployConstants.TRACKINGID_VALUE_YML);
    containerNode.put(DeployConstants.IMAGE_DEP_YML, imageTag);
    /*containerNode.put(DeployConstants.IMAGE_DEP_YML,
    cutil.getProxyImageName(imageTag, dBean.getDockerProxyHost(), dBean.getDockerProxyPort()));*/

    ArrayNode portsArrayNode = containerNode.arrayNode();
    ObjectNode portsNode = objectMapper.createObjectNode();
    portsNode.put(DeployConstants.NAME_DEP_YML, DeployConstants.PROTOBUF_API_DEP_YML);
    portsNode.put(DeployConstants.CONTAINERPORT_DEP_YML, dBean.getSingleTargetPort());

    portsArrayNode.add(portsNode);
    containerArrayNode.add(containerNode);
    containerNode.set(DeployConstants.PORTS_DEP_YML, portsArrayNode);

    ObjectNode imagePullSecretsNode = objectMapper.createObjectNode();
    ArrayNode imageSecretArrayNode = containerNode.arrayNode();
    imagePullSecretsNode.put(DeployConstants.NAME_DEP_YML, DeployConstants.ACUMOS_REGISTRY_DEP_YML);
    imageSecretArrayNode.add(imagePullSecretsNode);
    specTempNode.set(DeployConstants.IMAGEPULLSECRETS_DEP_YML, imageSecretArrayNode);

    specTempNode.set(DeployConstants.CONTAINERS_DEP_YML, containerArrayNode);

    specTempNode.put(DeployConstants.RESTARTPOLICY_DEP_YML, DeployConstants.ALWAYS_DEP_YML);

    templateNode.set(DeployConstants.METADATA_DEP_YML, metadataTemplateNode);
    templateNode.set(DeployConstants.SPEC_DEP_YML, specTempNode);
    specNode.set(DeployConstants.TEMPLATE_DEP_YML, templateNode);

    kindRootNode.put(DeployConstants.SPEC_DEP_YML, specNode);

    String solutionDeployment = yamlMapper.writeValueAsString(kindRootNode);
    logger.debug("solutionDeployment " + solutionDeployment);

    return solutionDeployment;
  }

  public byte[] compositeSolutionDetails(DeploymentBean dBean) throws Exception {
    logger.debug("compositeSolutionDetails start");
    byte[] solutionZip = null;
    List<ContainerBean> contList = null;
    ParseJSON parseJson = new ParseJSON();
    /** Blueprint.json* */
    ByteArrayOutputStream byteArrayOutputStream =
        getBluePrintNexus(
            dBean.getSolutionId(),
            dBean.getRevisionId(),
            dBean.getDatasource(),
            dBean.getDataUserName(),
            dBean.getDataPd(),
            dBean.getNexusUrl(),
            dBean.getNexusUserName(),
            dBean.getNexusPd());
    logger.debug("byteArrayOutputStream " + byteArrayOutputStream);
    String jsonString = byteArrayOutputStream.toString();
    /*File file = new File("blueprint.json");
    String jsonString = FileUtils.readFileToString(file);*/
    // ByteArrayOutputStream byteArrayOutputStream=jsonString.getBytes();
    logger.debug("byteArrayOutputStream " + jsonString);
    dBean.setBluePrintjson(jsonString);
    /** Solution Name */
    String solutionName = parseJson.getSolutionName(jsonString);
    logger.debug("solutionName " + solutionName);
    dBean.setSolutionName(solutionName);
    /** Proto file code* */
    contList = parseJson.getProtoDetails(jsonString);
    logger.debug("contList " + contList);
    dBean.setContainerBeanList(contList);
    getprotoDetails(dBean.getContainerBeanList(), dBean);
    logger.debug("Proto Details");
    /** DataBroker* */
    getDataBrokerFile(dBean.getContainerBeanList(), dBean, jsonString);
    logger.debug("DataBrokerFile");
    /** SolutionYml and Datainfo json* */
    getSolutionYMLFile(dBean, jsonString);
    logger.debug("SolutionYMLFile");
    /** Deploy.sh * */
    /** Create Zip* */
    solutionZip = createCompositeSolutionZip(dBean);
    logger.debug("compositeSolutionDetails End");
    return solutionZip;
  }

  public ByteArrayOutputStream getBluePrintNexus(
      String solutionId,
      String revisionId,
      String datasource,
      String userName,
      String dataPd,
      String nexusUrl,
      String nexusUserName,
      String nexusPd)
      throws Exception {
    logger.debug(" getBluePrintNexus Start");
    logger.debug("solutionId " + solutionId);
    logger.debug("revisionId " + revisionId);
    List<MLPSolutionRevision> mlpSolutionRevisionList;
    String solutionRevisionId = revisionId;
    List<MLPArtifact> mlpArtifactList;
    String nexusURI = "";
    String bluePrintStr = "";
    ByteArrayOutputStream byteArrayOutputStream = null;
    CommonDataServiceRestClientImpl cmnDataService = getClient(datasource, userName, dataPd);
    if (null != solutionRevisionId) {
      // 3. Get the list of Artifiact for the SolutionId and SolutionRevisionId.
      mlpArtifactList = cmnDataService.getSolutionRevisionArtifacts(solutionId, solutionRevisionId);
      if (null != mlpArtifactList && !mlpArtifactList.isEmpty()) {
        nexusURI =
            mlpArtifactList.stream()
                .filter(
                    mlpArt ->
                        mlpArt
                            .getArtifactTypeCode()
                            .equalsIgnoreCase(DeployConstants.ARTIFACT_TYPE_BLUEPRINT))
                .findFirst()
                .get()
                .getUri();
        logger.debug(" Nexus URI : " + nexusURI);
        if (null != nexusURI) {
          NexusArtifactClient nexusArtifactClient =
              nexusArtifactClient(nexusUrl, nexusUserName, nexusPd);
          byteArrayOutputStream = nexusArtifactClient.getArtifact(nexusURI);
        }
      }
    }
    logger.debug("getBluePrintNexus End byteArrayOutputStream " + byteArrayOutputStream);
    return byteArrayOutputStream;
  }

  public List<ContainerBean> getprotoDetails(List<ContainerBean> contList, DeploymentBean dBean)
      throws Exception {
    if (contList != null) {
      int j = 0;
      while (contList.size() > j) {
        ContainerBean contbean = contList.get(j);
        if (contbean != null
            && contbean.getContainerName() != null
            && !"".equals(contbean.getContainerName())
            && contbean.getProtoUriPath() != null
            && !"".equals(contbean.getProtoUriPath())) {
          ByteArrayOutputStream byteArrayOutputStream =
              getNexusUrlFile(
                  dBean.getNexusUrl(),
                  dBean.getNexusUserName(),
                  dBean.getNexusPd(),
                  contbean.getProtoUriPath());
          logger.debug(
              contbean.getProtoUriPath() + "byteArrayOutputStream " + byteArrayOutputStream);
          contbean.setProtoUriDetails(byteArrayOutputStream.toString());
        }
        j++;
      }
    }
    return contList;
  }

  public ByteArrayOutputStream getNexusUrlFile(
      String nexusUrl, String nexusUserName, String nexusPassword, String nexusURI)
      throws Exception {
    logger.debug("getNexusUrlFile start");
    ByteArrayOutputStream byteArrayOutputStream = null;
    NexusArtifactClient nexusArtifactClient =
        nexusArtifactClient(nexusUrl, nexusUserName, nexusPassword);
    byteArrayOutputStream = nexusArtifactClient.getArtifact(nexusURI);
    logger.debug("byteArrayOutputStream " + byteArrayOutputStream);
    logger.debug("getNexusUrlFile ");
    return byteArrayOutputStream;
  }

  public void getDataBrokerFile(
      List<ContainerBean> contList, DeploymentBean dBean, String jsonString) throws Exception {
    ParseJSON parseJson = new ParseJSON();
    DataBrokerBean dataBrokerBean = parseJson.getDataBrokerContainer(jsonString);
    if (dataBrokerBean != null) {
      if (dataBrokerBean != null) {
        ByteArrayOutputStream byteArrayOutputStream =
            getNexusUrlFile(
                dBean.getNexusUrl(),
                dBean.getNexusUserName(),
                dBean.getNexusPd(),
                dataBrokerBean.getProtobufFile());
        logger.debug("byteArrayOutputStream " + byteArrayOutputStream);
        if (byteArrayOutputStream != null) {
          dataBrokerBean.setProtobufFile(byteArrayOutputStream.toString());
          dBean.setDataBrokerJson(byteArrayOutputStream.toString());
        } else {
          dataBrokerBean.setProtobufFile("");
          dBean.setDataBrokerJson("");
        }
      }
    }
  }

  public void getSolutionYMLFile(DeploymentBean dBean, String jsonString) throws Exception {
    logger.debug("Start getSolutionYMLFile");
    DataBrokerBean dataBrokerBean = null;
    ParseJSON parseJson = new ParseJSON();
    // CommonUtil cutil=new CommonUtil();
    String solutionYml = "";
    DockerInfo dockerInfo = new DockerInfo();
    List<DockerInfoBean> dockerInfoBeanList = new ArrayList<DockerInfoBean>();
    ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
    dataBrokerBean = parseJson.getDataBrokerContainer(jsonString);
    // ObjectMapper mapper = new ObjectMapper(new
    // YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));
    // ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    List<DeploymentKubeBean> deploymentKubeBeanList = parseJson.parseJsonFileImageMap(jsonString);
    boolean probeIndicator = parseJson.checkProbeIndicator(jsonString);
    if (probeIndicator) {
      logger.debug("probeIndicator " + probeIndicator);
      DeploymentKubeBean probeNginxBean = new DeploymentKubeBean();
      probeNginxBean.setContainerName(DeployConstants.PROBE_CONTAINER_NAME);
      probeNginxBean.setImage(dBean.getProbeImageName());
      probeNginxBean.setImagePort(dBean.getProbePort());
      probeNginxBean.setNodeType(DeployConstants.PROBE_CONTAINER_NAME);
      deploymentKubeBeanList.add(probeNginxBean);
    }
    DeploymentKubeBean depBlueprintBean = new DeploymentKubeBean();
    depBlueprintBean.setContainerName(DeployConstants.BLUEPRINT_MODELCONNECTOR_NAME);
    depBlueprintBean.setImage(dBean.getBluePrintImage());
    depBlueprintBean.setImagePort(dBean.getBluePrintPort());
    depBlueprintBean.setNodeType(DeployConstants.BLUEPRINT_CONTAINER_NAME);
    deploymentKubeBeanList.add(depBlueprintBean);

    int contPort = Integer.parseInt(dBean.getIncrementPort());
    DockerInfo dockerBluePrintInfo = new DockerInfo();

    Iterator itr = deploymentKubeBeanList.iterator();
    while (itr.hasNext()) {
      String portDockerInfo = "";
      DeploymentKubeBean depBen = (DeploymentKubeBean) itr.next();
      if (depBen != null
          && depBen.getContainerName() != null
          && !"".equals(depBen.getContainerName())
          && depBen.getImage() != null
          && !"".equals(depBen.getImage())
          && depBen.getNodeType() != null
          && !"".equals(depBen.getNodeType())) {

        String imagePort = "";
        if (depBen.getNodeType() != null) {
          if (depBen.getNodeType().equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)
              || depBen.getNodeType().equalsIgnoreCase(DeployConstants.DATABROKER_NAME)
              || depBen.getNodeType().equalsIgnoreCase(DeployConstants.PROBE_CONTAINER_NAME)) {
            imagePort = "";
          } else {
            imagePort = String.valueOf(contPort);
            contPort++;
          }
        } else {
          imagePort = String.valueOf(contPort);
          contPort++;
        }

        logger.debug("imagePort " + imagePort);
        String serviceYml =
            getCompositeSolutionService(
                depBen.getContainerName(), imagePort, depBen.getNodeType(), dBean);
        String deploymentYml =
            getCompositeSolutionDeployment(
                depBen.getImage(),
                depBen.getContainerName(),
                imagePort,
                depBen.getNodeType(),
                dBean);

        if (depBen.getNodeType() != null
            && depBen.getNodeType().equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)) {
          portDockerInfo = dBean.getBluePrintPort();
        } else if (depBen.getNodeType() != null
            && depBen.getNodeType().equalsIgnoreCase(DeployConstants.DATA_BROKER)) {
          portDockerInfo = dBean.getDataBrokerTargetPort();
        } else if (depBen.getNodeType() != null
            && depBen.getNodeType().equalsIgnoreCase(DeployConstants.PROBE_CONTAINER_NAME)) {
          portDockerInfo = dBean.getProbeTargetPort();
        } else {
          portDockerInfo = imagePort;
        }
        logger.debug("depBen.getNodeType() " + depBen.getNodeType());
        logger.debug("portDockerInfo " + portDockerInfo);
        logger.debug("serviceYml " + serviceYml);
        logger.debug("deploymentYml " + deploymentYml);
        solutionYml = solutionYml + serviceYml;
        solutionYml = solutionYml + deploymentYml;
        logger.debug("solutionYml " + solutionYml);

        DockerInfoBean dockerInfoBean = new DockerInfoBean();
        dockerInfoBean.setContainer(depBen.getContainerName());
        dockerInfoBean.setIpAddress(depBen.getContainerName());
        dockerInfoBean.setPort(portDockerInfo);
        dockerInfoBeanList.add(dockerInfoBean);
      }
    }
    logger.debug("Final solutionYml " + solutionYml);
    dBean.setSolutionYml(solutionYml);
    dockerInfo.setDockerInfolist(dockerInfoBeanList);
    if (dockerInfo != null) {
      ObjectMapper objMapper = new ObjectMapper();
      String dockerJson = objMapper.writeValueAsString(dockerInfo);
      logger.debug("dockerJson " + dockerJson);
      dBean.setDockerInfoJson(dockerJson);
    }
    if (dataBrokerBean != null) {
      ObjectMapper dataBrokerMapper = new ObjectMapper();
      String dataBrokerJson = dataBrokerMapper.writeValueAsString(dataBrokerBean);
      logger.debug("dataBrokerJson " + dataBrokerJson);
      dBean.setDataBrokerJson(dataBrokerJson);
    }
    logger.debug("End getSolutionYMLFile");
  }

  public String getCompositeSolutionService(
      String containerName, String imagePort, String nodeType, DeploymentBean dBen)
      throws Exception {
    logger.debug("getSingleSolutionService Start");
    String serviceYml = "";

    ObjectMapper objectMapper = new ObjectMapper();
    YAMLMapper yamlMapper =
        new YAMLMapper(new YAMLFactory().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true));
    ObjectNode apiRootNode = objectMapper.createObjectNode();
    apiRootNode.put(DeployConstants.APIVERSION_YML, DeployConstants.V_YML);
    apiRootNode.put(DeployConstants.KIND_YML, DeployConstants.SERVICE_YML);

    ObjectNode metadataNode = objectMapper.createObjectNode();
    metadataNode.put(DeployConstants.NAMESPACE_YML, DeployConstants.NAMESPACE_VALUE_YML);
    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)) {
      metadataNode.put(
          DeployConstants.NAME_YML,
          DeployConstants.BLUEPRINT_MODELCONNECTOR_NAME
              + "-"
              + DeployConstants.TRACKINGID_VALUE_YML);
    } else if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.DATA_BROKER)) {
      metadataNode.put(
          DeployConstants.NAME_YML,
          DeployConstants.DATABROKER_NAME_YML + "-" + DeployConstants.TRACKINGID_VALUE_YML);
    } else {
      metadataNode.put(
          DeployConstants.NAME_YML, containerName + "-" + DeployConstants.TRACKINGID_VALUE_YML);
    }
    apiRootNode.set(DeployConstants.METADATA_YML, metadataNode);

    ObjectNode specNode = objectMapper.createObjectNode();

    ObjectNode selectorNode = objectMapper.createObjectNode();
    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)) {
      selectorNode.put(
          DeployConstants.APP_YML,
          DeployConstants.BLUEPRINT_MODELCONNECTOR_NAME
              + "-"
              + DeployConstants.TRACKINGID_VALUE_YML);
    } else if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.DATA_BROKER)) {
      selectorNode.put(
          DeployConstants.APP_YML,
          DeployConstants.DATABROKER_NAME_YML + "-" + DeployConstants.TRACKINGID_VALUE_YML);
    } else {
      selectorNode.put(
          DeployConstants.APP_YML, containerName + "-" + DeployConstants.TRACKINGID_VALUE_YML);
    }
    specNode.set(DeployConstants.SELECTOR_YML, selectorNode);
    if (nodeType != null
        && (nodeType.equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)
            || nodeType.equalsIgnoreCase(DeployConstants.DATA_BROKER)
            || nodeType.equalsIgnoreCase(DeployConstants.PROBE_CONTAINER_NAME))) {
      specNode.put(DeployConstants.TYPE_YML, DeployConstants.NODE_TYPE_PORT_YML);
    } else {
      specNode.put(DeployConstants.TYPE_YML, DeployConstants.CLUSTERIP_YML);
    }
    ArrayNode portsArrayNode = specNode.arrayNode();
    ObjectNode portsNode = objectMapper.createObjectNode();

    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)) {
      portsNode.put(DeployConstants.NAME_YML, DeployConstants.NAME_MCAPI_YML);
    } else if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.DATA_BROKER)) {
      portsNode.put(DeployConstants.NAME_YML, DeployConstants.NAME_DATABROKER_YML);
    } else if (nodeType != null
        && nodeType.equalsIgnoreCase(DeployConstants.PROBE_CONTAINER_NAME)) {
      // NA
    } else {
      portsNode.put(DeployConstants.NAME_YML, DeployConstants.PROTOBUF_API_DEP_YML);
    }

    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)) {
      // portsNode.put(DeployConstants.NODEPORT_YML, dBen.getBluePrintNodePort());
    } else if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.DATA_BROKER)) {
      portsNode.put(DeployConstants.NODEPORT_YML, dBen.getDataBrokerModelPort());
    } else if (nodeType != null
        && nodeType.equalsIgnoreCase(DeployConstants.PROBE_CONTAINER_NAME)) {
      portsNode.put(DeployConstants.NODEPORT_YML, dBen.getProbeNodePort());
    }

    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)) {
      portsNode.put(DeployConstants.PORT_YML, dBen.getBluePrintPort());
      portsNode.put(DeployConstants.TARGETPORT_YML, dBen.getBluePrintPort());
    } else if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.DATA_BROKER)) {
      portsNode.put(DeployConstants.PORT_YML, dBen.getDataBrokerModelPort());
      portsNode.put(DeployConstants.TARGETPORT_YML, dBen.getDataBrokerTargetPort());
    } else if (nodeType != null
        && nodeType.equalsIgnoreCase(DeployConstants.PROBE_CONTAINER_NAME)) {
      portsNode.put(DeployConstants.PORT_YML, dBen.getProbeModelPort());
      portsNode.put(DeployConstants.TARGETPORT_YML, dBen.getProbeTargetPort());
    } else {
      portsNode.put(DeployConstants.PORT_YML, imagePort);
      portsNode.put(DeployConstants.TARGETPORT_YML, dBen.getMlTargetPort());
    }
    portsArrayNode.add(portsNode);
    specNode.set(DeployConstants.PORTS_YML, portsArrayNode);
    apiRootNode.set(DeployConstants.SPEC_YML, specNode);
    serviceYml = yamlMapper.writeValueAsString(apiRootNode);
    logger.debug("solutionDeployment " + serviceYml);
    return serviceYml;
  }

  public String getCompositeSolutionDeployment(
      String imageTag,
      String containerName,
      String imagePort,
      String nodeType,
      DeploymentBean dBean)
      throws Exception {
    logger.debug("getSingleSolutionDeployment Start");
    ObjectMapper objectMapper = new ObjectMapper();
    YAMLMapper yamlMapper =
        new YAMLMapper(new YAMLFactory().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true));
    ObjectNode kindRootNode = objectMapper.createObjectNode();
    kindRootNode.put(DeployConstants.APIVERSION_DEP_YML, DeployConstants.APPS_V1_DEP_YML);
    kindRootNode.put(DeployConstants.KIND_DEP_YML, DeployConstants.DEPLOYMENT_DEP_YML);

    ObjectNode metadataNode = objectMapper.createObjectNode();
    metadataNode.put(DeployConstants.NAMESPACE_DEP_YML, DeployConstants.NAMESPACE_VALUE_YML);
    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)) {
      metadataNode.put(
          DeployConstants.NAME_DEP_YML,
          DeployConstants.BLUEPRINT_MODELCONNECTOR_NAME
              + "-"
              + DeployConstants.TRACKINGID_VALUE_YML);
    } else if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.DATA_BROKER)) {
      metadataNode.put(
          DeployConstants.NAME_YML,
          DeployConstants.DATABROKER_NAME_YML + "-" + DeployConstants.TRACKINGID_VALUE_YML);
    } else {
      metadataNode.put(
          DeployConstants.NAME_DEP_YML, containerName + "-" + DeployConstants.TRACKINGID_VALUE_YML);
    }

    ObjectNode labelsNode = objectMapper.createObjectNode();

    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)) {
      labelsNode.put(
          DeployConstants.APP_DEP_YML,
          DeployConstants.BLUEPRINT_MODELCONNECTOR_NAME
              + "-"
              + DeployConstants.TRACKINGID_VALUE_YML);
      labelsNode.put(DeployConstants.TRACKINGID_YML, DeployConstants.TRACKINGID_VALUE_YML);
    } else if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.DATA_BROKER)) {
      labelsNode.put(
          DeployConstants.APP_DEP_YML,
          DeployConstants.DATABROKER_NAME_YML + "-" + DeployConstants.TRACKINGID_VALUE_YML);
      labelsNode.put(DeployConstants.TRACKINGID_YML, DeployConstants.TRACKINGID_VALUE_YML);
    } else {
      labelsNode.put(
          DeployConstants.APP_DEP_YML, containerName + "-" + DeployConstants.TRACKINGID_VALUE_YML);
      labelsNode.put(DeployConstants.TRACKINGID_YML, DeployConstants.TRACKINGID_VALUE_YML);
    }
    metadataNode.put(DeployConstants.LABELS_DEP_YML, labelsNode);

    kindRootNode.set(DeployConstants.METADATA_DEP_YML, metadataNode);

    ObjectNode specNode = objectMapper.createObjectNode();
    specNode.put(DeployConstants.REPLICAS_DEP_YML, 1);

    ObjectNode selectorNode = objectMapper.createObjectNode();
    ObjectNode matchLabelsNode = objectMapper.createObjectNode();
    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)) {
      matchLabelsNode.put(
          DeployConstants.APP_DEP_YML,
          DeployConstants.BLUEPRINT_MODELCONNECTOR_NAME
              + "-"
              + DeployConstants.TRACKINGID_VALUE_YML);
    } else if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.DATA_BROKER)) {
      matchLabelsNode.put(
          DeployConstants.APP_DEP_YML,
          DeployConstants.DATABROKER_NAME_YML + "-" + DeployConstants.TRACKINGID_VALUE_YML);
    } else {
      matchLabelsNode.put(
          DeployConstants.APP_DEP_YML, containerName + "-" + DeployConstants.TRACKINGID_VALUE_YML);
    }

    selectorNode.set(DeployConstants.MATCHLABELS_DEP_YML, matchLabelsNode);

    specNode.set(DeployConstants.SELECTOR_DEP_YML, selectorNode);

    ObjectNode templateNode = objectMapper.createObjectNode();
    ObjectNode metadataTemplateNode = objectMapper.createObjectNode();
    ObjectNode labelsTemplateNode = objectMapper.createObjectNode();
    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)) {
      labelsTemplateNode.put(
          DeployConstants.APP_DEP_YML,
          DeployConstants.BLUEPRINT_MODELCONNECTOR_NAME
              + "-"
              + DeployConstants.TRACKINGID_VALUE_YML);
      labelsTemplateNode.put(DeployConstants.TRACKINGID_YML, DeployConstants.TRACKINGID_VALUE_YML);
    } else if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.DATA_BROKER)) {
      labelsTemplateNode.put(
          DeployConstants.APP_DEP_YML,
          DeployConstants.DATABROKER_NAME_YML + "-" + DeployConstants.TRACKINGID_VALUE_YML);
      labelsTemplateNode.put(DeployConstants.TRACKINGID_YML, DeployConstants.TRACKINGID_VALUE_YML);
    } else {
      labelsTemplateNode.put(
          DeployConstants.APP_DEP_YML, containerName + "-" + DeployConstants.TRACKINGID_VALUE_YML);
      labelsTemplateNode.put(DeployConstants.TRACKINGID_YML, DeployConstants.TRACKINGID_VALUE_YML);
    }

    metadataTemplateNode.set(DeployConstants.LABELS_DEP_YML, labelsTemplateNode);

    ObjectNode specTempNode = objectMapper.createObjectNode();
    ArrayNode containerArrayNode = templateNode.arrayNode();
    ObjectNode containerNode = objectMapper.createObjectNode();
    ObjectNode containerNodeNginx = objectMapper.createObjectNode();
    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)) {
      containerNode.put(
          DeployConstants.NAME_DEP_YML,
          DeployConstants.BLUEPRINT_MODELCONNECTOR_NAME
              + "-"
              + DeployConstants.TRACKINGID_VALUE_YML);
    } else if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.DATA_BROKER)) {
      containerNode.put(
          DeployConstants.NAME_DEP_YML,
          DeployConstants.DATABROKER_NAME_YML + "-" + DeployConstants.TRACKINGID_VALUE_YML);
    } else {
      containerNode.put(
          DeployConstants.NAME_DEP_YML, containerName + "-" + DeployConstants.TRACKINGID_VALUE_YML);
    }

    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)) {
      containerNode.put(DeployConstants.IMAGE_DEP_YML, imageTag);
    } else if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.DATA_BROKER)) {
      // containerNode.put(DeployConstants.IMAGE_DEP_YML,
      // getProxyImageName(imageTag, dBean.getDockerProxyHost(), dBean.getDockerProxyPort()));
    } else if (nodeType != null
        && nodeType.equalsIgnoreCase(DeployConstants.PROBE_CONTAINER_NAME)) {
      containerNode.put(DeployConstants.IMAGE_DEP_YML, imageTag);
    } else {
      containerNode.put(DeployConstants.IMAGE_DEP_YML, imageTag);
    }

    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.PROBE_CONTAINER_NAME)) {
      ArrayNode envArrayNode = containerNode.arrayNode();
      ObjectNode envNode = objectMapper.createObjectNode();
      envNode.put(DeployConstants.NAME_DEP_YML, DeployConstants.NEXUSENDPOINT_URL);
      envNode.put(DeployConstants.VALUE, dBean.getNexusEndPointURL());
      ObjectNode envNodeExternal = objectMapper.createObjectNode();
      envNodeExternal.put(DeployConstants.NAME_DEP_YML, DeployConstants.ACUMOS_PROBE_EXTERNAL_PORT);
      envNodeExternal.put(DeployConstants.VALUE, "\"" + dBean.getProbeExternalPort() + "\"");
      envArrayNode.add(envNode);
      envArrayNode.add(envNodeExternal);
      containerNode.set(DeployConstants.ENV, envArrayNode);
    }

    ArrayNode portsArrayNode = containerNode.arrayNode();
    ObjectNode portsNode = objectMapper.createObjectNode();
    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)) {
      portsNode.put(DeployConstants.NAME_DEP_YML, DeployConstants.NAME_MCAPI_YML);
    } else if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.DATA_BROKER)) {
      // NA
    } else if (nodeType != null
        && nodeType.equalsIgnoreCase(DeployConstants.PROBE_CONTAINER_NAME)) {
      portsNode.put(DeployConstants.NAME_DEP_YML, DeployConstants.PROBEAPI_NAME);
    } else {
      portsNode.put(DeployConstants.NAME_DEP_YML, DeployConstants.PROTOBUF_API_DEP_YML);
    }
    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)) {
      portsNode.put(DeployConstants.CONTAINERPORT_DEP_YML, dBean.getBluePrintPort());
    } else if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.DATA_BROKER)) {
      portsNode.put(DeployConstants.CONTAINERPORT_DEP_YML, dBean.getDataBrokerTargetPort());
    } else if (nodeType != null
        && nodeType.equalsIgnoreCase(DeployConstants.PROBE_CONTAINER_NAME)) {
      portsNode.put(DeployConstants.CONTAINERPORT_DEP_YML, dBean.getProbeApiPort());
    } else {
      portsNode.put(DeployConstants.CONTAINERPORT_DEP_YML, dBean.getMlTargetPort());
    }

    portsArrayNode.add(portsNode);
    /*if(nodeType!=null && nodeType.equalsIgnoreCase(DeployConstants.PROBE_CONTAINER_NAME)){
    	ObjectNode portsNode2  = objectMapper.createObjectNode();
    	portsNode2.put(DeployConstants.NAME_DEP_YML, DeployConstants.PROBEAPI_NAME);
    	portsNode2.put(DeployConstants.CONTAINERPORT_DEP_YML, dBean.getProbeApiPort());
    	portsArrayNode.add(portsNode2);
    }*/

    containerNode.set(DeployConstants.PORTS_DEP_YML, portsArrayNode);
    // for Nginx
    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.PROBE_CONTAINER_NAME)) {
      containerNodeNginx.put(DeployConstants.NAME_DEP_YML, DeployConstants.NGINX_CONTAINER_NAME);
      containerNodeNginx.put(DeployConstants.IMAGE_DEP_YML, dBean.getNginxImageName());
      ArrayNode portSchemaArrayNode = containerNodeNginx.arrayNode();
      ObjectNode portSchemaNode = objectMapper.createObjectNode();
      portSchemaNode.put(DeployConstants.NAME_DEP_YML, DeployConstants.PROBE_SCHEMA_YML);
      portSchemaNode.put(DeployConstants.CONTAINERPORT_DEP_YML, dBean.getProbeSchemaPort());
      portSchemaArrayNode.add(portSchemaNode);
      containerNodeNginx.set(DeployConstants.PORTS_DEP_YML, portSchemaArrayNode);
    }
    // BLUEPRINT or DataBroker
    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)) {
      ArrayNode volmeMountArrayNode = containerNode.arrayNode();
      ObjectNode volumeMountNode = objectMapper.createObjectNode();
      volumeMountNode.put(DeployConstants.MOUNTPATH_DEP_YML, DeployConstants.PATHLOGS_DEP_YML);
      volumeMountNode.put(DeployConstants.NAME_DEP_YML, DeployConstants.LOGS_DEP_YML);
      volmeMountArrayNode.add(volumeMountNode);
      containerNode.set(DeployConstants.VOLUMEMOUNTS_DEP_YML, volmeMountArrayNode);
    }
    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.DATABROKER_NAME)) {
      ArrayNode volmeMountArrayNode = containerNode.arrayNode();
      ObjectNode volumeMountNode = objectMapper.createObjectNode();
      volumeMountNode.put(
          DeployConstants.MOUNTPATH_DEP_YML, DeployConstants.DATABROKER_PATHLOG_DEP_YML);
      volumeMountNode.put(DeployConstants.NAME_DEP_YML, DeployConstants.DATABROKER_LOGNAME);
      volmeMountArrayNode.add(volumeMountNode);
      containerNode.set(DeployConstants.VOLUMEMOUNTS_DEP_YML, volmeMountArrayNode);
    }
    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.PROBE_CONTAINER_NAME)) {
      ArrayNode volmeMountArrayNode = containerNodeNginx.arrayNode();
      ObjectNode volumeMountNode = objectMapper.createObjectNode();
      volumeMountNode.put(
          DeployConstants.MOUNTPATH_DEP_YML, DeployConstants.PROBE_MOUNTPATH_DEP_YML);
      volumeMountNode.put(DeployConstants.NAME_DEP_YML, DeployConstants.VOLUME_PROTO_YML);
      volmeMountArrayNode.add(volumeMountNode);
      containerNodeNginx.set(DeployConstants.VOLUMEMOUNTS_DEP_YML, volmeMountArrayNode);
    }
    // Finish

    ObjectNode imagePullSecretsNode = objectMapper.createObjectNode();
    ArrayNode imageSecretArrayNode = containerNode.arrayNode();
    imagePullSecretsNode.put(DeployConstants.NAME_DEP_YML, DeployConstants.ACUMOS_REGISTRY_DEP_YML);
    imageSecretArrayNode.add(imagePullSecretsNode);
    specTempNode.set(DeployConstants.IMAGEPULLSECRETS_DEP_YML, imageSecretArrayNode);
    containerArrayNode.add(containerNode);
    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.PROBE_CONTAINER_NAME)) {
      containerArrayNode.add(containerNodeNginx);
    }
    specTempNode.set(DeployConstants.CONTAINERS_DEP_YML, containerArrayNode);
    // BLUEPRINT or DataBroker
    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.BLUEPRINT_CONTAINER)) {
      ArrayNode volumeArrNode = templateNode.arrayNode();
      ObjectNode volumeNode = objectMapper.createObjectNode();
      volumeNode.put(DeployConstants.NAME_DEP_YML, DeployConstants.LOGS_DEP_YML);
      volumeArrNode.add(volumeNode);
      ObjectNode hostPathNode = objectMapper.createObjectNode();
      hostPathNode.put(DeployConstants.PATH_DEP_YML, DeployConstants.ACUMOSPATHLOG_DEP_YML);
      volumeNode.put(DeployConstants.HOSTPATH_DEP_YML, hostPathNode);
      specTempNode.put(DeployConstants.RESTARTPOLICY_DEP_YML, DeployConstants.ALWAYS_DEP_YML);
      specTempNode.set(DeployConstants.VOLUMES_DEP_YML, volumeArrNode);
    }
    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.DATABROKER_NAME)) {

      ArrayNode volumeArrNode = templateNode.arrayNode();
      ObjectNode volumeNode = objectMapper.createObjectNode();
      volumeNode.put(DeployConstants.NAME_DEP_YML, DeployConstants.DATABROKER_LOGNAME);
      volumeArrNode.add(volumeNode);
      ObjectNode hostPathNode = objectMapper.createObjectNode();
      hostPathNode.put(DeployConstants.PATH_DEP_YML, DeployConstants.DATABROKER_PATHLOG_DEP_YML);
      volumeNode.put(DeployConstants.HOSTPATH_DEP_YML, hostPathNode);
      specTempNode.put(DeployConstants.RESTARTPOLICY_DEP_YML, DeployConstants.ALWAYS_DEP_YML);
      specTempNode.set(DeployConstants.VOLUMES_DEP_YML, volumeArrNode);
    }
    if (nodeType != null && nodeType.equalsIgnoreCase(DeployConstants.PROBE_CONTAINER_NAME)) {

      ArrayNode volumeArrNode = templateNode.arrayNode();
      ObjectNode volumeNode = objectMapper.createObjectNode();
      volumeNode.put(DeployConstants.NAME_DEP_YML, DeployConstants.VOLUME_PROTO_YML);
      volumeArrNode.add(volumeNode);
      ObjectNode hostPathNode = objectMapper.createObjectNode();
      hostPathNode.put(DeployConstants.PATH_DEP_YML, DeployConstants.PROBE_PATHLOG_DEP_YML);
      volumeNode.put(DeployConstants.HOSTPATH_DEP_YML, hostPathNode);
      specTempNode.put(DeployConstants.RESTARTPOLICY_DEP_YML, DeployConstants.ALWAYS_DEP_YML);
      specTempNode.set(DeployConstants.VOLUMES_DEP_YML, volumeArrNode);
    }
    // Finish

    templateNode.set(DeployConstants.METADATA_DEP_YML, metadataTemplateNode);
    templateNode.set(DeployConstants.SPEC_DEP_YML, specTempNode);
    specNode.set(DeployConstants.TEMPLATE_DEP_YML, templateNode);

    kindRootNode.put(DeployConstants.SPEC_DEP_YML, specNode);

    String solutionDeployment = yamlMapper.writeValueAsString(kindRootNode);
    logger.debug("before " + solutionDeployment);
    solutionDeployment = solutionDeployment.replace("'", "");
    logger.debug("After " + solutionDeployment);

    return solutionDeployment;
  }

  public String getProxyImageName(
      String imageName, String dockerProxyHost, String dockerProxyPort) {
    logger.debug("Start-geProxyImageName " + imageName);
    String dockerImage = "";
    String image = "";
    if (imageName != null) {
      String imageArr[] = imageName.split("/");
      if (imageArr != null && imageArr[1] != null) {
        image = imageArr[1];
      }
    }
    logger.debug("image " + image);
    if (image != null && !"".equals(image) && dockerProxyHost != null && dockerProxyPort != null) {
      dockerImage = dockerProxyHost + ":" + dockerProxyPort + "/" + image;
    }

    logger.debug(" end geProxyImageName dockerImage" + dockerImage);
    return dockerImage;
  }

  public MLPTask createTaskDetails(DeployBean deployBean, DeploymentBean dBean) throws Exception {
    logger.debug("createTaskDetails start");
    MLPTask mlpTask = new MLPTask();
    String trackingID = UUID.randomUUID().toString();
    logger.debug("trackingID " + trackingID);
    mlpTask.setTaskCode("DP");
    mlpTask.setStatusCode("ST");
    mlpTask.setName("DEP-" + dBean.getEnvId());
    mlpTask.setCreated(Instant.now());
    mlpTask.setModified(Instant.now());
    mlpTask.setTrackingId(trackingID);
    mlpTask.setUserId(deployBean.getUserId());
    mlpTask.setSolutionId(deployBean.getSolutionId());
    mlpTask.setRevisionId(deployBean.getRevisionId());
    CommonDataServiceRestClientImpl cmnDataService =
        getClient(dBean.getDatasource(), dBean.getDataUserName(), dBean.getDataPd());
    mlpTask = cmnDataService.createTask(mlpTask);
    // mlpTask.setTaskId(Long.getLong("3155"));
    logger.debug("mlpTask.getTaskId() " + mlpTask.getTaskId());
    logger.debug("createTaskDetails end");
    return mlpTask;
  }

  public MLPTask getTaskDetails(
      String datasource, String userName, String dataPd, long taskIdNum, DeploymentBean dBean)
      throws Exception {
    logger.debug("getTaskDetails start");
    MLPTask mlpTask = null;
    CommonDataServiceRestClientImpl cmnDataService = getClient(datasource, userName, dataPd);
    mlpTask = cmnDataService.getTask(taskIdNum);
    if (mlpTask != null && dBean != null) {
      logger.debug("mlpTask.getSolutionId() " + mlpTask.getSolutionId());
      if (mlpTask.getSolutionId() != null && !"".equalsIgnoreCase(mlpTask.getSolutionId().trim())) {
        dBean.setSolutionId(mlpTask.getSolutionId().trim());
      }
      logger.debug("mlpTask.getRevisionId() " + mlpTask.getRevisionId());
      if (mlpTask.getRevisionId() != null && !"".equalsIgnoreCase(mlpTask.getRevisionId().trim())) {
        dBean.setRevisionId(mlpTask.getRevisionId().trim());
      }
      logger.debug("mlpTask.getTrackingId() " + mlpTask.getTrackingId());
      if (mlpTask.getTrackingId() != null && !"".equalsIgnoreCase(mlpTask.getTrackingId().trim())) {
        dBean.setTrackingId(mlpTask.getTrackingId().trim());
      }
      logger.debug("mlpTask.getTaskId() " + mlpTask.getTaskId());
      if (mlpTask.getTaskId() != null) {
        dBean.setTaskId(mlpTask.getTaskId().toString());
      }
      logger.debug("mlpTask.getName() " + mlpTask.getName());
      if (mlpTask.getName() != null && !"".equalsIgnoreCase(mlpTask.getName().trim())) {
        dBean.setEnvId(mlpTask.getName().trim().substring(4));
      }
    }
    logger.debug("getTaskDetails end");
    return mlpTask;
  }

  public void updateTaskDetails(
      String datasource,
      String userName,
      String dataPd,
      long taskIdNum,
      String status,
      String reason,
      String ingress,
      MLPTask mlpTask)
      throws Exception {
    logger.debug("updateTaskDetails Start");
    CommonDataServiceRestClientImpl cmnDataService = getClient(datasource, userName, dataPd);
    mlpTask.setStatusCode(status);
    mlpTask.setModified(Instant.now());
    cmnDataService.updateTask(mlpTask);
    logger.debug("updated task Id " + taskIdNum);
    generateNotification(
      reason,
      mlpTask.getUserId(),
      datasource,
      userName,
      dataPd);
    logger.debug("updateTaskDetails End");
  }

  public void generateNotification(
      String msg, String userId, String dataSource, String dataUserName, String dataPassword)
      throws Exception {
    logger.debug("generateNotification Start");
    logger.debug("userId " + userId + "msg " + msg);
    MLPNotification notification = new MLPNotification();
    try {
      if (msg != null) {
        notification.setTitle("Solution Deployment Update");
        notification.setMessage(msg);
        Instant startDate = Instant.now();
        Instant endDate = startDate.plus(Period.ofDays(365));
        notification.setStart(startDate);
        notification.setEnd(endDate);
        notification.setCreated(startDate);
        CommonDataServiceRestClientImpl client = getClient(dataSource, dataUserName, dataPassword);
        notification.setMsgSeverityCode(DeployConstants.MSG_SEVERITY_ME);
        MLNotification mLNotification =
            createNotification(notification, dataSource, dataUserName, dataPassword);
        logger.debug("mLNotification.getNotificationId() " + mLNotification.getNotificationId());
        client.addUserToNotification(mLNotification.getNotificationId(), userId);
      }
    } catch (Exception e) {
      logger.error("generateNotification failed", e);
      throw e;
    }
    logger.debug("generateNotification End");
  }

  public org.acumos.deploymentclient.bean.MLNotification createNotification(
      MLPNotification mlpNotification,
      String dataSource,
      String dataUserName,
      String dataPassword) {
    logger.debug("createNotification Start");
    CommonDataServiceRestClientImpl client = getClient(dataSource, dataUserName, dataPassword);
    MLNotification mlNotification =
        convertToMLNotification(client.createNotification(mlpNotification));
    logger.debug("createNotification End");
    return mlNotification;
  }

  public static MLNotification convertToMLNotification(MLPNotification mlpNotification) {
    MLNotification mlNotification = new MLNotification();
    if (!isEmptyOrNullString(mlpNotification.getNotificationId())) {
      mlNotification.setNotificationId(mlpNotification.getNotificationId());
    }
    if (!isEmptyOrNullString(mlpNotification.getTitle())) {
      mlNotification.setTitle(mlpNotification.getTitle());
    }
    if (!isEmptyOrNullString(mlpNotification.getMessage())) {
      mlNotification.setMessage(mlpNotification.getMessage());
    }
    if (!isEmptyOrNullString(mlpNotification.getUrl())) {
      mlNotification.setUrl(mlpNotification.getUrl());
    }
    if (mlpNotification.getStart() != null) {
      mlNotification.setStart(mlpNotification.getStart());
    }
    if (mlpNotification.getEnd() != null) {
      mlNotification.setEnd(mlpNotification.getEnd());
    }
    return mlNotification;
  }

  public static boolean isEmptyOrNullString(String input) {
    boolean isEmpty = false;
    if (null == input || 0 == input.trim().length()) {
      isEmpty = true;
    }
    return isEmpty;
  }

  public void createJenkinTask(DeploymentBean dBean, String taskId, String jobName)
      throws Exception {
    logger.debug("createJenkinTask Start");
    JenkinsJobBuilder jobBuilder = new JenkinsJobBuilder();
    jobBuilder.buildJenkinsJob(
        dBean.getJenkinUrl(),
        dBean.getJenkinUserName(),
        dBean.getJenkinPassword(),
        jobName,
        taskId);
    logger.debug("createJenkinTask End");
  }

  public void setDeploymentBeanProperties(DeploymentBean dBean, Environment env) throws Exception {
    logger.debug("setDeploymentBeanProperties Start");

    String bluePrintImage =
        (env.getProperty(DeployConstants.BLUEPRINT_IMAGENAME_PROP) != null)
            ? env.getProperty(DeployConstants.BLUEPRINT_IMAGENAME_PROP)
            : "";
    String bluePrintPort =
        (env.getProperty(DeployConstants.BLUEPRINT_PORT_PROP) != null)
            ? env.getProperty(DeployConstants.BLUEPRINT_PORT_PROP)
            : "";
    String bluePrintNodePort =
        (env.getProperty(DeployConstants.BLUEPRINT_NODEPORT_PROP) != null)
            ? env.getProperty(DeployConstants.BLUEPRINT_NODEPORT_PROP)
            : "";

    String probeModelPort =
        (env.getProperty(DeployConstants.PROBE_MODEL_PORT) != null)
            ? env.getProperty(DeployConstants.PROBE_MODEL_PORT)
            : "";
    // String probeNodePort=(env.getProperty(DeployConstants.PROBE_NODE_PORT) != null) ?
    // env.getProperty(DeployConstants.PROBE_NODE_PORT) : "";
    String probeTargetPort =
        (env.getProperty(DeployConstants.PROBE_TARGET_PORT) != null)
            ? env.getProperty(DeployConstants.PROBE_TARGET_PORT)
            : "";
    String probeApiPort =
        (env.getProperty(DeployConstants.PROBE_API_PORT) != null)
            ? env.getProperty(DeployConstants.PROBE_API_PORT)
            : "";
    String probeImageName =
        (env.getProperty(DeployConstants.PROBEIMAGE_NAME) != null)
            ? env.getProperty(DeployConstants.PROBEIMAGE_NAME)
            : "";

    String singleModelPort =
        (env.getProperty(DeployConstants.SINGLE_MODEL_PORT) != null)
            ? env.getProperty(DeployConstants.SINGLE_MODEL_PORT)
            : "";
    // String singleNodePort=(env.getProperty(DeployConstants.SINGLE_NODE_PORT) != null) ?
    // env.getProperty(DeployConstants.SINGLE_NODE_PORT) : "";
    String singleTargetPort =
        (env.getProperty(DeployConstants.SINGLE_TARGET_PORT) != null)
            ? env.getProperty(DeployConstants.SINGLE_TARGET_PORT)
            : "";
    String incrementPort =
        (env.getProperty(DeployConstants.INCREMENT_PORT) != null)
            ? env.getProperty(DeployConstants.INCREMENT_PORT)
            : "";
    String folderPath =
        (env.getProperty(DeployConstants.FOLDERPATH) != null)
            ? env.getProperty(DeployConstants.FOLDERPATH)
            : "";

    String dataBrokerModelPort =
        (env.getProperty(DeployConstants.DATABROKER_MODEL_PORT) != null)
            ? env.getProperty(DeployConstants.DATABROKER_MODEL_PORT)
            : "";
    // String dataBrokerNodePort=(env.getProperty(DeployConstants.DATABROKER_NODE_PORT) != null) ?
    // env.getProperty(DeployConstants.DATABROKER_NODE_PORT) : "";
    String dataBrokerTargetPort =
        (env.getProperty(DeployConstants.DATABROKER_TARGET_PORT) != null)
            ? env.getProperty(DeployConstants.DATABROKER_TARGET_PORT)
            : "";

    String nginxImageName =
        (env.getProperty(DeployConstants.NGINX_IMAGE_NAME) != null)
            ? env.getProperty(DeployConstants.NGINX_IMAGE_NAME)
            : "";

    String nexusUrl =
        (env.getProperty(DeployConstants.NEXUS_URL_PROP) != null)
            ? env.getProperty(DeployConstants.NEXUS_URL_PROP)
            : "";
    String nexusUsername =
        (env.getProperty(DeployConstants.NEXUS_USERNAME_PROP) != null)
            ? env.getProperty(DeployConstants.NEXUS_USERNAME_PROP)
            : "";
    String nexusPd =
        (env.getProperty(DeployConstants.NEXUS_PD_PROP) != null)
            ? env.getProperty(DeployConstants.NEXUS_PD_PROP)
            : "";

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
    String jenkinUrl =
        (env.getProperty(DeployConstants.JENKINS_URL_PROP) != null)
            ? env.getProperty(DeployConstants.JENKINS_URL_PROP)
            : "";
    String jenkinUserName =
        (env.getProperty(DeployConstants.JENKINS_USER_PROP) != null)
            ? env.getProperty(DeployConstants.JENKINS_USER_PROP)
            : "";
    String jenkinPassword =
        (env.getProperty(DeployConstants.JENKINS_PASSWORD_PROP) != null)
            ? env.getProperty(DeployConstants.JENKINS_PASSWORD_PROP)
            : "";

    String jenkinJob =
        (env.getProperty(DeployConstants.JENKINS_JOB) != null)
            ? env.getProperty(DeployConstants.JENKINS_JOB)
            : "";
    String jenkinJobSimple =
        (env.getProperty(DeployConstants.JENKINS_JOBSIMPLE_PROP) != null)
            ? env.getProperty(DeployConstants.JENKINS_JOBSIMPLE_PROP)
            : "";
    String jenkinJobComposite =
        (env.getProperty(DeployConstants.JENKINS_JOBCOMPOSITE_PROP) != null)
            ? env.getProperty(DeployConstants.JENKINS_JOBCOMPOSITE_PROP)
            : "";
    // jenkinJobNifi=(env.getProperty(DeployConstants.JENKINS_JOBNIFI_PROP) != null) ?
    // env.getProperty(DeployConstants.JENKINS_JOBNIFI_PROP) : "";

    String logstashIP =
        (env.getProperty(DeployConstants.LOGSTASH_IP) != null)
            ? env.getProperty(DeployConstants.LOGSTASH_IP)
            : "";
    String logstashPort =
        (env.getProperty(DeployConstants.LOGSTASH_PORT) != null)
            ? env.getProperty(DeployConstants.LOGSTASH_PORT)
            : "";
    String logstashHost =
        (env.getProperty(DeployConstants.LOGSTASH_HOST) != null)
            ? env.getProperty(DeployConstants.LOGSTASH_HOST)
            : "";
    String acumosRegistryName =
        (env.getProperty(DeployConstants.DOCKER_REGISTRY_NAME) != null)
            ? env.getProperty(DeployConstants.DOCKER_REGISTRY_NAME)
            : "";
    String acumosRegistryUser =
        (env.getProperty(DeployConstants.DOCKER_REGISTRY_USER) != null)
            ? env.getProperty(DeployConstants.DOCKER_REGISTRY_USER)
            : "";
    String acumosRegistryPd =
        (env.getProperty(DeployConstants.DOCKER_REGISTRY_PD) != null)
            ? env.getProperty(DeployConstants.DOCKER_REGISTRY_PD)
            : "";
    String templateYmlDirectory =
        (env.getProperty(DeployConstants.TEMPLATE_YML_DIRECTORY) != null)
            ? env.getProperty(DeployConstants.TEMPLATE_YML_DIRECTORY)
            : "";
    String mlTargetPort =
        (env.getProperty(DeployConstants.ML_TARGET_PORT) != null)
            ? env.getProperty(DeployConstants.ML_TARGET_PORT)
            : "";
    String deploymentClientApiBaseUrl =
        (env.getProperty(DeployConstants.DEPLOYMENT_CLIENT_API_BASE_URL) != null)
            ? env.getProperty(DeployConstants.DEPLOYMENT_CLIENT_API_BASE_URL)
            : "";

    dBean.setDeploymentClientApiBaseUrl(deploymentClientApiBaseUrl);
    dBean.setMlTargetPort(mlTargetPort);
    dBean.setBluePrintImage(bluePrintImage);
    dBean.setBluePrintPort(bluePrintPort);
    dBean.setBluePrintNodePort(bluePrintNodePort);

    dBean.setProbeModelPort(probeModelPort);
    // dBean.setProbeNodePort(probeNodePort);
    dBean.setProbeTargetPort(probeTargetPort);
    dBean.setProbeApiPort(probeApiPort);
    dBean.setProbeImageName(probeImageName);

    dBean.setSingleModelPort(singleModelPort);
    // dBean.setSingleNodePort(singleNodePort);
    dBean.setSingleTargetPort(singleTargetPort);
    dBean.setIncrementPort(incrementPort);
    dBean.setFolderPath(folderPath);

    dBean.setDataBrokerModelPort(dataBrokerModelPort);
    // dBean.setDataBrokerNodePort(dataBrokerNodePort);
    dBean.setDataBrokerTargetPort(dataBrokerTargetPort);

    dBean.setNginxImageName(nginxImageName);
    dBean.setNexusUrl(nexusUrl);
    dBean.setNexusUserName(nexusUsername);
    dBean.setNexusPd(nexusPd);

    dBean.setDatasource(cmnDataUrl);
    dBean.setDataUserName(cmnDataUser);
    dBean.setDataPd(cmnDataPd);
    dBean.setJenkinUrl(jenkinUrl);
    dBean.setJenkinUserName(jenkinUserName);
    dBean.setJenkinPassword(jenkinPassword);

    dBean.setJenkinJobSimple(jenkinJobSimple);
    dBean.setJenkinJobComposite(jenkinJobComposite);
    dBean.setJenkinJobNifi("nifi");

    dBean.setLogstashIP(logstashIP);
    dBean.setLogstashHost(logstashHost);
    dBean.setLogstashPort(logstashPort);
    dBean.setAcumosRegistryName(acumosRegistryName);
    dBean.setAcumosRegistryUser(acumosRegistryUser);
    dBean.setAcumosRegistryPd(acumosRegistryPd);
    dBean.setTemplateYmlDirectory(templateYmlDirectory);

    logger.debug("deploymentClientApiBaseUrl: "+dBean.getDeploymentClientApiBaseUrl());
    logger.debug("mlTargetPort: "+dBean.getMlTargetPort());
    logger.debug("bluePrintImage: "+dBean.getBluePrintImage());
    logger.debug("bluePrintPort: "+dBean.getBluePrintPort());
    logger.debug("bluePrintNodePort: "+dBean.getBluePrintNodePort());
    logger.debug("probeModelPort: "+dBean.getProbeModelPort());
    logger.debug("probeTargetPort: "+dBean.getProbeTargetPort());
    logger.debug("probeApiPort: "+dBean.getProbeApiPort());
    logger.debug("probeImageName: "+dBean.getProbeImageName());
    logger.debug("singleModelPort: "+dBean.getSingleModelPort());
    logger.debug("singleTargetPort: "+dBean.getSingleTargetPort());
    logger.debug("incrementPort: "+dBean.getIncrementPort());
    logger.debug("folderPath: "+dBean.getFolderPath());
    logger.debug("dataBrokerModelPort: "+dBean.getDataBrokerModelPort());
    logger.debug("dataBrokerTargetPort: "+dBean.getDataBrokerTargetPort());
    logger.debug("nginxImageName: "+dBean.getNginxImageName());
    logger.debug("nexusUrl: "+dBean.getNexusUrl());
    logger.debug("nexusUsername: "+dBean.getNexusUserName());
    //logger.debug("nexusPd: "+dBean.getNexusPd());
    logger.debug("cmnDataUrl: "+dBean.getDatasource());
    logger.debug("cmnDataUser: "+dBean.getDataUserName());
    //logger.debug("cmnDataPd: "+dBean.getDataPd());
    logger.debug("jenkinUrl: "+dBean.getJenkinUrl());
    logger.debug("jenkinUserName: "+dBean.getJenkinUserName());
    //logger.debug("jenkinPassword: "+dBean.getJenkinPassword());
    logger.debug("jenkinJobSimple: "+dBean.getJenkinJobSimple());
    logger.debug("jenkinJobComposite: "+dBean.getJenkinJobComposite());
    logger.debug("jenkinJobComposite: "+dBean.getJenkinJobNifi());
    logger.debug("logstashIP: "+dBean.getLogstashIP());
    logger.debug("logstashHost: "+dBean.getLogstashHost());
    logger.debug("logstashPort: "+dBean.getLogstashPort());
    logger.debug("acumosRegistryName: "+dBean.getAcumosRegistryName());
    logger.debug("acumosRegistryUser: "+dBean.getAcumosRegistryUser());
    //logger.debug("acumosRegistryPd: "+dBean.getAcumosRegistryPd());
    logger.debug("templateYmlDirectory: "+dBean.getTemplateYmlDirectory());
    //logger.debug(dBean.getDataBrokerNodePort());

    logger.debug("setDeploymentBeanProperties End");
  }

  public String createEnvFile(DeploymentBean dBean, String solType) throws Exception {
    logger.debug("createEnvFile Start");
    StringBuffer envBuffer = new StringBuffer();
    String simpEnv = "";
    String compEnv = "";
    String setEnvDeploy =
        ""
            + "#!/bin/bash \n"
            + "export DEPLOYMENT_CLIENT_API_BASE_URL="
            + dBean.getDeploymentClientApiBaseUrl()
            + " \n"
            + "export ACUMOS_DOCKER_REGISTRY="
            + dBean.getAcumosRegistryName()
            + " \n"
            + "export ACUMOS_DOCKER_REGISTRY_USER="
            + dBean.getAcumosRegistryUser()
            + " \n"
            + "export ACUMOS_DOCKER_REGISTRY_PASSWORD="
            + dBean.getAcumosRegistryPd()
            + " \n"
            + "export K8S_CLUSTER="+dBean.getEnvId()
            //+ "export K8S_CLUSTER=default"
            + "\n"
            + "export TRACKING_ID="
            + dBean.getTrackingId()
            + "\n"
            + "export TASK_ID="
            + dBean.getTaskId()
            + "\n"
            + "export SOLUTION_TYPE="
            + solType
            + "\n"
            + "export SOLUTION_NAME="
            + dBean.getSolutionName()
            + " \n"
            // TODO: figure out how to determine the actual model runner version
            + "export SOLUTION_MODEL_RUNNER_STANDARD=v2\n"
            + "export LOGSTASH_HOST="
            + dBean.getLogstashHost()
            + "\n"
            + "export LOGSTASH_IP="
            + dBean.getLogstashIP()
            + "\n"
            + "export LOGSTASH_PORT="
            + dBean.getLogstashPort()
            + "\n";

    if (solType != null && "simple".equalsIgnoreCase(solType)) {
      simpEnv = "export SOLUTION_ID=" + dBean.getSolutionId() + " \n";
    } else {
      compEnv =
          "export COMP_SOLUTION_ID="
              + dBean.getLogstashPort()
              + " \n"
              + "export COMP_REVISION_ID="
              + dBean.getLogstashPort()
              + " \n";
    }
    envBuffer.append(setEnvDeploy);
    envBuffer.append(simpEnv);
	logger.debug("dBean.getEnvId()" + dBean.getEnvId());
    logger.debug("createEnvFile End" + setEnvDeploy);
    return setEnvDeploy;
  }

  public byte[] createSingleSolutionZip(DeploymentBean dBean) throws Exception {

    byte[] buffer = new byte[1024];
    ByteArrayOutputStream baos = null;
    ArrayList<String> filesListInDir = new ArrayList<String>();
    logger.debug("tempDirName" + dBean.getTemplateYmlDirectory());
    HashMap<String, ByteArrayOutputStream> hmap = new HashMap<String, ByteArrayOutputStream>();
    ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
    // CommonUtil util=new CommonUtil();
    if (dBean != null) {

      bOutput = new ByteArrayOutputStream(12);
      String deployFile = dBean.getFolderPath() + DeployConstants.KUBE_DEPLOY_SH;
      String deployScript = getFileDetails(deployFile);
      if (deployScript != null && !"".equals(deployScript)) {
        bOutput.write(deployScript.getBytes());
        hmap.put(DeployConstants.KUBE_DEPLOY_SH, bOutput);
        logger.debug(DeployConstants.KUBE_DEPLOY_SH + "   " + bOutput);
      }
      bOutput = new ByteArrayOutputStream(12);
      String tempEnvFile = createEnvFile(dBean, "simple");
      if (tempEnvFile != null && !"".equals(tempEnvFile)) {
        bOutput.write(tempEnvFile.getBytes());
        hmap.put(DeployConstants.KUBE_DEPLOY_ENV_SH, bOutput);
        logger.debug(DeployConstants.KUBE_DEPLOY_ENV_SH + " " + bOutput);
      }

      if (dBean.getSolutionYml() != null && !"".equals(dBean.getSolutionYml())) {
        bOutput = new ByteArrayOutputStream(12);
        bOutput.write(dBean.getSolutionYml().getBytes());
        hmap.put(DeployConstants.KUBE_SOLUTION_YML, bOutput);
        logger.debug(DeployConstants.KUBE_SOLUTION_YML + "  " + bOutput);
      }
    }

    baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);
    Iterator it = hmap.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pair = (Map.Entry) it.next();
      String fileName = (String) pair.getKey();
      ByteArrayOutputStream ba = (ByteArrayOutputStream) pair.getValue();

      ZipEntry ze = new ZipEntry(fileName);
      zos.putNextEntry(ze);
      InputStream in = new ByteArrayInputStream(ba.toByteArray());
      int len;
      while ((len = in.read(buffer)) > 0) {
        zos.write(buffer, 0, len);
      }
      in.close();
    }
    File dir = new File(dBean.getTemplateYmlDirectory());
    filesListInDir = populateFilesList(dir, filesListInDir);

    for (String filePath : filesListInDir) {
      logger.debug("Zipping " + filePath);
      // for ZipEntry we need to keep only relative file path, so we used substring on absolute path
      ZipEntry ze =
          new ZipEntry(
              "templates/"
                  + filePath.substring(dir.getAbsolutePath().length() + 1, filePath.length()));
      zos.putNextEntry(ze);
      // read the file and write to ZipOutputStream
      FileInputStream fis = new FileInputStream(filePath);
      int len;
      while ((len = fis.read(buffer)) > 0) {
        zos.write(buffer, 0, len);
      }
      fis.close();
    }

    zos.closeEntry();
    zos.close();
    logger.debug("Done");

    return baos.toByteArray();
  }

  public ArrayList<String> populateFilesList(File dir, ArrayList<String> filesListInDir)
      throws Exception {
    File[] files = dir.listFiles();
    for (File file : files) {
      if (file.isFile()) filesListInDir.add(file.getAbsolutePath());
      else populateFilesList(file, filesListInDir);
    }
    return filesListInDir;
  }

  public byte[] createCompositeSolutionZip(DeploymentBean dBean) throws Exception {

    byte[] buffer = new byte[1024];
    ByteArrayOutputStream baos = null;
    ArrayList<String> filesListInDir = new ArrayList<String>();
    HashMap<String, ByteArrayOutputStream> hmap = new HashMap<String, ByteArrayOutputStream>();
    ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);
    // CommonUtil util=new CommonUtil();
    if (dBean != null) {

      bOutput = new ByteArrayOutputStream(12);
      String deployFile = dBean.getFolderPath() + DeployConstants.KUBE_DEPLOY_SH;
      String deployScript = getFileDetails(deployFile);
      if (deployScript != null && !"".equals(deployScript)) {
        bOutput.write(deployScript.getBytes());
        hmap.put(DeployConstants.KUBE_DEPLOY_SH, bOutput);
        logger.debug(DeployConstants.KUBE_DEPLOY_SH + " " + bOutput);
      }
      // Env file
      bOutput = new ByteArrayOutputStream(12);
      String deployEnvScript = createEnvFile(dBean, "composite");
      if (deployEnvScript != null && !"".equals(deployEnvScript)) {
        bOutput.write(deployEnvScript.getBytes());
        hmap.put(DeployConstants.KUBE_DEPLOY_ENV_SH, bOutput);
        logger.debug(DeployConstants.KUBE_DEPLOY_ENV_SH + " " + bOutput);
      }
      if (dBean.getBluePrintjson() != null && !"".equals(dBean.getBluePrintjson())) {
        bOutput = new ByteArrayOutputStream(12);
        bOutput.write(dBean.getBluePrintjson().getBytes());
        hmap.put(DeployConstants.KUBE_BLUEPRINT_JSON, bOutput);
        logger.debug(DeployConstants.KUBE_BLUEPRINT_JSON + " " + bOutput);
      }

      if (dBean.getDockerInfoJson() != null && !"".equals(dBean.getDockerInfoJson())) {
        bOutput = new ByteArrayOutputStream(12);
        bOutput.write(dBean.getDockerInfoJson().getBytes());
        hmap.put(DeployConstants.KUBE_DOCKERINFO_JSON, bOutput);
        logger.debug(DeployConstants.KUBE_DOCKERINFO_JSON + "  " + bOutput);
      }
      if (dBean.getSolutionYml() != null && !"".equals(dBean.getSolutionYml())) {
        bOutput = new ByteArrayOutputStream(12);
        bOutput.write(dBean.getSolutionYml().getBytes());
        hmap.put(DeployConstants.KUBE_SOLUTION_YML, bOutput);
        logger.debug(DeployConstants.KUBE_SOLUTION_YML + "  " + bOutput);
      }
      if (dBean.getDataBrokerJson() != null && !"".equals(dBean.getDataBrokerJson())) {
        bOutput = new ByteArrayOutputStream(12);
        bOutput.write(dBean.getDataBrokerJson().getBytes());
        hmap.put(DeployConstants.KUBE_DATABROKER_JSON, bOutput);
        logger.debug(DeployConstants.KUBE_DATABROKER_JSON + "  " + bOutput);
      }
      if (dBean.getContainerBeanList() != null && dBean.getContainerBeanList().size() > 0) {
        List<ContainerBean> contList = dBean.getContainerBeanList();
        if (contList != null) {
          int j = 0;
          while (contList.size() > j) {
            ContainerBean contbean = contList.get(j);
            if (contbean != null
                && contbean.getProtoUriPath() != null
                && !"".equals(contbean.getProtoUriPath())
                && contbean.getProtoUriDetails() != null
                && !"".equals(contbean.getProtoUriDetails())) {
              int index = contbean.getProtoUriPath().lastIndexOf("/");
              String protoFileName = contbean.getProtoUriPath().substring(index + 1);
              bOutput = new ByteArrayOutputStream(12);
              bOutput.write(contbean.getProtoUriDetails().getBytes());
              String protoFolderName =
                  "microservice" + "/" + contbean.getContainerName() + "/" + "model.proto";
              logger.debug(protoFolderName + "  " + protoFolderName);
              hmap.put(protoFolderName, bOutput);
              logger.debug(contbean.getProtoUriPath() + " " + bOutput);
            }
            j++;
          }
        }
      }
    }

    baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);
    Iterator it = hmap.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pair = (Map.Entry) it.next();
      String fileName = (String) pair.getKey();
      ByteArrayOutputStream ba = (ByteArrayOutputStream) pair.getValue();

      ZipEntry ze = new ZipEntry(fileName);
      zos.putNextEntry(ze);
      InputStream in = new ByteArrayInputStream(ba.toByteArray());
      int len;
      while ((len = in.read(buffer)) > 0) {
        zos.write(buffer, 0, len);
      }
      in.close();
    }

    File dir = new File(dBean.getTemplateYmlDirectory());
    filesListInDir = populateFilesList(dir, filesListInDir);

    for (String filePath : filesListInDir) {
      logger.debug("Zipping " + filePath);
      // for ZipEntry we need to keep only relative file path, so we used substring on absolute path
      ZipEntry ze =
          new ZipEntry(
              "templates/"
                  + filePath.substring(dir.getAbsolutePath().length() + 1, filePath.length()));
      zos.putNextEntry(ze);
      // read the file and write to ZipOutputStream
      FileInputStream fis = new FileInputStream(filePath);
      int len;
      while ((len = fis.read(buffer)) > 0) {
        zos.write(buffer, 0, len);
      }
      fis.close();
    }

    zos.closeEntry();
    zos.close();
    return baos.toByteArray();
  }

  public String getFileDetails(String fileDetails) throws Exception {
    String content = "";
    logger.debug("fileDetails " + fileDetails);
    BufferedReader reader = new BufferedReader(new FileReader(fileDetails));
    StringBuilder stringBuilder = new StringBuilder();
    String line = null;
    String ls = System.getProperty("line.separator");
    while ((line = reader.readLine()) != null) {
      stringBuilder.append(line);
      stringBuilder.append(ls);
    }
    // delete the last new line separator
    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    reader.close();

    content = stringBuilder.toString();
    return content;
  }

  public String getModelName(String imageName, String solutionId) {
    logger.debug("Start getModelName: imgName:" + imageName + ", solutionId:" + solutionId);
    String modelName = null;
    if (imageName != null) {
      // imageName=acumos-aio-host:30883/face_privacy_filter_detect_96fc199b-eb96-4162-b33e-b1fc629b28c5:1
      String[] imageArr = imageName.split("/");
      if (imageArr != null && imageArr.length >= 2 && imageArr[1] != null) {
        String[] imageNameArr = imageArr[1].split(":");
        if (imageNameArr != null && imageNameArr[0] != null) {
          // imageFullName derived as
          // face_privacy_filter_detect_96fc199b-eb96-4162-b33e-b1fc629b28c5:1
          String imageFullName = imageNameArr[0];
          Pattern p = Pattern.compile("(.*)(" + solutionId + ")");
          Matcher m = p.matcher(imageFullName);
          if (m.matches()) {
            // modelName as face_privacy_filter_detect_
            modelName = m.group(1);
            if (modelName != null) {
              if ((modelName.endsWith("_") || modelName.endsWith("-"))) {
                modelName = modelName.substring(0, modelName.length() - 1);
              }
              // make dns-compliant i.e. replace '_' with '-'
              modelName = modelName.replaceAll("_", "-");
              logger.debug("-getModelName " + modelName);
            }
          }
        }
      }
    }

    logger.debug(" End-getModelName " + modelName);
    return modelName;
  }
}
