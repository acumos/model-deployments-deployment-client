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
# What this is: Script to copy a prior Acumos deployment parameters/state and
# key data to a new clone, for redeployment.
#
# Prerequisites:
#
# Usage:
#   $ bash clean_solution.sh [ns=<namespace>] [days=<days>] [match=<match>] [--dry-run] [--force]
#     namespace: k8s namespace (default: acumos)
#     days: select all solutions that are <days> old or older
#     match: select solutions that match <pattern>
#     --dry-run: show what would be deleted only
#     --force: delete without prompt
#
#     If no <match> is specified, resources that match the default pattern
#     [0-9]{5}-[0-9]{5} will be selected. These are resources for solutions
#     identified by the uniqueid value generated by the deploy.sh script.

days=10
ns=acumos
for arg; do
  if [[ "$arg" == *"ns="* ]]; then ns=$(echo $arg | cut -d '=' -f 2)
  elif [[ "$arg" == *"days="* ]]; then days=$(echo $arg | cut -d '=' -f 2)
  elif [[ "$arg" == *"match="* ]]; then match=$(echo $arg | cut -d '=' -f 2)
  elif [[ "$arg" == *"--dry-run"* ]]; then dry="--dry-run"
  elif [[ "$arg" == *"--force"* ]]; then force="--force"
  fi
done

if [[ "$match" != "" ]]; then
  if [[ "$dry" == "--dry-run" ]]; then
    echo "Showing what resources would be cleaned for solution that matches $match"
  else
    echo "Cleaning all resources for solution that matches $match"
  fi
  ts="deployments daemonset service configmap serviceaccount clusterrole clusterrolebinding configmap pvc ingress"
  for t in $ts; do
    rs=$(kubectl get $t -n $ns | awk "/$match/{print \$1}")
    for r in $rs; do
      if [[ "$(kubectl get $t -n $ns $r | tail -1)" != "" ]]; then
        echo "$t: $r"
        if [[ "$dry" != "--dry-run" ]]; then
          if [[ "$force" == "--force" ]]; then
            kubectl delete $t -n $ns $r
          else
            read -p "Delete this resource ? " -n 1 -r
            echo    # (optional) move to a new line
            if [[ $REPLY =~ ^[Yy]$ ]]
            then
              kubectl delete $t -n $ns $r
            fi
          fi
        fi
      fi
    done
  done
else
  if [[ "$dry" == "--dry-run" ]]; then
    echo "Showing what resources would be cleaned for solutions older than $days days"
  else
    echo "Cleaning all resources for solutions older than $days days"
  fi
  dthen=$(date -d "$days days ago" -Ins --utc | sed 's/+0000/Z/')
  ms=$(kubectl get ingress -n $ns -o go-template --template '{{range .items}}{{.metadata.name}} {{.metadata.creationTimestamp}}{{"\n"}}{{end}}' | awk "\$2 <= \"$dthen\"" | awk '/[0-9]{5}-[0-9]{5}/{print $1}')
  for m in $ms; do
    id=$(echo $m | rev | cut -d '-' -f 1,2 | rev)
    bash clean_solution.sh ns=$ns match=$id $dry $force
  done
fi
