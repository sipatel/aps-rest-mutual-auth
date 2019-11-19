/*
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
(function() {'use strict';

/* activitiIdentity module 
 * 
 * Provides functionalities related to Activit identity (users & groups)
 * 
 * */

angular.module('activitiIdentity', [])

/**
 * identityService
 * 
 * Holds all servers identities data and provides functions
 * for retrieving data of currently active server
 */
.service('identityService', function(serverIdentity) {
    var servers = {}, currentServer = {};

    this.setAvailableServers = function (availableServers) {
        for (var i = 0; i < availableServers.length; i++) {
            servers[availableServers[i].id] = createServerIdentity(availableServers[i]);
        }
    };

    this.setActiveServer = function (activeServer) {
        if (!servers[activeServer.id]) {
            servers[activeServer.id] = createServerIdentity(activeServer);
        }
        currentServer = servers[activeServer.id];
        currentServer.load();
    };

    this.getUser = function (userId) {
        return currentServer.getUser(userId);
    };

    this.getGroup = function (groupId) {
        return currentServer.getGroup(groupId);
    };

    this.filterUsers = function (params) {
        return currentServer.filterUsers(params);
    };

    function createServerIdentity (serverIdentityData) {
        var newServerIdentity = angular.extend({}, serverIdentity);
        newServerIdentity.id = serverIdentityData.id;
        return newServerIdentity;
    };

})

/** Model for fetching and holding server specific identities */
.factory('serverIdentity', function ($http, $q){

    function fetchUsers(serverId, params){
        var defer = $q.defer();

        $http({method: 'GET', url: '/app/rest/activiti/users', params:extendParams({serverId: serverId, summary: 'true'}, params)}).
        success(function(data, status, headers, config) {
            defer.resolve(data.data);
        }).
        error(function(data, status, headers, config) {
            defer.reject(data);
        });

        return defer.promise;
    };

    function fetchUser(userId, serverId, params){
        var defer = $q.defer();

        $http({method: 'GET', url: '/app/rest/activiti/users/'+userId, params:extendParams({serverId: serverId, summary: 'true'}, params)}).
        success(function(data, status, headers, config) {
            defer.resolve(data);
        }).
        error(function(data, status, headers, config) {
            defer.reject(data);
        });

        return defer.promise;
    };

    function fetchGroups(serverId, params){
        var defer = $q.defer();

        $http({method: 'GET', url: '/app/rest/activiti/groups', params:extendParams({serverId: serverId, summary: 'true', functional: 'true'}, params)}).
        success(function(data, status, headers, config) {
            defer.resolve(data);//results are returned as an array
        }).
        error(function(data, status, headers, config) {
            defer.reject(data);
        });

        return defer.promise;
    };

    function fetchGroup(groupId, serverId, params){
        var defer = $q.defer();

        $http({method: 'GET', url: '/app/rest/activiti/groups/'+groupId, params:extendParams({serverId: serverId, summary: 'true'}, params)}).
        success(function(data, status, headers, config) {
            defer.resolve(data);
        }).
        error(function(data, status, headers, config) {
            defer.reject(data);
        });

        return defer.promise;
    };

    function extendParams(params, extendedParams){
        if (extendedParams) {
            return angular.extend(params, extendedParams);
        }
        return params;
    };

    function setUserName(user){
        if(user.firstName && user.lastName) {
            user.name = user.firstName + ' ' + user.lastName;
        } else if(!user.firstName && !user.lastName) {
            user.name = user.id;
        } else {
            user.name = user.firstName || user.lastName;
        }

    };

    function fakePromise(successCallback){
        successCallback(this);
    };

    return {
        loaded: false,
        users: {},
        groups: {},
        loadUsers: function (users){
            for (var i = 0; i < users.length; i++) {
                setUserName (users[i]);
                users[i].then = fakePromise;
                this.users[users[i].id] = users[i];
            }
        },
        loadGroups: function (groups){
            for (var i = 0; i < groups.length; i++) {
                groups[i].then = fakePromise
                this.groups[groups[i].id] = groups[i];
            }
        },
        getUser: function (userId){
            if (!this.users[userId]) {
                var deferred = $q.defer();
                fetchUser (userId, this.id).then(
                        function (user){
                            setUserName(user);
                            deferred.resolve(user);
                        },
                        function () {
                            deferred.resolve({id:userId, name:userId+''});
                        }
                );
                this.users[userId] = deferred.promise;
            }
            return this.users[userId];
        },
        getGroup: function (groupId){
            if (!this.groups[groupId]) {
                var deferred = $q.defer();
                fetchGroup (groupId, this.id).then(
                        function (group){
                            deferred.resolve(group);
                        },
                        function () {
                            deferred.resolve({id:groupId, name:groupId+''});
                        }
                );
                this.groups[groupId] = deferred.promise;
            }
            return this.groups[groupId];
        },	
        load: function (){
            if(!this.loaded){
                fetchUsers(this.id).then(angular.bind(this, this.loadUsers));
                fetchGroups(this.id).then(angular.bind(this, this.loadGroups));
                this.loaded = true;
            }
        },
        filterUsers: function (params){
            var filteredUsers = fetchUsers(this.id, params);
            filteredUsers.then(angular.bind(this, this.loadUsers));
            return filteredUsers;
        }
    };
})

