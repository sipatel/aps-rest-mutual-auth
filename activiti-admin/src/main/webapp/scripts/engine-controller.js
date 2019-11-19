/*
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/* Controllers */

activitiAdminApp.controller('EngineController', ['$rootScope', '$scope', '$http', '$timeout','$modal', '$translate',
    function ($rootScope, $scope, $http, $timeout, $modal, $translate) {

        // Set root navigation
		$rootScope.navigation = {selection: 'engine'};

		$rootScope.checkLicenseValidity();

        // Static data
        $scope.options = {
          schemaUpdate : ['true', 'false'],
          history : ['none', 'activity', 'audit', 'full']
        };

        // Empty model
        $scope.model = {};

        $scope.addMasterConfiguration = function() {
        	var newConfig = {};
			var modalInstance = $modal.open({
				templateUrl: 'views/engine-edit-config-popup.html',
				controller: 'CreateConfigModalInstanceCrtl',
                windowClass : 'master-config-modal',
				resolve: {
					config: function() {return newConfig;},
					options: function() {return $scope.options;},
					newConfig: function() {return true;}
				}
			});

			modalInstance.result.then(function (config) {
				if(config) {
				    $scope.addAlert($translate.instant('ALERT.ENGINE.CONFIG-CREATED', config), 'info');
				    $scope.loadMasterConfig();
				}
			});
		};

		$scope.editMasterConfiguration = function() {
            var modalInstance = $modal.open({
        	    templateUrl: 'views/engine-edit-config-popup.html',
        		controller: 'CreateConfigModalInstanceCrtl',
                windowClass : 'master-config-modal',
        		resolve: {
        		    config: function() {return $scope.model.masterConfig},
        			options: function() {return $scope.options;},
        			newConfig: function() {return false;}
        		}
        	});

        	modalInstance.result.then(function (config) {
        		if(config) {
        		    $scope.addAlert($translate.instant('ALERT.ENGINE.MASTER-CONFIG-UPDATE', config), 'info');
        		    $scope.loadMasterConfig();
        		}
        	});
        };

		$scope.deleteMasterConfiguration = function() {
			var modalInstance = $modal.open({
				templateUrl: 'views/engine-delete-master-popup.html',
				controller: 'DeleteMasterConfigModalInstanceCrtl'
			});

			modalInstance.result.then(function (config) {
				if(config) {
				    $scope.addAlert($translate.instant('ALERT.ENGINE.MASTER-CONFIG-DELETE'), 'info');
				    $scope.loadMasterConfig();
				}
			});
		};

        // Show popup to edit current cluster configuration
        $scope.editClusterConfiguration = function() {
            var modalInstance = $modal.open({
                templateUrl: 'views/engine-edit-cluster-config.html',
                controller: 'EditClusterConfigModalInstanceCrtl',
                resolve: {
                    clusterConfig: function() {return $scope.activeCluster;}
                }
            });

            modalInstance.result.then(function (result) {
                if(result) {
                  $scope.addAlert($translate.instant('ALERT.ENGINE.CLUSTER-UPDATED'), 'info');
                }
            });
        };

        // Show popup that allows to download cluster jar
        $scope.downloadClusterConfigJar = function() {
            var modalInstance = $modal.open({
                templateUrl: 'views/generate-cluster-config-jar-poup.html',
                controller: 'GenerateClusterConfigJarCrtl',
                resolve: {
                    clusterConfig : function() {return $scope.activeCluster;}
                }
            });
        };

        $scope.createClusterConfiguration = function() {
            var modalInstance = $modal.open({
                templateUrl: 'views/engine-edit-cluster-config.html',
                controller: 'EditClusterConfigModalInstanceCrtl',
                resolve: {
                    clusterConfig: function() {return null;}  // No cluster config provided ==> save mode
                }
            });

            modalInstance.result.then(function (result) {
                if(result) {
                  $scope.addAlert($translate.instant('ALERT.ENGINE.CLUSTER-CREATED'), 'info');
                }
            });
        };

        $scope.deleteClusterConfiguration = function() {
            $modal.open({
                templateUrl: 'views/delete-cluster-config-popup.html',
                controller: 'DeleteClusterConfigCrtl',
                resolve: {}
            });
        };

        // Show popup to edit the Activiti endpoint for the current cluster
		$scope.editEndpointConfig = function() {
		    if ($scope.activeServer) {
		        showEditpointConfigModel($scope.activeServer);
		    } else {
		        // load default endpoint configs properties
		        $http({method: 'GET', url: '/app/rest/server-configs/default'}).
	            success(function(defaultServerconfig, status, headers, config) {
	                defaultServerconfig.clusterConfigId = $scope.activeCluster.id;
	                showEditpointConfigModel(defaultServerconfig);
	            });
		    }
			
		    function showEditpointConfigModel(server) {
		        var cloneOfModel = {};
	            for(var prop in server) {
	                cloneOfModel[prop] = server[prop];
	            }

	            var modalInstance = $modal.open({
	                templateUrl: 'views/engine-edit-endpoint-popup.html',
	                controller: 'EditEndpointConfigModalInstanceCrtl',
	                resolve: {
	                    server: function() {return cloneOfModel;}
	                }
	            });

	            modalInstance.result.then(function (result) {
	                if(result) {
	                  $scope.addAlert($translate.instant('ALERT.ENGINE.ENDPOINT-UPDATED', result), 'info');
	                    $rootScope.activeServer = result;
	                }
	            });
		    }
		};

		$scope.checkEndpointConfig = function() {
			$http({method: 'GET', url: '/app/rest/activiti/engine-info', params:{serverId:$rootScope.activeServer.id}, ignoreErrors: true}).
        	success(function(data, status, headers, config) {
        	  $scope.addAlert($translate.instant('ALERT.ENGINE.ENDPOINT-VALID', data), 'info');
            }).error(function(data, status, headers, config) {
              $scope.addAlert($translate.instant('ALERT.ENGINE.ENDPOINT-INVALID',  $rootScope.activeServer), 'error');
            });
		};

        /* When loading the page, loading the master config */
        $scope.loadMasterConfig = function() {
            if (!$scope.activeCluster) return;
            $http({method: 'GET', url: '/app/rest/activiti/master-configs?clusterConfigId=' + $scope.activeCluster.id})
                .success(function(data, status, headers, config) {
                    $scope.model.masterConfig = data;
        	    })
        	    .error(function(data, status, headers, config) {
        	        $scope.model.masterConfig = undefined;
                });
        }

        $scope.executeWhenReady(function() {
          $scope.loadMasterConfig();
        });

    }]);


