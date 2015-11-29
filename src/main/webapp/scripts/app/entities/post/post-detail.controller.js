'use strict';

angular.module('expperApp')
    .controller('PostDetailController', function ($scope, $rootScope, $stateParams, $timeout, entity, Post, User) {
        $scope.post = entity;
        $scope.load = function (id) {
            Post.get({id: id}, function(result) {
                $scope.post = result;
            });
        };
        $rootScope.$on('expperApp:postUpdate', function(event, result) {
            $scope.post = result;
        });

        $rootScope.$$postDigest(function(){
            $timeout($scope.highlightCode, 100);
        });

        $scope.highlightCode = function(){
          $('.content pre').each(function(i, block) {
            hljs.highlightBlock(block);
          });

          $scope.$apply();
        };
    })
    .filter('unsafe', function($sce) { return $sce.trustAsHtml; });
