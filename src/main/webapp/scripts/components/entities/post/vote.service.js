'use strict';

angular.module('expperApp')
    .factory('Vote', function ($resource, DateUtils) {
        return $resource('/api/posts/:post_id/votes/:vote_id', {}, {
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
