'use strict';

angular.module('expperApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('reply', {
                parent: 'entity',
                url: '/replys',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Replys'
                },
                views: {
                    'content@': {
                        templateUrl: '/scripts/app/entities/reply/replys.html',
                        controller: 'ReplyController'
                    }
                },
                resolve: {
                }
            })
            .state('reply.detail', {
                parent: 'entity',
                url: '/reply/{id}',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Reply'
                },
                views: {
                    'content@': {
                        templateUrl: '/scripts/app/entities/reply/reply-detail.html',
                        controller: 'ReplyDetailController'
                    }
                },
                resolve: {
                    entity: ['$stateParams', 'Reply', function($stateParams, Reply) {
                        return Reply.get({id : $stateParams.id});
                    }]
                }
            })
            .state('reply.new', {
                parent: 'reply',
                url: '/new',
                data: {
                    authorities: ['ROLE_USER']
                },
                onEnter: ['$stateParams', '$state', '$modal', function($stateParams, $state, $modal) {
                    $modal.open({
                        templateUrl: '/scripts/app/entities/reply/reply-dialog.html',
                        controller: 'ReplyDialogController',
                        size: 'lg',
                        resolve: {
                            entity: function () {
                                return {
                                    content: null,
                                    status: null,
                                    created_at: null,
                                    id: null
                                };
                            }
                        }
                    }).result.then(function(result) {
                        $state.go('reply', null, { reload: true });
                    }, function() {
                        $state.go('reply');
                    })
                }]
            })
            .state('reply.edit', {
                parent: 'reply',
                url: '/{id}/edit',
                data: {
                    authorities: ['ROLE_USER']
                },
                onEnter: ['$stateParams', '$state', '$modal', function($stateParams, $state, $modal) {
                    $modal.open({
                        templateUrl: '/scripts/app/entities/reply/reply-dialog.html',
                        controller: 'ReplyDialogController',
                        size: 'lg',
                        resolve: {
                            entity: ['Reply', function(Reply) {
                                return Reply.get({id : $stateParams.id});
                            }]
                        }
                    }).result.then(function(result) {
                        $state.go('reply', null, { reload: true });
                    }, function() {
                        $state.go('^');
                    })
                }]
            });
    });
