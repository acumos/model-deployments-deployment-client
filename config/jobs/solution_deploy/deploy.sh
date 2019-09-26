#!/bin/bash
# ===============LICENSE_START=======================================================
# Acumos Apache-2.0
# ===================================================================================
# Copyright (C) 2018-2019 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
# Modifications Copyright (C) 2019 Nordix Foundation.
# ===================================================================================
# This Acumos software file is distributed by AT&T and Tech Mahindra
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
# What this is: Deployment script for Acumos solutions under Jenkins control,
# to kubernetes clusters.
#.
# Prerequisites:
# - Configure the Acumos Deployment Client with this script in a PV mapped to
#   the container folder /app/config/jobs/solution_deploy
# - Provide the following values in the deploy_env.sh script:
#   SOLUTION_MODEL_RUNNER_STANDARD: v1|v2
#   SOLUTION_ID: Solution ID for simple solution
#   COMP_SOLUTION_ID: Solution ID for composite solution (if applicable)
#   COMP_REVISION_ID: Revision ID for composite solution (if applicable)
#   LOGSTASH_HOST: IP/FQDN of the Logstash service
#   LOGSTASH_PORT: Port of the Logstash service
#
# Usage: Intended to be called from Jenkins jobs created by the Acumos
#   Deployment Client
# - bash deploy.sh
#

trap 'fail' ERR

function fail() {
  log "$1"
  exit 1
}

function log() {
  fname=$(caller 0 | awk '{print $2}')
  fline=$(caller 0 | awk '{print $1}')
  echo; echo "$fname:$fline ($(date)) $1"
}

function replace_env() {
  trap 'fail' ERR
  local files; local vars; local v; local vv
  log "Set variable values in k8s templates at $1"
  set +x
  if [[ -f $1 ]]; then files="$1";
  else files="$1/*.yaml"; fi
  vars=$(grep -Rho '<[^<.]*>' $files | sed 's/<//' | sed 's/>//' | sort | uniq)
  for f in $files; do
    for v in $vars ; do
      eval vv=\$$v
      sed -i -- "s~<$v>~$vv~g" $f
    done
  done
  set -x
}

prepare_k8s() {
  trap 'fail' ERR
  if [[ $(kubectl get secrets -n $NAMESPACE | grep -c 'acumos-registry ') == 0 ]]; then
    log "Create k8s secret for image pulling from docker using ~/.docker/config.json"
    b64=$(cat ~/.docker/config.json | base64 -w 0)
    cat << EOF >deploy/acumos-registry.yaml
apiVersion: v1
kind: Secret
metadata:
  name: acumos-registry
  namespace: $NAMESPACE
data:
  .dockerconfigjson: $b64
type: kubernetes.io/dockerconfigjson
EOF

    kubectl create -f deploy/acumos-registry.yaml
  fi
}

function update_blueprint() {
  trap 'fail' ERR
  if [[ -d microservice ]]; then
    log "update URL to model.proto files in blueprint.json"
    # Note: modelconnector sends these URLs to probe which retrieves the proto
    # files from the solution-embedded nginx server
    nodes=$(jq '.nodes | length' blueprint.json)
    models=$(ls microservice)
    for model in $models ; do
      node=0
      while [[ $node -lt $nodes ]] ; do
        name=$(jq -r ".nodes[$node].container_name" blueprint.json)
        if [[ $name == $model ]]; then
          echo ".nodes[$node].proto_uri = \"http://localhost/$model/model.proto\""
          jq ".nodes[$node].proto_uri = \"http://localhost/$model/model.proto\"" blueprint.json > blueprint1.json
          mv blueprint1.json blueprint.json
        fi
        node=$((node+1))
      done
    done
  fi
}

function update_dockerinfo() {
  trap 'fail' ERR
  if [[ -d microservice ]]; then
    log "update address of containers in dockerinfo.json"
    nodes=$(jq '.docker_info_list | length' dockerinfo.json)
    node=0
    while [[ $node -lt $nodes ]]; do
      ip=$(jq -r ".docker_info_list[$node].ip_address" dockerinfo.json)
      jq ".docker_info_list[$node].ip_address = \"$ip-$TRACKING_ID\"" dockerinfo.json > dockerinfo1.json
      mv dockerinfo1.json dockerinfo.json
      node=$((node+1))
    done
  fi
}

