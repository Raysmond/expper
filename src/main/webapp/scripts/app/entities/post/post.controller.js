'use strict';

angular.module('expperApp')
  .controller('PostController', function($scope, Post, PostTag, ParseLinks) {
    $scope.posts = [];
    $scope.tags = [];
    $scope.filterTag = {};
    $scope.page = 0;
    $scope.searchQuery = '';
    var PAGE_SIZE = 40;

    $scope.loadAll = function() {
      Post.query({
        page: $scope.page,
        size: PAGE_SIZE,
        keywords: $scope.searchQuery
      }, function(result, headers) {
        $scope.links = ParseLinks.parse(headers('link'));
        $scope.posts = result;
      });

      PostTag.query({}, function(result, headers) {
        $scope.tags = result;
      });
    };
    $scope.loadPage = function(page) {
      $scope.page = page;
      $scope.loadAll();
    };

    $scope.loadAll();

    $scope.loadAllByTag = function(tag) {
      if (tag == $scope.filterTag) {
        $scope.filterTag = {};
        $scope.loadAll();
      } else {
        $scope.filterTag = tag;
        PostTag.query({
          id: tag.id,
          page: $scope.page,
          size: PAGE_SIZE
        }, function(result, headers) {
          $scope.links = ParseLinks.parse(headers('link'));
          $scope.posts = result;
        });
      }
    };

    $scope.delete = function(id) {
      Post.get({
        id: id
      }, function(result) {
        $scope.post = result;
        $('#deletePostConfirmation').modal('show');
      });
    };

    $scope.confirmDelete = function(id) {
      Post.delete({
          id: id
        },
        function() {
          $scope.loadAll();
          $('#deletePostConfirmation').modal('hide');
          $scope.clear();
        });
    };

    $scope.refresh = function() {
      $scope.loadAll();
      $scope.clear();
    };

    $scope.clear = function() {
      $scope.post = {
        title: null,
        url: null,
        domain: null,
        author: null,
        summary: null,
        content: null,
        status: null,
        id: null
      };
    };
  }).directive('searchQuery', function() {
    return function(scope, element, attrs) {
      element.bind("keydown keypress", function(event) {
        if (event.which === 13) {
          scope.refresh();
          event.preventDefault();
        }
      });
    };
  });
