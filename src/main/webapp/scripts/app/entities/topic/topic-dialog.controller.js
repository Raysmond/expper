'use strict';

angular.module('expperApp').controller('TopicDialogController',
    ['$scope', '$stateParams', '$modalInstance', 'entity', 'Topic', 'Tag',
        function($scope, $stateParams, $modalInstance, entity, Topic, Tag) {

        $scope.topic = {};
        $scope.tags = [];

        $scope.load = function(id) {
            Topic.get({id : id}, function(result) {
                $scope.topic = result;
                Tag.query({page:0, size:100000}, function(res){
                    $scope.tags = res;
                    $scope.tags.forEach(function(tag){
                        var selected = false;
                        for(var i=0;i<$scope.topic.tags.length;i++){
                            if($scope.topic.tags[i].id == tag.id){
                                selected = true;
                                break;
                            }
                        }

                        tag.ticked = selected;
                    });
                });
            });
        };

        $scope.load($stateParams.id);

        var onSaveFinished = function (result) {
            $scope.$emit('expperApp:topicUpdate', result);
            $modalInstance.close(result);
        };

        $scope.save = function () {
            $scope.topic.tags = $scope.selectedTags;

            if ($scope.topic.id != null) {
                Topic.update($scope.topic, onSaveFinished);
            } else {
                Topic.save($scope.topic, onSaveFinished);
            }
        };

        $scope.clear = function() {
            $modalInstance.dismiss('cancel');
        };
}]);
