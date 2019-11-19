/*
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

/* Controllers */

activitiAdminApp.controller('DecisionAuditController', ['$rootScope', '$scope', '$http', '$timeout', '$location', '$translate', '$q', '$modal', '$routeParams',
    function ($rootScope, $scope, $http, $timeout, $location, $translate, $q, $modal, $routeParams) {

        $rootScope.checkLicenseValidity();

        $scope.returnToList = function () {
            $location.path("/decision-tables");
        };

        $scope.showDecisionAudit = function () {
            $modal.open({
                templateUrl: 'views/decision-audit-popup.html',
                windowClass: 'modal modal-full-width',
                controller: 'ShowDecisionAuditPopupCrtl',
                resolve: {
                    decisionAudit: function () {
                        return $scope.decisionAudit;
                    }
                }
            });
        };

        $scope.executeWhenReady(function () {
            // Load deployment
            $http({method: 'GET', url: '/app/rest/activiti/decision-audits/' + $routeParams.decisionAuditId, params: {serverId: $rootScope.activeServer.id}}).
            success(function (data, status, headers, config) {
                $scope.decisionAudit = data;
            }).
            error(function (data, status, headers, config) {
                if (data && data.message) {
                    // Extract error-message
                    $rootScope.addAlert(data.message, 'error');
                } else {
                    // Use default error-message
                    $rootScope.addAlert($translate.instant('ALERT.GENERAL.HTTP-ERROR'), 'error');
                }
            });
        });
    }]);

