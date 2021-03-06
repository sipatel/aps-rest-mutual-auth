/*
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/* App Module */

var activitiAdminApp = angular.module('activitiAdminApp', ['http-auth-interceptor', 'ngResource', 'ngRoute', 'ngCookies',
    'pascalprecht.translate', 'ngGrid', 'ui.select2', 'ui.bootstrap', 'angularFileUpload', 'ui.keypress', 'activitiIdentity',
    'ui.grid', 'ui.grid.edit', 'ui.grid.selection', 'ui.grid.autoResize', 'ui.grid.moveColumns', 'ui.grid.cellNav']);

var authRouteResolver = ['$rootScope', 'AuthenticationSharedService', function($rootScope, AuthenticationSharedService) {

  if(!$rootScope.authenticated) {
    // Return auth-promise. On success, the promise resolves and user is assumed authenticated from now on. If
    // promise is rejected, route will not be followed (no unneeded HTTP-calls will be dne, which case a 401 in the end, anyway)
    return AuthenticationSharedService.authenticate();
  } else {
    // Authentication done on rootscope, no need to call service again. Any unauthenticated access to REST will result in
    // a 401 and will redirect to login anyway. Done to prevent additional call to authenticate every route-change
    $rootScope.authenticated = true;
    return true;
  }
}];
activitiAdminApp
    .config(['$routeProvider', '$httpProvider', '$translateProvider', '$provide',
        function ($routeProvider, $httpProvider, $translateProvider, $provide) {
            $routeProvider
            	.when('/login', {
            		templateUrl: 'views/login.html',
            		controller: 'LoginController'
            	})
                .when('/process-definitions', {
                    templateUrl: 'views/process-definitions.html',
                    controller: 'ProcessDefinitionsController',
                    resolve: authRouteResolver,
                    reloadOnSearch: true
                })
                .when('/process-definition/:definitionId', {
                    templateUrl: 'views/process-definition.html',
                    controller: 'ProcessDefinitionController',
                    resolve: authRouteResolver,
                    reloadOnSearch: true
                })
                .when('/deployments', {
                    templateUrl: 'views/deployments.html',
                    controller: 'DeploymentsController',
                    resolve: authRouteResolver,
                    reloadOnSearch: true
                })
                .when('/deployment/:deploymentId', {
                    templateUrl: 'views/deployment.html',
                    controller: 'DeploymentController',
                    resolve: authRouteResolver,
                    reloadOnSearch: true
                })
                .when('/process-instances', {
                    templateUrl: 'views/process-instances.html',
                    controller: 'ProcessInstancesController',
                    resolve: authRouteResolver,
                    reloadOnSearch: true
                })
                .when('/process-instance/:processInstanceId', {
                	templateUrl: 'views/process-instance.html',
                	controller: 'ProcessInstanceController',
                  resolve: authRouteResolver,
                  reloadOnSearch: true
                })
                .when('/tasks', {
                	templateUrl: 'views/tasks.html',
                	controller: 'TasksController',
                  resolve: authRouteResolver,
                  reloadOnSearch: true
                })
                .when('/task/:taskId', {
                	templateUrl: 'views/task.html',
                	controller: 'TaskController',
                  resolve: authRouteResolver,
                  reloadOnSearch: true
                })
                .when('/jobs', {
                	templateUrl: 'views/jobs.html',
                	controller: 'JobsController',
                  resolve: authRouteResolver,
                  reloadOnSearch: true
                })
                .when('/job/:jobId', {
                	templateUrl: 'views/job.html',
                	controller: 'JobController',
                  resolve: authRouteResolver,
                  reloadOnSearch: true
                })
                .when('/users', {
                	templateUrl: 'views/users.html',
                	controller: 'UsersController',
                  resolve: authRouteResolver,
                  reloadOnSearch: true
                })
                .when('/engine', {
                    templateUrl: 'views/engine.html',
                    controller: 'EngineController',
                    resolve: authRouteResolver,
                    reloadOnSearch: true
                })
                .when('/monitoring', {
                    templateUrl: 'views/monitoring.html',
                    controller: 'MonitoringController',
                    resolve: authRouteResolver,
                    reloadOnSearch: true
                })
                .when('/logout', {
              		templateUrl: 'views/login.html',
              		controller: 'LogoutController',
                  resolve: authRouteResolver,
                  reloadOnSearch: true
              	})
              	.when('/', {
              	  redirectTo: '/engine'
                })
              	.when('/process-definitions-refresh', {
              		redirectTo: '/process-definitions'
              	})
                .when('/apps', {
                    templateUrl: 'views/app-deployments.html',
                    controller: 'AppDeploymentsController',
                    resolve: authRouteResolver,
                    reloadOnSearch: true
                })
                .when('/app/:appId', {
                    templateUrl: 'views/app-deployment.html',
                    controller: 'AppDeploymentController',
                    resolve: authRouteResolver,
                    reloadOnSearch: true
                })
                .when('/decision-tables', {
                    templateUrl: 'views/decision-tables.html',
                    controller: 'DecisionTablesController',
                    resolve: authRouteResolver,
                    reloadOnSearch: true
                })
                .when('/decision-table/:decisionTableId', {
                    templateUrl: 'views/decision-table.html',
                    controller: 'DecisionTableController',
                    resolve: authRouteResolver,
                    reloadOnSearch: true
                })
                .when('/decision-audit/:decisionAuditId', {
                    templateUrl: 'views/decision-audit.html',
                    controller: 'DecisionAuditController',
                    resolve: authRouteResolver,
                    reloadOnSearch: true
                })
                .when('/forms', {
                    templateUrl: 'views/forms.html',
                    controller: 'FormsController',
                    resolve: authRouteResolver,
                    reloadOnSearch: true
                })
                .when('/form/:formId', {
                    templateUrl: 'views/form.html',
                    controller: 'FormController',
                    resolve: authRouteResolver,
                    reloadOnSearch: true
                })
                .when('/submitted-form/:submittedFormId', {
                    templateUrl: 'views/submitted-form.html',
                    controller: 'SubmittedFormController',
                    resolve: authRouteResolver,
                    reloadOnSearch: true
                })
                .otherwise({
                	templateUrl: 'views/login.html',
                    controller: 'LoginController',
                    reloadOnSearch: true
                });

            // Initialize angular-translate
            $translateProvider.useStaticFilesLoader({
                prefix: './i18n/',
                suffix: '.json'
            });

            $translateProvider.registerAvailableLanguageKeys(['en', 'de', 'es', 'fr', 'it', 'nl', 'ja', 'nb', "ru", "pt-BR", "zh-CN"], {
                'en_*': 'en',
                'de_*': 'de',
                'es_*': 'es',
                'fr_*': 'fr',
                'it_*': 'it',
                'nl_*': 'nl',
                'ja_*': 'ja',
                'pt_BR': 'pt-BR',
                'zh_CN': 'zh-CN',
                'en-*': 'en',
                'de-*': 'de',
                'es-*': 'es',
                'fr-*': 'fr',
                'it-*': 'it',
                'nl-*': 'nl',
                'ja-*': 'ja'
            }).determinePreferredLanguage();

        }])

    // Custom Http interceptor that adds the correct prefix to each url
    .config(['$httpProvider', function ($httpProvider) {
        $httpProvider.interceptors.push(function ($q) {
            return {
                'request': function (config) {

                    // Check if it starts with /app, if so add ., such that it works with a context root
                    if (config.url && config.url.indexOf('/app') === 0) {
                        config.url = '.' + config.url;
                    } else  if (config.url && config.url.indexOf('app') === 0) {
                        config.url = './' + config.url;
                    }

                    return config || $q.when(config);
                }
            };
        });
    }])

    /* Filters */

    .filter('dateformat', function() {
		    return function(date, format) {
		    	if(date) {
		    		if(format == ('full')) {
		    			// Format the given value
		    			return moment(date).format("LLL");
		    		} else {
		    			// By default, return a pretty date, based on the current time
		    			return moment(date).format("lll");
		    		}
		    	}
		    	return '';
		    };
		})
		.filter('empty', function() {
		    return function(value) {
		    	if(value) {
		    		return value;
		    	}
		    	return '(None)';
		    };
		})
        .filter('humanTime', function() {
            return function(milliseconds){
                var seconds = milliseconds / 1000;
                var numyears = Math.floor(seconds / 31536000);
                var numdays = Math.floor((seconds % 31536000) / 86400);
                var numhours = Math.floor(((seconds % 31536000) % 86400) / 3600);
                var numminutes = Math.floor((((seconds % 31536000) % 86400) % 3600) / 60);
                var numseconds = Math.floor((((seconds % 31536000) % 86400) % 3600) % 60);
                return numyears + " years " +  numdays + " days " + numhours + " hours " + numminutes + " minutes " + numseconds + " seconds";
            };
        })
        .filter('megabytes', function() {
            return function(bytes) {
                 return Math.floor((bytes/1048576)) + 'MB';
            };
        })
        .filter('round', function() {
        	return function(number) {
        		if(!number) {
        			return "0";
        		} else {
        			return +number.toFixed(3);
        		}
        	};
        })
        .filter('range', function() {
          return function(input, min, max) {
            min = parseInt(min); //Make string input int
            max = parseInt(max);
            for (var i=min; i<max; i++)
              input.push(i);
            return input;
          };
        })
        .run(['$rootScope', '$location', 'AuthenticationSharedService', 'Account',
            function($rootScope, $location, AuthenticationSharedService, Account) {
	            // Call when the 401 response is returned by the client
	            $rootScope.$on('event:auth-loginRequired', function(rejection) {
	                $rootScope.authenticated = false;

	                if ($location.path() !== "/" && $location.path() !== "" && $location.path() !== "/login") {
	                    $rootScope.pageAfterLogin = $location.path();
	                    $location.path('/login').replace();
	                }
	            });

	            // Call when the user is authenticated
	           $rootScope.$on('event:auth-authConfirmed', function() {
	               if (!$rootScope.authenticated) {
	                   $rootScope.authenticated = true;
	                   $rootScope.account = Account.get(function () {
	                       // Load cluster configs, in case direct navigation is done to the page
	                       $rootScope.loadClusterConfigs(false);
	                   }, function () {
	                       $rootScope.authenticated = false;
	                       $location.path('/login');
	                   });

	               }

	            });

	            // Call when the user logs in
	            $rootScope.$on('event:auth-loginConfirmed', function() {
	                if (!$rootScope.authenticated) {
	                    $rootScope.authenticated = true;
	                    $rootScope.account = Account.get(function () {
	                        $rootScope.loadClusterConfigs(true);

	                        if($rootScope.pageAfterLogin) {
	                          $location.path($rootScope.pageAfterLogin);
	                        } else {
	                          // Show default page
	                          $location.path('engine');
	                        }

	                       }, function () {
	                           $rootScope.authenticated = false;
	                           $location.path('/login');
	                       });
	                }

	            });

	            // Call when the user logs out
	            $rootScope.$on('event:auth-loginCancelled', function() {
	                $rootScope.authenticated = false;

	                // Explicitly clear cluster data to re-fetch
	                $rootScope.activeServer = undefined;
                    $rootScope.changeActiveCluster(undefined);
	                $rootScope.availableClusters = undefined;
	                $rootScope.availableServers = undefined;
	                $rootScope.serverLoaded = false;
	                $location.path('');
            });
        }])
        .run(['$rootScope', '$http', '$timeout', '$location', '$cookies', '$modal', '$translate',
            function($rootScope, $http, $timeout, $location, $cookies, $modal, $translate) {

                $rootScope.serverStatus = {
                };

                var proposedLanguage = $translate.proposedLanguage();
                var supportedLanguages = ['en','de','es','fr','it','nl','ja','nb','ru','pt-BR','zh-CN'];

                if (supportedLanguages.indexOf(proposedLanguage) === -1) {
                    $translate.use('en');
                }

        		$rootScope.serverLoaded = false;

                $rootScope.loadServers = function(callbackAfterLoad) {
                    $http({method: 'GET', url: '/app/rest/server-configs'}).
                        success(function(data, status, headers, config) {
                            if (data.length > 0) {
                                $rootScope.availableServers = data;
                                $rootScope.selectServerForActiveCluster();

                                $rootScope.serverLoaded = true;

                                if (callbackAfterLoad) {
                                    callbackAfterLoad();
                                }
                            } else {
                                if (callbackAfterLoad) {
                                    callbackAfterLoad();
                                }

                                $rootScope.handleNoServerConfigs();

                            }
                        }).
                        error(function(data, status, headers, config) {
                            console.log('Something went wrong: ' + data);
                        });
                };

                $rootScope.loadClusterConfigs = function(force, clusterConfigToSelect, callbackAfterLoad) {
                    // Only load configs when not already loaded or when forced
                    if(!$rootScope.availableClusters || force) {
                      $http({method: 'GET', url: '/app/rest/cluster-configs'}).
                          success(function(data, status, headers, config) {
                              if (data.length > 0) {
                                  $rootScope.availableClusters = data;

                                  // This method can be called when the app is booting, and the 'clusterConfigToSelect'
                                  // param will not be provided then.
                                  // When changing the cluster config in the dropdown, it will be provided, so we
                                  // can switch to the new cluster config.

                                  // When the page is hard refreshed, we also must check if there isn't a cluster id set in the cookie
                                  if (!clusterConfigToSelect) {
                                      var clusterIdFromCookie = $cookies['activeClusterId'];
                                      if (clusterIdFromCookie) {
                                          clusterConfigToSelect = parseInt(clusterIdFromCookie); // values from cookies are strings!
                                      }
                                  }

                                  if (!clusterConfigToSelect) {
                                      $rootScope.selectCluster(data[0]);
                                  } else {
                                      var clusterFound = false;
                                      for (var clusterIndex = 0; clusterIndex<data.length; clusterIndex++) {
                                          if (data[clusterIndex].id === clusterConfigToSelect) {
                                              $rootScope.selectCluster(data[clusterIndex]);
                                              clusterFound = true;
                                          }
                                      }

                                      if (!clusterFound && data.length > 0) {
                                          $rootScope.activeServer = data[0].serverConfigs[0];
                                          $rootScope.selectCluster(data[0]);
                                      }
                                  }

                                  $rootScope.loadServers(callbackAfterLoad);
                              } else {
                                  console.log('Warning! No cluster configurations received, showing create new cluster wizard');
                                  $rootScope.showCreateClusterConfigPopup({closable: false});
                              }
                          }).
                          error(function(data, status, headers, config) {
                              console.log('Something went wrong: ' + data);
                          });
                    }
                };

                $rootScope.showLicenseInfo = function() {
                	var modalInstance = $modal.open({
        				templateUrl: 'views/license-info-popup.html?version=' + Date.now(),
        				controller: 'LicenseInfoModalInstanceCrtl'
        			});
        		};

                /**
                 * Switch currently active cluster.
                 */
        		$rootScope.selectCluster = function(cluster) {
        			var firstTime = !$rootScope.activeCluster;
        			$rootScope.changeActiveCluster(cluster);
        			$rootScope.selectServerForActiveCluster(function(){
        			    if (!firstTime) {
                            if($rootScope.pageAfterLogin) {
                                $rootScope.pageAfterLogin = undefined;
                            } else {
                                $location.path('/process-definitions-refresh');
                            }
                        }
        			});
        		};

                /**
                 * Show popup that allows to create a new cluster configuration.
                 */
        		$rootScope.showCreateClusterConfigPopup = function(options) {
        		    var options = options || {closable : true};
        		    var modalInstance = $modal.open({
                        templateUrl: 'views/engine-edit-cluster-config.html',
                        controller: 'EditClusterConfigModalInstanceCrtl',
                        backdrop: (options.closable === false) ? 'static': true,
                        keyboard: (options.closable === false) ? false: true,
                        resolve: {
                            clusterConfig: function() {return null;},  // No cluster config (null) provided ==> save mode
        		            options: function() {return {closable:options.closable};}
                        }
                    });

                    modalInstance.result.then(function (result) {
                        if(result) {
                            if (options.callBack) {
                                options.callBack();
                            } else {
                                $location.path('engine');
                            }

                        }
                    });
        		};

                $rootScope.selectServerForActiveCluster = function(callBack) {
                    if ($rootScope.activeCluster && $rootScope.availableServers) {
                        for (var i=0; i<$rootScope.availableServers.length; i++) {
                            var server = $rootScope.availableServers[i];
                            if (server.clusterConfigId && server.clusterConfigId === $rootScope.activeCluster.id) {
                                $http({method: 'GET', url: '/app/rest/server-configs/' + server.id}).
                                success(function(serverData, status, headers, config) {
                                    $rootScope.activeServer = serverData;
                                    // save the endpoint type
                                    $rootScope.isBpmSuite = serverData.endpointType === "bpmSuite";
                                    $rootScope.checkLicenseValidity();
                                    if (callBack) {
                                        callBack();
                                    }

                                    // check license
                                    $rootScope.checkLicenseValidity();
                                }).
                                error(function(data, status, headers, config) {
                                    // TODO show error
                                });
                                return;
                            }
                        }
                        console.log('No server found for current cluster config');
                        delete $rootScope.activeServer;
                        if ($rootScope.activeCluster && !$rootScope.activeCluster.serverConfigs.length) {
                            $rootScope.handleNoServerConfigs();
                        }
                    }
                };

                $rootScope.changeActiveCluster = function(cluster) {
                    if (cluster) {
                        $rootScope.activeCluster = cluster;
                        $cookies['activeClusterId'] = cluster.id + ''; // cookies -> only strings
                    } else {
                        $rootScope.activeCluster = undefined;
                        delete $cookies['activeClusterId'];
                    }
                };

	        	$rootScope.loadProcessDefinitionsCache = function() {
                    if ($rootScope.activeServer && $rootScope.activeServer.id) {
                        var promise = $http({
                            method: 'GET',
                            url: '/app/rest/activiti/process-definitions?serverId=' + $rootScope.activeServer.id + '&size=100000000'
                        }).success(function (data, status, headers, config) {
                            return data;
                        }).error(function (data, status, headers, config) {
                            return {'status': false};
                        });

                        return promise;
                    }
	            };

	            $rootScope.getProcessDefinitionFromCache = function(processDefId) {
	            	for (var i = 0; i < $rootScope.processDefinitionsCache.data.length; i++) {
            			if ($rootScope.processDefinitionsCache.data[i].id === processDefId) {
            				return $rootScope.processDefinitionsCache.data[i];
            			}
            		}
	            	return null;
	            };

	            $rootScope.checkLicenseValidity = function() {
                    // setting default read only state to true
                    $rootScope.serverStatus.readonlyStatus = true;

                    if ($rootScope.activeServer && $rootScope.activeServer.id) {
	                    $http({method: 'GET', url: '/app/rest/license-validity?serverConfigId=' + $rootScope.activeServer.id}).
                            success(function(data, status, headers, config) {
                                var readonlyStatus = true;
                                if (data.serverLicenseStatus == 'license-valid') {
                                    // Go into read write mode if endpoint license is valid
                                    readonlyStatus = false;
                                }

                                $rootScope.serverStatus.serverLicenseStatus = data.serverLicenseStatus;
                                $rootScope.serverStatus.readonlyStatus = readonlyStatus;
                            }).
                            error(function(data, status, headers, config) {
                               console.log('Something went wrong: ' + data);
                            });
	                }
	            };

	            // Reference the fixed configuration values on the root scope
	            $rootScope.config = ActivitiAdmin.Config;

	            // Store empty object for filter-references
	            $rootScope.filters = { forced: {} };

	            // Alerts
	            $rootScope.alerts = {
	                queue: []
	            };

	            $rootScope.showAlert = function(alert) {
	                if (alert.queue.length > 0) {
	                    alert.current = alert.queue.shift();
	                    // Start timout for message-pruning
	                    alert.timeout = $timeout(function() {
	                        if(alert.queue.length == 0) {
	                            alert.current = undefined;
	                            alert.timeout = undefined;
	                        } else {
	                            $rootScope.showAlert(alert);
	                        }
	                    }, 1500);
	                } else {
	                    $rootScope.alerts.current = undefined;
	                }
	            };

	            $rootScope.addAlert = function(message, type) {
	                var newAlert = {message: message, type: type};
                    if (!$rootScope.alerts.timeout) {
                        // Timeout for message queue is not running, start one
                        $rootScope.alerts.queue.push(newAlert);
                        $rootScope.showAlert($rootScope.alerts);
                    } else {
                        $rootScope.alerts.queue.push(newAlert);
                    }
	            };

	            $rootScope.dismissAlert = function() {
	                if (!$rootScope.alerts.timeout) {
	                    $rootScope.alerts.current = undefined;
	                } else {
	                    $timeout.cancel($rootScope.alerts.timeout);
	                    $rootScope.alerts.timeout = undefined;
	                    $rootScope.showAlert($rootScope.alerts);
	                }
	            };

	            $rootScope.addAlertPromise = function(promise, type) {
	                promise.then(function(data) {
	                    $rootScope.addAlert(data, type);
	                });
	            };

	            $rootScope.executeWhenReady = function(callback) {
	                if ($rootScope.activeServer) {
	                    callback();
	                } else {
	                    $rootScope.$watch('activeServer', function() {
	                        if ($rootScope.activeServer) {
	                            callback();
	                        }
	                    });
	                }
	            };

	            $rootScope.handleNoServerConfigs = function (clusterId, callback) {
	                if (!$rootScope.account.multiTenant) {
	                    // do nothing on single tenant mode
	                    return;
	                }
	                var clusterId = clusterId || $rootScope.activeCluster.id;
	                $http({method: 'GET', url: '/app/rest/server-configs/default'}).
                    success(function(defaultServerconfig, status, headers, config) {

                        defaultServerconfig.clusterConfigId = clusterId;
                        var modalInstance = $modal.open({
                            templateUrl: 'views/engine-edit-endpoint-popup.html',
                            controller: 'EditEndpointConfigModalInstanceCrtl',
                            resolve: {
                                server: function() {return defaultServerconfig;}
                            }
                        });

                        modalInstance.result.then(function (result) {
                            if(result) {
                                $rootScope.addAlert($translate.instant('ALERT.ENGINE.ENDPOINT-UPDATED', result), 'info');
                                $rootScope.loadServers();
                                if (callback) {
                                    callback(clusterId);
                                }
                            }
                        });

                    });
	            };
        	}
        ]);

activitiAdminApp.controller('LicenseInfoModalInstanceCrtl',
    ['$scope', '$modalInstance', '$http', function ($scope, $modalInstance, $http) {

	$scope.model = {};

	$http({method: 'GET', url: '/app/rest/license-info'}).
	    success(function(data, status, headers, config) {
	    	$scope.model = data;
	    }).
	    error(function(data, status, headers, config) {
	        console.log('Something went wrong when fetching license info');
	    });

	$scope.close = function () {
		$modalInstance.dismiss('cancel');
	};
}]);
