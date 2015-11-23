'use strict';

angular.module('expperApp').controller('TagDialogController',
    ['$scope', '$stateParams', '$modalInstance', 'entity',  'Tag',
        function($scope, $stateParams, $modalInstance, entity, Tag) {

        $scope.tag = entity;
        $scope.load = function(id) {
            Tag.get({id : id}, function(result) {
                $scope.tag = result;
            });
        };

        var onSaveFinished = function (result) {
            $scope.$emit('expperApp:tagUpdate', result);
            $modalInstance.close(result);
        };

        $scope.save = function () {
            if ($scope.tag.id != null) {
                Tag.update($scope.tag, onSaveFinished);
            } else {
                Tag.save($scope.tag, onSaveFinished);
            }
        };

        $scope.clear = function() {
            $modalInstance.dismiss('cancel');
        };
}]);
