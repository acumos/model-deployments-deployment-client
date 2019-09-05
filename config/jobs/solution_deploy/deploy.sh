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

function docker_login() {
  trap 'fail' ERR
  while ! docker login $1 -u $2 -p $3 ; do
    sleep 10
    log "Docker login failed at $1, trying again"
  done
}

function prepare_docker() {
  trap 'fail' ERR
  log "login to the Acumos platform docker proxy"
  docker_login $ACUMOS_DOCKER_REGISTRY $ACUMOS_DOCKER_REGISTRY_USER \
    $ACUMOS_DOCKER_REGISTRY_PASSWORD
  log "Log into LF Nexus Docker repos"
  docker_login https://nexus3.acumos.org:10004 docker docker
  docker_login https://nexus3.acumos.org:10003 docker docker
  docker_login https://nexus3.acumos.org:10002 docker docker
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
      while [[ $node < $nodes ]] ; do
        name=$(jq -r ".nodes[$node].container_name" blueprint.json)
        if [[ $name == $model ]]; then
          echo ".nodes[$node].proto_uri = \"http://localhost/$model/model.proto\""
          jq ".nodes[$node].proto_uri = \"http://localhost/$model/model.proto\"" blueprint.json > blueprint1.json
          mv blueprint1.json blueprint.json
        fi
        node=$[$node+1]
      done
    done
  fi
}

function deploy_solution() {
  trap 'fail' ERR
  log "invoke kubectl to deploy the services and deployments in solution.yaml"
  kubectl create -f deploy/solution.yaml

  log "Wait for all pods to be Running"
  pods=$(kubectl get pods -n $ACUMOS_NAMESPACE | awk '/-/ {print $1}')
  while [[ "$pods" == "No resources found." ]]; do
    log "pods are not yet created, waiting 10 seconds"
    pods=$(kubectl get pods -n $ACUMOS_NAMESPACE | awk '/-/ {print $1}')
  done

  for pod in $pods; do
    status=$(kubectl get pods -n $ACUMOS_NAMESPACE | awk "/$pod/ {print \$3}")
    while [[ "$status" != "Running" ]]; do
      log "$pod status is $status. Waiting 10 seconds"
      sleep 10
      status=$(kubectl get pods -n $ACUMOS_NAMESPACE | awk "/$pod/ {print \$3}")
    done
    log "$pod status is $status"
  done

  if [[ $(grep -c 'app: modelconnector' solution.yaml) -gt 0 ]]; then
    log "Patch dockerinfo.json as workaround for https://jira.acumos.org/browse/ACUMOS-1791"
    sed -i -- 's/"container_name":"probe"/"container_name":"Probe"/' dockerinfo.json

    log "Update blueprint.json and dockerinfo.json for use of logging components"
    sol=$(grep "app:" solution.yaml | awk '{print $2}' | grep -v 'modelconnector' | uniq | sed ':a;N;$!ba;s/\n/ /g')
    apps="$sol"
    cp blueprint.json deploy/.
    cp dockerinfo.json deploy/.
    for app in $apps; do
       sed -i -- "s/$app/nginx-proxy-$app/g" deploy/dockerinfo.json
       sed -i -- "s/\"container_name\": \"$app\"/\"container_name\": \"nginx-proxy-$app\"/g" deploy/blueprint.json
    done
    # update to nginx-proxy service port
    sed -i -- "s/\"port\":\"8556\"/\"port\":\"8550\"/g" deploy/dockerinfo.json

    log "send dockerinfo.json to the Model Connector service via the /putDockerInfo API"
    ACUMOS_MODELCONNECTOR_PORT=$(kubectl get pods -n $ACUMOS_NAMESPACE federation-service -o json | jq -r '.spec.ports[0].nodePort')
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

function deploy_logging() {
  trap 'fail' ERR
  cp kubernetes-client/deploy/private/templates/filebeat*.yaml deploy/.
  replace_env deploy
  kubectl create -f deploy/filebeat-configmap.yaml
  kubectl create -f deploy/filebeat-rbac.yaml
  kubectl create -f deploy/filebeat-daemonset.yaml
  if [[ -d microservice ]]; then
    # Composite model
    sol=$(grep "app:" solution.yaml | awk '{print $2}' | grep -v 'modelconnector' | uniq | sed ':a;N;$!ba;s/\n/ /g')
    apps="$sol"
    modelrunnerversion="v1";
    for app in $apps; do
      export MODEL_NAME=$app
      get_model_env $app
      modelrunnerversion="v1";
      if [[ $SOLUTION_MODEL_RUNNER_STANDARD -eq 1 ]]; then
        modelrunnerversion="v2";
      fi
      cp kubernetes-client/deploy/private/templates/nginx-configmap-$modelrunnerversion.yaml deploy/$app-nginx-configmap-$modelrunnerversion.yaml
      cp kubernetes-client/deploy/private/templates/nginx-service-composite.yaml deploy/$app-nginx-service-composite.yaml
      cp kubernetes-client/deploy/private/templates/nginx-deployment.yaml deploy/$app-nginx-deployment.yaml
      # copy k8s conf for modelconnector nginx-proxy
      cp kubernetes-client/deploy/private/templates/nginx-mc-configmap-$modelrunnerversion.yaml deploy/.
      cp kubernetes-client/deploy/private/templates/nginx-mc-service.yaml deploy/.
      cp kubernetes-client/deploy/private/templates/nginx-mc-deployment.yaml deploy/.

      replace_env deploy
      kubectl create -f deploy/$app-nginx-configmap-$modelrunnerversion.yaml
      kubectl create -f deploy/$app-nginx-service-composite.yaml
      kubectl create -f deploy/$app-nginx-deployment.yaml

    done

    # create nginx-proxy for model-connector
    kubectl create -f deploy/nginx-mc-configmap-$modelrunnerversion.yaml
    kubectl create -f deploy/nginx-mc-service.yaml
    kubectl create -f deploy/nginx-mc-deployment.yaml

  else
    # Simple model
    app=$(grep "app:" solution.yaml | awk '{print $2}' | grep -v 'modelconnector' | uniq)
    export MODEL_NAME=$(grep -m 1 "name:" solution.yaml | awk '{print $2}')
    get_model_env $app
    modelrunnerversion="v1";
    if [[ $SOLUTION_MODEL_RUNNER_STANDARD -eq 1 ]]; then
      modelrunnerversion="v2";
    fi
    cp kubernetes-client/deploy/private/templates/nginx-configmap-$modelrunnerversion.yaml deploy/$app-nginx-configmap-$modelrunnerversion.yaml
    cp kubernetes-client/deploy/private/templates/nginx-service.yaml deploy/$app-nginx-service.yaml
    cp kubernetes-client/deploy/private/templates/nginx-deployment.yaml deploy/$app-nginx-deployment.yaml

    replace_env deploy
    kubectl create -f deploy/$app-nginx-configmap-$modelrunnerversion.yaml
    kubectl create -f deploy/$app-nginx-service.yaml
    kubectl create -f deploy/$app-nginx-deployment.yaml
  fi
}

set -x
source ./deploy_env.sh

prepare_docker
update_blueprint
deploy_solution
deploy_logging
