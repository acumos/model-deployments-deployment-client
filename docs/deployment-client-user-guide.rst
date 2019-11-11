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
      mkdir ~/.kube

    ..

  * if your Jenkins server restricts privileged jobs, you can either run the
    commands above manually, or build and use an updated Jenkins docker image,
    e.g. as built using the `Dockerfile <https://github.com/acumos/system-integration/blob/master/charts/jenkins/Dockerfile>`_
    in the system-integration repo

Once you have completed the basic configuration of the Jenkins server, you will
need to provide a k8s config file ('kube config') that contains the token(s)
used by the kubectl client to connect to your k8s server(s). If you used the OneClick
toolset to deploy the Jenkins service under your Acumos platform, it will have
already been configured to use the same k8s cluster and namespace for deploying
solutions. But for access to other clusters, you will need to update the client
configuration also, as described below.

* it's assumed that you have access to the k8s cluster(s) from your workstation,
  and have the current context set to the default cluster you want to use for
  deploying Acumos solutions
* copy the 'config' file in the '.kube' folder of your home folder, to the
  home folder of the Jenkins user in your Jenkins server. For example, if you
  are using the default Jenkins server installed by the OneClick toolset and want
  to update the kube config,

  * login to the k8s cluster(s) using a kubectl client on your workstation, and
    save the resulting kube config as e.g. 'kube-config', e.g.

    .. code-block:: bash

      kubectl config use-context <context name>
      Switched to context "<context name>".
      cp ~/.kube/config kube-config
    ..

  * login into your Acumos platform k8s cluster, and copy the saved kube-config
    into the running Jenkins server

    .. code-block:: bash

      kubectl config use-context <Acumos platform context name>
      Switched to context "<Acumos platform context name>".
      pod=$(kubectl get pods | awk '/jenkins/{print $1}')
      kubectl exec -it $pod -- bash -c 'mkdir /var/jenkins_home/.kube'
      kubectl kube-config $pod:/var/jenkins_home/.kube/.
      kubectl exec -it $pod -- bash -c 'ls /var/jenkins_home/.kube'
    ..

.........................
Acumos Site Configuration
.........................

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

* configure the "acumos-registry" secret in the target namespace to include a
  docker client token for the the Acumos platform's docker registry; the process
  for this is supported by the Acumos OneClick utility "create_acumos_registry_secret",
  which you can use as below to update the secret, if you deployed your platform
  using the `Acumos OneClick toolset <https://docs.acumos.org/en/latest/submodules/system-integration/docs/oneclick-deploy/index.html>`_.

  .. code-block:: bash

    docker login http://<docker registry domain>:<docker registry port> -u <username> -p <password>
    cd system-integration/AIO
    source utils.sh
    create_acumos_registry_secret <acumos platform namespace>
  ..


..............................
Kubernetes Cluster Maintenance
..............................

The Clio release does not include any platform capabilities or specific tools
that enable Admins to manage the Acumos solutions deployed in k8s clusters, once
those solutions have been deployed. Such capabilities are planned for the next
release. For Clio, Admins and/or users will need to know the following and take
action as needed to manage consumed resources in the k8s cluster:

* a variety of k8s resources are created during solution deployment. These
  resources are specific to the particular solution and deployment job, and will
  not be deleted automatically. When the deployed solution is no longer needed,
  Admins and/or users should clean up the resources e.g. using the kubectl client
  and a script such as the following:

  .. code-block:: bash

    #!/bin/bash
    id=$1
    if [[ "$2" != "" ]]; then ns="-n $2"; fi
    if [[ "$id" != "" ]]; then
      ts="deployments daemonset service configmap serviceaccount clusterrole clusterrolebinding configmap pvc ingress"
      for t in $ts; do
        rs=$(kubectl get $t $ns | awk "/$id/{print \$1}")
        for r in $rs; do
          kubectl delete $t $ns $r
        done
      done
    else
      echo "usage: bash clean_solution.sh <id> [namespace]"
    fi

  ..

* in order to identify a specific deployment job and its resources, use the
  "ingress URL" provided to the user when the deployment job completion
  notification was provided on the Acumos platform, e.g.

  .. code-block:: text

    square deployment is complete. The solution can be accessed at the ingress
    URL https://acumos.example.com/square/191111-162114/
  ..

  * The URL part after the model name is the unique ID assigned to the
    deployment job, and provides a timestamp when the deployment job was
    invoked by the default deploy,sh deployment script:

    .. code-block:: text

      UNIQUE_ID=$(date +%y%m%d)-$(date +%H%M%S)
    ..


* given the unique ID, you should be able to clean up all related resources
  as needed, using the example script above

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
