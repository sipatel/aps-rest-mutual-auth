/*
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/* Controllers */

activitiAdminApp.controller('AppDeploymentsController', ['$rootScope', '$scope', '$http', '$timeout', '$location', '$translate', '$q', '$modal', 'gridConstants',
    function ($rootScope, $scope, $http, $timeout, $location, $translate, $q, $modal, gridConstants) {

        $rootScope.navigation = {selection: 'apps'};

        $rootScope.checkLicenseValidity();

        $scope.filter = {};
        $scope.appsData = {};

        var filterConfig = {
            url: '/app/rest/activiti/apps',
            method: 'GET',
            success: function (data, status, headers, config) {
                $scope.appsData = data;
            },
            error: function (data, status, headers, config) {
                if (data && data.message) {
                    // Extract error-message
                    $rootScope.addAlert(data.message, 'error');
                } else {
                    // Use default error-message
                    $rootScope.addAlert($translate.instant('ALERT.GENERAL.HTTP-ERROR'), 'error');
                }
            },

            sortObjects: [
                {name: 'APP-DEPLOYMENTS.SORT.ID', id: 'id'}
            ],

            supportedProperties: [
                {id: 'nameLike', name: 'APP-DEPLOYMENTS.FILTER.NAME', showByDefault: true},
                {id: 'tenantId', name: 'APP-DEPLOYMENTS.FILTER.TENANTID', showByDefault: true},
                {id: 'latest', name: 'APP-DEPLOYMENTS.FILTER.TENANTID', showByDefault: true, defaultValue: true}
            ]
        };

        if ($rootScope.filters && $rootScope.filters.appsFilter) {
            // Reuse the existing filter
            $scope.filter = $rootScope.filters.appsFilter;
            $scope.filter.config = filterConfig;
        } else {
            $scope.filter = new ActivitiAdmin.Utils.Filter(filterConfig, $http, $timeout, $rootScope);
            $rootScope.filters.appsFilter = $scope.filter;
        }

        $scope.appSelected = function (app) {
            if (app && app.getProperty('id')) {
                $location.path('/app/' + app.getProperty('id'));
            }
        };

        $q.all([$translate('APP-DEPLOYMENTS.HEADER.ID'),
                $translate('APP-DEPLOYMENTS.HEADER.APP-DEFINITION-ID'),
                $translate('APP-DEPLOYMENTS.HEADER.NAME'),
                $translate('APP-DEPLOYMENTS.HEADER.DEPLOY-TIME'),
                $translate('APP-DEPLOYMENTS.HEADER.DEPLOYED-BY'),
                $translate('APP-DEPLOYMENTS.HEADER.TENANT')])
            .then(function (headers) {

                // Config for grid
                $scope.gridApps = {
                    data: 'appsData.data',
                    enableRowReordering: true,
                    multiSelect: false,
                    keepLastSelected: false,
                    rowHeight: 36,
                    afterSelectionChange: $scope.appSelected,
                    columnDefs: [
                        {field: 'id', displayName: headers[0], cellTemplate: gridConstants.defaultTemplate},
                        {field: 'appDefinition.id', displayName: headers[1], cellTemplate: gridConstants.defaultTemplate},
                        {field: 'appDefinition.name', displayName: headers[2], cellTemplate: gridConstants.defaultTemplate},
                        {field: 'created', displayName: headers[3], cellTemplate: gridConstants.dateTemplate},
                        {field: 'createdBy', displayName: headers[4], cellTemplate: gridConstants.userObjectTemplate},
                        {field: 'appDefinition.tenantId', displayName: headers[5], cellTemplate: gridConstants.defaultTemplate}]
                };
            });

        $scope.executeWhenReady(function () {
            $scope.filter.refresh();
        });

        /*
         * ACTIONS
         */
        $scope.uploadApp = function () {
            var modalInstance = $modal.open({
                templateUrl: 'views/upload-app.html',
                controller: 'UploadAppCrtl'
            });
            modalInstance.result.then(function (result) {
                // Refresh page if closed successfully
                if (result) {
                    $scope.appsData = {};
                    $scope.filter.refresh();
                }
            });
        };
        
        $scope.publishAppModel = function () {
            var modalInstance = $modal.open({
                templateUrl: 'views/publish-app-model-popup.html',
                controller: 'PublishAppModelCrtl'
            });
            modalInstance.result.then(function (result) {
                // Refresh page if closed successfully
                if (result) {
                    $scope.appsData = {};
                    $scope.filter.refresh();
                }
            });
        };
        
    }]);


/**\
 * Controller for the upload a model from the process Modeler.
 */
activitiAdminApp.controller('UploadAppCrtl',
    ['$scope', '$modalInstance', '$http', '$upload', function ($scope, $modalInstance, $http, $upload) {

        $scope.status = {loading: false};

        $scope.model = {};

        $scope.onFileSelect = function ($files) {

            for (var i = 0; i < $files.length; i++) {
                var file = $files[i];
                $upload.upload({
                    url: '/app/rest/activiti/apps?serverId=' + $scope.activeServer.id,
                    method: 'POST',
                    file: file
                }).progress(function (evt) {
                    $scope.status.loading = true;
                    $scope.model.uploadProgress = parseInt(100.0 * evt.loaded / evt.total);
                }).success(function (data, status, headers, config) {
                        $scope.status.loading = false;
                        if (data.error) {
                            $scope.model.errorMessage = data.errorDescription;
                            $scope.model.error = true;
                        } else {
                            $modalInstance.close(true);
                        }
                }).error(function (data, status, headers, config) {

                    if (data && data.message) {
                        $scope.model.errorMessage = data.message;
                    }

                    $scope.model.error = true;
                    $scope.status.loading = false;
                });
            }
        };

        $scope.cancel = function () {
            if (!$scope.status.loading) {
                $modalInstance.dismiss('cancel');
            }
        };

    }]);


