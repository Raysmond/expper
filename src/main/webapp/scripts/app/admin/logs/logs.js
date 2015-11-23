'use strict';

angular.module('expperApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('logs', {
                parent: 'admin',
                url: '/logs',
                data: {
                    authorities: ['ROLE_ADMIN'],
                    pageTitle: 'Logs'
                },
                views: {
                    'content@': {
                        templateUrl: '/scripts/app/admin/logs/logs.html',
                        controller: 'LogsController'
                    }
                },
                resolve: {

                }
            });
    });
