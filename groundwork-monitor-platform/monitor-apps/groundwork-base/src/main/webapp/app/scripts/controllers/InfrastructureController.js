'use strict';

/* Controllers: */
angular.module('myApp.controllers')
    .controller('InfrastructureController', function ($scope, $q, $interval, DataService, TextMessages, PortletService) {
        $scope.hypervisorCount = 0;
        $scope.datastoreCount = 0;
        $scope.networkCount = 0;
        $scope.vmCount = 0;
        $scope.initialized = false;

        $scope.items = [];
        $scope.activetab =  'vm';

        $scope.init = function (readResourceURL) {
            $scope.readResourceURL = readResourceURL;
            PortletService.lookupPreferences(readResourceURL).then(
                function success(prefs, status) {
                    $scope.prefs = prefs;
                    $scope.initialized = true;
                    $scope.switchTo($scope.activetab);
                    //$interval(refresh, prefs.refreshSeconds * 1000);
                },
                function error(msg, status) {
                    console.log(msg);
                    $scope.addFailureAlert(msg, status);
                }
            )
        };

        function refresh() {
            var deferred = $q.defer();
            DataService.doQueries($scope.prefs.sortOrder).then(
                function success(data, status) {
                    $scope.switchTo($scope.activetab);
                    deferred.resolve(data);
                },
                function error(msg, status) {
                    $scope.addFailureAlert(msg, status);
                    deferred.reject(msg);
                }
            );
            return deferred.promise;

        };

        var Status_ = {
            "UP": "green",
            "OK": "green",
            "UNKNOWN": "gray",
            "SCHEDULED CRITICAL": "red",
            "PENDING": "blue",
            "SCHEDULED DOWN": "orange",
            "UNREACHABLE": "gray",
            "UNSCHEDULED DOWN": "red",
            "WARNING": "yellow",
            "UNSCHEDULED CRITICAL": "red",
            "ACKNOWLEDGEMENT (WARNING)": "yellow",
            "ACKNOWLEDGEMENT (CRITICAL)": "red",
            "ACKNOWLEDGEMENT (DOWN)": "red",
            "ACKNOWLEDGEMENT (UP)": "green",
            "ACKNOWLEDGEMENT (OK)": "green",
            "ACKNOWLEDGEMENT (UNREACHABLE)": "red",
            "ACKNOWLEDGEMENT (UNKNOWN)": "gray",
            "ACKNOWLEDGEMENT (PENDING)": "blue",
            "ACKNOWLEDGEMENT (MAINTENANCE)": "yellow",
            "CRITICAL": "red",
            "DOWN": "red",
            "MAINTENANCE": "yellow",
            "SUSPENDED": "gray"
        };

        $scope.closeAlert = function (index) {
            $scope.alerts = [];
        };

        $scope.alerts = [
        ];

        $scope.addFailureAlert = function (errorMessage, status) {
            $scope.alerts.length = 0;
            var statusMsg = (status === undefined) ? "none" : status;
            $scope.alerts.push({type: 'danger', msg: TextMessages.get('serverFailure', errorMessage, statusMsg) });
        };

        $scope.getStatusColor = function (status) {
            var ret = Status_[status];

            return ret.length ? ('-' + ret) : '';
        };

        $scope.viewDetails = function(hostName) {
            if (!window.location.origin) {
                window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
            }

            var url = window.location.origin + '/portal-statusviewer/urlmap?host=' + encodeURIComponent(hostName);
            window.location = url;
        }

        $scope.switchTo = function (section) {
            if ($scope.initialized == false)
                return;
            switch (section) {
                case 'vm':
                    $scope.activetab = 'vm';
                    DataService.getVmData($scope.prefs.sortOrder)
                        .then(function success(data, status) {
                            $scope.items = data.vms;
                            $scope.hypervisorCount = data.hypervisors.length;
                            $scope.datastoreCount = data.storage.length;
                            $scope.networkCount = data.networks.length;
                            $scope.vmCount = data.vms.length;
                        },
                        function error(msg, status) {
                            console.log(msg);
                            $scope.addFailureAlert(msg, status);
                        });
                    break;

                case 'hypervisor':
                    $scope.activetab = 'hypervisor';
                    DataService.getHostsData($scope.prefs.sortOrder)
                        .then(function success(data, status) {
                            $scope.items = data.hypervisors;
                            $scope.hypervisorCount = data.hypervisors.length;
                            $scope.datastoreCount = data.storage.length;
                            $scope.networkCount = data.networks.length;
                            $scope.vmCount = data.vms.length;
                        },
                        function error(msg, status) {
                            $scope.addFailureAlert(msg, status);
                        });
                    break;

                case 'network':
                    $scope.activetab = 'network';
                    DataService.getNetworkData($scope.prefs.sortOrder)
                        .then(function success(data, status) {
                            $scope.items = data.networks;
                            $scope.hypervisorCount = data.hypervisors.length;
                            $scope.datastoreCount = data.storage.length;
                            $scope.networkCount = data.networks.length;
                            $scope.vmCount = data.vms.length;
                        },
                        function error(msg, status) {
                            $scope.addFailureAlert(msg, status);
                        });
                    break;

                case 'datastore':
                    $scope.activetab = 'datastore';
                    DataService.getDataStoreData($scope.prefs.sortOrder)
                        .then(function (data, status) {
                            $scope.items = data.storage;
                            $scope.hypervisorCount = data.hypervisors.length;
                            $scope.datastoreCount = data.storage.length;
                            $scope.networkCount = data.networks.length;
                            $scope.vmCount = data.vms.length;
                        },
                        function error(msg, status) {
                            $scope.addFailureAlert(msg, status);
                        });
                    break;

                default:
                    $scope.items = [];
                    break;
            }
        };
    });
