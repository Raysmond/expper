'use strict';

angular.module('expperApp').controller('ReplyController',
  function($scope, Reply) {
    $scope.replies = [];
    $scope.page = 0;
    $scope.size = 0;
    $scope.pages = 0;
    $scope.postId = getPostId();
    $scope.showLoadMore = false;
    $scope.reply = {
      post_id: $scope.postId,
      content: ''
    };

    function getPostId() {
      var params = document.location.pathname.split("/");
      return params[params.length - 1];
    };


    $scope.initEditor = function(){
        $(function(){
            $scope.editor = new Simditor({
                textarea: $('#reply-content'),
                // markdown: true,
                toolbar: ['bold', 'italic', 'underline', '|', 'ol', 'ul', 'blockquote', 'code', '|', 'link', 'image', 'hr', '|', 'indent', 'outdent', '|', 'markdown']
            });

            setInterval(function(){
                $scope.reply.content = $scope.editor.getValue();
                if($scope.reply.content.length - 7 >= 3){
                    $('.alert-size').hide();
                }
            }, 1000);
        });
    };

    $scope.initEditor();

    // load all post replies by page
    $scope.loadAll = function() {
      Reply.query({
          post_id: $scope.postId,
          page: $scope.page,
          size: 50,
          id: 'all'
        },
        function(result, headers) {
          for (var i = 0; i < result.length; i++) {
            $scope.replies.push(result[i]);
          }
          $scope.size = headers('x-total-count');
          $scope.pages = parseInt($scope.size / 50) + ($scope.size % 50 ==
            0 ? 0 :
            1);
          $scope.showLoadMore = $scope.page < $scope.pages - 1;
        });
    };

    $scope.reset = function() {
      $scope.page = 0;
      $scope.replies = [];
      $scope.loadAll();
    };

    $scope.loadPage = function(page) {
      $scope.page = page;
      $scope.loadAll();
    };
    $scope.loadAll();

    $scope.loadMore = function() {
      $scope.page++;
      $scope.loadAll();
      $scope.showLoadMore = $scope.page < $scope.pages - 1;
    };

    $scope.delete = function(id) {
      Reply.get({
          post_id: $scope.postId,
          id: id
        },
        function(result) {
          $scope.reply = result;
          $('#deleteReplyConfirmation').modal('show');
        });
    };

    $scope.confirmDelete = function(id) {
      Reply.delete({
          post_id: $scope.postId,
          id: id
        },
        function() {
          $scope.reset();
          $('#deleteReplyConfirmation').modal('hide');
          $scope.clear();
        });
    };

    var onSaveFinished = function(result) {
      // $scope.$emit('expperApp:replyUpdate', result);
      //$modalInstance.close(result);
      $scope.replies.push(result);
      $scope.clear();
    };

    var onErrorResponse = function(result) {
      if (result.status == 401) {
        window.location.href = "/me#/login";
      }
    };

    $scope.save = function() {
      $scope.reply.content = $scope.editor.getValue();
      if($scope.reply.content.length - 7 < 3){
          $('.alert-size').show();
          return;
      }
      if ($scope.reply.id != null) {
        Reply.update($scope.reply, onSaveFinished, onErrorResponse);
      } else {
        Reply.save($scope.reply, onSaveFinished, onErrorResponse);
      }
    };

    $scope.replyTo = function(reply) {
      $scope.reply.content  = $scope.editor.getValue();

      if ($scope.reply.content == undefined) {
        $scope.reply.content = '';
      }

      $scope.reply.reply_to_id = reply.id;

      if($scope.reply.content.endsWith('</p>')){
          $scope.reply.content = $scope.reply.content.substr(0, $scope.reply.content.length - 4) + " @" + reply.username + ' </p>';
      } else {
          $scope.reply.content += " @" + reply.username + ' ';
      }

      $scope.editor.setValue($scope.reply.content);
    };

    $scope.refresh = function() {
      $scope.reset();
      $scope.clear();
    };

    $scope.clear = function() {
      $scope.reply = {
        post_id: $scope.postId,
        content: ''
      };
    };
  }).filter('unsafe', function($sce) { return $sce.trustAsHtml; });
