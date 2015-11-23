'use strict';

angular.module('expperApp')
    .factory('Register', function ($resource) {
        return $resource('/api/register', {}, {
        });
    });


