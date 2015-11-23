'use strict';

angular.module('expperApp')
    .factory('Activate', function ($resource) {
        return $resource('/api/activate', {}, {
            'get': { method: 'GET', params: {}, isArray: false}
        });
    });