activitiAdminApp.controller('CreateConfigModalInstanceCrtl',
    ['$scope', '$modalInstance', '$http', 'config', 'options', 'newConfig', function ($scope, $modalInstance, $http, config, options, newConfig) {

	$scope.model = {config: config, newConfig: newConfig, fetchingTemplate: false};

	$scope.options = options;
    $scope.status = {loading: false};

    if (!newConfig) {
        $scope.model.configType = config.type;
        if (config.type === 'bpmSuite') {
            $scope.model.bpmSuiteConfig = config.propertiesText;
        } else {
            $scope.model.engineConfig = config;
        }
    }

	$scope.configTypeChanged = function() {

	    if ($scope.model.configType === 'bpmSuite') {

	        if (!$scope.model.bpmSuiteConfig) {
	            $scope.model.fetchingTemplate = true;
	            $http({method: 'GET', url: '/app/rest/activiti/master-config-templates?type=suite'})
                    .success(function(data, status, headers, config) {
                        $scope.model.bpmSuiteConfig = data;
                        $scope.model.fetchingTemplate = false;
                    });
	        }

	    } else if ($scope.model.configType === 'engine') {

	        if(!$scope.model.engineConfig) {
	            $scope.model.engineConfig = {};
	        }

	        // Set defaults
            if (!$scope.model.engineConfig.databaseSchemaUpdate) {
                $scope.model.engineConfig.databaseSchemaUpdate = 'false';
            }
            if (!$scope.model.engineConfig.history) {
                $scope.model.engineConfig.history = 'full';
            }
            if (!$scope.model.engineConfig.enableJobExecutor) {
                $scope.model.engineConfig.enableJobExecutor = true;
            }

            $scope.model.enableMailServerCfg = ($scope.model.newConfig) ? false : true;
            $scope.model.enableConnectionPoolCfg = ($scope.model.newConfig) ? false : true;
            $scope.model.enableJobExecutorCfg = ($scope.model.newConfig) ? false : true;
	    }

	};

    $scope.ok = function () {
        $scope.status.loading = true;
        var url = '/app/rest/activiti/master-configs';
        var data = {};
        data.clusterConfigId = $scope.activeCluster.id;

        if ($scope.model.configType === 'bpmSuite') {

            data.type = 'bpmSuite';
            data.config = $scope.model.bpmSuiteConfig;

        } else if ($scope.model.configType === 'engine') {

             data.type = 'engine';
             data.config = $scope.model.engineConfig;
             data.config.type = 'engine';

        }

        $http({method: ($scope.model.newConfig ? 'POST' : 'PUT'), url: url, data: data}).
            success(function (data, status, headers, config) {
                $scope.status.loading = false;
                $modalInstance.close($scope.model.config);
            }).
            error(function (data, status, headers, config) {
                $scope.status.loading = false;
                $modalInstance.close();
            });
    };

    $scope.cancel = function () {
        if (!$scope.status.loading) {
            $modalInstance.dismiss('cancel');
        }
    };

}]);

