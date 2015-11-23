'use strict';

angular.module('expperApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('health', {
                parent: 'admin',
                url: '/health',
                data: {
                    authorities: ['ROLE_ADMIN'],
                    pageTitle: 'Health checks'
                },
                views: {
                    'content@': {
                        templateUrl: '/scripts/app/admin/health/health.html',
                        controller: 'HealthController'
                    }
                },
                resolve: {

                }
            });
    });
