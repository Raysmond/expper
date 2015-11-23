'use strict';

angular.module('expperApp')
    .controller('ReplyDetailController', function ($scope, $rootScope, $stateParams, entity, Reply, User, Post) {
        $scope.reply = entity;
        $scope.load = function (id) {
            Reply.get({id: id}, function(result) {
                $scope.reply = result;
            });
        };
        var unsubscribe = $rootScope.$on('expperApp:replyUpdate', function(event, result) {
            $scope.reply = result;
        });
        $scope.$on('$destroy', unsubscribe);

    });