activitiAdminApp.controller('DeleteMasterConfigModalInstanceCrtl',
    ['$scope', '$modalInstance', '$http', function ($scope, $modalInstance, $http) {

	$scope.status = {loading: false};

	  $scope.ok = function () {
		  $scope.status.loading = true;

		  $http({method: 'DELETE', url: '/app/rest/activiti/master-configs/' + $scope.activeCluster.id})
		  .success(function(data, status, headers, config) {
	    	  $scope.status.loading = false;
	    	  $modalInstance.close('success');
	      })
	      .error(function(data, status, headers, config) {
	    	  $scope.status.loading = false;
	    	  $modalInstance.close();
	      });
	  };

	  $scope.cancel = function () {
		if(!$scope.status.loading) {
			$modalInstance.dismiss('cancel');
		}
	  };
}]);


activitiAdminApp.controller('EditEndpointConfigModalInstanceCrtl',
    ['$scope', '$modalInstance', '$http', 'server', function ($scope, $modalInstance, $http, server) {

	$scope.model = {server: server};

	$scope.status = {loading: false};

          $scope.ok = function () {
              $scope.status.loading = true;

              delete $scope.model.error;

              var serverConfigUrl = '/app/rest/server-configs';
              var method = 'PUT';
              if ($scope.model.server && $scope.model.server.id) {
                  serverConfigUrl += '/' + $scope.model.server.id;
              } else {
                  method = 'POST';
              }
              
              $http({method: method, url: serverConfigUrl, data: $scope.model.server}).
                  success(function(data, status, headers, config) {
                      $scope.status.loading = false;
                      $modalInstance.close($scope.model.server);
                      $scope.checkLicenseValidity();
                  }).
                  error(function(data, status, headers, config) {
                      $scope.status.loading = false;
                      $scope.model.error = {
                          statusCode: status,
                          message: data
                      };
                   });
          };

          $scope.cancel = function () {
            if(!$scope.status.loading) {
                $modalInstance.dismiss('cancel');
            }
          };
	}]);


