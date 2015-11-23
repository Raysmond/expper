'use strict';

angular.module('expperApp')
    .factory('Password', function ($resource) {
        return $resource('/api/account/change_password', {}, {
        });
    });

angular.module('expperApp')
    .factory('PasswordResetInit', function ($resource) {
        return $resource('/api/account/reset_password/init', {}, {
        })
    });

angular.module('expperApp')
    .factory('PasswordResetFinish', function ($resource) {
        return $resource('/api/account/reset_password/finish', {}, {
        })
    });
