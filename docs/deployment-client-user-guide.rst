..
  ===============LICENSE_START=======================================================
  Acumos CC-BY-4.0
  ===================================================================================
  Copyright (C) 2017-2019 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
  ===================================================================================
  This Acumos documentation file is distributed by AT&T and Tech Mahindra
  under the Creative Commons Attribution 4.0 International License (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
..
  http://creativecommons.org/licenses/by/4.0
..
  This file is distributed on an "AS IS" BASIS,
  See the License for the specific language governing permissions and
  limitations under the License.
  ===============LICENSE_END=========================================================
..

===================================
Acumos Deployment Client User Guide
===================================

-----
Scope
-----

This guide covers the use of the Deployment Client for the Acumos platform,
as of the Clio (3rd) release. The Deployment Client enables the user to
deploy Acumos ML models ("solutions") into kubernetes (k8s) clusters, and use
them.

............................
Previously Released Features
............................

This is the first release of the Deployment Client.

........................
Current Release Features
........................

Current release features include:

* deployment of Acumos solutions (simple and composite models) into
  pre-configured kubernetes clusters

-------------------
For Platform Admins
-------------------

.......................................
Deploying the Deployment Client Service
.......................................

If you use the `Acumos OneClick toolset <https://docs.acumos.org/en/latest/submodules/system-integration/docs/oneclick-deploy/index.html>`_
tools, the Deployment Client will automatically be deployed as part of the
Acumos platform, in either an All-in-One (AIO) or kubernetes (k8s) multi-node
cluster, as you choose per the OneClick toolset options.

If you install the Deployment Client in a docker-compose managed platform without
using the OneClick toolset, you will need to adapt the docker-compose template
provided under the system-integration repo as
`AIO/docker/acumos/deployment-client.yml <https://raw.githubusercontent.com/acumos/system-integration/master/AIO/docker/acumos/deployment-client.yml>`_.

...................
Configuring Jenkins
...................

The Deployment Client also depends upon configuration of a Jenkins job named
"solution-deploy", in a Jenkins server that is accessible from the
Deployment Client. By default, the OneClick toolset will install a Jenkins server
as part of the Acumos platform, which is the tested configuration in Clio.
Future releases are expected to support use of an external Jenkins server.

The default Jenkins job template is available as
`solution-deploy.xml <https://raw.githubusercontent.com/acumos/model-deployments-deployment-client/master/config/jobs/jenkins/solution-deploy.xml>`_ in the
`model-deployments-deployment-client repo <https://github.com/acumos/model-deployments-deployment-client>`_.
By default, the OneClick toolset will install this template as a Jenkins job,
unless the template is pre-existing under charts/jenkins/jobs in the
system-integration clone used to execute the OneClick-based deployment, with
the following values set per your selection in the environment file
acumos_env.sh:

* ACUMOS_DEFAULT_SOLUTION_DOMAIN: by default set the same as ACUMOS_DOMAIN
* ACUMOS_DEFAULT_SOLUTION_NAMESPACE by default set the same as ACUMOS_NAMESPACE

If you deployed the platform using some other method, you will need to manually
create and configure the solution-deploy job, and can use the example commands from
`oneclick_deploy.sh <https://raw.githubusercontent.com/acumos/system-integration/master/AIO/oneclick_deploy.sh>`_
(see function "setup_jenkins"). Once the solution-deploy job is created under Jenkins, there is one manual step
required to complete the configuration. Use these steps to complete it:

* login to the Jenkins UI, by default https://<ACUMOS_DOMAIN/jenkins
* select the solution-deploy job link
* select 'Configure'
* under "Build", edit the "Execute shell" "Command" field to align the bash shell
  code for the job, with the set of k8s clusters you have configured in the
  Acumos site-config as per `Configuring Target Kubernetes Clusters`_. The key
  section to update is that which matches the default example below:

  .. code-block:: bash

    case "$K8S_CLUSTER"; in
      cluster1)
        SOLUTION_DOMAIN=acumos.example.com
        NAMESPACE=acumos
        FILEBEAT_DATA_PVC_STORAGE_CLASS_NAME=
        FILEBEAT_DATA_PVC_SIZE=1Gi
        NGINX_PROXY_LOG_PVC_STORAGE_CLASS_NAME=
        NGINX_PROXY_LOG_PVC_SIZE=1Gi
        ;;
      *)
        exit 1
    esac

  ..

  * There should be one case block per configured k8s cluster, with each block
    named per the "name" values for your k8s clusters. For example, if you have
    only one configured cluster:

    * replace "default" with the "name" value of the cluster
    * set SOLUTION_DOMAIN to the ingress domain name assigned for interaction with
      ML solutions deployed under the cluster
    * set NAMESPACE to the k8s namespace to use for the cluster

      * note that you can have multiple case blocks for the same cluster, that
        use different namespaces

    * if you want to use specific storage classes for the logging component PVCs,
      set FILEBEAT_DATA_PVC_STORAGE_CLASS_NAME and/or
      NGINX_PROXY_LOG_PVC_STORAGE_CLASS_NAME
    * if you want to reserve a different size for the logging component
      PVCs (persistent volume claims), set FILEBEAT_DATA_PVC_SIZE and/or
      NGINX_PROXY_LOG_PVC_SIZE

  * once you have completed the customization, select 'Save'


