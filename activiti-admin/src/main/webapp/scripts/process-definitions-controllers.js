/*
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/* Controllers */

activitiAdminApp.controller('ProcessDefinitionsController', ['$rootScope', '$scope', '$http', '$timeout', '$location','$translate', '$q', '$modal', 'gridConstants',
    function ($rootScope, $scope, $http, $timeout, $location, $translate, $q, $modal, gridConstants) {

		$rootScope.navigation = {selection: 'process-definitions'};

		$rootScope.checkLicenseValidity();

		$scope.filter = {};
		$scope.processDefinitionsData = {};

		// Array to contain selected properties (yes - we only can select one, but ng-grid isn't smart enough)
	    $scope.selectedDefinitions = [];

	    var filterConfig = {
	    	url: '/app/rest/activiti/process-definitions',
	    	method: 'GET',
	    	success: function(data, status, headers, config) {
	    		$scope.processDefinitionsData = data;
            },
            error: function(data, status, headers, config) {
                if (data && data.message) {
                    // Extract error-message
                    $rootScope.addAlert(data.message, 'error');
                } else {
                    // Use default error-message
                    $rootScope.addAlert($translate.instant('ALERT.GENERAL.HTTP-ERROR'), 'error');
                }
            },

            sortObjects: [
                {name: 'PROCESS-DEFINITIONS.SORT.ID', id: 'id'},
                {name: 'PROCESS-DEFINITIONS.SORT.NAME', id: 'name'},
                {name: 'PROCESS-DEFINITIONS.SORT.VERSION', id: 'version'}
            ],

            supportedProperties: [
                {id: 'nameLike', name: 'PROCESS-DEFINITIONS.FILTER.NAME', showByDefault: false},
                {id: 'keyLike', name: 'PROCESS-DEFINITIONS.FILTER.KEY', showByDefault: true},
                {id: 'deploymentId', name: 'PROCESS-DEFINITIONS.FILTER.DEPLOYMENT-ID', showByDefault: false},
                {id: 'tenantIdLike', name: 'PROCESS-DEFINITIONS.FILTER.TENANT-ID', showByDefault: false},
                {id: 'latest', name: 'PROCESS-DEFINITIONS.FILTER.LATEST', showByDefault: true, defaultValue: true}
            ]
	    };

	    if ($rootScope.filters.forced.processDefinitionFilter) {
	        // Always recreate the filter and add all properties
	        $scope.filter = new ActivitiAdmin.Utils.Filter(filterConfig, $http, $timeout, $rootScope);
	        $rootScope.filters.processDefinitionFilter = $scope.filter;

	        for (var prop in $rootScope.filters.forced.processDefinitionFilter) {
	            $scope.filter.addProperty({id: prop}, $rootScope.filters.forced.processDefinitionFilter[prop]);
	            if (prop == 'deploymentId') {
	                $scope.filter.removeProperty({id: 'latest'});
	            }
	        }

	        $rootScope.filters.forced.processDefinitionFilter = undefined;

	    } else if ($rootScope.filters && $rootScope.filters.processDefinitionFilter) {
	    	// Reuse the existing filter
	    	$scope.filter = $rootScope.filters.processDefinitionFilter;
	    	$scope.filter.config = filterConfig;
	    } else {
		    $scope.filter = new ActivitiAdmin.Utils.Filter(filterConfig, $http, $timeout, $rootScope);
		    $rootScope.filters.processDefinitionFilter = $scope.filter;
	    }

	    $scope.definitionSelected = function(definition) {
	    	if (definition && definition.getProperty('id')) {
	    		$location.path('/process-definition/' + definition.getProperty('id'));
	    	}
	    };

	    $scope.toggleLatestVersion = function() {
	      if($scope.filter.properties.latest === true) {
	        $scope.filter.properties.nameLike = undefined;
	        $scope.filter.properties.deploymentId = undefined;
	      }

	      $scope.filter.refresh();
	    };

	    $q.all([$translate('PROCESS-DEFINITIONS.HEADER.ID'),
              $translate('PROCESS-DEFINITIONS.HEADER.NAME'),
              $translate('PROCESS-DEFINITIONS.HEADER.VERSION'),
              $translate('PROCESS-DEFINITIONS.HEADER.KEY'),
              $translate('PROCESS-DEFINITIONS.HEADER.TENANT')])
              .then(function(headers) {

          // Config for grid
          $scope.gridDefinitions = {
              data: 'processDefinitionsData.data',
              enableRowReordering: true,
              multiSelect: false,
              keepLastSelected : false,
              rowHeight: 36,
              selectedItems: $scope.selectedDefinitions,
              afterSelectionChange: $scope.definitionSelected,
              columnDefs: [
                  { field: 'id', displayName: headers[0], cellTemplate: gridConstants.defaultTemplate},
                  { field: 'name', displayName: headers[1], cellTemplate: gridConstants.defaultTemplate},
                  { field: 'version', displayName: headers[2], cellTemplate: gridConstants.defaultTemplate},
                  { field: 'key', displayName: headers[3], cellTemplate: gridConstants.defaultTemplate},
                  { field: 'tenantId', displayName: headers[4], cellTemplate: gridConstants.defaultTemplate}]
          };
       });


	    // Hook in initial fetching of the definitions
	     $scope.executeWhenReady(function() {
	       $scope.filter.refresh();
	     });



        /*
         * ACTIONS
         */


        $scope.uploadFromModeler = function() {
            var modalInstance = $modal.open({
                templateUrl: 'views/upload-from-modeler-popup.html',
                controller: 'UploadFromModelerCrtl',
                windowClass: 'upload-from-modeler-dialog'
            });
            modalInstance.result.then(function (result) {
                // Refresh page if closed
                if (result) {
                    $scope.processDefinitionsData = {};
                    $scope.filter.refresh();
                }
            });
        };

    }]);


