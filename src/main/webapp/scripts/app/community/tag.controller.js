'use strict';

angular.module('expperApp').controller('UserTagController',
  function($scope, $http) {
    $scope.follow_tags = {};
    $scope.followers_count = {};

    $scope.followTag = function(id) {
      $http.post('/api/tags/' + id + '/follow').then(function(res) {
          $scope.follow_tags['tag_' + id] = true;
          $scope.isFollowed = true;
          $('.tag-item-' + id + ' .followers-count').text(res.data.followers_count);
        },
        function(res) {
          if (res.status === 401) {
            window.location.href = "/me#/login";
          }
          if (res.status === 500) {
            alert('服务器出错了，请稍后重试');
          }
        });
    };

    $scope.unfollowTag = function(id) {
      $http.post('/api/tags/' + id + '/unfollow').then(function(res) {
          $scope.follow_tags['tag_' + id] = false;
          $scope.isFollowed = false;
          $('.tag-item-' + id + ' .followers-count').text(res.data.followers_count);
        },
        function(res) {
          if (res.status === 401) {
            window.location.href = "/me#/login";
          }
          if (res.status === 500) {
            alert('服务器出错了，请稍后重试');
          }
        });
    };

    $scope.initPager = function(page, totalPages) {
      var options = {
        currentPage: page,
        totalPages: totalPages,
        pageUrl: function(type, page, current) {
          var url = document.location.href;
          var path = document.location.pathname;
          var search = document.location.search;

          if (search == "") {
            return path + "?page=" + page;
          }

          search = search.substr(1); // remove '?'
          var searchs = search.split('&');
          var found = false;
          for (var i = 0; i < searchs.length; i++) {
            if (searchs[i].startsWith('page=')) {
              searchs[i] = 'page=' + page;
              found = true;
              break;
            }
          }

          if (!found) {
            searchs.push('page=' + page);
          }

          return path + '?' + searchs.join('&');
        }
      }

      $('#pagination').bootstrapPaginator(options);
    };
  });
