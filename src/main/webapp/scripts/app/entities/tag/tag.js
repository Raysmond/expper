'use strict';

angular.module('expperApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('tag', {
                parent: 'entity',
                url: '/tags',
                data: {
                    authorities: ['ROLE_ADMIN'],
                    pageTitle: 'Tags'
                },
                views: {
                    'content@': {
                        templateUrl: '/scripts/app/entities/tag/tags.html',
                        controller: 'TagController'
                    }
                },
                resolve: {
                }
            })
            .state('tag.detail', {
                parent: 'entity',
                url: '/tag/{id}',
                data: {
                    authorities: ['ROLE_ADMIN'],
                    pageTitle: 'Tag'
                },
                views: {
                    'content@': {
                        templateUrl: '/scripts/app/entities/tag/tag-detail.html',
                        controller: 'TagDetailController'
                    }
                },
                resolve: {
                    entity: ['$stateParams', 'Tag', function($stateParams, Tag) {
                        return Tag.get({id : $stateParams.id});
                    }]
                }
            })
            .state('tag.new', {
                parent: 'tag',
                url: '/new',
                data: {
                    authorities: ['ROLE_ADMIN'],
                },
                onEnter: ['$stateParams', '$state', '$modal', function($stateParams, $state, $modal) {
                    $modal.open({
                        templateUrl: '/scripts/app/entities/tag/tag-dialog.html',
                        controller: 'TagDialogController',
                        size: 'lg',
                        resolve: {
                            entity: function () {
                                return {
                                    name: null,
                                    friendly_name: null,
                                    id: null
                                };
                            }
                        }
                    }).result.then(function(result) {
                        $state.go('tag', null, { reload: true });
                    }, function() {
                        $state.go('tag');
                    })
                }]
            })
            .state('tag.edit', {
                parent: 'tag',
                url: '/{id}/edit',
                data: {
                    authorities: ['ROLE_ADMIN']
                },
                onEnter: ['$stateParams', '$state', '$modal', function($stateParams, $state, $modal) {
                    $modal.open({
                        templateUrl: '/scripts/app/entities/tag/tag-dialog.html',
                        controller: 'TagDialogController',
                        size: 'lg',
                        resolve: {
                            entity: ['Tag', function(Tag) {
                                return Tag.get({id : $stateParams.id});
                            }]
                        }
                    }).result.then(function(result) {
                        $state.go('tag', null, { reload: true });
                    }, function() {
                        $state.go('^');
                    })
                }]
            });
    });
