#!/bin/bash
#
# Copyright (c) 2024 Oracle and/or its affiliates.
#
# You may not use this file except in compliance with the Universal Permissive
# License (UPL), Version 1.0 (the "License.")
#
# You may obtain a copy of the License at https: //opensource.org/licenses/UPL.
#
# Unless required by applicable law or agreed to in writing, software distributed
# under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
# CONDITIONS OF ANY KIND, either express or implied.
#
# See the License for the specific language governing permissions and limitations
# under the License.
#
#

RELEASE_IMAGE_PREFIX=ghcr.io/coherence-community

echo "Building Go image..."
cd go
CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o ./go-demo .
docker build -t ${RELEASE_IMAGE_PREFIX}/go-demo .
cd ..

echo "Building JavaScript image..."
cd js
docker build -t ${RELEASE_IMAGE_PREFIX}/js-demo .
cd ..

echo "Building Python image..."
cd py
docker build -t ${RELEASE_IMAGE_PREFIX}/py-demo .
cd ..




