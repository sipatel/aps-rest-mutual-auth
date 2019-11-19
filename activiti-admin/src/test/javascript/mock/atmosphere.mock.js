/*
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
atmosphere = {
    AtmosphereRequest: function () {
        return {
            onError : function(response) {
            },
            onClose : function(response) {
            },
            onOpen : function(response) {
            },
            onMessage : function(response) {
            },
            onReconnect : function(request, response) {
            },
            onMessagePublished : function(response) {
            },
            onTransportFailure : function (reason, request) {
            },
            onLocalMessage : function (response) {
            }
        }
    },
    subscribe: function (request) {
        var _request = request;
        return {
            push: function(data){
                _request.onMessage({responseBody:data});
            },
            request: function() {
                return {
                    isOpen: function() {
                        return false;
                    }
                }
            }
        }
    }
}