'use strict';

angular.module('expperApp')
    .factory('Account', function Account($resource) {
        return $resource('/api/account', {}, {
            'get': { method: 'GET', params: {}, isArray: false,
                interceptor: {
                    response: function(response) {
                        // expose response
                        return response;
                    }
                }
            }
        });
    });
