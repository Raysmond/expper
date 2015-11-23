'use strict';

angular.module('expperApp').controller('PostDialogController', ['$scope',
  '$stateParams','$location', '$modalInstance', 'entity', 'Post',
  function($scope, $stateParams, $location, $modalInstance, entity, Post) {

    $scope.post = entity;

    if($location.search().url != undefined){
        $scope.post.url = $location.search().url;
        $scope.post.status = 'PRIVATE';
    }

    $scope.initTagInput = function() {
      $("#tag-input").tokenInput(
        '/api/tags/search', {
          theme: "bootstrap",
          preventDuplicates: true,
          tokenLimit: 10,
          hintText: null,
          animateDropdown: false,
          noResultsText: '没有找到相关标签',
          searchingText: '搜索中...',
          allowFreeTagging: true,
          queryParam: 'name',
          propertyToSearch: 'friendly_name',
          zindex: 999999,
          placeholder: '按回车输入一个标签'
        });

      for (var i = 0; i < $scope.post.tags.length; i++) {
        var tag = $scope.post.tags[i];
        $('#tag-input').tokenInput('add', tag);
      }
    };

    $scope.load = function(id) {
      if (id == undefined) {
        return;
      }
      Post.get({
        id: id
      }, function(result) {
        $scope.post = result;
        $scope.initTagInput();
      });
    };

    $scope.load($stateParams.id);

    var onSaveFinished = function(result) {
      $scope.$emit('expperApp:postUpdate', result);
      $modalInstance.close(result);
    };

    $scope.save = function() {
      $scope.post.tags = $scope.getTagInput();

      if ($scope.post.id != null) {
        Post.update($scope.post, onSaveFinished);
      } else {
        Post.save($scope.post, onSaveFinished);
      }
    };

    $scope.clear = function() {
      $modalInstance.dismiss('cancel');
    };
    $scope.getTagInput = function() {
      var tags = $('#tag-input').tokenInput('get');
      for (var i = 0; i < tags.length; i++) {
        if (!Number.isInteger(tags[i].id)) {
          tags[i].id = 0;
        }
      }

      return tags;
    };

  }
]);
