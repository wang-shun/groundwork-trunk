'use strict';

/* Controllers: */
angular.module('myApp.controllers')
    .controller('EventsController', function ($scope, $q, $interval, DataService, TextMessages, PortletService) {
        $scope.events = [];
        $scope.initialized = false;
        var Status_ = {
            "UP": /*"gray"*/"",
            "OK": /*"gray"*/"",
            "UNKNOWN": "blue",
            "SCHEDULED CRITICAL": "red",
            "PENDING": "blue",
            "SCHEDULED DOWN": "red",
            "UNREACHABLE": "red",
            "UNSCHEDULED DOWN": "red",
            "WARNING": "yellow",
            "UNSCHEDULED CRITICAL": "red",
            "ACKNOWLEDGEMENT (WARNING)": "yellow",
            "ACKNOWLEDGEMENT (CRITICAL)": "red",
            "ACKNOWLEDGEMENT (DOWN)": "red",
            "ACKNOWLEDGEMENT (UP)": /*"gray"*/"",
            "ACKNOWLEDGEMENT (OK)": /*"gray"*/"",
            "ACKNOWLEDGEMENT (UNREACHABLE)": "red",
            "ACKNOWLEDGEMENT (UNKNOWN)": "blue",
            "ACKNOWLEDGEMENT (PENDING)": "blue",
            "ACKNOWLEDGEMENT (MAINTENANCE)": "yellow",
            "CRITICAL": "red",
            "DOWN": "red",
            "MAINTENANCE": "yellow",
            "SUSPENDED": "blue"
        };

        var ColorIndex = {
            "red": 0,
            "yellow": 1,
            "blue": 2,
            "": 3
        };

        $scope.getStatusColor = function (status) {
            var ret = Status_[status];

            return ret.length ? ('-' + ret) : '';
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

        $scope.viewDetails = function(hostName, serviceName) {
            if (!window.location.origin) {
                window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
            }

            var url = window.location.origin + '/portal-statusviewer/urlmap?host=' + encodeURIComponent(hostName) + '&service=' + encodeURIComponent(serviceName);
            window.location = url;
        }

        $scope.init = function (readResourceURL) {

            $scope.readResourceURL = readResourceURL;
            PortletService.lookupPreferences(readResourceURL).then(
                function success(prefs, status) {
                    $scope.prefs = prefs;
                    DataService.getGroupedEventData().then(
                        function success(events) {
                            var indexedEvents = [];
                            var preferenceCount = $scope.prefs.rows,
                                counts = [0, 0, 0, 0];

                            _.forEach(events, function (event) {
                                var color = Status_[event.monitorStatus];
                                if (color !== undefined) {
                                    var index = ColorIndex[color];
                                    if (index !== undefined) {
                                        counts[index]++;
                                        if (counts[index] <= preferenceCount) {
                                            indexedEvents.push(event);
                                        }
                                    }
                                }
                            })

                            var events_ = indexedEvents.sort(function (a, b) {
                                if (!a)
                                    return 1;

                                if (!b)
                                    return -1;

                                var a_ = Status_[a.monitorStatus],
                                    b_ = Status_[b.monitorStatus];

                                if (a_ && !b_) return -1;
                                if (!a_ && b_) return 1;

                                if (a_ == b_) {
                                    if (a.count > b.count) return -1;
                                    if (a.count < b.count) return 1;
                                    return 0;
                                }
                                if (a_ == 'red') return -1;
                                if (b_ == 'red') return 1;

                                if ((a_ == 'yellow') && (b_ != 'yellow')) return -1;
                                if ((b_ == 'yellow') && (a_ != 'yellow')) return 1;

                                if ((a_ == 'blue') && (b_ != 'blue')) return -1;
                                if ((b_ == 'blue') && (a_ != 'blue')) return 1;

                                return 0;
                            });

                            $scope.events = events_;

                            if ($scope.initialized == false) {
                                //$interval(refresh, $scope.prefs.refreshSeconds * 1000);
                            }
                            $scope.initialized = true;

                        },
                        function error(msg, status) {
                            $scope.addFailureAlert(msg, status);
                        }
                )
            },
            function error(msg, status) {
                console.log(msg);
                $scope.addFailureAlert(msg, status);
            })
        } // end init()

        function refresh() {
            $scope.init($scope.readResourceURL);
        };
    });
