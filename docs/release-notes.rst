.. ===============LICENSE_START=======================================================
.. Acumos CC-BY-4.0
.. ===================================================================================
.. Copyright (C) 2017-2019 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
.. ===================================================================================
.. This Acumos documentation file is distributed by AT&T and Tech Mahindra
.. under the Creative Commons Attribution 4.0 International License (the "License");
.. you may not use this file except in compliance with the License.
.. You may obtain a copy of the License at
..
.. http://creativecommons.org/licenses/by/4.0
..
.. This file is distributed on an "AS IS" BASIS,
.. WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
.. See the License for the specific language governing permissions and
.. limitations under the License.
.. ===============LICENSE_END=========================================================

===============================
Deployment Client Release Notes
===============================


--------------------------------
Version 1.0.4, 22 January 2020
-----------------------------
* deployment client api changes(`ACUMOS-3934 <https://jira.acumos.org/browse/ACUMOS-3934>`_)

--------------------------------
Version 1.0.3, 30 December 2019
-----------------------------
* update acumos-azure-client, acumos-openstack-client,kubernetis-client and deployment-client for logging library 3.0.5(`ACUMOS-3880 <https://jira.acumos.org/browse/ACUMOS-3880>`_)



--------------------------------
Version 1.0.2, 11 December 2019
-----------------------------
* update acumos-azure-client, acumos-openstack-client,kubernetis-client and deployment-client for cds 3.1.0 (`ACUMOS-3835 <https://jira.acumos.org/browse/ACUMOS-3835>`_)


--------------------------
Version 1.0.1, 09 Dec 2019
--------------------------

This is the Clio maintenance release of the Deployment Client. The current
k8s and docker-compose templates are at:

* `deployment-client-deployment.yaml <https://github.com/acumos/system-integration/blob/master/AIO/kubernetes/deployment/deployment-client-deployment.yaml>`_
* `deployment-client.yml <https://github.com/acumos/system-integration/blob/master/AIO/docker/acumos/deployment-client.yml>`_

Jira items:

* `ACUMOS-3677: Ensure all resources have names with max length 63 chars <https://jira.acumos.org/browse/ACUMOS-3677>`_

  * `5816: Max resource name length 63 chars <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5816/>`_
  * `5873: Admin/user guide updates <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5873/>`_
  * `5983: deployment-client release 1.0.1 <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5983/>`_
  * `5996: deployment-client release 1.0.1 <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5996/>`_

* `ACUMOS-3188: Invoke Jenkins Job for Model deployment <https://jira.acumos.org/browse/ACUMOS-3188>`_

  * `5606: Update spring env file <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5606/>`_

* `ACUMOS-3133: Migrate Solution/Pipeline deployment to Jenkins-based process <https://jira.acumos.org/browse/ACUMOS-3133>`_

  * `6051: Fix deploy flow image <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/6051/>`_
  * `6048: Add clean_solution.sh, update docs <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/6048/>`_
  * `5589: Complete deployment job template <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5589/>`_
  * `5652: Add link to user guide <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5652/>`_
  * `5716: Fix XML file format <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5716/>`_
  * `5749: Add more content in docs <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5749/>`_

--------------------------
Version 1.0.0, 01 Oct 2019
--------------------------

This is the first release of the Deployment Client. The Deployment Client is
derived from the Acumos Boreas release of the Kubernetes Client, and is dependent
upon a Jenkins server being deployed as part of the Acumos platform or externally.
See the
`Deployment Client <https://docs.acumos.org/en/latest/submodules/model-deployments/deployment-client/docs/index.html>`_
documents for further information.

* `3181: Deployment Client to deploy model to K8s using Jenkins <https://jira.acumos.org/browse/ACUMOS-3181>`_

  * `5368: Jenkins integration <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5368/>`_
  * `5309: update code for deployment-client <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5309/>`_
  * `5279: Add missing detail on /deploy API response <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5279/>`_
  * `5276: Update siteConfig key k8sCluster design <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5276/>`_
  * `5189: API structure for deployment-client <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5189/>`_
  * `5237: Fix diagram <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5237/>`_
  * `5178: Add Jenkins job and updates to deploy process <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5178/>`_
  * `5071: Add docs and other base folders <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5071/>`_
  * `5069: Initial repo files <https://gerrit.acumos.org/r/#/c/model-deployments/deployment-client/+/5069/>`_

