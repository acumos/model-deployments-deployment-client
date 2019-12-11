#!/bin/bash
# ===============LICENSE_START=======================================================
# Acumos Apache-2.0
# ===================================================================================
# Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
# ===================================================================================
# This Acumos software file is distributed by AT&T
# under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# This file is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ===============LICENSE_END=========================================================
#
# What this is: Script to update (or create) the k8s cluster configuration for
# a single-node k8s cluster, so that Acumos project and platform images can be
# deployed there.
#
# Prerequisites:
#   - Single-node generic k8s cluster deployed. (DO NOT USE WITH OPENSHIFT!)
#   - User running this script has key-based SSH access to the k8s cluster master
#     node, and sudo permissions on the node.
#
# Usage:
#   NOTE: This script will disrupt docker service on the k8s master node for
#   a short time, but all k8s services will be restarted by docker. FOR TEST
#   PURPOSES ONLY.
#   $ bash update_k8s_config.sh <user@host> <namespace> <registry> <registry_user> <registry_password>
#     user@host: user account and hostname in "user@host" form
#     namespace: k8s namespace
#     registry: host:port of the docker registry for the Acumos platform
#     registry_user: username for access to the docker registry
#     registry_password: passwsord for access to the docker registry

USER=$1
NAMESPACE=$2
DOCKER_REGISTRY=$3
DOCKER_REGISTRY_USER=$4
DOCKER_REGISTRY_PASSWORD=$5

ssh -x -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $USER <<EOF
  set -x
  updated=
  if [[ "\$(sudo ls /etc/docker/daemon.json)" != '' ]]; then
    sudo cp /etc/docker/daemon.json /tmp/daemon-save.json
    if [[ \$(sudo grep -c "disable-legacy-registry" /etc/docker/daemon.json) -eq 0 ]]; then
      sudo sed -i -- "s/\{/\{\n\"disable-legacy-registry\",\"true\",\n/" /etc/docker/daemon.json
      updated=true
    fi
    if [[ \$(sudo grep -c "insecure-registries" /etc/docker/daemon.json) -eq 0 ]]; then
      sudo sed -i -- "s/\{/\{\n\"insecure-registries\": \[ \"$DOCKER_REGISTRY\" \],\n/" /etc/docker/daemon.json
      updated=true
    fi
    if [[ \$(sudo grep -c "$DOCKER_REGISTRY" /etc/docker/daemon.json) -eq 0 ]]; then
      sudo sed -i -- "s/\"insecure-registries\": \[/\"insecure-registries\": \[ \"$DOCKER_REGISTRY\",/" /etc/docker/daemon.json
      updated=true
    fi
  else
    cat <<EOG | sudo tee >/etc/docker/daemon.json
{
"insecure-registries": [ "$DOCKER_REGISTRY" ],
"disable-legacy-registry": true
}
EOG
    new=true
  fi
  sudo cat /etc/docker/daemon.json
  if [[ "\$updated" == "true" || "\$new" == "true" ]]; then
    sudo systemctl daemon-reload
    sudo service docker restart
    if [[ \$? -ne 0 ]]; then
      if [[ "\$new" == "\$true" ]]; then
        sudo rm /etc/docker/daemon.json
      else
        sudo cp /tmp/daemon-save.json /etc/docker/daemon.json
      fi
      sudo systemctl daemon-reload
      sudo service docker restart
      exit 1
    fi
  fi
  while [[ "\$(kubectl get namespace $NAMESPACE)" == "" ]]; do
    echo "Waiting for kubernetes API to be available after docker restart"
    sleep 10
  done
  docker login $DOCKER_REGISTRY -u $DOCKER_REGISTRY_USER -p $DOCKER_REGISTRY_PASSWORD
  docker login https://nexus3.acumos.org:10004
  docker login https://nexus3.acumos.org:10003
  docker login https://nexus3.acumos.org:10002
  kubectl delete secret -n $NAMESPACE acumos-registry
  cat /home/\$USER/.docker/config.json
  b64=\$(cat /home/\$USER/.docker/config.json | base64 -w 0)
  cat <<EOG >acumos-registry.yaml
apiVersion: v1
kind: Secret
metadata:
  name: acumos-registry
  namespace: $NAMESPACE
data:
  .dockerconfigjson: \$b64
type: kubernetes.io/dockerconfigjson
EOG
  kubectl create -f acumos-registry.yaml
  kubectl get secret -n $NAMESPACE acumos-registry -o yaml | \
    awk "/.dockerconfigjson:/{print \$2}" | \
    sed "s/  .dockerconfigjson: //"  | base64 --decode
EOF
