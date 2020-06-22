/*
 * File: controllers.js
 *
 * Copyright (c) 2019, 2020 Oracle and/or its affiliates.
 *
 * You may not use this file except in compliance with the Universal Permissive
 * License (UPL), Version 1.0 (the "License.")
 *
 * You may obtain a copy of the License at https://opensource.org/licenses/UPL.
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */

/**
 * The Coherence Demonstration Application
 */
var demoApp = angular.module('demoApp', ['filters', 'ngCookies']);

/**
 * Define a "memory" filter for formatting content.
 */
angular.module('filters', [])
    .filter('memory', function () {
        return function (size) {
            if (isNaN(size))
                size = 0;

            if (size < 1024)
                return size + ' Bytes';

            size /= 1024;

            if (size < 1024)
                return size.toFixed(2) + ' KB';

            size /= 1024;

            if (size < 1024)
                return size.toFixed(2) + ' MB';

            size /= 1024;

            if (size < 1024)
                return size.toFixed(2) + ' GB';

            size /= 1024;

            return size.toFixed(2) + ' TB';
        };
    })
    .filter('clusterName', function() {
        return function(cluster) {
            return cluster === '' ? '' : '(' + cluster + ')';
        };
    })
    // directive to make adding demo insight easier
    .directive('insight', function() {
        return {
            restrict: 'E',
            replace: true,
            template: '<a href="#" data-toggle="tooltip" title="Click for demo insight"><i class="fa fa-info fa-fw"></i></a>'
        };
    });

/**
 * The Demo Application Controller.
 */
