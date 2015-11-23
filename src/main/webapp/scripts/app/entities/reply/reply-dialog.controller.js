'use strict';

angular.module('expperApp').controller('ReplyDialogController',
    ['$scope', '$stateParams', '$modalInstance', 'entity', 'Reply', 'User', 'Post',
        function($scope, $stateParams, $modalInstance, entity, Reply, User, Post) {

        $scope.reply = entity;
        $scope.users = User.query();
        $scope.posts = Post.query();
        $scope.replys = Reply.query();
        $scope.load = function(id) {
            Reply.get({id : id}, function(result) {
                $scope.reply = result;
            });
        };

        var onSaveFinished = function (result) {
            $scope.$emit('expperApp:replyUpdate', result);
            $modalInstance.close(result);
        };

        $scope.save = function () {
            if ($scope.reply.id != null) {
                Reply.update($scope.reply, onSaveFinished);
            } else {
                Reply.save($scope.reply, onSaveFinished);
            }
        };

        $scope.clear = function() {
            $modalInstance.dismiss('cancel');
        };
}]);