activitiAdminApp.controller('ShowDecisionAuditPopupCrtl',
    ['$rootScope', '$scope', '$modalInstance', '$http', 'decisionAudit', '$timeout', '$translate', 'uiGridConstants',
        function ($rootScope, $scope, $modalInstance, $http, decisionAudit, $timeout, $translate, uiGridConstants) {

            $scope.decisionAudit = decisionAudit;

            $scope.status = {loading: false};

            $scope.cancel = function () {
                if (!$scope.status.loading) {
                    $modalInstance.dismiss('cancel');
                }
            };

            $scope.model = {
                loading: true,
                decisionTableDefinition: {},
                decisionTableRules: [],
                columnDefs: [],
                columnVariableIdMap: {},
                columnVariableTypeVariableIdMap: {},
                decisionTableExpanded: true,

                metadataVariablesColumnDefs: [],
                inputVariableValues: [],
                inputListExpanded: false,
                outputVariableValues: [],
                outputListExpanded: false
            };

            $scope.model.metadataVariablesColumnDefs.push({
                name: 'name',
                displayName: $translate.instant('DECISION-AUDIT.TABLE.METADATA-NAME'),
                field: 'name',
                type: 'string',
                headerCellClass: 'header-expression',
                sort: {
                    direction: uiGridConstants.ASC,
                    priority: 1
                }
            });
            $scope.model.metadataVariablesColumnDefs.push({
                name: 'value',
                displayName: $translate.instant('DECISION-AUDIT.TABLE.METADATA-VALUE'),
                field: 'value',
                type: 'string',
                headerCellClass: 'header-expression',
                cellTemplate: "<div class=\"ui-grid-cell-contents\">" +
                "       <span class=\"contents-value\">{{grid.appScope.formatVariableValue(COL_FIELD, row)}}</span>" +
                "   </div>"
            });

            // config for grid
            $scope.gridOptions = {
                data: $scope.model.decisionTableRules,
                enableRowHeaderSelection: false,
                multiSelect: false,
                modifierKeysToMultiSelect: false,
                enableHorizontalScrollbar: 0,
                enableColumnMenus: true,
                enableSorting: false,
                enableCellEditOnFocus: false,
                columnDefs: $scope.model.columnDefs
                //headerTemplate: appResourceRoot + 'views/templates/decision-table-header-template.html'
            };

            $scope.gridOptions.onRegisterApi = function (gridApi) {
                //set gridApi on scope
                $scope.gridApi = gridApi;

                var cellTemplate = _rowHeaderTemplate();   // you could use your own template here
                $scope.gridApi.core.addRowHeaderColumn({name: 'rowHeaderCol', displayName: '', width: 35, cellTemplate: cellTemplate});
            };

            $scope.inputVariablesGridOptions = {
                data: $scope.model.inputVariableValues,
                columnDefs: $scope.model.metadataVariablesColumnDefs,
                //headerTemplate: appResourceRoot + 'views/templates/decision-table-header-template.html',
                enableSorting: true,
                enableCellEditOnFocus: false,
                enableRowHeaderSelection: false,
                multiSelect: false,
                modifierKeysToMultiSelect: false,
                enableHorizontalScrollbar: 0,
                enableColumnMenus: false
            };

            $scope.inputVariablesGridOptions.onRegisterApi = function (gridApi) {
                //set gridApi on scope
                $scope.inputVariableGridApi = gridApi;

                var cellTemplate = _rowHeaderTemplate();   // you could use your own template here
                $scope.inputVariableGridApi.core.addRowHeaderColumn({name: 'rowHeaderCol', displayName: '', width: 35, cellTemplate: cellTemplate});
            };

            $scope.outputVariablesGridOptions = {
                data: $scope.model.outputVariableValues,
                columnDefs: $scope.model.metadataVariablesColumnDefs,
                //headerTemplate: appResourceRoot + 'views/templates/decision-table-header-template.html',
                enableSorting: true,
                enableCellEditOnFocus: false,
                enableRowHeaderSelection: false,
                multiSelect: false,
                modifierKeysToMultiSelect: false,
                enableHorizontalScrollbar: 0,
                enableColumnMenus: false
            };

            $scope.outputVariablesGridOptions.onRegisterApi = function (gridApi) {
                //set gridApi on scope
                $scope.outputVariableGridApi = gridApi;

                var cellTemplate = _rowHeaderTemplate();   // you could use your own template here
                $scope.outputVariableGridApi.core.addRowHeaderColumn({name: 'rowHeaderCol', displayName: '', width: 35, cellTemplate: cellTemplate});
            };


            $scope.getDecisionTableInfo = function (id, activityId, executionId, decisionKey) {

                    $scope.model.loading = false;

                    var decisionTableDefinition = JSON.parse($scope.decisionAudit.decisionModelJson);
                    var auditTrail = JSON.parse($scope.decisionAudit.auditTrailJson);
                    $scope.auditTrail = auditTrail;
                    $scope.model.decisionTableDefinition = decisionTableDefinition;

                    // Input variables table
                    var inputVariableValues = [];
                    for (var name in auditTrail.inputVariables) {
                        if (auditTrail.inputVariables.hasOwnProperty(name)) {
                            inputVariableValues.push({
                                name: name,
                                value: auditTrail.inputVariables[name]
                            });
                        }
                    }
                    Array.prototype.push.apply($scope.model.inputVariableValues, inputVariableValues);
                    $scope.inputVariableGridApi.core.notifyDataChange(uiGridConstants.dataChange.ALL);

                    // Output variables table
                    var outputVariableValues = [];
                    for (var name in auditTrail.outputVariables) {
                        if (auditTrail.outputVariables.hasOwnProperty(name)) {
                            outputVariableValues.push({
                                name: name,
                                value: auditTrail.outputVariables[name]
                            });
                        }
                    }
                    Array.prototype.push.apply($scope.model.outputVariableValues, outputVariableValues);
                    $scope.outputVariableGridApi.core.notifyDataChange(uiGridConstants.dataChange.ALL);

                    // Decision table table
                    decisionTableDefinition.inputExpressions.forEach(function (inputExpression) {
                        inputExpression.id += "";
                        $scope.model.columnVariableIdMap[inputExpression.id] = inputExpression.variableId;
                        $scope.model.columnVariableTypeVariableIdMap[inputExpression.variableId] = inputExpression.type;

                    });

                    decisionTableDefinition.outputExpressions.forEach(function (outputExpression) {
                        outputExpression.id += "";
                        $scope.model.columnVariableIdMap[outputExpression.id] = outputExpression.variableId;
                        $scope.model.columnVariableTypeVariableIdMap[outputExpression.variableId] = outputExpression.type;
                    });

                    // initialize ui grid model
                    if (decisionTableDefinition.rules && decisionTableDefinition.rules.length > 0) {
                        Array.prototype.push.apply($scope.model.decisionTableRules, decisionTableDefinition.rules);
                    }

                    // get column definitions
                    $scope.getColumnDefinitions(decisionTableDefinition);

            };

            $scope.backToProcessInstance = function () {
                $scope.$parent.model.decisionTableAudit = {};
            };

            // helper for looking up variable id by col id
            $scope.getVariableNameColumnId = function (col) {
                var colId = col.name;
                if (!colId) {
                    return '';
                }
                if ($scope.model.columnVariableIdMap[colId]) {
                    return $scope.model.columnVariableIdMap[colId];
                }
                return '';
            };

            $scope.getVariableValueColumnId = function (col) {
                var colId = col.name;
                if (!colId) {
                    return '';
                }
                if ($scope.model.columnVariableIdMap[colId]) {
                    var variableName = $scope.model.columnVariableIdMap[colId];
                    if ($scope.auditTrail && $scope.auditTrail.inputVariables && $scope.auditTrail.inputVariables.hasOwnProperty(variableName)) {
                        var value = $scope.auditTrail.inputVariables[variableName];
                        var type = $scope.model.columnVariableTypeVariableIdMap[variableName];
                        if (value && type == 'date') {
                            value = new moment(value).format('LL');
                        } else if (value && type == 'datetime') {
                            value = new moment(value).format('LLL');
                        }
                        return value;
                    }
                }
                return '';
            };

            $scope.formatVariableValue = function (value, row) {
                var name = row.entity.name;
                var type = $scope.model.columnVariableTypeVariableIdMap[name];
                if (!type) {
                    type = $scope.auditTrail.inputVariableTypes[name];
                }
                return $scope.formatValue(value, type);
            };

            $scope.formatValue = function (value, type) {
                if (value && type == 'date') {
                    value = new moment(value).format('LL');
                } else if (value && type == 'datetime') {
                    value = new moment(value).format('LLL');
                }
                return value;
            };

            // Custom UI grid template
            var _getHeaderInputExpressionCellTemplate = function () {
                return "" +
                    "<div role=\"columnheader\" ui-grid-one-bind-aria-labelledby-grid=\"col.uid + '-header-text ' + col.uid + '-sortdir-text'\">" +
                    "   <div class=\"ui-grid-cell-contents ui-grid-header-cell-primary-focus\" col-index=\"renderIndex\" title=\"TOOLTIP\">" +
                    "       <div class=\"text-center text-label\" title=\"{{ col.displayName CUSTOM_FILTERS }}\"><span>{{ col.displayName CUSTOM_FILTERS }}</span></div>" +
                    "       <div class=\"text-center text-value\" title=\"{{ grid.appScope.getVariableNameColumnId(col) }}\"><span ui-grid-one-bind-id-grid=\"col.uid + '-header-text'\">{{ grid.appScope.getVariableNameColumnId(col) }}</span></div>" +
                    "       <div class=\"text-center text-value\" title=\"{{ grid.appScope.getVariableValueColumnId(col) }}\"><span ui-grid-one-bind-id-grid=\"col.uid + '-header-text'\">{{ grid.appScope.getVariableValueColumnId(col) }}</span></div>" +
                    "       <div class=\"text-center subtle\"><span>{{ 'DECISION-AUDIT.TABLE.INPUT-VARIABLE' | translate }}</span></div>" +
                    "   </div>" +
                    "</div>";
            };

            var _getHeaderOutputExpressionCellTemplate = function () {
                return "" +
                    "<div role=\"columnheader\" ui-grid-one-bind-aria-labelledby-grid=\"col.uid + '-header-text ' + col.uid + '-sortdir-text'\">" +
                    "   <div class=\"ui-grid-cell-contents ui-grid-header-cell-primary-focus\" col-index=\"renderIndex\" title=\"TOOLTIP\">" +
                    "       <div class=\"text-center text-label\" title=\"{{ col.displayName CUSTOM_FILTERS }}\"><span>{{ col.displayName CUSTOM_FILTERS }}</span></div>" +
                    "       <div class=\"text-center text-value\" title=\"{{ grid.appScope.getVariableNameColumnId(col) }}\"><span ui-grid-one-bind-id-grid=\"col.uid + '-header-text'\">{{ grid.appScope.getVariableNameColumnId(col) }}</span></div>" +
                    "       <div class=\"text-center text-value\"><span></span></div>" +
                    "       <div class=\"text-center subtle\"><span>{{ 'DECISION-AUDIT.TABLE.OUTPUT-VARIABLE' | translate }}</span></div>" +
                    "   </div>" +
                    "</div>";
            };

            var _rowHeaderTemplate = function () {
                return "<div class=\"ui-grid-disable-selection\"><div class=\"ui-grid-cell-contents text-center customRowHeader\">{{rowRenderIndex + 1}}</div></div>"
            };

            var _getInputCellTemplate = function (columnType) {
                var cellTemplate = "" +
                    "<div class=\"ui-grid-cell-contents\" ng-class=\"grid.appScope.getInputClass(rowRenderIndex, colRenderIndex)\" title=\"{{grid.appScope.getInputTooltip(rowRenderIndex, colRenderIndex, COL_FIELD)}}\">" +
                    "   <span class=\"contents-value\">{{COL_FIELD}}</span>" +
                    "</div>";
                return cellTemplate;
            };

            var _getOutputCellTemplate = function (columnType) {
                var cellTemplate = "" +
                    "<div class=\"ui-grid-cell-contents\" ng-class=\"grid.appScope.getOutputClass(rowRenderIndex, colRenderIndex)\" title=\"{{grid.appScope.getOutputTooltip(rowRenderIndex, colRenderIndex, COL_FIELD)}}\">" +
                    "   <span class=\"contents-value\">{{grid.appScope.getResult(rowRenderIndex, colRenderIndex, row)}}</span>" +
                    "</div>";
                return cellTemplate;
            };

            var CSS_BASE = 'decision-table-display';
            $scope.getInputClass = function (rowIndex, colIndex) {
                var inputExpressionIndex = colIndex;
                var ruleExecutions = $scope.auditTrail.ruleExecutions;
                if (ruleExecutions) {
                    if (ruleExecutions[rowIndex] && ruleExecutions[rowIndex].conditionResults && inputExpressionIndex < ruleExecutions[rowIndex].conditionResults.length) {
                        var cellResult = ruleExecutions[rowIndex].conditionResults[inputExpressionIndex];
                        if (cellResult.exception) {
                            return CSS_BASE + '-cell-exception';
                        }
                        else {
                            return CSS_BASE + (cellResult.result ? '-cell-match' : '-cell-nomatch');
                        }
                    }
                }
                return CSS_BASE + '-cell-notexecuted';
            };

            $scope.getInputTooltip = function (rowIndex, colIndex, expression) {
                var inputExpressionIndex = colIndex;
                var ruleExecutions = $scope.auditTrail.ruleExecutions;
                if (ruleExecutions) {
                    if (ruleExecutions[rowIndex] && ruleExecutions[rowIndex].conditionResults && inputExpressionIndex < ruleExecutions[rowIndex].conditionResults.length) {
                        var cellResult = ruleExecutions[rowIndex].conditionResults[inputExpressionIndex];
                        if (cellResult.exception) {
                            return $translate.instant("DECISION-AUDIT.TABLE.TOOLTIP.INPUT-EXCEPTION", {error: cellResult.exception});
                        }
                        else {
                            if (cellResult.result) {
                                if (expression && expression.trim().length > 0) {
                                    return $translate.instant("DECISION-AUDIT.TABLE.TOOLTIP.INPUT-MATCH");
                                }
                                else {
                                    return $translate.instant("DECISION-AUDIT.TABLE.TOOLTIP.INPUT-EMPTYEXPRESSION", {expression: expression});
                                }
                            }
                            else {
                                return $translate.instant("DECISION-AUDIT.TABLE.TOOLTIP.INPUT-NOMATCH");
                            }
                        }
                    }
                }
                return $translate.instant("DECISION-AUDIT.TABLE.TOOLTIP.INPUT-NOTEXECUTED");
            };

            $scope.getOutputClass = function (rowIndex, colIndex) {
                var outputExpressionIndex = colIndex - $scope.model.decisionTableDefinition.inputExpressions.length;
                var ruleExecutions = $scope.auditTrail.ruleExecutions;
                if (ruleExecutions) {
                    if (ruleExecutions[rowIndex] && ruleExecutions[rowIndex].conclusionResults && outputExpressionIndex < ruleExecutions[rowIndex].conclusionResults.length) {
                        var cellResult = ruleExecutions[rowIndex].conclusionResults[outputExpressionIndex];
                        if (cellResult.exception) {
                            return CSS_BASE + '-cell-exception';
                        }
                        else {
                            return CSS_BASE + (cellResult.hasOwnProperty('result') ? '-cell-match' : '-cell-nomatch');
                        }
                    }
                }
                return CSS_BASE + '-cell-notexecuted';
            };

            $scope.getOutputTooltip = function (rowIndex, colIndex, expression) {
                var outputExpressionIndex = colIndex - $scope.model.decisionTableDefinition.inputExpressions.length;
                var ruleExecutions = $scope.auditTrail.ruleExecutions;
                if (ruleExecutions) {
                    if (ruleExecutions[rowIndex] && ruleExecutions[rowIndex].conclusionResults && outputExpressionIndex < ruleExecutions[rowIndex].conclusionResults.length) {
                        var cellResult = ruleExecutions[rowIndex].conclusionResults[outputExpressionIndex];
                        if (cellResult.exception) {
                            return $translate.instant("DECISION-AUDIT.TABLE.TOOLTIP.OUTPUT-EXCEPTION", {expression: expression, error: cellResult.exception});
                        }
                        else {
                            return $translate.instant("DECISION-AUDIT.TABLE.TOOLTIP.OUTPUT-RESULT", {expression: expression});
                        }
                    }
                }
                return $translate.instant("DECISION-AUDIT.TABLE.TOOLTIP.OUTPUT-NOTEXECUTED", {expression: expression});
            };

            $scope.getResult = function (rowIndex, colIndex) {
                var outputExpressionIndex = colIndex - $scope.model.decisionTableDefinition.inputExpressions.length;
                var ruleExecutions = $scope.auditTrail.ruleExecutions;
                if (ruleExecutions) {
                    if (ruleExecutions[rowIndex] && ruleExecutions[rowIndex].conclusionResults && outputExpressionIndex < ruleExecutions[rowIndex].conclusionResults.length) {
                        var cellResult = ruleExecutions[rowIndex].conclusionResults[outputExpressionIndex];
                        if (!cellResult.exception && cellResult.hasOwnProperty('result')) {
                            var name = $scope.model.columnVariableIdMap[colIndex];
                            var type = $scope.model.columnVariableTypeVariableIdMap[name];
                            var value = $scope.formatValue(cellResult.result, type);
                            return value; //no need to escape since ui-grid will do that before outputting it
                        }
                    }
                }
                return '';
            };

            // create UI grid column definitions based on input / output expression
            $scope.getColumnDefinitions = function (decisionTableDefinition) {

                if (!decisionTableDefinition) {
                    return;
                }

                var newColumnDefs = [];

                // input expression column defs
                if (decisionTableDefinition.inputExpressions && decisionTableDefinition.inputExpressions.length > 0) {

                    decisionTableDefinition.inputExpressions.forEach(function (inputExpression, i) {

                        newColumnDefs.push({
                            name: inputExpression.id,
                            displayName: inputExpression.label ? inputExpression.label : "",
                            field: inputExpression.id,
                            type: 'string',
                            headerCellClass: 'header-expression header-input-expression' + (i == (decisionTableDefinition.inputExpressions.length - 1) ? ' header-input-expression-last' : ''),
                            headerCellTemplate: _getHeaderInputExpressionCellTemplate(),
                            cellClass: 'cell-expression cell-input-expression' + (i == 0 ? ' cell-input-expression-first' : ''),
                            cellTemplate: _getInputCellTemplate(),
                            enableHiding: false,
                            enableCellEditOnFocus: false
                        });
                    });
                }

                // output expression column defs
                if (decisionTableDefinition.outputExpressions && decisionTableDefinition.outputExpressions.length > 0) {

                    decisionTableDefinition.outputExpressions.forEach(function (outputExpression, i) {

                        newColumnDefs.push({
                            name: outputExpression.id,
                            displayName: outputExpression.label ? outputExpression.label : "",
                            field: outputExpression.id,
                            type: 'string',
                            headerCellTemplate: _getHeaderOutputExpressionCellTemplate(),
                            headerCellClass: 'header-expression header-output-expression' + (i == 0 ? ' header-output-expression-first' : ''),
                            cellClass: 'cell-expression cell-output-expression' + (i == 0 ? ' cell-output-expression-first' : ''),
                            cellTemplate: _getOutputCellTemplate(),
                            enableHiding: false,
                            enableCellEditOnFocus: false
                        });
                    });
                }

                // merge models
                if ($scope.model.columnDefs) {
                    $scope.model.columnDefs.length = 0;
                }

                else {
                    $scope.model.columnDefs = [];
                }
                Array.prototype.push.apply($scope.model.columnDefs, newColumnDefs);

                $scope.gridApi.core.notifyDataChange(uiGridConstants.dataChange.ALL);
            };

            $scope.$watch('id', function (newId, oldId) {
                $scope.getDecisionTableInfo();
            });

        }]);