demoApp.controller('DemoController', ['$scope', '$http', '$interval', '$location', '$window', '$timeout', '$sce', '$cookies',
    function ($scope, $http, $interval, $location, $window, $timeout, $sce, $cookies) {

    // define 'self' as a means of referring to this controller
    // from with in other closures
    var self = $scope;

    // application constants
    self.MAXIMUM_AGGREGATION_TICKS = 20;
    self.START_FEDERATION          = 'Start';
    self.STOP_FEDERATION           = 'Stop';
    self.SUSPEND_FEDERATION        = 'Pause';
    self.RESUME_FEDERATION         = 'Resume';
    self.MAX_SPARKLINE             = 15;

    var KB = 1024;
    var MB = KB * KB;
    var GB = MB * KB;
    var TB = GB * KB;

    // define initial states for the application (these will be refreshed asynchronously)
    self.positions         = 0;
    self.valuation         = 0;
    self.symbolNames       = [];
    self.symbolFrequency   = {};
    self.symbolCount       = {};
    self.memberInfo        = [];
    self.currentPrice      = {};
    self.lastPrice         = {};
    self.priceChange       = {};
    self.firstRefresh      = true;
    self.currentBytesSent  = 0;
    self.lastBytesSent     = -1;
    self.bytesSentData     = [];
    self.totalFrequency    = 0;
    self.lastStatusMessage = '';
    self.displayStatus     = false;
    self.averageQueryTime  = 0;

    self.portfolioRefresh = {
        enabled: false,
        useIndexes: true
        };

    self.startingMember            = false;
    self.secondaryCluster          = 'disabled';
    self.primaryClusterName        = 'primary-cluster';
    self.secondaryClusterName      = 'secondary-cluster';
    self.localClusterName          = '';
    self.federationControlLabel    = self.START_FEDERATION;
    self.federationStateLabel      = self.SUSPEND_FEDERATION;
    self.persistenceResult         = 'N/A';
    self.valuationDirection        = 'N/A';
    self.insightLabel              = 'Disable Insight';
    self.insightEnabled            = true;
    self.insightContent            = [];
    self.isRunningInKubernetes     = false;
    self.coherenceEdition          = undefined;
    self.coherenceEditionFull      = undefined;
    self.isThisPrimaryCluster      = false;
    self.federationConfiguredInK8s = false;
    self.isMetricsEnabled          = false;
    self.coherenceVersion          = undefined;
    self.javaVersion               = undefined;
    self.coherenceVersionAsInt     = 0;
    self.thisClusterName           = undefined;
    self.lastMemberCount           = undefined;
    self.runningMode               = "";
    self.maxServers                = 0;
    self.maxCacheEntries           = 0;
    self.disableShutdown           = false;

    self.displayingSplash = false;

    var skipSplashCookie     = 'com.oracle.coherence.demo.skipSplashScreen';
    var disableInsightCookie = 'com.oracle.coherence.demo.DemoDisableInsight';

    // cookies retrieved for splash
    var insightCookies = {
       skipSplash:     $cookies.get(skipSplashCookie),
       disableInsight: $cookies.get(disableInsightCookie)
    };

    if (insightCookies.disableInsight) {
        self.insightLabel   = 'Enable Insight';
        self.insightEnabled = false;
    }

    // retrieve the environment to determine if we are running standalone or under Kubernetes
    $http.get('/service/developer/environment').then(function(response) {
        self.isRunningInKubernetes     = response.data.runningInKubernetes;
        self.coherenceVersion          = response.data.coherenceVersion;
        self.coherenceVersionAsInt     = response.data.coherenceVersionAsInt;
        self.isThisPrimaryCluster      = response.data.primaryCluster;
        self.federationConfiguredInK8s = response.data.federationConfiguredInK8s;
        self.thisClusterName           = response.data.thisClusterName;
        self.coherenceEdition          = response.data.coherenceEdition;
        self.coherenceEditionFull      = response.data.coherenceEditionFull;
        self.javaVersion               = response.data.javaVersion;
        self.isMetricsEnabled          = response.data.metricsEnabled;
        self.maxServers                = response.data.maxServers;
        self.maxCacheEntries           = response.data.maxCacheEntries;
        self.disableShutdown           = response.data.disableShutdown;

        self.runningMode = self.isRunningInKubernetes ? " - running in Kubernetes" : "";
        
        // setup cluster name
        var params       = $location.hash().substring(1).split("&");
        var clusterParam = params[0].split("=");
        var cluster      = clusterParam[1];

        if (self.federationConfiguredInK8s && !self.isThisPrimaryCluster)
           {
           // if we have configuration federation in K8s and this is the
           // secondary cluster, then set the cluster name from the "/environment/" call
           // as the cluster name will not be abel to be retrieved from URL

           self.clusterName = self.thisClusterName;
           }
        else {
            self.clusterName = (cluster === undefined) ? '' : cluster;
        }
        
        // obtain the cluster names from the developer resource as they can be overridden
        // via -Dprimary.cluster or -Dsecondary.cluster
        $http.get('/service/developer/clusterNames').then(function(response) {
            var clusterNames = response.data.split(':');
            self.primaryClusterName   = clusterNames[0];
            self.secondaryClusterName = clusterNames[1];

            // display welcome insight (on primary cluster only)
            if (self.clusterName !== self.secondaryClusterName && !insightCookies.skipSplash) {
                 self.displayInsight('welcome');
            }
        });

    });

    self.symbolsChartData = [];

    // setup the members distribution chart ----

    self.memberChartData = [{ memberId: 0, entryCount: 0 }];

    // data distribution graph
    var distributionChart = Morris.Bar({
            element: 'dataDistributionGraph',
            data: self.memberChartData ,
            xkey: 'memberId',
            ykeys: ['entryCount'],
            labels: ['Positions'],
            hideHover: 'auto',
            gridIntegers: true,
            resize: true
        });

    self.aggregationChartData = [];

    // aggregation time graph
    var aggregationChart = Morris.Line({
            element: 'aggregationGraph',
            data: self.aggregationChartData ,
            xkey: 'timestamp',
            ykeys: ['aggregationTime'],
            labels: ['Aggregation Time'],
            xlabels: '5min',
            hideHover: 'auto',
            gridIntegers: true,
            resize: true,
            smooth: false
        });

    // mini chart for showing federation data
    var federationChart = Morris.Bar({
            element: 'federationGraph',
            data: [0, 0] ,
            xkey: 'timestamp',
            ykeys: ['bytes'],
            labels: [''],
            hideHover: 'true',
            axes: false,
            grid: false,
            gridIntegers: true ,
            hoverCallback: function (index, options, content, row) {
               return row.bytes + ' bytes';
            }
        });

    // ---- the function to refresh the application state ----

    self.refresh = function() {
        // refresh the application state when the chart-data is returned
        $http.get('/service/chart-data/' + self.portfolioRefresh.enabled).then(function (response) {

            var chartData = response.data;

            self.symbolNames = chartData.symbols.sort();;
            self.symbolsChartData = [];
            self.symbolFrequency = {};
            self.symbolCount = {};

            var valuationTotal = 0;

            // on first time, set the last price to the current
            if (self.firstRefresh) {
                self.lastPrice = chartData.symbolPrice;
            }

            var frequencySum = 0;

            // update the chart with current valuations for each symbol
            self.symbolNames.forEach(function(symbolName) {

                // get the frequency for each symbol
                var frequency   = chartData.symbolFrequency[symbolName];
                frequencySum   += frequency;

                self.symbolFrequency[symbolName] = frequency;

                // get the count of positions for each symbol
                var count = chartData.symbolCount[symbolName];
                self.symbolCount[symbolName] = count;

                // get the current price and determine the amount
                var amount = frequency * chartData.symbolPrice[symbolName];

                // calculate the delta
                self.priceChange[symbolName] = chartData.symbolPrice[symbolName] - self.lastPrice[symbolName];

                valuationTotal += amount;

                self.symbolsChartData.push({"label":symbolName, "value": amount});
            });

            // check to see if the valuation went up or down
            self.valuationDirection = valuationTotal === self.valuation ? 'N/A' :
                                      valuationTotal >   self.valuation ? 'up'  : 'down';

            self.valuationStyle = 'color: ' +
                (self.valuationDirection === 'N/A' ? "'black'" :
                 self.valuationDirection === 'up'  ? "'dark-green;" : "'red'") + ';';

            // update the valuation
            self.valuation      = valuationTotal;
            self.totalFrequency = frequencySum;
            self.currentPrice   = chartData.symbolPrice;

            // update the total positions
            self.positions = chartData.positionCount;

            // save the last price
            self.lastPrice = chartData.symbolPrice;

            // store the latest member information (sorted by member)
            self.memberInfo = chartData.memberInfo.sort(function(m1, m2) {
                return m1.id - m2.id;
            });

            var currentMemberCount = (self.memberInfo.length !== undefined ? self.memberInfo.length : 0);

            // check to see if member count has changed
            if (self.lastMemberCount !== undefined && self.lastMemberCount !== currentMemberCount) {
                self.displayNotification('Member count changed from ' + self.lastMemberCount + ' to ' + currentMemberCount,'info', true);
            }
            self.lastMemberCount = currentMemberCount;

            // update the member-info based charts
            var newData = [];
            self.memberInfo.forEach(function(member) {
                newData.push({ memberId: member.id, entryCount: member.entryCount });
            });
            distributionChart.setData(newData);

            // add the latest performance data (truncate first if required)
            if (self.aggregationChartData.length > self.MAXIMUM_AGGREGATION_TICKS) {
                self.aggregationChartData.shift();
            }

            self.aggregationChartData.push({timestamp: chartData.instant, aggregationTime: chartData.aggregationDuration});
            aggregationChart.setData(self.aggregationChartData);

            // determine rolling average
            var totalAggregation = 0;
            self.aggregationChartData.forEach(function(entry) {
                totalAggregation += entry.aggregationTime;
            });
            self.averageQueryTime = Math.round(totalAggregation / self.aggregationChartData.length);

            // update "messages sent" graph only if federation enabled
            if (self.secondaryCluster === 'enabled') {
                $http.get('/service/jmx/query/Coherence:type=Federation,name=' + self.secondaryClusterName +
                    ',subType=Destination,*/TotalBytesSent').then(function(response) {
                    var data           = response.data;
                    var totalBytesSent = 0;
                    data.forEach(function(values) {
                        totalBytesSent += values.attributes.TotalBytesSent;
                    });
                    self.currentBytesSent = totalBytesSent;
                });

                if (self.lastBytesSent === -1) {
                    self.lastBytesSent = self.currentBytesSent;
                }

                var deltaMessages = self.currentBytesSent - self.lastBytesSent;

                // add value to federation graph
                self.bytesSentData.push({timestamp: new Date().getMilliseconds(), bytes: deltaMessages < 0 ? 0 : deltaMessages});

                // ensure graph lengths stays constant
                if (self.bytesSentData.length > self.MAX_SPARKLINE) {
                    self.bytesSentData = self.bytesSentData.splice(1);
                }

                self.lastBytesSent = self.currentBytesSent;
                federationChart.setData(self.bytesSentData) ;
            }

            self.firstRefresh = false;
        });
    };

    // ---- the function to enable/disable tracing for a specific member ----

    self.toggleTracing = function (memberInfo) {
      $http.post(encodeURI('/management/coherence/cluster/members/' + memberInfo.id),
                 {"tracingSamplingRatio": memberInfo.tracingEnabled ? -1.0 : 1.0})
    };

     // ---- the function to render the options to enable/disable tracing on the primary cluster ----

    self.tracingOptions = function() {
        var options    = {};
        var memberInfo = self.memberInfo;

        var tracingClusterWide = memberInfo.map(function(member) {
                                     return member.tracingEnabled;
                                 }).reduce(function (acc, tracingEnabled) {
                                     return acc & tracingEnabled;
                                 }, true);

        if (tracingClusterWide) {
            options["Disable Tracing for Cluster"] = "disable-cluster";
        } else {
            options["Enable Tracing for Cluster"] = "enable-cluster";
        }

        if (self.lastMemberCount > 1) {
            var tracingOdd = memberInfo.filter(function (member) {
                                 return member.roleName.endsWith("Odd");
                             }).map(function (member) {
                                 return member.tracingEnabled;
                             }).reduce(function (sum, tracingEnabled) {
                                 return sum & tracingEnabled;
                             }, true);

            var tracingEven = memberInfo.filter(function (member) {
                                  return member.roleName.endsWith("Even");
                              }).map(function (member) {
                                  return member.tracingEnabled;
                              }).reduce(function (acc, tracingEnabled) {
                                  return acc & tracingEnabled;
                              }, true);

            if (tracingEven) {
                options["Disable Tracing for Even Members"] = "disable-even";
            } else {
                options["Enable Tracing for Even Members"] = "enable-even";
            }

            if (tracingOdd) {
                options["Disable Tracing for Odd Members"] = "disable-odd";
            } else {
                options["Enable Tracing for Odd Members"] = "enable-odd";
            }
        }

        return options;
    };

    // ---- the function to enable/disable tracing on the primary cluster ----

    self.tracingSelected = function () {
        var postData = {
            "role":         "",
            "tracingRatio": -1.0
        };
        var message;

        if (self.selectedOption.endsWith("even")) {
            postData.role = "CoherenceDemoServerEven"
        } else if (self.selectedOption.endsWith("odd")) {
            postData.role = "CoherenceDemoServerOdd"
        }

        if (self.selectedOption.startsWith("enable")) {
            postData.tracingRatio = 1.0;
            message = "Tracing enabled";
        }
        else {
            message = "Tracing disabled";
        }

        $http.post(encodeURI('/management/coherence/cluster/configureTracing/'), postData).then(
            function () {
                $('#tracingModal').modal('hide');
                self.displayNotification(message, 'success', true);
                self.selectedOption = null;
            });
    };

    self.configureTracing = function() {
        self.modalContent = $sce.trustAsHtml('<p>Loading...</p>');
        // using XHR so we can more easily compose html rather than using strings
        var xhr = new XMLHttpRequest();
        xhr.open('GET', "fragments/tracing.html", true);
        xhr.onreadystatechange = function() {
          if (this.readyState !== 4 || this.status !== 200) {
            return;
          }
          self.modalContent = $sce.trustAsHtml(this.responseText);
          $("#tracingModal").modal();
        };
        xhr.send();
    };

    // ---- the function to start a new cluster member ----

    self.startMember = function() {
        if (self.isRunningInKubernetes === true) {
            self.displayInsight(self.federationConfiguredInK8s ? "addOrRemoveServerK8sFederation" : "addOrRemoveServerK8s");
        }
        else {
            var serverCount = parseInt(prompt('Enter the number of servers to start', '1')); 
            if (isNaN(serverCount) === false) { 
                if (self.memberInfo.length + serverCount > self.maxServers) {
                    alert("This value would exceed the maximum number of servers allowed of " + self.maxServers);
                }
                else {
                    self.startingMember = true;
                    $http.get('/service/start-member/' + serverCount).then(function(response) {
                        self.startingMember = false;
                        self.displayInsightIfEnabled('serverStarted');
                    });
                }
            }
        }
    };

    // ---- the function to display insight for functions that may vary when running in k8s ----

    self.displayInsightK8sAware = function(target) {
        if (self.isRunningInKubernetes) {
            if (target === "addServer") {
                self.displayInsight("addOrRemoveServerK8s");
            }
        }
        else {
            self.displayInsight(target);
        }
    };

    // ---- the function to stop an existing cluster member for a given cluster ----

    self.stopMember = function(memberId) {
        if (self.isRunningInKubernetes === true) {
            self.displayInsight(self.federationConfiguredInK8s ? "addOrRemoveServerK8sFederation" : "addOrRemoveServerK8s");
        }
        else {
            $http.get('/service/stop-member/' + memberId).then(function(response) {
                self.displayInsightIfEnabled('serverStopped');
            });
        }
    };

    // ---- the function to control secondary cluster ----

    self.toggleSecondary = function() {
        if (self.coherenceEdition === "CE") {
            self.displayInsight("commercial");
        }
        else {
            if (self.federationControlLabel === self.START_FEDERATION) {
                 self.startSecondary();
            }
            else {
                if ($window.confirm('Are you sure you want to stop Federation?')) {
                    self.stopSecondary();
                }
            }
        }
    };

    // ---- the function to show about information ----

    self.about = function() {
        $("#aboutModal").modal();
    };

    // ---- the function to toggle insight mode ----

    self.toggleInsightMode = function() {
        self.insightEnabled = !self.insightEnabled;
        if (self.insightEnabled) {
            self.insightLabel = 'Disable Insight';
            $cookies.remove(disableInsightCookie);
        }
        else {
            self.insightLabel = 'Enable Insight';
            $cookies.put(disableInsightCookie,'true');
        }
    };

    // ---- the function to display demo insight ----

    self.displayInsight = function(item) {
        var insight       = self.insightContent[item];
        self.modalContent = $sce.trustAsHtml('<p>Loading...</p>');

        if (insight) {
            var htmlFragment = insight.content;
            self.modalHeader = insight.header;

            self.displayingSplash = htmlFragment === 'fragments/welcome.html';

            // using XHR so we can more easily compose html rather than using strings
            var xhr = new XMLHttpRequest();
            xhr.open('GET', htmlFragment, true);
            xhr.onreadystatechange = function() {
                if (this.readyState !== 4 || this.status !== 200) {
                    return;
                }
                self.modalContent = $sce.trustAsHtml(this.responseText);
                $("#insightModal").modal();
            };
            xhr.send();
        }
        else {
            self.modalHeader  = 'No insight for available for ' + item + '.';
            self.modalContent = $sce.trustAsHtml('<p>Not available.</p>');
            $("#insightModal").modal();
        }
    };

    // ---- the function to load the html content for insight

    self.loadInsightContent = function() {
        self.insightContent['index'] = {
           "header":  "Adding and Removing Indexes",
           "content": "fragments/index.html"
       };
       self.insightContent['aggregation'] = {
           "header":  "Aggregating Data",
           "content": "fragments/aggregation.html"
       };
        self.insightContent['addServer'] = {
           "header":  "Start an Additional Server",
           "content": "fragments/addServer.html"
       };
        self.insightContent['serverStarted'] = {
           "header":  "Additional Server(s) Started",
           "content": "fragments/serverStarted.html"
       };
       self.insightContent['serverStopped'] = {
           "header":  "Server Stopped",
           "content": "fragments/serverStopped.html"
       };
        self.insightContent['distribution'] = {
           "header":  "Data Distribution",
           "content": "fragments/serverStarted.html"
       };
        self.insightContent['portfolio'] = {
           "header":  "Portfolio Calculation",
           "content": "fragments/portfolio.html"
       };
       self.insightContent['federationStarted'] = {
           "header":  "Federation Started",
           "content": "fragments/federationStarted.html"
       };
       self.insightContent['dataSent'] = {
           "header":  "Data Sent",
           "content": "fragments/dataSent.html"
       };
       self.insightContent['priceUpdates'] = {
           "header":  "Real-Time Price Updates",
           "content": "fragments/priceUpdates.html"
       };
       self.insightContent['createSnapshot'] = {
           "header":  "Create Snapshot",
           "content": "fragments/createSnapshot.html"
       };
       self.insightContent['recoverSnapshot'] = {
           "header":  "Recover Snapshot",
           "content": "fragments/recoverSnapshot.html"
       };
        self.insightContent['federationReplicateAll'] = {
           "header":  "Replicate All",
           "content": "fragments/federationReplicateAll.html"
       };
       self.insightContent['federationPaused'] = {
           "header":  "Federation Paused",
           "content": "fragments/federationPaused.html"
       };
       self.insightContent['federationResumed'] = {
           "header":  "Federation Resumed",
           "content": "fragments/federationResumed.html"
       };
       self.insightContent['federationStopped'] = {
           "header":  "Federation Stopped",
           "content": "fragments/federationStopped.html"
       };
       self.insightContent['clear'] = {
           "header":  "Cache Cleared",
           "content": "fragments/clear.html"
       };
       self.insightContent['addTrades'] = {
           "header":  "Add Trades",
           "content": "fragments/addTrades.html"
       };
       self.insightContent['populate'] = {
           "header":  "Populate Trades",
           "content": "fragments/populate.html"
       };
       self.insightContent['jvisualvm'] = {
           "header":  "JVisualVM Plugin",
           "content": "fragments/jvisualvm.html"
       };
       self.insightContent['tracingEnabled'] = {
           "header": "OpenTracing Support Enabled",
           "content": "fragments/tracingEnabled.html"
       };
       self.insightContent['tracingDisabled'] = {
         "header": "OpenTracing Support Disabled",
         "content": "fragments/tracingDisabled.html"
       };
       self.insightContent['welcome'] = {
           "header":  "Coherence Demonstration",
           "content": "fragments/welcome.html"
       };
       self.insightContent['shutdown'] = {
           "header":  "Shutdown Coherence Demonstration",
           "content": "fragments/shutdown.html"
       };
       self.insightContent['addOrRemoveServerK8s'] = {
           "header":  "Add or Remove Server",
           "content": "fragments/addOrRemoveServerK8s.html"
       };
       self.insightContent['addOrRemoveServerK8sFederation'] = {
           "header":  "Add or Remove Server - Federation in Kubernetes Enabled",
           "content": "fragments/addOrRemoveServerK8sFederation.html"
       };
       self.insightContent['shutdownFederationInK8s'] = {
           "header":  "Coherence Demonstration Shutdown - Federation in Kubernetes Enabled",
           "content": "fragments/shutdownFederationInK8s.html"
       };
       self.insightContent['demoShutdown'] = {
           "header":  "Coherence Demonstration Shutdown",
           "content": "fragments/demoShutdown.html"
       };
       self.insightContent['commercial'] = {
           "header":  "Feature Requires Coherence Grid Edition",
           "content": "fragments/commercial.html"
       };
    };

    // ---- the function to close the splash screen

    self.closeSplashScreen = function(control) {
        if (control === 'dontDisplaySplash') {
            $cookies.put(skipSplashCookie, 'true');
        }
        $("#insightModal").modal('hide');
    };

    // ---- the function to start a secondary cluster ----

    self.startSecondary = function() {
        self.secondaryCluster = 'starting';
        self.displayNotification('Starting secondary cluster...','info', false);

        self.secondaryCluster       = 'enabled';
        self.localClusterName       = self.primaryClusterName;
        self.federationControlLabel = self.STOP_FEDERATION;

        // don't start new DefaultCache server if we are in K8s
        if (self.isRunningInKubernetes && self.federationConfiguredInK8s) {
            self.startFederationInternal();
        }
        else {
            $http.get('/service/start-secondary').then(function(response) {
                // automatically start federation as it will be paused
                self.startFederationInternal();
            });
        }
    };

    // ---- the function to start federation internally ----

    self.startFederationInternal = function() {
        self.federationOperation('start');
        self.displayNotification('Secondary cluster started','success', true);

        self.currentBytesSent = 0;
        self.bytesSentData    = [];
        self.lastBytesSent    = -1;

        self.displayInsightIfEnabled('federationStarted');
    };

    // ---- the function to stop a secondary cluster ----

    self.stopSecondary = function() {

        self.secondaryCluster = 'stopping';
        self.displayNotification('Stopping secondary cluster...','info', false);

        // don't start new DefaultCache server if we are in K8s
        if (self.isRunningInKubernetes && self.federationConfiguredInK8s) {
            self.pauseFederationInternal();
        }
        else {
            $http.get('/service/stop-member/secondary').then(function(response) {
                self.pauseFederationInternal();
            });
        }
    };

    // ---- the function to pause federation internally ----

    self.pauseFederationInternal = function() {
        self.secondaryCluster       = 'disabled';
        self.federationControlLabel = self.START_FEDERATION;
        self.localClusterName       = '';

        // pause replication
        self.federationOperation('pause');
        self.displayNotification('Secondary cluster stopped','success', true);
        self.displayInsightIfEnabled('federationStopped');
    };

    // ---- the function to open the secondary cluster window

    self.openSecondary = function() {
        // get the hostname from the server so that if we specify -Dhttp.hostname
        // to be something other than 127.0.0.1 the secondary cluster URL will work.
        $http.get('/service/developer/hostname').then(function(response) {
            var secondaryPort =  Number($location.port()) + 1;
            var secondaryURL  = $location.protocol() + '://' + response.data + ':' + secondaryPort +
                           '/application/index.html#?clusterName=' + self.secondaryClusterName;
            $window.open(secondaryURL);
        });
    };

    // ---- the function to show the raw metrics

    self.openMetrics = function() {
        // get the hostname from the server so that if we specify -Dhttp.hostname
        // to be something other than 127.0.0.1 the secondary cluster URL will work.
        $http.get('/service/developer/hostname').then(function(response) {
            $window.open($location.protocol() + '://' + response.data + ':9612/metrics');
        });
    };

    // ---- the function to carry out federation operations ----

    self.federationOperation = function(operation) {
        $http.get('/service/federation/' + operation);
    };

    // ---- the function to carry out persistence operations ----

    self.persistenceOperation = function(operation) {
        self.persistenceResult = 'Working...';
        var description = operation === 'createSnapshot' ? 'Creating' : 'Recovering';
        self.displayNotification(description + ' snapshot...', 'info', false);
        $http.get('/service/persistence/' + operation).then(function(response) {
            self.persistenceResult = response.data;
            self.displayNotification('Operation completed: ' + response.data, 'success', true);
            self.displayInsightIfEnabled(operation);
        });
    };

    // ---- the function to start
        // developer tools ----

    self.startDeveloper = function(command) {
        // default to continue processing command. Only shutdown has confirmation
        var continueCommand = true;

        if (command === 'populate') {
            self.displayNotification('Adding 100,000 trades...', 'info', false);
        }
        else if (command === 'clear') {
            self.displayNotification('Clearing all trades...', 'info', false);
        }
        else if (command === 'shutdown') {
            if (self.disableShutdown) {
                alert("You are not able to shutdown this demonstration application");
                return;
            }
            else if (self.isRunningInKubernetes === true) {
                self.displayInsight(self.federationConfiguredInK8s ? 'shutdownFederationInK8s' : 'shutdown');
                return;
            }
            else {
                continueCommand = $window.confirm('Are you sure you want to shutdown the Demo?');
            }
        }

        if (continueCommand) {
            if (command === "shutdown") {
                self.displayInsight("demoShutdown");
            }
            $http.get('/service/developer/' + command).then(function(response) {
                if (command === 'populate' || command === 'clear') {
                    self.displayNotification('Operation completed','success', true);
                    self.displayInsightIfEnabled(command);
                }
                else {
                    self.displayInsightIfEnabled(command);
                }
            });
        }
    };

    // ---- the function to suspend or resume federation  ----

    self.toggleFederationState = function() {
       if (self.federationStateLabel === self.SUSPEND_FEDERATION) {
           self.federationOperation('pause');
           self.federationStateLabel = self.RESUME_FEDERATION;
           self.displayNotification('Replication Paused', 'success', true);
           self.displayInsightIfEnabled('federationPaused');
       }
       else {
           self.federationOperation('start');
           self.federationStateLabel = self.SUSPEND_FEDERATION;
           self.displayNotification('Replication Resumed','success', true);
           self.displayInsightIfEnabled('federationResumed');
       }
    };

    // ---- the function to replicate all entries to Secondary Cluster ----

    self.replicateAll = function() {
        self.federationOperation('replicateAll');
        self.displayNotification('Initiated "Replicate All" to ' + self.secondaryClusterName + ' Cluster', 'success', true);
        self.displayInsightIfEnabled('federationReplicateAll');
    };

    // ---- the function to add "n" trades ----

    self.addTrades = function() { 
        var val = parseInt(prompt('Enter the number of random trades to add', '1000')); 
        if (isNaN(val) === false) { 
            if (self.positions + val > self.maxCacheEntries) {
                alert("This value would exceed the maximum number of cache entries allowed of " + self.maxCacheEntries);
            }
            else {
                self.displayNotification('Adding ' + val + ' trades...', 'info', false);
                $http.get('/service/developer/insert/' + val) .then( function(response) {
                    self.displayNotification('Operation completed','success', true);
                    self.displayInsightIfEnabled('addTrades');
                });
            }
         } 
    };

    // ---- the function to display a notification ----

    self.displayNotification = function(message, type, fadeOut) {
        self.displayStatus     = true;
         $("#statusMessage").fadeIn(1);
        self.lastStatusMessage = message;
        self.lastStatusClass   = 'alert ' +
                 (type === 'success' ? 'alert-success' :
                  type === 'error'  ?  'alert-warning'   :
                                       'alert-info');

        if (fadeOut) {
            $timeout( function() {
                $("#statusMessage").fadeOut();
            }, 4000);
        }
    };

    // ---- the function to toggle whether real-time updates are enabled ----

    self.updatePortfolio = function(enabled) {
        self.portfolioRefresh.enabled = enabled;
    };

    // ---- the function to toggle whether indexes are used ----

    self.updateIndexes = function(enabled) {
        self.portfolioRefresh.useIndexes = enabled;
        self.displayNotification((self.portfolioRefresh.useIndexes ? 'Adding' : 'Removing') + ' indexes...', 'info', false);
        $http.get('/service/developer/indexes/' + self.portfolioRefresh.useIndexes)
            .then(function (response) {
            self.displayNotification('Operation completed','success', true);
        });
    };

    // ---- the function to display insight if enabled ----

    self.displayInsightIfEnabled = function(item) {
        if (self.insightEnabled) {
           self.displayInsight(item);
        }
    };

    // ---- the application lifecycle events ----

    $scope.$on('$destroy', function() {
        // stop the portfolio updates
        self.updatePortfolio(false);

        // cancel the background refresh
        if (angular.isDefined(self.refreshPromise)) {
            $interval.cancel(self.refreshPromise);
            self.refreshPromise = undefined;
        }
    });

    // ---- start the application ----

    // perform an initial refresh of application state
    self.refresh();

    // start / stop the portfolio updates
    self.updatePortfolio(false);

    // load the demo insight content
    self.loadInsightContent();

    // schedule application state to be refreshed
    self.refreshPromise = $interval(self.refresh, 5000);
}]);