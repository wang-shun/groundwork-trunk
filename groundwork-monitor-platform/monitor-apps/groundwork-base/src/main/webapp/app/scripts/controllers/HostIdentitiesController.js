'use strict';

/* Controllers: */
angular.module('myApp.controllers')
    .controller('HostIdentitiesController', function ($scope, $q, $interval, $log, $modal, DataService, TextMessages, PortletService) {
        $scope.columnDefs = [
            {field: 'hostName', displayName: 'Host Name', width: '40%',
                cellTemplate: '<div ng-click="addHost(row.rowIndex, row.entity)"><div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'},
            {field: 'hostNames', displayName: 'Aliases', width: '60%',
                cellTemplate: '<div ng-click="addHost(row.rowIndex, row.entity)"><div class="ngCellText">{{row.getProperty(col.field).join(", ")}}</div></div>'}
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

            DataService.getHostIdentities(filters, $scope.page * $scope.perPage, $scope.perPage).then(
                function success(data) {
                    $scope.hosts = data.hostIdentities;
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

        $scope.addHost = function(index, host) {
            $scope.currentIndex = index;

            if(activeDialog) {
                return;
            }

            var modalInstance = $modal.open({
                templateUrl: '/portal-groundwork-base/app/views/modals/hosts-detail.html',
                controller: HostDetailsInstanceController,
                backdrop: false,
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
                templateUrl: '/portal-groundwork-base/app/views/modals/hosts-delete.html',
                controller: DeleteHostInstanceController,
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

var HostDetailsInstanceController = function ($scope, $modalInstance, DataService, hostIdentity, parentScope) {

    $scope.hostIdentity = hostIdentity || {
        hostName: '',
        hostNames: []
    };

    $scope.getHostNames = function(prefix)
    {
        return DataService.autocomplete(prefix, 10, 'HOST')
            .then(function(names)
            {
                return _.pluck(names, "name");
            });
    };

    $scope.alerts = [];

    $scope.hasAlias = function(alias) {
        return ($scope.hostIdentity.hostNames.indexOf(alias) !== -1);
    };

    $scope.addAlias = function(alias) {
        $scope.hostIdentity.hostNames.push('');
    };

    $scope.removeAlias = function(index) {
        $scope.hostIdentity.hostNames.splice(index, 1);
    };

    $scope.add = function() {
        var names = $scope.hostIdentity.hostNames;

        for(var i = 0; i < names.length; i++) {
            if(!names[i].length) {
                names.splice(i, 1);
                i--;
            }
        }

        DataService.createOrUpdateHostIdentity($scope.hostIdentity).then(
            function success(entity) {
                $modalInstance.close();
            },
            function error(message) {
                if(message.indexOf('ConstraintViolationException') != -1) {
                    message = 'This host name is likely to exist already - please choose another name.';
                }

                console.log(message);
                $scope.addFailureAlert(message);
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
HostDetailsInstanceController.$inject = ['$scope', '$modalInstance', 'DataService', 'hostIdentity', 'parentScope'];

var DeleteHostInstanceController = function ($scope, $modalInstance, DataService, selectedItems) {

    $scope.deleteItems = function() {
		DataService.deleteHostIdentities(selectedItems).then(
            function success(data) {
                $modalInstance.close();
            },
            function failures(message, status) {
                $scope.addFailureAlert(message);
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
DeleteHostInstanceController.$inject = ['$scope', '$modalInstance', 'DataService', 'selectedItems'];
