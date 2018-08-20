'use strict';

/* Controllers: */
angular.module('myApp.controllers')
    .controller('TopController', function ($scope, $q, $timeout, DataService, TextMessages, PortletService) {
        $scope.columnDefs = [
            {field: 'name', displayName: 'Host', width: '70%',
                cellTemplate: '<div ng-click="viewDetails(row.rowIndex)"><div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'},
            {field: 'service', displayName: 'Service %', width: '30%'}
        ];
        $scope.options = {multiSelect: false, data: 'services', columnDefs: 'columnDefs'};
        $scope.initialized = false;

        $scope.viewDetails = function(index) {
            if (!window.location.origin) {
                window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
            }

            var url = window.location.origin + '/portal-statusviewer/urlmap?host=' + encodeURIComponent($scope.services[index].name);
            window.location = url;
        }

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

        $scope.init = function (readResourceURL) {

            $scope.readResourceURL = readResourceURL;
            PortletService.lookupPreferences(readResourceURL).then(
                function success(prefs, status) {
                    $scope.prefs = prefs;
                    $scope.columnDefs[1].displayName = (!!~prefs.service.indexOf('cpu')) ? "CPU %" : "Memory %";
                    DataService.getServiceData($scope.prefs.service).then(
                        function success(data) {
                            var services = [];
                            _.forEach(data, function (item) {
                                if (item.properties && item.properties.PerformanceData) {
                                    services.push({ name: item.hostName, service: item.properties.PerformanceData });
                                }
                            });
                            var sorted = services.sort(function (a, b) {
                                if (!a) return 1;
                                if (!b) return -1;
                                var a1 = a.service.replace("%", "");
                                var b1 = b.service.replace("%", "");
                                var aNum = parseInt(a1, 10);
                                var bNum = parseInt(b1, 10);
                                if (aNum > bNum) return -1;
                                if (aNum < bNum) return 1;
                                return 0;
                            });
                            $scope.services = sorted.slice(0, $scope.prefs.rows);
                            if ($scope.initialized == false) {
                                //$timeout(refresh, $scope.prefs.refreshSeconds * 1000);
                            }
                            $scope.initialized = true;
                        },
                        function error(msg, status) {
                            $scope.addFailureAlert(msg, status);
                        }
                    );
                },
                function error(msg, status) {
                    console.log(msg);
                    $scope.addFailureAlert(msg, status);
                }
            )

        } // end init

        function refresh() {
            $scope.init($scope.readResourceURL);
        };
    })
    .controller('HitListController', function ($scope, $q, $timeout, DataService, TextMessages, PortletService) {
        $scope.current = {
            hosts: '',
            serviceUp: '',
            serviceDown: ''
        };
        $scope.currentData = {
            hosts: [],
            serviceUp: [],
            serviceDown: []
        };

        $scope.optionsHosts = {
            multiSelect: false,
            enableSorting: true,
            columnDefs: [
                {field: 'name', displayName: 'Host', width: '60%', enableSorting: true,
                 cellTemplate: '<div ng-click="viewDetails(row)" tooltip-placement="top" tooltip-append-to-body="true" tooltip-class="hittooltip" tooltip="{{row.getProperty(\'name\')}}"><div class="ngCellText">{{row.getProperty("name")}}</div></div>'},
                {field: 'lastCheckTime', displayName: 'Timestamp', width: '40%', cellFilter: 'date:"medium"', enableSorting: true}
            ],
            data: 'currentData["hosts"]',
            sortInfo: { fields: ['lastCheckTime'], directions: ['desc'] },
            enableColumnResize: true
        };
        $scope.optionsServiceUp = {
            multiSelect: false,
            enableSorting: true,
            columnDefs: [
                {field: 'hostService', displayName: 'Service', width: '60%', enableSorting: true,
                 cellTemplate: '<div ng-click="viewDetails(row)" tooltip-placement="top" tooltip-append-to-body="true" tooltip-class="hittooltip" tooltip="{{row.getProperty(\'hostService\')}}"><div class="ngCellText">{{row.getProperty("hostService")}}</div></div>'},
                {field: 'lastCheckTime', displayName: 'Timestamp', width: '40%', cellFilter: 'date:"medium"', enableSorting: true}
            ],
            data: 'currentData["serviceUp"]',
            sortInfo: { fields: ['lastCheckTime'], directions: ['desc'] },
            enableColumnResize: true
        };
        $scope.optionsServiceDown = {
            multiSelect: false,
            enableSorting: true,
            columnDefs: [
                {field: 'hostService', displayName: 'Service', width: '60%', enableSorting: true,
                 cellTemplate: '<div ng-click="viewDetails(row)" tooltip-placement="top" tooltip-append-to-body="true" tooltip-class="hittooltip" tooltip="{{row.getProperty(\'hostService\')}}"><div class="ngCellText">{{row.getProperty("hostService")}}</div></div>'},
                {field: 'lastCheckTime', displayName: 'Timestamp', width: '40%', cellFilter: 'date:"medium"', enableSorting: true}
            ],
            data: 'currentData["serviceDown"]',
            sortInfo: { fields: ['lastCheckTime'], directions: ['desc'] },
            enableColumnResize: true
        };

        $scope.data = {};

        $scope.setCurrent = function (section, type) {
            $scope.current[section] = type;
            $scope.currentData[section] = $scope.data[type];
        }; // end init

        $scope.viewDetails = function(item) {
            if (!window.location.origin) {
                window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
            }

            var url = window.location.origin + '/portal-statusviewer/urlmap?host=' + encodeURIComponent(item.entity.hostName || item.entity.name) + (item.entity.hostName ? ('&service=' + encodeURIComponent(item.entity.name)) : '');

            window.location = url;
        };

        $scope.getCurrentTypeName = function(section) {
            switch($scope.current[section]) {
                case "hostsDownUnacknowledged": return "Down and Unacknowledged";
                case "hostsDownAcknowledged": return "Down and Acknowledged";
                case "hostsScheduledDown": return "Scheduled Down";
                case "hostsUnreachable": return "Unreachable";

                case "servicesCriticalUnacknowledged": return "Critical and Unacknowledged";
                case "servicesWarningUnacknowledged": return "Warning and Unacknowledged";
                case "servicesCriticalAcknowledged": return "Critical and Acknowledged";
                case "servicesWarningAcknowledged": return "Warning and Acknowledged";

                case "servicesCriticalDown": return "Critical on Down Hosts";
                case "servicesWarningDown": return "Warning on Down Hosts";

                default: return "";
            }
        };

        $scope.init = function (hitListURL) {
            $scope.readResourceURL = hitListURL;

            PortletService.computeHitList(hitListURL).then(
                function success(result, status) {
                    if (!result.success) {
                        var msg = "Failed retrieving Hit List: " + result.message;
                        $scope.addFailureAlert(msg, status);
                        return;
                    }

                    var arrays = ['servicesCriticalAcknowledged', 'servicesCriticalDown', 'servicesCriticalUnacknowledged', 'servicesWarningAcknowledged', 'servicesWarningDown', 'servicesWarningUnacknowledged'];

                    for(var index in arrays) {
                        var entries = result[arrays[index]];

                        if(entries) {
                            for(var i = 0, iLimit = entries.length; i < iLimit; i++) {
                                var entry = entries[i];

                                entries[i].hostService = entry.hostName + ':' + entry.name;
                            }
                        }
                    }

                    $scope.data = result;

                    if(!$scope.current['hosts']) {
                        $scope.setCurrent('hosts', 'hostsDownUnacknowledged');
                        $scope.setCurrent('serviceUp', 'servicesCriticalUnacknowledged');
                        $scope.setCurrent('serviceDown', 'servicesCriticalDown');
                    }
                    else {
                        $scope.setCurrent('hosts', $scope.current['hosts']);
                        $scope.setCurrent('serviceUp', $scope.current['serviceUp']);
                        $scope.setCurrent('serviceDown', $scope.current['serviceDown']);
                    }

                    if($scope.data.prefs.refreshSeconds && ($scope.data.prefs.refreshSeconds > 10)) {
                        $timeout(refresh, $scope.data.prefs.refreshSeconds * 1000);
                    }
                },
                function error(msg, status) {
                    console.log(msg);
                    $scope.addFailureAlert(msg, status);
                }
            )
        }; // end init

        function refresh() {
            $scope.init($scope.readResourceURL);
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
    })
    .controller('HitListEditController', function ($scope, $q, $timeout, DataService, TextMessages, PortletService) {
        $scope.master = {};

        $scope.init = function (readResourceURL, writeResourceURL, renderURL) {
            $scope.readResourceURL = readResourceURL;
            $scope.writeResourceURL = writeResourceURL;
            $scope.renderURL = renderURL;
            PortletService.lookupPreferences(readResourceURL).then(
                function success(prefs, status) {
                    $scope.prefs = prefs; console.log(prefs);
                    if (_.has(prefs, 'sortOrder')) {
                        $scope.sortOrders = ['name','monitorStatus'];
                    }
                    $scope.master = angular.copy(prefs);
                    $scope.hitlistForm.$setPristine();
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
            $scope.hitlistForm.$setPristine();
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
    })
