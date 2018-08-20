'use strict';

/* Controllers: */
angular.module('myApp.controllers')
    .controller('MonitorEditController', function ($scope, DataService, PortletService) {
        $scope.master = {};

        $scope.init = function (readResourceURL, writeResourceURL, renderURL) {
            $scope.readResourceURL = readResourceURL;
            $scope.writeResourceURL = writeResourceURL;
            $scope.renderURL = renderURL;
            PortletService.lookupPreferences(readResourceURL).then(
                function success(prefs, status) {
                    $scope.prefs = prefs;
                    if (_.has(prefs, 'sortOrder')) {
                        $scope.sortOrders = ['name','monitorStatus'];
                    }
                    $scope.master = angular.copy(prefs);
                    $scope.topForm.$setPristine();
                },
                function error(msg, status) {
                    console.log(msg);
                    $scope.addFailureAlert(msg, status);
                }
            )
        };

        $scope.update = function (prefs) {
            PortletService.storePreferences($scope.writeResourceURL, prefs).then(
                function success(result, status) {
                    window.location = $scope.renderURL;
                },
                function error(msg, status) {
                    console.log(msg);
                    $scope.addFailureAlert(msg, status);
                }
            )
        };

        $scope.reset = function () {
            $scope.prefs = angular.copy($scope.master);
        };

        $scope.clear = function () {
            $scope.master = {};
            $scope.prefs = {};
            $scope.topForm.$setPristine();
        };

        $scope.isUnchanged = function (prefs) {
            return angular.equals(prefs, $scope.master);
        };

        $scope.alerts = [
        ];

        $scope.closeAlert = function (index) {
            $scope.alerts = [];
        };

        $scope.addFailureAlert = function (errorMessage, status) {
            $scope.alerts.length = 0;
            var statusMsg = (status === undefined) ? "none" : status;
            $scope.alerts.push({type: 'danger', msg: TextMessages.get('serverFailure', errorMessage, statusMsg) });
        };

        $scope.addSuccessAlert = function () {
            $scope.alerts.length = 0;
            $scope.alerts.push({type: 'success', msg: TextMessages.get('PrefsUpdated') });
        };

        $scope.reset();
    });
