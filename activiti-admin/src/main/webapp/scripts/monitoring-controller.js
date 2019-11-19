/*
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/* Controllers */

activitiAdminApp.controller('MonitoringController', ['$rootScope', '$scope', '$http', '$timeout','$modal',
    function ($rootScope, $scope, $http, $timeout, $modal) {

        // Set root navigation
		$rootScope.navigation = {selection: 'monitoring'};

		$rootScope.checkLicenseValidity();

        // Model
        $scope.model = {
            lastUpdate: undefined,
            refreshIntervals: [
            	{name: "5 seconds", value: 5000},
            	{name: "10 seconds", value: 10000},
            	{name: "30 seconds", value: 30000}
            ],
            loading: false
        };

        // Default interval is 30 seconds
        $scope.model.refreshInterval = $scope.model.refreshIntervals[2];

        var scheduleClusterInfoRefreshPromise = undefined;
        $scope.updateRefreshInterval = function(interval) {
        	$scope.model.refreshInterval = interval;
            if (scheduleClusterInfoRefreshPromise) {
                $timeout.cancel(scheduleClusterInfoRefreshPromise);
                scheduleClusterInfoRefresh();
            }
        };

        // Cleanup auto refresh
        $scope.$on("$destroy", function( event ) {
            if(scheduleClusterInfoRefreshPromise) {
              $timeout.cancel(scheduleClusterInfoRefreshPromise);
            }
        });

        $scope.selectNode = function(node) {
        	$scope.model.selectedNode = node;
        	$scope.model.detailTab = 1;
        };

        $scope.selectDetailTab = function(tab) {
        	$scope.model.detailTab = tab;
        };

        var calculatePercentages = function(clusterInfo) {
        	for(var i=0; i <clusterInfo.length; i++) {
        		var info = clusterInfo[i];
        		if(info.jvmMetrics && info.jvmMetrics.memory) {
        			var memory = info.jvmMetrics.memory;
    				memory['total.percentage'] = memory['total.used'] / memory['total.max'] * 100;
    				memory['heap.percentage'] = memory['heap.used'] / memory['heap.max'] * 100;
    				memory['nonheap.percentage'] = memory['nonheap.used'] / memory['nonheap.max'] * 100;
        		}
        	}
        };

        /**
         * Does the calculations necessary for displaying the runtime hour charts.
         */
        var calculateRuntimeMetricChartNumbers = function(clusterInfo) {

            var internalCalculate = function(info, metricsProperty) {

                var receivedHourCountMetrics = info.runtimeMetrics
                    && info.runtimeMetrics[metricsProperty]
                    && info.runtimeMetrics[metricsProperty]['hour-counts'];

                if (receivedHourCountMetrics) {

                    // Get highest value
                    var highestValue = 0;
                    for (var hour=0; hour<24; hour++) {
                        var value = info.runtimeMetrics[metricsProperty]['hour-counts']['' + hour];
                        if (value !== null && value !== undefined && value > highestValue) {
                            highestValue = value;
                        }
                    }

                    // Determine scaling factor (height of chart is fixed 100px)
                    var scalingFactor = 100.0 / (highestValue !== 0 ? highestValue : 100);

                    // Recalculate values using this scaling factor
                    for (var hour=0; hour<24; hour++) {
                        var value = info.runtimeMetrics[metricsProperty]['hour-counts']['' + hour];
                        info.runtimeMetrics[metricsProperty]['hour-counts'][hour + '-percentage'] = Math.round(value * scalingFactor);
                    }

                }

                var receivedWeekDayMetrics = info.runtimeMetrics
                    && info.runtimeMetrics[metricsProperty]
                    && info.runtimeMetrics[metricsProperty]['weekday-counts'];

                if (receivedWeekDayMetrics) {

                    var highestValue = 0;
                    for (var weekday=0; weekday<7; weekday++) {
                        var value = info.runtimeMetrics[metricsProperty]['weekday-counts']['' + weekday];
                        if (value !== null && value !== undefined && value > highestValue) {
                            highestValue = value;
                        }
                    }

                    // Determine scaling factor (height of chart is fixed 100px)
                    var scalingFactor = 100.0 / (highestValue !== 0 ? highestValue : 100);

                    // Recalculate values using this scaling factor
                    for (var weekday=0; weekday<7; weekday++) {
                        var value = info.runtimeMetrics[metricsProperty]['weekday-counts']['' + weekday];
                        info.runtimeMetrics[metricsProperty]['weekday-counts'][weekday + '-percentage'] = Math.round(value * scalingFactor);
                    }

                }
            };

            for(var i=0; i <clusterInfo.length; i++) {
                internalCalculate(clusterInfo[i], 'processinstance-start');
                internalCalculate(clusterInfo[i], 'task-completion');
                internalCalculate(clusterInfo[i], 'job-execution');
            }

        };

        var cleanElasticSearchStats = function(clusterInfo) {
            for(var i=0; i <clusterInfo.length; i++) {
                if (clusterInfo[i]['bpmSuiteElasticSearchStatsJson'] !== null
                    && clusterInfo[i]['bpmSuiteElasticSearchStatsJson'] !== undefined) {
                    var stats = clusterInfo[i]['bpmSuiteElasticSearchStatsJson'];
                    clusterInfo[i]['bpmSuiteElasticSearchStats'] = angular.fromJson(stats);
                    delete clusterInfo[i]['bpmSuiteElasticSearchStatsJson'];
                }
            }
        }

        // Method to refresh the cluster info with the backend
        var refreshClusterInfo = function() {
        	$scope.model.loading = true;
            $http({method: 'GET', url: '/app/rest/activiti/cluster-info/' + $scope.activeCluster.id}).
                success(function(data, status, headers, config) {
                	// Calculate percentages for graphs
                	calculatePercentages(data);
                	calculateRuntimeMetricChartNumbers(data);

                	// If the elastic search stats are given, they need to be jsonized as they are sent as string
                	cleanElasticSearchStats(data);

                	// Check selection
                	var resetSelection = true;
                	if($scope.model.selectedNode) {
                		for(var i=0; i<data.length; i++) {
                			var node = data[i];

                			if($scope.model.selectedNode.port == node.port && $scope.model.selectedNode.host == node.host) {
                				resetSelection = false;
                				$scope.model.selectedNode = node;
                				break;
                			}
                		}
                	}

                    $scope.clusterInfo = data;

                    // Select first, if no selection is set
                    if(resetSelection && data.length > 0) {
                    	$scope.model.selectedNode = data[0];
                    	$scope.model.detailTab = 1;
                    }

                    $scope.model.lastUpdate = moment(new Date()).format('LLL');
                    $scope.model.loading = false;
                }).
                error(function(data, status, headers, config) {
                	$scope.model.loading = false;
                });
        };

        // Repeating timer for refreshing cluster information
        var scheduleClusterInfoRefresh = function() {
            scheduleClusterInfoRefreshPromise = $timeout(function() {
                refreshClusterInfo();
                scheduleClusterInfoRefresh();
            }, $scope.model.refreshInterval.value);
        };

        $scope.executeWhenReady(function() {
          refreshClusterInfo();
          scheduleClusterInfoRefresh();
        });

    }]);
