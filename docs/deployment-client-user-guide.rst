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

*******************
Configuring Jenkins
*******************

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
system-integration clone used to execute the OneClick-based deployment.

Once the solution-deploy job is created under Jenkins, there is one manual step
required to complete the configuration. Use these steps to complete it:

* login to the Jenkins UI, by default https://<ACUMOS_DOMAIN/jenkins
* select the solution-deploy job link
* select 'Configure'
* set the string parameter value for "DEFAULT_SOLUTION_DOMAIN" to the domain
  name of the k8s cluster where solutions should be deployed
* set the string parameter value for "DEFAULT_NAMESPACE" to the namespace
  under which solutions should be deployed
* select 'Save'