*******************************************
Customizing the Jenkins solution-deploy job
*******************************************

The actual deployment process occurs through a combination of the Acumos
`solution-deploy Jenkins job <https://github.com/acumos/model-deployments-deployment-client/blob/master/config/jobs/jenkins/solution-deploy.xml>`_
and the `deploy.sh <https://github.com/acumos/model-deployments-deployment-client/blob/master/config/jobs/solution_deploy/deploy.sh>`_
script that it calls to execute the deployment. Note that both the Jenkins job
and the deploy.sh script can be customized to fit the specific requirements of
your target k8s environments.

TODO: this section will describe the customization process, in general.

......................................
Configuring Target Kubernetes Clusters
......................................

*************************
Acumos Site Configuration
*************************

The "deploy to k8s" feature supports provisioning of a set of k8s clusters to
be offered to users as deployment target environments.

NOTE: as of the Clio release, automatic configuration of the Acumos site
configuration is not yet supported, but is planned for the maintenance release
of Clio.

Admins will have two methods to configure the k8s clusters to be offered to users
for solution deployment. In the examples below, the "name" values should
be aligned with the solution-deploy Jenkins job as described under
`Configuring Jenkins`_.

* by setting the site-config value through the Swagger UI of the Acumos
  `Common Data Service (CDS) <https://docs.acumos.org/en/clio/submodules/common-dataservice/docs/index.html>`_
  or direcly to the CDS API via curl, as below:

  .. code-block:: json

    curl -H 'Content-Type: application/json' \
      -u <ACUMOS_CDS_USER>:<ACUMOS_CDS_PASSWORD> \
      http://<ACUMOS_DOMAIN>:<ACUMOS_CDS_PORT>/ccds/site/config \
      -d '{ "configKey": "k8sCluster", "configValue": "[ \
            { \"name\": \"cluster1\" }, \
            { \"name\": \"cluster2\" }, \
            { \"name\": \"cluster3\" } ]", \
            "userId": "<ACUMOS_ADMIN_USER_ID>" }'

  ..

  where:

    * ACUMOS_DOMAIN is the domain name or IP address of the Acumos platform host
      where the CDS API is exposed
    * ACUMOS_CDS_PORT is the TCP port at which the CDS API is exposed
    * ACUMOS_CDS_USER is the username configured for CDS API access
    * ACUMOS_CDS_PASSWORD is the password configured for CDS API access
    * ACUMOS_ADMIN_USER_ID is the CDS user table ID value (GUID) of an Admin role
      user

* by configuring the Deployment Client deployment template, under "siteConfig"
  in the Spring environment settings (SPRING_APPLICATION_JSON value) of the
  Deployment Client; the following example shows the default values.

  .. code-block:: json

    "siteConfig": "[
        { \"name\": \"cluster1\" },
        { \"name\": \"cluster2\" },
        { \"name\": \"cluster3\" }
      ]"
    }
  ..

********************************
Kubernetes Cluster Configuration
********************************

Following are prerequisite requirements for k8s cluster configuration per the
default design:

* an nginx-ingress controller, e.g. deployed using the
  `nginx-ingress helm chart <https://github.com/helm/charts/tree/master/stable/nginx-ingress>`_
  or the Acumos OneClick tool
  `setup_ingress_controller.sh <https://github.com/acumos/system-integration/blob/master/charts/ingress/setup_ingress_controller.sh>`_
* persistent volumes available for use by the ML solution logging support
  components
* as needed, configure your k8s cluster to use the docker registry for Acumos
  solution docker images as an insecure registry; by default, the Nexus service
  is used as the docker registry for the Acumos platform. If the Nexus service or
  other docker registry being used was deployed as an insecure registry (e.g.
  using self-signed certs), you must configure the docker daemon for the k8s
  cluster to accept insecure connections to the registry. Below is the process
  for that configuration:

  * add the docker registry to /etc/docker/daemon.json, and restart the docker
    service

    * edit /etc/docker/daemon.json (requires root or sudo permission)
    * if /etc/docker/daemon.json is a new file, enter this content

      .. code-block:: json

        {
        "insecure-registries": [ "<ACUMOS_DOCKER_REGISTRY_HOST>:<ACUMOS_DOCKER_MODEL_PORT>" ],
        "disable-legacy-registry": true
        }

      ..

      * where

        * ACUMOS_DOCKER_REGISTRY_HOST is the domain name or IP address of your
          docker registry service
        * ACUMOS_DOCKER_MODEL_PORT is the TCP port where the docker registry
          service is provided

    * if /etc/docker/daemon.json already has values for "insecure-registries",
      add the additional <ACUMOS_DOCKER_REGISTRY_HOST>:<ACUMOS_DOCKER_MODEL_PORT>
      to the list
    * enter the following commands to restart the docker daemon service

      .. code-block:: json

        sudo systemctl daemon-reload
        sudo service docker restart
      ..

      * NOTE: this restart action will restart your k8s cluster, and may be
        disruptive to any running services under the cluster; ALSO note that for
        OpenShift clusters, additional actions may be needed to restore the
        cluster and services running under it

------------------
For Platform Users
------------------

To be provided.
