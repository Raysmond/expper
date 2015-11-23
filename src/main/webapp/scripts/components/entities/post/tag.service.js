'use strict';

angular.module('expperApp').factory('PostTag',
  function($resource, DateUtils) {
    return $resource('/api/posts/tags/:id', {}, {
      'query': {
        method: 'GET',
        isArray: true
      },
      'get': {
        method: 'GET',
        transformResponse: function(data) {
          data = angular.fromJson(data);
          return data;
        }
      },
      'update': {
        method: 'PUT'
      }
    });
  });
