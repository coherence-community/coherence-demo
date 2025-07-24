# Copyright 2025 Oracle Corporation and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at
# https://oss.oracle.com/licenses/upl.
#
# Test the demo deployed to K8s using Kind
#

if [ $# -ne 2 ]; then
  echo "Please provide kind config.yam and operator version"
  exit 1
fi

KIND_CONFIG=$1
OPERATOR_VERSION=$2
IMAGE_VERSION=8.3.0-SNAPSHOT

KIND_IMAGE="kindest/node:v1.33.0@sha256:91e9ed777db80279c22d1d1068c091b899b2078506e4a0f797fbf6e397c0b0b2"
KIND_CLUSTER=coherence-demo
NAMESPACE=coherence-example

set -e
kind create cluster --name $KIND_CLUSTER --wait 10m --config $KIND_CONFIG --image $KIND_IMAGE

trap "kind delete cluster --name $KIND_CLUSTER || true" 0 1 2 3

kubectl apply -f https://github.com/oracle/coherence-operator/releases/download/${OPERATOR_VERSION}/coherence-operator.yaml
echo "Waiting for operator to be ready..."
kubectl -n coherence wait --timeout=300s --for condition=available deployment/coherence-operator-controller-manager

echo "Coherence Operator is installed"

kubectl create namespace coherence-example

mvn clean install -Pdocker

IMAGE=coherence-demo:${IMAGE_VERSION}

kind --name $KIND_CLUSTER load docker-image $IMAGE

echo "Deploy the Coherence cluster"
kubectl create --namespace $NAMESPACE -f yaml/demo-cluster.yaml

sleep 5

echo "Waiting for cluster to be ready..."

kubectl -n $NAMESPACE wait --timeout=300s --for condition=Ready coherence/primary-cluster-storage

kubectl port-forward --namespace $NAMESPACE primary-cluster-http-0 8080:8080 &
PID=$!
sleep 5

curl -s http://127.0.0.1:8080/service/chart-data/false | jq
curl -s http://127.0.0.1:8080/service/chart-data/true | jq
curl -v http://127.0.0.1:8080/service/start-member/1
sleep 10
curl -v http://127.0.0.1:8080/service/stop-member/1
curl -q http://127.0.0.1:8080/service/developer/hostname
curl -q http://127.0.0.1:8080/service/developer/populate
kill $PID
