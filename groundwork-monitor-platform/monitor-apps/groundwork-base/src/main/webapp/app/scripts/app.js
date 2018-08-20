'use strict';

// Declare app level module which depends on filters, and services
angular.module('myApp', [
    'ngRoute',
    'ngGrid',
    'ui.grid',
    'ui.grid.pagination',
    'ui.grid.saveState',
    'ui.bootstrap',
    'ngCookies',
    'myApp.filters',
    'myApp.services',
    'myApp.directives',
    'myApp.controllers'
])
    .factory('ServerService', ['$cookies', ServerService])
    .factory('TextMessages', [TextMessages])
    .factory('DataService', ['$http', '$q', 'ServerService', RestService])
    .factory('PortletService', ['$http', '$q', 'ServerService', PortletService])
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/monitor', {templateUrl: 'views/monitor.html'});
        $routeProvider.otherwise({redirectTo: '/monitor'});
    }]);