/**
 * Controller for the upload a model from the process Modeler.
 */
 activitiAdminApp.controller('UploadFromModelerCrtl',
    ['$scope', '$modalInstance', '$http', function ($scope, $modalInstance, $http) {

    $scope.status = {loading: false};

    $scope.model = {
        showingCredentials: true,
        showingProcesses: false,
        showingProcessName: false,
        error: false
    };

    $scope.getProcessDiagramUrl = function(modelId) {
    	var username = encodeURIComponent($scope.model.username);
        var password = encodeURIComponent($scope.model.password);
    	return './app/rest/modeler-processes/' + modelId + '/thumbnail?username=' + username + '&password=' + password;
    };

    $scope.next = function () {
        if ($scope.model.showingCredentials) {

            if (!$scope.model.username || $scope.model.username.length == 0
                || !$scope.model.password || $scope.model.password.length == 0) {
                return''
            }

            // Fetch processes
            $scope.status.loading = true;
            var username = encodeURIComponent($scope.model.username);
            var password = encodeURIComponent($scope.model.password);
            $http({method: 'GET', url: '/app/rest/modeler-processes?username=' + username + '&password=' + password, ignoreErrors: true}).
                success(function (data, status, headers, config) {

                    // Reset
                    $scope.model.unauthorizedError = false;
                    $scope.model.error = false;

                    if (!data.error) {

                        $scope.model.processes = data;

                        // Set state
                        $scope.model.showingCredentials = false;
                        $scope.model.showingProcesses = true;

                    } else {

                        if (data.error == "unauthorized") {
                            $scope.model.unauthorizedError = true;
                        }
                    }

                    $scope.status.loading = false;

                }).error(function (data, status, headers, config) {
                    // Reset
                    $scope.model.unauthorizedError = false;
                    $scope.model.error = false;

                    $scope.model.error = true;
                    $scope.status.loading = false;
                });

        } else if ($scope.model.showingProcesses) {

            $scope.model.showingProcesses = false;
            $scope.model.showingProcessName = true;

        } else if ($scope.model.showingProcessName) {

            if (!$scope.model.processName || $scope.model.processName.length ==0) {
                return;
            }

            // Deploy process
            $scope.status.loading = true;

            var postData = {
                processModelId: $scope.model.selectedProcess.id,
                username: $scope.model.username,
                password: $scope.model.password,
                name: $scope.model.processName
            };

            $http({method: 'POST', url: '/app/rest/modeler-processes?serverId=' + $scope.activeServer.id, data: postData}).
                success(function(data, status, headers, config) {
                    $modalInstance.close(true);
                    $scope.status.loading = false;
                }).error(function(data, status, headers, config) {
                    $scope.model.error = true;
                    $scope.status.loading = false;
                });
        }
    };

    $scope.showNextButton = function() {
        if ($scope.model.showingCredentials) {

            return $scope.model.username && $scope.model.username.length > 0
                && $scope.model.password && $scope.model.password.length > 0;

        } else if ($scope.model.showingProcesses) {

            if ($scope.model.selectedProcess) {
                $scope.model.processName = $scope.model.selectedProcess.name;
                return true;
            }
            return false;

        } else if ($scope.model.showingProcessName) {

            return $scope.model.processName && $scope.model.processName.length > 0;
        }

        return true;
    };

    $scope.cancel = function () {
        if (!$scope.status.loading) {
            $modalInstance.dismiss('cancel');
        }
    };

}]);