The Kubernetes templates are in the System Integration repo, under AIO/kubernetes.
The docker-compose template is in the System Integration repo under AIO/docker,
and copied below:

.. code-block:: yaml

  version: '3.2'
  services:
    deployment-client-service:
      image: ${DEPLOYMENT_CLIENT_IMAGE}
      environment:
        SPRING_APPLICATION_JSON:  '{
          "logging": {
            "level": {
              "root": "INFO"
            }
          },
          "server": {
            "port": 8337
          },
          "jenkins": {
            "url": "${ACUMOS_JENKINS_API_URL}",
            "user": "${ACUMOS_JENKINS_USER}",
            "password": "${ACUMOS_JENKINS_PASSWORD}",
            "job": {
              "simple": "${ACUMOS_JENKINS_SIMPLE_SOLUTION_DEPLOY_JOB}",
              "composite": "${ACUMOS_JENKINS_COMPOSITE_SOLUTION_DEPLOY_JOB}",
              "nifi": "${ACUMOS_JENKINS_NIFI_DEPLOY_JOB}"
            }
          },
          "api": {
            "baseUrl": "https://${ACUMOS_DOMAIN}/deployment/"
          },
          "kube" : {
            "incrementPort": "8557",
            "singleModelPort": "8556",
            "folderPath": "/maven/home",
            "singleNodePort": "30333",
            "singleTargetPort": "3330",
            "dataBrokerModelPort": "8556",
            "dataBrokerNodePort": "30556",
            "dataBrokerTargetPort": "8556",
            "mlTargetPort": "3330",
            "nginxImageName": "nginx",
            "nexusEndPointURL": "http://localhost:80"
          },
          "dockerRegistry": {
            "url": "https://${ACUMOS_DOCKER_PROXY_HOST}:${ACUMOS_DOCKER_PROXY_PORT}/",
            "username": "${ACUMOS_DOCKER_PROXY_USERNAME}",
            "password": "${ACUMOS_DOCKER_PROXY_PASSWORD}"
          },
          "blueprint": {
            "ImageName": "${BLUEPRINT_ORCHESTRATOR_IMAGE}",
            "name": "blueprint-orchestrator",
            "port": "8555"
          },
          "nexus": {
            "url": "http://${ACUMOS_NEXUS_HOST}:${ACUMOS_NEXUS_API_PORT}/${ACUMOS_NEXUS_MAVEN_REPO_PATH}/${ACUMOS_NEXUS_MAVEN_REPO}/",
            "password": "${ACUMOS_NEXUS_RW_USER_PASSWORD}",
            "username": "${ACUMOS_NEXUS_RW_USER}",
            "groupid": "${ACUMOS_NEXUS_GROUP}"
          },
          "cmndatasvc": {
            "cmndatasvcendpointurl": "http://<ACUMOS_CDS_HOST>:<ACUMOS_CDS_PORT>/ccds",
            "cmndatasvcuser": "${ACUMOS_CDS_USER}",
            "cmndatasvcpwd": "${ACUMOS_CDS_PASSWORD}"
          },
          "logstash": {
            "host": "${ACUMOS_ELK_HOST}",
            "ip": "${ACUMOS_ELK_HOST_IP}",
            "port": "${ACUMOS_ELK_LOGSTASH_PORT}"
          }
        }'
      expose:
        - 8337
      volumes:
        - type: bind
          source: /mnt/${ACUMOS_NAMESPACE}/logs
          target: /maven/logs
      logging:
        driver: json-file
      extra_hosts:
        - "${ACUMOS_HOST}:${ACUMOS_HOST_IP}"
      restart: on-failure