/**
 * Controller for the modal window for creating/editing cluster configuration
 */
 activitiAdminApp.controller('EditClusterConfigModalInstanceCrtl',
    ['$scope', '$modalInstance', '$http', 'clusterConfig', 'options',
    function ($scope, $modalInstance, $http, clusterConfig, options) {

    var isNewClusterConfig = clusterConfig === null;
    $scope.closable = options !== undefined && !options.closable;

    $scope.status = {loading: false};

    $scope.wizard = {
        steps: [
            {  type: 'groupSettings',
               clusterName: isNewClusterConfig ? 'New cluster' : clusterConfig.clusterName
            },
            {  type: 'securitySettings',
                 clusterUserName: isNewClusterConfig ? 'activiti' : clusterConfig.user.login,
                 clusterPassword: isNewClusterConfig ? 'activiti' : clusterConfig.clusterPassword
            },
            {  type: 'activitiInstanceSettings',
                requiresMasterConfiguration: isNewClusterConfig ? true : clusterConfig.requiresMasterConfiguration,
                metricSendingInterval: isNewClusterConfig ? 60 : clusterConfig.metricSendingInterval
            }
        ],
        currentStepIndex : 0
    };
    $scope.wizard.currentStep = $scope.wizard.steps[$scope.wizard.currentStepIndex]

    /* Helpers for wizard */

    $scope.multiCastStateChanged = function() {
        $scope.wizard.currentStep.tcpIpEnabled = !$scope.wizard.currentStep.multiCastEnabled;
    };

    $scope.tcpIpStateChanged = function() {
        $scope.wizard.currentStep.multiCastEnabled = !$scope.wizard.currentStep.tcpIpEnabled;
    };

    $scope.determineStepIcon = function() {
        if ($scope.wizard.currentStep.type === 'groupSettings') {
            return 'cluster-config-wizard-step-cluster';
        }else if ($scope.wizard.currentStep.type === 'securitySettings') {
            return 'cluster-config-wizard-step-security';
        } else if ($scope.wizard.currentStep.type === 'activitiInstanceSettings') {
            return 'cluster-config-wizard-step-activiti';
        }
        return '';
    };

    $scope.previous = function() {
        $scope.wizard.currentStepIndex = $scope.wizard.currentStepIndex - 1;
        $scope.wizard.currentStep = $scope.wizard.steps[$scope.wizard.currentStepIndex];
    };
    $scope.next = function() {
        $scope.wizard.currentStepIndex = $scope.wizard.currentStepIndex + 1
        $scope.wizard.currentStep = $scope.wizard.steps[$scope.wizard.currentStepIndex];
    };

    $scope.isNextButtonDisabled = function() {

        var currentStep = $scope.wizard.currentStep;
        if (currentStep.type === 'groupSettings') {
            return !currentStep.clusterName || currentStep.clusterName.length === 0;
        } else if (currentStep.type === 'securitySettings' ) {
            return !currentStep.clusterUserName || currentStep.clusterUserName.length === 0
                || !currentStep.clusterPassword || currentStep.clusterPassword.length === 0;
        }

        return false;
    };

    /* Actual saving the cluster config */

    $scope.save = function() {

        $scope.status.loading = true;
        $scope.wizard.error = {};

        // Model to use when updating the cluster config
        var clusterConfigData = {
            id: isNewClusterConfig ? null : $scope.activeCluster.id,
            clusterName: $scope.wizard.steps[0].clusterName,
            clusterUserName: $scope.wizard.steps[1].clusterUserName,
            clusterPassword: $scope.wizard.steps[1].clusterPassword,
            requiresMasterConfiguration: $scope.wizard.steps[2].requiresMasterConfiguration,
            metricSendingInterval: $scope.wizard.steps[2].metricSendingInterval
        };

        $http({method: (isNewClusterConfig ? 'POST' : 'PUT'),
            url: '/app/rest/cluster-configs' + (isNewClusterConfig ? '' : '/' + $scope.activeCluster.id), data: clusterConfigData}).
            success(function (data, status, headers, config) {
                var clusterId = isNewClusterConfig ? data.id : $scope.activeCluster.id;
                if (data.serverConfigs.length === 0) {
                    $scope.handleNoServerConfigs(clusterId, function () {
                        $scope.loadClusterConfigs(true, clusterId, function() {
                            $scope.status.loading = false;
                            $modalInstance.close(clusterId);
                        });
                    });
                } else {
                    $scope.loadClusterConfigs(true, clusterId, function() {
                        $scope.status.loading = false;
                        $modalInstance.close(clusterId);
                    });
                }
                
                
            }).
            error(function (data, status, headers, config) {
                if (status === 409) {
                    if (data && data.message) {
                        if (data.message === 'clusterName') {
                            $scope.wizard.error.clusterNameTaken = true
                            $scope.wizard.currentStepIndex = 0;
                            $scope.wizard.currentStep = $scope.wizard.steps[$scope.wizard.currentStepIndex];
                        } else if (data.message === 'clusterUser') {
                            $scope.wizard.error.clusterUserTaken = true;
                             $scope.wizard.currentStepIndex = 1;
                             $scope.wizard.currentStep = $scope.wizard.steps[$scope.wizard.currentStepIndex];
                        }
                    }
                } else {
                    $modalInstance.dismiss();
                }

                  $scope.status.loading = false;
            });
    };

    $scope.cancel = function () {
        if(!$scope.status.loading) {
            $modalInstance.dismiss('cancel');
        }
    };
}]);



