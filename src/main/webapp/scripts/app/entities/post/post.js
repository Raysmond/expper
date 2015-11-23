'use strict';

angular.module('expperApp')
  .config(function($stateProvider) {
    $stateProvider
      .state('post', {
        parent: 'entity',
        url: '/posts',
        data: {
          authorities: ['ROLE_USER'],
          pageTitle: 'Posts'
        },
        views: {
          'content@': {
            templateUrl: '/scripts/app/entities/post/posts.html',
            controller: 'PostController'
          }
        },
        resolve: {}
      })
      .state('post.detail', {
        parent: 'entity',
        url: '/post/{id}',
        data: {
          authorities: ['ROLE_USER'],
          pageTitle: 'Post'
        },
        views: {
          'content@': {
            templateUrl: '/scripts/app/entities/post/post-detail.html',
            controller: 'PostDetailController'
          }
        },
        resolve: {
          entity: ['$stateParams', 'Post', function($stateParams, Post) {
            return Post.get({
              id: $stateParams.id
            });
          }]
        }
      })
      .state('post.new', {
        parent: 'post',
        url: '/new',
        data: {
          authorities: ['ROLE_USER']
        },
        onEnter: ['$stateParams', '$state', '$modal', function($stateParams,
          $state, $modal) {
          $modal.open({
            templateUrl: '/scripts/app/entities/post/post-new-dialog.html',
            controller: 'PostDialogController',
            size: 'lg',
            resolve: {
              entity: function() {
                return {
                  title: null,
                  url: null,
                  domain: null,
                  author: null,
                  summary: null,
                  content: null,
                  status: 'PUBLIC',
                  tags: [],
                  id: null
                };
              }
            }
          }).result.then(function(result) {
            $state.go('post', null, {
              reload: true
            });
          }, function() {
            $state.go('post');
          })
        }]
      })
      .state('post.edit', {
        parent: 'post',
        url: '/{id}/edit',
        data: {
          authorities: ['ROLE_USER']
        },
        onEnter: ['$stateParams', '$state', '$modal', function($stateParams,
          $state, $modal) {
          $modal.open({
            templateUrl: '/scripts/app/entities/post/post-dialog.html',
            controller: 'PostDialogController',
            size: 'lg',
            resolve: {
              entity: ['Post', function(Post) {
                // return Post.get({
                //   id: $stateParams.id
                // });
                return {
                  id: $stateParams.id
                };
              }]
            }
          }).result.then(function(result) {
            $state.go('post', null, {
              reload: true
            });
          }, function() {
            $state.go('^');
          })
        }]
      });
  });
