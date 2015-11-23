'use strict';

angular.module('expperApp')
  .factory('Reply', function ($resource) {
    return $resource('/api/posts/:post_id/replies/:id', {post_id: '@post_id', id: '@id'}, {
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
