define([
    'angular',
    './queryCtrl'
],
function (angular) {
    'use strict';

    var module = angular.module('grafana.controllers');

    module.controller('GroundWorkOpenTSDBQueryCtrl', function($controller, $scope, $timeout) {
        
        // inherit OpenTSDB controller
        $controller('OpenTSDBQueryCtrl', {$scope: $scope, $timeout: $timeout});
    });
});