/**
 * Controller for the modal window for creating/editing cluster configuration
 */
 activitiAdminApp.controller('GenerateClusterConfigJarCrtl',
    ['$scope', '$modalInstance', '$http', '$location', 'clusterConfig', function ($scope, $modalInstance, $http, $location, clusterConfig) {

    $scope.model = {};

    // Url
    $scope.model.adminAppUrl = $location.absUrl().replace('/#/engine', '');

    // Download url
    $scope.model.download = { label: 'activiti-cluster-cfg.jar', url: ('./app/rest/cluster-config-jars/' + clusterConfig.id + '?adminAppUrl=' + window.encodeURIComponent($scope.model.adminAppUrl)) };

    $scope.adminAppUrlChanged = function() {
        $scope.model.download.url =  './app/rest/cluster-config-jars/' + clusterConfig.id + '?adminAppUrl=' + window.encodeURIComponent($scope.model.adminAppUrl);
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };

    $scope.closePopup = function() {
        $modalInstance.dismiss('cancel');
    };

}]);

/**
 * Controller for the modal window used to delete the current cluster configuration.
 */
 activitiAdminApp.controller('DeleteClusterConfigCrtl',
    ['$scope', '$modalInstance', '$http', function ($scope, $modalInstance, $http) {

    $scope.status = {loading: true}; // We're loading the number of cluster configs the first time!
    $scope.model = {};

    // Fetch the number of cluster configs when this popup is loaded
    $http({method: 'GET', url: '/app/rest/cluster-configs'}).
        success(function(data, status, headers, config) {
            if (data.length >= 0) {
                $scope.model.numberOfClusterConfigs = data.length;
                $scope.status.loading = false;
            }
        }).
        error(function(data, status, headers, config) {
            console.log('Something went wrong when fetching cluster configs: ' + data);
        });

    $scope.enableDeleteButton = function() {
        return !$scope.status.loading &&  $scope.model.numberOfClusterConfigs &&  $scope.model.numberOfClusterConfigs > 1;
    };

    $scope.confirmClusterConfigDelete = function() {
        $scope.status.loading = true;
        $http({method: 'DELETE', url: '/app/rest/cluster-configs/' + $scope.activeCluster.id}).
            success(function(data, status, headers, config) {
                $scope.status.loading = false;
                $modalInstance.dismiss('deleted');
                $scope.loadClusterConfigs(true); // Rootscope function
            }).
            error(function(data, status, headers, config) {
                console.log('Something went wrong when fetching cluster configs: ' + data);
            });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };

}]);