//directives

/** Resolves user name */
.directive('username', function(identityService) {
    return {link: function(scope, element, attrs) {
        scope.$watch(attrs.username, function (userId) {
            if(userId){
                element.text(userId); // set text to userId for now
                identityService.getUser(userId).then(
                        function(user){
                            element.text(user.name);
                        });
            } else {
                element.text(''); // clear previous value if any
            }
        });
    }};
})

/** Resolves group name */
.directive('groupname', function(identityService) {
    return {link: function(scope, element, attrs) {
        scope.$watch(attrs.groupname, function (groupId) {
            if(groupId){
                element.text(groupId); // set text to groupId for now
                identityService.getGroup(groupId).then(
                        function(group){
                            element.text(group.name);
                        });
            } else {
                element.text(''); // clear previous value if any
            }
        });
    }};
})

/** Used with typeahead to filter users
 *  Built and configured by usersTypeahead directive
 *  Not meant to be used directly */
.directive('users', function(identityService, $parse) {
    return {link: function(scope, element, attrs) {

        if (attrs.userOnClear) {
            var onClearCallback = $parse(attrs.userOnClear);
            scope.$watch(attrs.ngModel, function (newValue, oldValue) {
                if(newValue == '') {
                    onClearCallback(scope);
                }
            });
        }
        scope.filterUsers =  function (filterUsers){
            var params = {filter: filterUsers, page: 0, sort: 'emailAsc'};
            if (attrs.userResultLimit) {
                params.size = attrs.userResultLimit;
            } else {
                params.size = 50;
            }

            if (attrs.userStatus) {
                params.status = attrs.userStatus;
            }

            return identityService.filterUsers(params);
        };

    }};
})

/** Builds typeahead and users directives for filtering users */

.directive('usersTypeahead', function(identityService, $compile) {

    function createTypeaheadElement(scope, element, attrs, user){
        if (user) {
            element.attr('ng-init', attrs.userDisplayModel+' = {id:'+user.id+', name:"'+user.name+'"}');
        }
        element.attr('ng-model', attrs.userDisplayModel);
        element.attr('users', '');
        element.attr('user-on-clear', attrs.userModel+'=null;' + ((attrs.userOnChange) ? attrs.userOnChange : ''));
        element.attr('typeahead', 'user as user.name for user in filterUsers($viewValue)');
        element.attr('typeahead-editable', 'false');
        element.attr('typeahead-on-select', attrs.userModel+'=$item.id;' + ((attrs.userOnChange) ? attrs.userOnChange : ''));
        element.attr('typeahead-wait-ms', (attrs.delayMs)? attrs.delayMs : '200');
        element.attr('typeahead-template-url', 'userItemTemplate.html');
        $compile(element)(scope);
    };

    return{
        restrict: 'A',
        terminal: true,
        priority: 100000,
        link: function(scope, element, attrs) {
            
            element.removeAttr('users-typeahead'); 
            
            // if a userId is set then load the user first
            var userId = scope.$eval(attrs.userModel);
            if (userId) {
                identityService.getUser(userId).then(function(user){
                    createTypeaheadElement(scope, element, attrs, user);
                },function(){
                    createTypeaheadElement(scope, element, attrs);
                });
            }else {
                createTypeaheadElement(scope, element, attrs);
            }

        }};
})

.run(function($rootScope, identityService) {
    
    $rootScope.$watch('availableServers', function(newValue, oldValue) {
        if (newValue) {
            identityService.setAvailableServers(newValue);
        }
    });

    $rootScope.$watch('activeServer', function(newValue, oldValue) {
        if (newValue) {
            identityService.setActiveServer(newValue);
        }
    });
})
})();
