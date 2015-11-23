'use strict';

angular.module('expperApp')
    .factory('Tag', function ($resource, DateUtils) {
                 return $resource('/api/admin/tags/:id', {}, {
                     'query': { method: 'GET', isArray: true},
                     'get': {
                         method: 'GET',
                         transformResponse: function (data) {
                             data = angular.fromJson(data);
                             return data;
                         }
                     },
                     'update': { method:'PUT' }
                 });
             });
