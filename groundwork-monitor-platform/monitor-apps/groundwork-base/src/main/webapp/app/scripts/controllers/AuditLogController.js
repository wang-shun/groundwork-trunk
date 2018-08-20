'use strict';

/* Controllers: */
angular.module('myApp.controllers')
    .controller('AuditLogController', function ($scope, $q, $interval, DataService, TextMessages, PortletService) {
        $scope.columnDefs = [
            {field: 'description', displayName: 'Activity Message', width: '30%',
                cellTemplate:
                    '<div ng-click="viewService(row.rowIndex)" title=\"{{row.getProperty(col.field)}}\">' +
                    '<div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'},
            {field: 'subsystem', displayName: 'Subsystem', width: '10%'},
            {field: 'getHostName()', displayName: 'Host or Group', width: '10%',
                cellTemplate:
                    '<div ng-click="viewHostOrGroup(row.rowIndex)" title=\"{{row.getProperty(col.field)}}\">' +
                    '<div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'},
            {field: 'serviceDescription', displayName: 'Service', width: '10%',
                cellTemplate:
                    '<div ng-click="viewService(row.rowIndex)" title=\"{{row.getProperty(col.field)}}\">' +
                    '<div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'},
            {field: 'action', displayName: 'Action', width: '10%'},
            {field: 'username', displayName: 'User', width: '10%'},
            {field: 'timestamp', displayName: 'Timestamp', width: '20%', cellFilter: 'date: \'yyyy-MM-dd h:mm:ss a\'' }
        ];

        $scope.filters = {
            subsystem: '',
            hostname: '',
            servicename: '',
            username: '',
            timestampmin: '',
            timestampmax: '',
            message: ''
        };

        $scope.page = 0;
        $scope.perPage = 25;

        $scope.options = {multiSelect: false, data: 'auditLogs', columnDefs: 'columnDefs'};
        $scope.initialized = false;

        /**
         * Jump to Status Viewer with focus on host, host group, or service group.
         *
         * @param index audit log row index
         */
        $scope.viewHostOrGroup = function(index) {
            var urlMapUrl = window.location.origin + '/portal-statusviewer/urlmap?';
            var hostName = $scope.auditLogs[index].hostName;
            if (!!hostName) {
                window.location = urlMapUrl + "host=" + encodeURIComponent(hostName);
                return;
            }
            var hostGroupName = $scope.auditLogs[index].hostGroupName;
            if (!!hostGroupName) {
                window.location = urlMapUrl + "hostgroup=" + encodeURIComponent(hostGroupName);
                return;
            }
            var serviceGroupName = $scope.auditLogs[index].serviceGroupName;
            if (!!serviceGroupName) {
                window.location = urlMapUrl + "servicegroup=" + encodeURIComponent(serviceGroupName);
                return;
            }
        };

        /**
         * Jump to Status Viewer with focus on service. Fall back to view host,
         * host group, or service group.
         *
         * @param index audit log row index
         */
        $scope.viewService = function(index) {
            var urlMapUrl = window.location.origin + '/portal-statusviewer/urlmap?';
            var hostName = $scope.auditLogs[index].hostName;
            var serviceDescription = $scope.auditLogs[index].serviceDescription;
            if (!!hostName && !!serviceDescription) {
                window.location = urlMapUrl + "host=" + encodeURIComponent(hostName) + "&service=" +
                    encodeURIComponent(serviceDescription);
                return;
            }
            $scope.viewHostOrGroup(index);
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

        $scope.init = function (readResourceURL) {

            if (!window.location.origin) {
                window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
            }

            $scope.readResourceURL = readResourceURL;
            PortletService.lookupPreferences(readResourceURL).then(
                function success(prefs, status) {
                    $scope.prefs = prefs;
//                  $scope.columnDefs[1].displayName = (!!~prefs.service.indexOf('cpu')) ? "CPU %" : "Memory %";
                    $scope.getPage();

                    if ($scope.initialized == false) {
                        //$interval(refresh, $scope.prefs.refreshSeconds * 1000);
                    }

                    $scope.initialized = true;
                },
                function error(msg, status) {
                    console.log(msg);
                    $scope.addFailureAlert(msg, status);
                }
            );
        }; // end init

        $scope.getPage = function() {
            var filters = {};

            if($scope.filters.subsystem.length) {
                filters.subsystem = $scope.filters.subsystem;
            }
            if($scope.filters.message.length) {
                filters.description = $scope.filters.message;
            }
            if($scope.filters.hostname.length) {
                filters.hostName = $scope.filters.hostname;
                filters.hostGroupName = $scope.filters.hostname;
                filters.serviceGroupName = $scope.filters.hostname;
            }
            if($scope.filters.servicename.length) {
                filters.serviceDescription = $scope.filters.servicename;
            }
            if($scope.filters.username.length) {
                filters.username = $scope.filters.username;
            }
            
            if($scope.filters.timestampmin.length) {
                filters.mintimestamp = moment($scope.filters.timestampmin, "YYYY-MM-DD h:mm a").toDate();
            }

            if($scope.filters.timestampmax.length) {
                filters.maxtimestamp = moment($scope.filters.timestampmax, "YYYY-MM-DD h:mm a").toDate();
            }

            DataService.getAuditData(filters, $scope.page * $scope.perPage, $scope.perPage).then(
                function success(data) {
                    var auditLogs = data.auditLogs;

                    angular.forEach(auditLogs, function (row)
                    {
                        row.getHostName = function ()
                        {
                            return row.serviceGroupName || row.hostGroupName || row.hostName;
                        };
                    });

                    $scope.auditLogs = auditLogs;
                },
                function error(msg, status) {
                    $scope.addFailureAlert(msg, status);
                }
            );
        };

        $scope.getPrevPage = function () {
            $scope.page++;

            $scope.getPage();
        };

        $scope.getNextPage = function () {
            $scope.page--;

            if($scope.page < 0) {
                $scope.page = 0;
            }

            $scope.getPage();
        };

        $scope.reset = function() {
            $scope.filters.subsystem = '';
            $scope.filters.hostname = '';
            $scope.filters.servicename = '';
            $scope.filters.username = '';
            $scope.filters.message = '';

            $scope.filters.timestampmin = '';
            $scope.filters.timestampmax = '';
            $('#audit-timestampmin').val('');
            $('#audit-timestampmax').val('');

            $scope.getPage(0);
        };

        function refresh() {
            $scope.init($scope.readResourceURL);
        };
    });
