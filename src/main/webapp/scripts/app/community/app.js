'use strict';

angular.module('expperApp', ['ngResource', 'ui.router'])

.run(function($rootScope, $location, $window, $http, $state, ENV, VERSION) {
    $rootScope.ENV = ENV;
    $rootScope.VERSION = VERSION.replace('-SNAPSHOT', '');
  })
  .config(function($stateProvider, $urlRouterProvider, $httpProvider) {
    //enable CSRF
    $httpProvider.defaults.xsrfCookieName = 'CSRF-TOKEN';
    $httpProvider.defaults.xsrfHeaderName = 'X-CSRF-TOKEN';

    //Cache everything except rest api requests
    //httpRequestInterceptorCacheBusterProvider.setMatchlist([/.*api.*/, /.*protected.*/], true);
    //$httpProvider.interceptors.push('errorHandlerInterceptor');
    //$httpProvider.interceptors.push('authExpiredInterceptor');
    $httpProvider.interceptors.push('notificationInterceptor');

  });

function activeUrls() {
  var path = document.location.pathname;
  // 话题
  $('ul.topics-list > li > a').each(function() {
    if (encodeURI($(this).attr('href')) == path) {
      $(this).parent().addClass('active');
    }
  });

  // 导航
  $('ul.navbar-nav li a').each(function() {
    if (encodeURI($(this).attr('href')) == path) {
      $(this).parent().addClass('active');
    }
  });
  if (path == '/posts/new') {
    $('ul.navbar-nav li:first').addClass('active');
  }

  if (path.startsWith('/tags')) {
    $('ul.navbar-nav > li:first').next().addClass('active');
  }
};

$(function() {
  activeUrls();
});
