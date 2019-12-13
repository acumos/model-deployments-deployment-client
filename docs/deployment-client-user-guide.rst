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

****************************************
Creating the Jenkins solution-deploy job
****************************************

The Deployment Client also depends upon configuration of a Jenkins job named
"solution-deploy", in a Jenkins server that is accessible from the
Deployment Client. By default, the OneClick toolset will install a Jenkins server
as part of the Acumos platform, which is the tested configuration in Clio.
Future releases are expected to support use of an external Jenkins server.

The default Jenkins job template is available as
`solution-deploy.xml <https://raw.githubusercontent.com/acumos/model-deployments-deployment-client/master/config/jobs/jenkins/solution-deploy.xml>`_ in the
`model-deployments-deployment-client repo <https://github.com/acumos/model-deployments-deployment-client>`_.
By default, the OneClick toolset script
`setup_jenkins.sh <https://raw.githubusercontent.com/acumos/system-integration/master/AIO/jenkins/setup_jenkins.sh>`_
will install this template as a Jenkins job, with the following values
as set in the environment file acumos_env.sh:

* ACUMOS_DEFAULT_SOLUTION_DOMAIN: by default set the same as ACUMOS_DOMAIN
* ACUMOS_DEFAULT_SOLUTION_NAMESPACE: by default set the same as ACUMOS_NAMESPACE

These values will be used in the first target cluster configuration block of the
case statement in the solution-deploy job, in place of "acumos.example.com" and
"acumos". If you also specify a value for ACUMOS_DEFAULT_SOLUTION_KUBE_CONFIG in
acumos_env.sh, setup_jenkins.sh will also copy that kube-config file into the
Jenkins configuration as "kube-config-<ACUMOS_DEFAULT_SOLUTION_DOMAIN>" so that
when a solution-deploy job is executed for that cluster, the correct kube-config
is used to interact with that cluster via kubectl.

If you deployed the platform using some other method, you will need to manually
create and configure the solution-deploy job, similar to how it is configured in
setup_jenkins.sh. Once the solution-deploy job is created under Jenkins, there
is one manual step required to complete the configuration. Use these steps to
complete it:

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

    * replace "cluster1" with the "name" value of a cluster that you have
      cofigured in the Acumos platform site-config (by default, "cluster1" is
      configured)
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

The actual deployment process occurs through a combination of the Acumos
`solution-deploy Jenkins job <https://github.com/acumos/model-deployments-deployment-client/blob/master/config/jobs/jenkins/solution-deploy.xml>`_
and the `deploy.sh <https://github.com/acumos/model-deployments-deployment-client/blob/master/config/jobs/solution_deploy/deploy.sh>`_
script that it calls to execute the deployment. Note that both the Jenkins job
and the deploy.sh script can be customized to fit the specific requirements of
your target k8s environments. Customizing the Jenkins solution-deploy job is
beyond the scope of this document.

****************************************
Configure Jenkins Access to k8s Clusters
****************************************

In order to access the k8s cluster API, the Jenkins server needs to be configured
to operate as a kubernetes client, and for access to other tools that are used by
the default "solution-deploy" Jenkins job and 'deploy.sh' script that is used
by it, both of which are available in the
`deployment-client repo <https://github.com/acumos/model-deployments-deployment-client/tree/clio/config/jobs>`_.

Note that the guide below assumes you are using k8s cluster(s) compatible with
the default design of the Acumos Clio release, which is based upon the generic
k8s distribution version 1.13.8, and has not been tested on other k8s versions,
or k8s distributions such as OpenShift or Azure-AKS (those are planned for the
next release). If you need to use some other k8s version:

* in order to install a compatible kubectl version, you will need to ensure you
  use a kubectl version within one minor version of the k8s server
* you can customize the 'initial-setup' job described below, to use another
  supported k8s version
* if you have multiple target k8s clusters that you want to configure, you
  will need to ensure that they are all the same version, or customize
  the default Acumos "solution-deploy" Jenkins job to be capable of switching
  between k8s client versions on a per-deployment-job basis
* any other differences may require that you customize both the "solution-deploy"
  Jenkins job and the 'deploy.sh' script it calls

