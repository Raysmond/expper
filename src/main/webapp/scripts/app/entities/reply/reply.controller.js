'use strict';

angular.module('expperApp')
    .controller('ReplyController', function ($scope, Reply, ParseLinks) {
        $scope.replys = [];
        $scope.page = 0;
        $scope.loadAll = function() {
            Reply.query({page: $scope.page, size: 20}, function(result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                for (var i = 0; i < result.length; i++) {
                    $scope.replys.push(result[i]);
                }
            });
        };
        $scope.reset = function() {
            $scope.page = 0;
            $scope.replys = [];
            $scope.loadAll();
        };
        $scope.loadPage = function(page) {
            $scope.page = page;
            $scope.loadAll();
        };
        $scope.loadAll();

        $scope.delete = function (id) {
            Reply.get({id: id}, function(result) {
                $scope.reply = result;
                $('#deleteReplyConfirmation').modal('show');
            });
        };

        $scope.confirmDelete = function (id) {
            Reply.delete({id: id},
                function () {
                    $scope.reset();
                    $('#deleteReplyConfirmation').modal('hide');
                    $scope.clear();
                });
        };

        $scope.refresh = function () {
            $scope.reset();
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.reply = {
                content: null,
                status: null,
                created_at: null,
                id: null
            };
        };
    });
