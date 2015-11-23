'use strict';

angular.module('expperApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('settings', {
                parent: 'account',
                url: '/settings',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Settings'
                },
                views: {
                    'content@': {
                        templateUrl: '/scripts/app/account/settings/settings.html',
                        controller: 'SettingsController'
                    }
                },
                resolve: {

                }
            });
    });