How you prepare the Jenkins server depends upon how your Jenkins server was
installed:

* if you installed your Jenkins server via the
  `Acumos OneClick toolset <https://docs.acumos.org/en/latest/submodules/system-integration/docs/oneclick-deploy/index.html>`_,
  Jenkins will have been fully configured by installation and execution of the
  Jenkins job
  `initial-setup <https://github.com/acumos/system-integration/blob/master/charts/jenkins/jobs/initial-setup.xml>`_

* if you installed your Jenkins server manually, or are using an existing Jenkins
  service

  * If your Jenkins server is capable of running privileged jobs, you can create
    a job similar to the 'initial-setup' job described above, or run these
    commands directly in the Jenkins server's shell

    * Note: this an Ubuntu example; update as needed for Centos

    .. code-block:: bash

      apt-get update
      apt-get install -y jq uuid-runtime
      # Install kubectl per https://kubernetes.io/docs/setup/independent/install-kubeadm/
      KUBE_VERSION=1.13.8
      apt-get install -y apt-transport-https
      curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key add -
      cat &lt;&lt;EOF | tee /etc/apt/sources.list.d/kubernetes.list
      deb http://apt.kubernetes.io/ kubernetes-xenial main
      EOF
      apt-get update
      apt-get -y install --allow-downgrades kubectl=${KUBE_VERSION}-00
    ..

  * if your Jenkins server restricts privileged jobs, you can either run the
    commands above manually, or build and use an updated Jenkins docker image,
    e.g. as built using the `Dockerfile <https://github.com/acumos/system-integration/blob/master/charts/jenkins/Dockerfile>`_
    in the system-integration repo.

    * NOTE: a pre-built Jenkins image customized for Acumos as above is
      available under Docker Hub as blsaws/jenkins, and is the image use by
      default in the OneClick toolset installation of Jenkins. In future
      releases, it's expected that a similarly-prepared image will be provided
      through the Acumos project Nexus repository.

Once you have completed the basic configuration of the Jenkins server, you will
need to provide a k8s config file ('kube config') that contains the token(s)
used by the kubectl client to connect to your k8s server(s). If you used the OneClick
toolset to deploy the Jenkins service under your Acumos platform, it will have
already been configured by default to use the same k8s cluster and namespace
for deploying solutions, unless you specified values for the following in
acumos_env.sh:

* ACUMOS_DEFAULT_SOLUTION_DOMAIN: by default set the same as ACUMOS_DOMAIN
* ACUMOS_DEFAULT_SOLUTION_NAMESPACE: by default set the same as ACUMOS_NAMESPACE
* ACUMOS_DEFAULT_SOLUTION_KUBE_CONFIG: by default empty, which causes
  setup_jenkins.sh to use the kube-config for the Acumos platform k8s cluster

You can add other target k8s clusters to the solution-deploy job through the
Jenkins admin UI or by preparing a customized solution-deploy.xml file as
AIO/jenkins/deploy/jobs/solution-deploy.xml in the system-integration
clone used to execute the OneClick-based deployment. To do that, you should:

* add additional target cluster case blocks to solution-deploy as described in
  `Creating the Jenkins solution-deploy job`_, updating at least the first two
  lines in the case block as shown below for a "cluster2" that has been added:

  .. code-block:: bash

    case "$K8S_CLUSTER"; in
      cluster1)
        SOLUTION_DOMAIN=acumos-1.example.com
        NAMESPACE=acumos-1
        FILEBEAT_DATA_PVC_STORAGE_CLASS_NAME=
        FILEBEAT_DATA_PVC_SIZE=1Gi
        NGINX_PROXY_LOG_PVC_STORAGE_CLASS_NAME=
        NGINX_PROXY_LOG_PVC_SIZE=1Gi
        ;;
      cluster2)
        SOLUTION_DOMAIN=acumos-2.example.com
        NAMESPACE=acumos-2
        FILEBEAT_DATA_PVC_STORAGE_CLASS_NAME=
        FILEBEAT_DATA_PVC_SIZE=1Gi
        NGINX_PROXY_LOG_PVC_STORAGE_CLASS_NAME=
        NGINX_PROXY_LOG_PVC_SIZE=1Gi
        ;;
      *)
        exit 1
    esac
  ..

