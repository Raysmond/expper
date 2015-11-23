'use strict';

angular.module('expperApp')
    .controller('TopicDetailController', function ($scope, $rootScope, $stateParams, entity, Topic, Tag) {
        $scope.topic = entity;
        $scope.load = function (id) {
            Topic.get({id: id}, function(result) {
                $scope.topic = result;
            });
        };
        var unsubscribe = $rootScope.$on('expperApp:topicUpdate', function(event, result) {
            $scope.topic = result;
        });
        $scope.$on('$destroy', unsubscribe);

    });
