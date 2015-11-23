'use strict';

angular.module('expperApp')
    .controller('TopicController', function ($scope, Topic, ParseLinks) {
        $scope.topics = [];
        $scope.page = 0;
        $scope.loadAll = function() {
            Topic.query({page: $scope.page, size: 20}, function(result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.topics = result;
            });
        };
        $scope.loadPage = function(page) {
            $scope.page = page;
            $scope.loadAll();
        };
        $scope.loadAll();

        $scope.delete = function (id) {
            Topic.get({id: id}, function(result) {
                $scope.topic = result;
                $('#deleteTopicConfirmation').modal('show');
            });
        };

        $scope.confirmDelete = function (id) {
            Topic.delete({id: id},
                function () {
                    $scope.loadAll();
                    $('#deleteTopicConfirmation').modal('hide');
                    $scope.clear();
                });
        };

        $scope.refresh = function () {
            $scope.loadAll();
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.topic = {
                name: null,
                friendlyName: null,
                createdAt: null,
                weight: null,
                description: null,
                id: null
            };
        };
    });