* create and/or copy an applicable kube-config file for the additional clusters
  into the Jenkins container; to do that for the "cluster2" example above:

  * it's assumed that you have access to the k8s cluster(s) from your workstation,
    and have the current context set as needed

    .. code-block:: bash

      kubectl config use-context cluster2-acumos
      Switched to context "cluster2-acumos".
      $ kubectl config get-contexts cluster2-acumos
      CURRENT   NAME             CLUSTER   AUTHINFO   NAMESPACE
      *         cluster2-acumos  cluster2  <your id>  acumos
    ..

  * once you have set the context, copy ~/.kube/config to the home folder of
    the Jenkins user in your Jenkins server, identifying the config file as
    related to the solution domain for "cluster2". For example, if you are using
    the default Jenkins server installed by the OneClick toolset and want
    to update the kube config,

    .. code-block:: bash

      cp ~/.kube/config kube-config-acumos-2.example.com
      # Switch back to using the Acumos platform kube config
      kubectl config use-context <Acumos platform context name>
      Switched to context "<Acumos platform context name>".
      pod=$(kubectl get pods | awk '/jenkins/{print $1}')
      kubectl cp kube-config-acumos-2.example.com $pod:/var/jenkins_home/.
      kubectl exec -it $pod -- bash -c 'ls /var/jenkins_home/'
    ..

.........................
Acumos Site Configuration
.........................

The "deploy to k8s" feature supports provisioning of a set of k8s clusters to
be offered to users as deployment target environments.

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

................................
Kubernetes Cluster Configuration
................................

Following are prerequisite requirements for k8s cluster configuration per the
default design:

* an nginx-ingress controller, e.g. deployed using the
  `nginx-ingress helm chart <https://github.com/helm/charts/tree/master/stable/nginx-ingress>`_
  or the Acumos OneClick tool
  `setup_ingress_controller.sh <https://github.com/acumos/system-integration/blob/master/charts/ingress/setup_ingress_controller.sh>`_
* persistent volumes available for use by the ML solution logging support
  components

+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
Configure k8s cluster access to Acumos solution docker registry
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

A docker daemon service on each k8s cluster node is used by k8s to pull images
that are being deployed on the k8s node. One or both of the following
configuration updates are needed to ensure the cluster can pull solution images
from the docker registry used by the Acumos platform:

* create a k8s secret with a valid access token for the Acumos project docker
  registry and your Acumos platform docker registry
* IF your Acumos docker registry service is installed in insecure mode,
  configure the docker daemon on each k8s cluster node to be able to access the
  registry as an insecure registry

The deployment-client repo contains a tool to help you perform these actions
as needed for AIO test platforms, in tools/update_k8s_config.sh. NOTE: This script
will update and restart the docker daemon as needed, which will disrupt docker
service on the k8s master node for a short time, but all k8s services will be
restarted by docker. FOR TEST PURPOSES ONLY. To run that tool:

.. code-block:: bash

   $ bash update_k8s_config.sh <kube-context> <k8s-host> <admin-user>
      <solution-namespace> <registry-host> <nexus-namespace> <acumos-namespace>
..

* where:

  * kube-context: name of k8s context for Acumos platform
  * k8s-host: target k8s cluster hostname/FQDN
  * admin-user: admin username on k8s host
  * solution-namespace: namespace for creating acumos-registry secret on k8s-host
  * registry-host: hostname/FQDN of the docker registry
  * nexus-namespace: namespace of the Nexus service on the Acumos platform
  * acumos-namespace: namespace of the Acumos core on the Acumos platform

The following sections describe the actions in detail.

*****************************************************
Create a k8s secret for Acumos docker registry access
*****************************************************

Deploying Acumos project and platform docker images into k8s clusters requires
that the cluster be pre-configured with access tokens for the registries, since
they are password-protected. This is a two-step process, with the second step
being applied for each namespace under which Acumos solutions will be deployed:

* create a docker client configuration file (~/.docker/config.json) by logging
  into the Acumos project and Acumos platform docker registry; this can be done
  from any host that has access to the k8s cluster via kubectl, e.g. your
  workstation or the k8s cluster master node

  .. code-block:: bash

    docker login nexus3.acumos.org:10002 -u docker -p docker
    docker login nexus3.acumos.org:10003 -u docker -p docker
    docker login nexus3.acumos.org:10004 -u docker -p docker
    docker login http://<docker registry domain>:<docker registry port> -u <username> -p <password>
  ..

  * where:

    * <docker registry domain> is the host/FQDN of your Acumos platform docker registry
    * <docker registry port> is the port of your Acumos platform docker registry
    * <username> is a username with access to the Acumos platform docker registry
    * <password> is the password for the username

* create/update the "acumos-registry" secret in the target namespace with the
  content of the docker config updated above

  .. code-block:: bash

    b64=$(cat $HOME/.docker/config.json | base64 -w 0)
    cat <<EOF >acumos-registry.yaml
    apiVersion: v1
    kind: Secret
    metadata:
      name: acumos-registry
      namespace: <namespace>
    data:
      .dockerconfigjson: $b64
    type: kubernetes.io/dockerconfigjson
    EOF

    kubectl create -f acumos-registry.yaml
  ..

  * where <namespace> is the k8s namespace under which you plan to deploy
    solutions, as described in `Creating the Jenkins solution-deploy job`_
  * NOTE: if you are running the commands above under MacOS, remove the option
    '-w 0' in the base64 command shown above

* verify the acumos-registry secret now contains the correct address and
  credentials for your docker registry

  .. code-block:: bash

    kubectl get secret -n acumos acumos-registry -o yaml | \
      awk '/.dockerconfigjson:/{print $2}' | base64 --decode
  ..

  * to verify the credentials, copy the "auth" value from the "auths" array
    member for the updated registry and decode it, e.g.

    .. code-block:: bash

      $ kubectl get secret -n acumos acumos-registry -o yaml | awk '/.dockerconfigjson:/{print $2}' | base64 --decode
      {
        "auths": {
          "nexus3.acumos.org:10002": {
            "auth": "ZG9ja2VyOmRvY2tlcg=="
          },
          "nexus3.acumos.org:10003": {
            "auth": "ZG9ja2VyOmRvY2tlcg=="
          },
          "nexus3.acumos.org:10004": {
            "auth": "ZG9ja2VyOmRvY2tlcg=="
          },
          "opnfv04:30908": {
            "auth": "YWN1bW9zX3J3OmQ3YTkxODcyLWFmNWItNDhkNi1hMGViLWU0ODdhN2YwNmYzZg=="
          }
        },
        "HttpHeaders": {
          "User-Agent": "Docker-Client/18.06.3-ce (linux)"
        }
      }
      $ echo YWN1bW9zX3J3OmQ3YTkxODcyLWFmNWItNDhkNi1hMGViLWU0ODdhN2YwNmYzZg== | base64 --decode
      acumos_rw:d7a91872-af5b-48d6-a0eb-e487a7f06f3f
    ..

*************************************************************
Configure docker daemon to access your Acumos docker registry
*************************************************************

If your Acumos solution docker registry is configured in either of the
following ways, it is an insecure registry from the docker daemon's perspective
and must be configured specifically for access as an insecure registry:

* accessed over HTTP (vs HTTP), e.g. per the default for the OneClick toolset
  deployment of Nexus as a platform-internal docker registry
* accessed over HTTPS without a commercial cert, i.e. with no cert or a
  self-signed cert

Configuring the docker daemon to access your Acumos solution docker registry
as an insecure registry requires host admin (root or sudo user), and the
following actions:

* add the docker registry to /etc/docker/daemon.json

  * edit /etc/docker/daemon.json)
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

* restart the docker service

  .. code-block:: json

    sudo systemctl daemon-reload
    sudo service docker restart
  ..

  * NOTE: this restart action will restart your k8s service on the updated
    node, and may be disruptive to any running services on that node; ALSO note
    that for OpenShift clusters, additional actions may be needed to restore the
    cluster and services running under it

