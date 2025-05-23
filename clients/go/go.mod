//
// Copyright (c) 2024 Oracle and/or its affiliates.
//
// You may not use this file except in compliance with the Universal Permissive
// License (UPL), Version 1.0 (the "License.")
//
// You may obtain a copy of the License at https: //opensource.org/licenses/UPL.
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied.
//
// See the License for the specific language governing permissions and limitations
// under the License.
//
module main

go 1.23.0

toolchain go1.23.7

require (
	github.com/google/uuid v1.6.0
	github.com/oracle/coherence-go-client/v2 v2.2.0
)

require (
	github.com/golang/protobuf v1.5.3 // indirect
	golang.org/x/net v0.38.0 // indirect
	golang.org/x/sys v0.31.0 // indirect
	golang.org/x/text v0.23.0 // indirect
	google.golang.org/genproto/googleapis/rpc v0.0.0-20230711160842-782d3b101e98 // indirect
	google.golang.org/grpc v1.58.3 // indirect
	google.golang.org/protobuf v1.36.1 // indirect
)