function deploy_solution() {
  trap 'fail' ERR
  log "invoke kubectl to deploy the services and deployments in solution.yaml"
  cp solution.yaml deploy/.
  replace_env deploy
  kubectl create -f deploy/solution.yaml

  log "Wait for all pods to be Running"
  pods=$(kubectl get pods -n $NAMESPACE | awk "/$TRACKING_ID/ {print \$1}")
  while [[ "$pods" == "No resources found." ]]; do
    log "pods are not yet created, waiting 10 seconds"
    pods=$(kubectl get pods -n $NAMESPACE | awk "/$TRACKING_ID/ {print \$1}")
  done

  for pod in $pods; do
    status=$(kubectl get pods -n $NAMESPACE | awk "/$pod/ {print \$3}")
    while [[ "$status" != "Running" ]]; do
      log "$pod status is $status. Waiting 10 seconds"
      sleep 10
      status=$(kubectl get pods -n $NAMESPACE | awk "/$pod/ {print \$3}")
    done
    log "$pod status is $status"
  done

  if [[ $(grep -c 'app: modelconnector' deploy/solution.yaml) -gt 0 ]]; then
    log "Patch dockerinfo.json as workaround for https://jira.acumos.org/browse/ACUMOS-1791"
    sed -i -- 's/"container_name":"probe"/"container_name":"Probe"/' dockerinfo.json

    log "Update blueprint.json and dockerinfo.json for use of logging components"
    sol=$(grep "app:" deploy/solution.yaml | awk '{print $2}' | grep -v 'modelconnector' | uniq | sed ':a;N;$!ba;s/\n/ /g')
    apps="$sol"
    cp blueprint.json deploy/.
    cp dockerinfo.json deploy/.
    for app in $apps; do
       sed -i -- "s/$app/nginx-$app/g" deploy/dockerinfo.json
       sed -i -- "s/\"container_name\": \"$app\"/\"container_name\": \"nginx-$app\"/g" deploy/blueprint.json
    done
    # update to nginx-proxy service port
    sed -i -- 's/"8556"/"8550"/g' deploy/dockerinfo.json

    log "send dockerinfo.json to the Model Connector service via the /putDockerInfo API"
    ACUMOS_MODELCONNECTOR_PORT=$(kubectl get svc -n $NAMESPACE -o json modelconnector-$TRACKING_ID | jq -r '.spec.ports[0].nodePort')
    while ! curl -v -X PUT -H "Content-Type: application/json" \
      http://$SOLUTION_DOMAIN:${ACUMOS_MODELCONNECTOR_PORT}/putDockerInfo -d @deploy/dockerinfo.json; do
        log "wait for Model Connector service via the /putDockerInfo API"
        sleep 10
    done
    log "send blueprint.json to the Model Connector service via the /putBlueprint API"
    curl -v -X PUT -H "Content-Type: application/json" \
      http://$SOLUTION_DOMAIN:${ACUMOS_MODELCONNECTOR_PORT}/putBlueprint \
        -d @deploy/blueprint.json
  fi
}

function deploy_logging() {
  trap 'fail' ERR
  cp templates/filebeat-*.yaml deploy/.
  cp templates/nginx-proxy-log-pvc.yaml deploy/.
  replace_env deploy
  kubectl create -f deploy/filebeat-data-pvc.yaml
  kubectl create -f deploy/filebeat-configmap.yaml
  kubectl create -f deploy/filebeat-rbac.yaml
  kubectl create -f deploy/filebeat-daemonset.yaml
  kubectl create -f deploy/nginx-proxy-log-pvc.yaml

  if [[ -d microservice ]]; then
    # Composite model
    ns=$(jq '.nodes | length' blueprint.json)
    n=0; apps=""
    while [[ $n -lt $ns ]]; do
      app=$(jq -r ".nodes[$n].container_name" blueprint.json)
      apps="$apps $app"
      n=$((n+1))
    done
    for app in $apps; do
      export MODEL_NAME=$app
      cp templates/nginx-configmap-$SOLUTION_MODEL_RUNNER_STANDARD.yaml deploy/$app-nginx-configmap.yaml
      cp templates/nginx-service.yaml deploy/$app-nginx-service.yaml
      cp templates/nginx-deployment.yaml deploy/$app-nginx-deployment.yaml
      replace_env deploy
      kubectl create -f deploy/$app-nginx-configmap.yaml
      kubectl create -f deploy/$app-nginx-service.yaml
      kubectl create -f deploy/$app-nginx-deployment.yaml
    done

    # create nginx-proxy for model-connector
    # copy k8s conf for modelconnector nginx-proxy
    cp templates/nginx-mc-configmap-$SOLUTION_MODEL_RUNNER_STANDARD.yaml deploy/nginx-mc-configmap.yaml
    cp templates/nginx-mc-service.yaml deploy/.
    cp templates/nginx-mc-deployment.yaml deploy/.
    replace_env deploy
    kubectl create -f deploy/nginx-mc-configmap.yaml
    kubectl create -f deploy/nginx-mc-service.yaml
    kubectl create -f deploy/nginx-mc-deployment.yaml
  else
    # Simple model
    cp templates/nginx-configmap-$SOLUTION_MODEL_RUNNER_STANDARD.yaml deploy/nginx-configmap.yaml
    cp templates/nginx-service.yaml deploy/nginx-service.yaml
    cp templates/nginx-deployment.yaml deploy/nginx-deployment.yaml
    MODEL_NAME=$SOLUTION_NAME
    replace_env deploy
    kubectl create -f deploy/nginx-configmap.yaml
    kubectl create -f deploy/nginx-service.yaml
    kubectl create -f deploy/nginx-deployment.yaml
  fi

  log "Create ingress rule"
  cp templates/ingress.yaml deploy/.
  replace_env deploy/ingress.yaml
  kubectl create -f deploy/ingress.yaml
}

set -x
WORK_DIR=$(pwd)
cd $(dirname "$0")
if [[ -e deploy ]]; then rm -rf deploy; fi
mkdir deploy
source ./deploy_env.sh

update_blueprint
update_dockerinfo
deploy_solution
deploy_logging