..............................
Kubernetes Cluster Maintenance
..............................

++++++++++++++++++++++++++++++++++++++++++++++++++++++
Updating Docker Daemon Insecure Registry Configuration
++++++++++++++++++++++++++++++++++++++++++++++++++++++

If there is a change to your Acumos docker registry host or port, e.g. you have
redeployed the Acumos platform or the Nexus service resulting in assignment of
a new nodePort value for the Nexus service at ACUMOS_DOCKER_MODEL_PORT, you will
need to update the docker daemon configuration for k8s clusters that are used
for solution deployment, as described in
`Configure docker daemon to access your Acumos docker registry`_.

+++++++++++++++++++++++++++
Cleaning Solution Resources
+++++++++++++++++++++++++++

The Clio release does not include any platform capabilities or specific solution
lifecycle management tools enabling Admins to manage the Acumos solutions once
the solutions have been deployed in k8s clusters, other than as described below.
Platform-integrated tools are planned for the next release (Demeter).

In the meantime, Admins or users (if they have access to the k8s clusters via
kubectl) will need to manually manage deployed solution resources, e.g. when the
running solution is no longer needed, removing all solution-specific resources
created during deployment.

One tool is provided in the deployment-client repo to simplify cleaning up
solution resources, as
deployment-client/tools/clean_solution.sh. To run that tool:

.. code-block:: bash

  $ bash clean_solution.sh [ns=<namespace>] [days=<days>] [match=<match>] [--dry-run] [--force]
..

  * where:

    * namespace: k8s namespace under which the solution is deployed, in the
      cluster to which the user is currently connected, as the active
      context for the kubectl client (run 'kubectl config current-context' to
      see the current active context)
    * days: select all solutions that are <days> old or older
    * match: select solutions that match <pattern>

      * If no <match> is specified, resources that match the default pattern
        [0-9]{5}-[0-9]{5} will be selected. These are resources for solutions
        identified by the uniqueid value generated by the deploy.sh script.

    * --dry-run: show what would be deleted only
    * --force: do not prompt for confirmation of resource deletion

In order to identify a specific deployment job and its resources, use the
"ingress URL" provided to the user when the deployment job completion
notification was provided on the Acumos platform, e.g.

.. code-block:: text

  square deployment is complete. The solution can be accessed at the ingress
  URL https://acumos.example.com/square/191111-162114/
..

The URL part after the model name is the unique ID assigned to the
deployment job, and provides a timestamp when the deployment job was
invoked by the default deploy,sh deployment script:

.. code-block:: text

  UNIQUE_ID=$(date +%y%m%d)-$(date +%H%M%S)
..

Using the unique ID as the "match=" parameter shown above, you should be able
to clean up all related resources as needed.

------------------
For Platform Users
------------------

In the Clio release, a solution is deployed using these steps:

* select a solution you want to deploy, and ensure that microservice images
  have been built for all models included in the solution
* in the upper-right of the screen, select "Deploy to Cloud" and in the list
  of target cloud types, select "Kubernetes"
* You will see a disclaimer e.g.

  .. code-block:: text

    Deploying this model outside the Acumos system may expose its information to
    third parties. Please click OK to confirm this deployment is being done in
    compliance with all local policies.
  ..

* Click-thru the disclaimer, and you will see a "Select Kubernetes cluster"
  drop-down, from which you can select the target k8s cluster
* Select the target cluster, and and select "Deploy"
* You will see a briefly presented notification ala

  .. code-block:: text

    The deployment process has been started, will take some time to complete.
    Notification will be sent on completion.
  ..

* Watch for updates in the Notification list, accessed by the "bell" icon in the
  top menu bar. When deployment is complete, you should see a notice e.g.

  .. code-block:: text

    <model name> deployment is complete. The solution can be accessed at the ingress
    URL https://acumos.example.com/<model name>/<unique id>/
  ..

* in the notification, the URL is the API where you should be able to send
  data to the solution, and get results. The 'model name' is the displayed
  name of the model on the Acumos platform. The 'unique id' is an identifier
  for the specific deployment job, in the form of a timestamp.
