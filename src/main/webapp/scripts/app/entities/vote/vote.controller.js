'use strict';

angular.module('expperApp')
    .controller('VoteController', function ($scope, Vote, Post) {

        $scope.delete = function (id) {
            Post.delete({id: id},
                function () {
                    $scope.loadAll();
                    $('#deletePostConfirmation').modal('hide');
                    $scope.clear();
                });
        };
    });
