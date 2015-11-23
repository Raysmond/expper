'use strict';

angular.module('expperApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('configuration', {
                parent: 'admin',
                url: '/configuration',
                data: {
                    authorities: ['ROLE_ADMIN'],
                    pageTitle: 'Configuration'
                },
                views: {
                    'content@': {
                        templateUrl: '/scripts/app/admin/configuration/configuration.html',
                        controller: 'ConfigurationController'
                    }
                },
                resolve: {

                }
            });
    });