/**\
 * Controller for publishing an app model
 */
activitiAdminApp.controller('PublishAppModelCrtl',
    ['$rootScope', '$scope', '$modalInstance', '$http', '$q', '$translate', 'gridConstants', function ($rootScope, $scope, $modalInstance, $http, $q, $translate, gridConstants) {

        $scope.status = {loading: false};
        $scope.model = {};
        $scope.selectedAppModels = [];
        
        $scope.selectTargetServer = function (targetServer) {
            $scope.targetServer = targetServer;
            $scope.model = {};
        };
        
        $scope.resetModel = function () {
            $scope.model = {};
        };
        
        $q.all([$translate('APPS.POPUP.PUBLISH-MODEL.HEADER.ID'),
                $translate('APPS.POPUP.PUBLISH-MODEL.HEADER.NAME'),
                $translate('APPS.POPUP.PUBLISH-MODEL.HEADER.LAST-UPDATE'),
                $translate('APPS.POPUP.PUBLISH-MODEL.HEADER.CREATED-BY'),
                $translate('APP-DEPLOYMENTS.HEADER.TENANT')])
            .then(function (headers) {
                // Config for grid
                $scope.gridAppsModels = {
                    data: 'appModels.data',
                    enableRowReordering: true,
                    multiSelect: false,
                    keepLastSelected: false,
                    rowHeight: 36,
                    selectedItems: $scope.selectedAppModels,
                    afterSelectionChange: $scope.resetModel,
                    columnDefs: [
                        {field: 'id', displayName: headers[0], cellTemplate: gridConstants.defaultTemplate},
                        {field: 'name', displayName: headers[1], cellTemplate: gridConstants.defaultTemplate},
                        {field: 'lastUpdated', displayName: headers[2], cellTemplate: gridConstants.dateTemplate},
                        {field: 'createdByFullName', displayName: headers[3], cellTemplate: gridConstants.defaultTemplate}]
                };
            });
        
        $scope.publishAppModel = function () {
            if ($scope.selectedAppModels[0]) {
                $scope.status.loading = true;
                var targetServerId = '';
                if ($scope.targetServer) {
                    targetServerId = $scope.targetServer.serverConfigs[0].id;
                }
                $http({method: 'POST', 
                    url: '/app/rest/activiti/apps/'+$scope.selectedAppModels[0].id+'/publish', 
                    params: {serverId: $scope.activeServer.id, targetServerId: targetServerId},
                    data: {comment: ""}}).
                success(function(data, status, headers, config) {
                    $scope.status.loading = false;
                    if (data.error) {
                        $scope.model.errorMessage = data.errorDescription;
                        $scope.model.error = true;
                    } else {
                        $modalInstance.close(true);
                    }
                }).
                error(function(data, status, headers, config) {
                    $scope.status.loading = false;
                    // handle app conflict only if conflictingAppId is provided
                    if (status === 409 && data.customData && data.customData.conflictingAppId) { 
                        $scope.model.isConflict = true;
                        $scope.model.conflictAppId = data.customData.conflictingAppId;
                        $scope.model.conflictingAppName = data.customData.conflictingAppName;
                        $scope.model.conflictingAppCreatedBy = data.customData.conflictingAppCreatedBy;
                        $scope.model.conflictMessage = data.message;
                    } else {
                        $scope.model.error = true;
                        if (data.message) { 
                            $scope.model.errorMessage = data.message;
                        }
                    }
                });
            }
        };
        
        $scope.publishAppAsNewVersion = function (replaceAppId) {
            if ($scope.selectedAppModels[0]) {
                $scope.status.loading = true;
                var targetServerId = '';
                if ($scope.targetServer) {
                    targetServerId = $scope.targetServer.serverConfigs[0].id;
                }
                $http({method: 'POST', 
                    url: '/app/rest/activiti/apps/'+$scope.selectedAppModels[0].id+'/publish', 
                    params: {serverId: $scope.activeServer.id, targetServerId: targetServerId, replaceAppId: replaceAppId},
                    data: {comment: ""}}).
                success(function(data, status, headers, config) {
                    $scope.status.loading = false;
                    if (data.error) {
                        $scope.model.errorMessage = data.errorDescription;
                        $scope.model.error = true;
                    } else {
                        $modalInstance.close(true);
                    }
                }).
                error(function(data, status, headers, config) {
                    if (data && data.message) {
                        $scope.model.errorMessage = data.message;
                    }
                    $scope.model.error = true;
                    $scope.status.loading = false;
                });
            }
        };
        
        $scope.executeWhenReady(function () {
            $scope.everyonesApps = false;
            $scope.filterText = '';
            
            // Load App models
            fetchAppModels();
            
            
        });

        $scope.cancel = function () {
            if (!$scope.status.loading) {
                $modalInstance.dismiss('cancel');
            }
        };
        
        $scope.filterChanged = function () {
            fetchAppModels();
        };
        
        function fetchAppModels() {
            $http({
                method: 'GET',
                url: '/app/rest/activiti/models',
                params: {
                    serverId: $rootScope.activeServer.id, 
                    modelType: 3,
                    sort: 'modifiedDesc',
                    filter: ($scope.everyonesApps) ? 'everyone' : 'myApps',
                    filterText: $scope.filterText
                    }
            }).
            success(function (data, status, headers, config) {
                $scope.appModels = data;
            });
        }

    }]);
