'use strict';

/* Controllers: */
angular.module('myApp.controllers')
    .controller('BlackListController', function ($scope, $q, $interval, $modal, $log, DataService, TextMessages, PortletService) {
        $scope.columnDefs = [
            {field: 'hostName', displayName: 'Host Name', width: '100%',
                cellTemplate: '<div ng-click="addHost(row.rowIndex, row.entity)"><div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'}
        ];

        $scope.filters = {
            hostname: ''
        };

        $scope.page = 0;
        $scope.perPage = 25;
        $scope.currentIndex = -1;

        $scope.options = {showSelectionCheckbox: true, selectWithCheckboxOnly: true, selectedItems: [], data: 'hosts', columnDefs: 'columnDefs'};
        $scope.initialized = false;

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

            if($scope.filters.hostname.length) {
                filters.hostname = $scope.filters.hostname;
            }

            DataService.getHostBlacklists(filters, $scope.page * $scope.perPage, $scope.perPage).then(
                function success(data) {
                    $scope.hosts = data.hostBlacklists;
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

        var searchTimeout = null;

        $scope.search = function() {
            if(searchTimeout) {
                clearTimeout(searchTimeout);
            }

            searchTimeout = setTimeout(function() {
                $scope.getPage();
            },
            500);
        };

        var activeDialog = null;

        $scope.checkAddHost = function(event) {
            if(event.keyCode != 13) {
                $scope.search();
            }
            else {
                $scope.addHost();
            }
        };

        $scope.addHost = function(index, host) {
            $scope.currentIndex = index;

            if(activeDialog) {
                return;
            }

            var modalInstance = $modal.open({
                templateUrl: '/portal-groundwork-base/app/views/modals/blacklist-detail.html',
                controller: BlackListDetailInstanceController,
                resolve: {
                    DataService: function() { return DataService; },
                    hostIdentity: function() { return (typeof host == 'undefined') ? null : host; },
                    parentScope: function() { return $scope; }
                }
            });

            activeDialog = modalInstance;

            modalInstance.result.then(function () {
                activeDialog = null;
                $scope.getPage();
            }, function () {
                activeDialog = null;
                $log.info('Modal dismissed at: ' + new Date());
            });
        };

        $scope.deleteHosts = function() {
            var modalInstance = $modal.open({
                templateUrl: '/portal-groundwork-base/app/views/modals/blacklist-delete.html',
                controller: DeleteBlackItemInstanceController,
                resolve: {
                    DataService: function() { return DataService; },
                    selectedItems: function() { return $scope.options.selectedItems; }
                }
            });

            modalInstance.result.then(function () {
                $scope.options.selectedItems = [];
                $scope.getPage();
            }, function () {
                $log.info('Modal dismissed at: ' + new Date());
            });
        };
    });

var BlackListDetailInstanceController = function ($scope, $modalInstance, $modal, DataService, hostIdentity, parentScope) {

    $scope.hostIdentity = hostIdentity || {
        hostName: ''
    };

    $scope.alerts = [];

    $scope.isValidHostName = function() {
        return (!!$scope.hostIdentity.hostName && ($scope.hostIdentity.hostName != ".*") && ($scope.hostIdentity.hostName.length < 255));
    };

    $scope.add = function() {
        DataService.createOrUpdateHostBlacklist($scope.hostIdentity).then(
            function success(entity) {
                $modalInstance.close();
            },
            function error(message) {
                if(message.indexOf('ConstraintViolationException') != -1) {
                    message = 'This host name is likely to exist already - please choose another name.';
                }
                else {
                    message = 'There was an error with communicating with the server. Please try again later.';
                }

                notyfy({text: message, type: 'error', timeout: 5000});

                console.log(message);
                $scope.addFailureAlert(message);
            });
    };

    $scope.deleteMatchingHostsEnabled = function() {
        return ($scope.isValidHostName());
    }

    $scope.deleteMatchingHosts = function() {
        // lookup all non-nagios host names and host identity host names
        var allHostNames = [];
        var matchingHostNames = [];
        DataService.getNonNagiosHostNames().then(function(hostNames) {
            // aggregate host names
            if (!!hostNames) {
                allHostNames = hostNames;
            }
            return DataService.getHostIdentities();
        }).then(function(hostIdentities) {
            // aggregate host identity host names, (aliases)
            if (!!hostIdentities && !!hostIdentities.hostIdentities) {
                _.forEach(hostIdentities.hostIdentities, function(hostIdentity) {
                    if (_.contains(allHostNames, hostIdentity.hostName)) {
                        allHostNames = allHostNames.concat(hostIdentity.hostNames);
                    }
                });
            }
            allHostNames = _.uniq(allHostNames.sort(), true);

            // filter host names by host black list
            var matchPattern = $scope.hostIdentity.hostName;
            matchPattern = (!~matchPattern.lastIndexOf('^', 0) ? '^' : '') + matchPattern;
            matchPattern += (!~matchPattern.lastIndexOf('$', matchPattern.length-1) ? '$' : '');
            var matchRegex = new RegExp(matchPattern, 'i');
            _.forEach(allHostNames, function(hostName) {
                if (matchRegex.test(hostName)) {
                    matchingHostNames.push(hostName);
                }
            });

            // open modal instance to confirm delete
            var modalInstanceInner = $modal.open({
                templateUrl: '/portal-groundwork-base/app/views/modals/blacklist-confirm.html',
                controller: DeleteMatchingInstanceController,
                resolve: {
                    count: function() {
                        return matchingHostNames.length;
                    }
                }
            });
            return modalInstanceInner.result;
        }).then(function() {
            return DataService.deleteHosts(matchingHostNames);
        }).catch(function(message) {
            if (!!message) {
                $scope.addFailureAlert(message);
            }
        });
    };

    $scope.close = function() {
        $modalInstance.dismiss();
    };

    $scope.addFailureAlert = function(message) {
        $scope.alerts.length = 0;
        $scope.alerts.push({type: 'danger', msg: message});
    };

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };
};
BlackListDetailInstanceController.$inject = ['$scope', '$modalInstance', '$modal', 'DataService', 'hostIdentity', 'parentScope'];

var DeleteMatchingInstanceController = function ($scope, $modalInstance, count) {

    $scope.count = count;

    $scope.confirm = function() {
        $modalInstance.close();
    };

    $scope.close = function() {
        $modalInstance.dismiss();
    };
};
DeleteMatchingInstanceController.$inject = ['$scope', '$modalInstance', 'count'];

var DeleteBlackItemInstanceController = function ($scope, $modalInstance, DataService, selectedItems) {

    $scope.alerts = [];

    $scope.deleteItems = function() {
		DataService.deleteHostBlacklists(selectedItems).then(
            function success(data) {
                $modalInstance.close();
            },
            function failures(message, status) {
                $scope.addFailureAlert(message);
            });
    };

    $scope.addFailureAlert = function(message) {
        $scope.alerts.length = 0;
        $scope.alerts.push({type: 'danger', msg: message});
    };

    $scope.close = function() {
        $modalInstance.dismiss();
    };
};
DeleteBlackItemInstanceController.$inject = ['$scope', '$modalInstance', 'DataService', 'selectedItems'];
