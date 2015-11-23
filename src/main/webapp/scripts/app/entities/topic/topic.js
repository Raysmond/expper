'use strict';

angular.module('expperApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('topic', {
                parent: 'entity',
                url: '/topics',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Topics'
                },
                views: {
                    'content@': {
                        templateUrl: '/scripts/app/entities/topic/topics.html',
                        controller: 'TopicController'
                    }
                },
                resolve: {
                }
            })
            .state('topic.detail', {
                parent: 'entity',
                url: '/topic/{id}',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Topic'
                },
                views: {
                    'content@': {
                        templateUrl: '/scripts/app/entities/topic/topic-detail.html',
                        controller: 'TopicDetailController'
                    }
                },
                resolve: {
                    entity: ['$stateParams', 'Topic', function($stateParams, Topic) {
                        return Topic.get({id : $stateParams.id});
                    }]
                }
            })
            .state('topic.new', {
                parent: 'topic',
                url: '/new',
                data: {
                    authorities: ['ROLE_USER'],
                },
                onEnter: ['$stateParams', '$state', '$modal', function($stateParams, $state, $modal) {
                    $modal.open({
                        templateUrl: '/scripts/app/entities/topic/topic-dialog.html',
                        controller: 'TopicDialogController',
                        size: 'lg',
                        resolve: {
                            entity: function () {
                                return {
                                    name: null,
                                    friendlyName: null,
                                    weight: 0,
                                    description: null,
                                    id: null
                                };
                            }
                        }
                    }).result.then(function(result) {
                        $state.go('topic', null, { reload: true });
                    }, function() {
                        $state.go('topic');
                    })
                }]
            })
            .state('topic.edit', {
                parent: 'topic',
                url: '/{id}/edit',
                data: {
                    authorities: ['ROLE_USER']
                },
                onEnter: ['$stateParams', '$state', '$modal', function($stateParams, $state, $modal) {
                    $modal.open({
                        templateUrl: '/scripts/app/entities/topic/topic-dialog.html',
                        controller: 'TopicDialogController',
                        size: 'lg',
                        resolve: {
                            //entity: ['Topic', function(Topic) {
                            //    return Topic.get({id : $stateParams.id});
                            //}]
                            entity: ['Topic', function(Topic){
                                return {};
                            }]
                        }
                    }).result.then(function(result) {
                        $state.go('topic', null, { reload: true });
                    }, function() {
                        $state.go('^');
                    })
                }]
            });
    });
