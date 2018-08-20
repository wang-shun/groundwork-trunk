'use strict';

/* Controllers: */
angular.module('myApp.controllers')
    .controller('DeviceTemplatesController', function ($scope, $q, $interval, $log, $modal, DataService, TextMessages, PortletService) {
        $scope.columnDefs = [
            {field: 'deviceIdentification', displayName: 'Device Identity', width: '20%',
                cellTemplate: '<div ng-click="addHost(row.rowIndex, row.entity)"><div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'},
            {field: 'cactiHostTemplate', displayName: 'Cacti Template', width: '20%',
                cellTemplate: '<div ng-click="addHost(row.rowIndex, row.entity)"><div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'},
            {field: 'monarchHostProfile', displayName: 'Monarch Template', width: '20%',
                cellTemplate: '<div ng-click="addHost(row.rowIndex, row.entity)"><div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'},
            {field: 'deviceDescription', displayName: 'Description', width: '20%',
                cellTemplate: '<div ng-click="addHost(row.rowIndex, row.entity)"><div class="ngCellText">{{row.getProperty(col.field)}}</div></div>'},
            {field: 'timestamp', displayName: 'Timestamp', width: '20%', cellFilter: 'date: \'yyyy-MM-dd h:mm:ss a\''}
        ];

        $scope.filters = {
            id: '',
            cacti: '',
            monarch: ''
        };

        $scope.page = 0;
        $scope.perPage = 25;
        $scope.currentIndex = -1;

        $scope.options = {showSelectionCheckbox: true, selectWithCheckboxOnly: true, selectedItems: [], data: 'deviceTemplates', columnDefs: 'columnDefs'};
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

            if($scope.filters.id.length) {
                filters.device = $scope.filters.id;
            }
            if($scope.filters.cacti.length) {
                filters.template = $scope.filters.cacti;
            }
            if($scope.filters.monarch.length) {
                filters.profile = $scope.filters.monarch;
            }

            DataService.getDeviceTemplateProfiles(filters, $scope.page * $scope.perPage, $scope.perPage).then(
                function success(data) {
                    $scope.deviceTemplates = data.deviceTemplateProfiles;
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

        $scope.addHost = function(index, device) {
            $scope.currentIndex = index;

            if(activeDialog) {
                return;
            }

            var modalInstance = $modal.open({
                templateUrl: '/portal-groundwork-base/app/views/modals/devicetemplates-detail.html',
                controller: DeviceTemplateDetailsInstanceController,
                backdrop: false,
                resolve: {
                    DataService: function() { return DataService; },
                    deviceTemplate: function() { return (typeof device == 'undefined') ? null : device; },
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
                templateUrl: '/portal-groundwork-base/app/views/modals/devicetemplates-delete.html',
                controller: DeleteDeviceTemplateInstanceController,
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

var DeviceTemplateDetailsInstanceController = function ($scope, $modalInstance, DataService, deviceTemplate, parentScope) {

    $scope.deviceTemplate = deviceTemplate || {
        deviceIdentification: '',
        cactiHostTemplate: '',
        monarchHostProfile: '',
        deviceDescription: '',
        timestamp: ''
    };

    $scope.alerts = [];

    $scope.isValidHostName = function() {
        var hostNameRegEx = /^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])(\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]{0,61}[a-zA-Z0-9]))*$/;

        return ipaddr.isValid($scope.deviceTemplate.deviceIdentification) || hostNameRegEx.test($scope.deviceTemplate.deviceIdentification);
    };

    $scope.add = function() {
        var deviceTemplate = {};
        deviceTemplate.deviceIdentification = $scope.deviceTemplate.deviceIdentification;
        deviceTemplate.deviceDescription = $scope.deviceTemplate.deviceDescription;
        deviceTemplate.timestamp = $scope.deviceTemplate.timestamp;

        if($scope.deviceTemplate.cactiHostTemplate.length)
            deviceTemplate.cactiHostTemplate = $scope.deviceTemplate.cactiHostTemplate;

        if($scope.deviceTemplate.monarchHostProfile.length)
            deviceTemplate.monarchHostProfile = $scope.deviceTemplate.monarchHostProfile;

        DataService.createOrUpdateDeviceTemplateProfile(deviceTemplate).then(
            function success(entity) {
                $modalInstance.close();
            },
            function error(message) {
                if(message.indexOf('ConstraintViolationException') != -1) {
                    message = 'This device identification is likely to exist already - please choose another one.';
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
DeviceTemplateDetailsInstanceController.$inject = ['$scope', '$modalInstance', 'DataService', 'deviceTemplate', 'parentScope'];

var DeleteDeviceTemplateInstanceController = function ($scope, $modalInstance, DataService, selectedItems) {

    $scope.alerts = [];

    $scope.deleteItems = function() {
		DataService.deleteDeviceTemplateProfiles(selectedItems).then(
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
DeleteDeviceTemplateInstanceController.$inject = ['$scope', '$modalInstance', 'DataService', 'selectedItems'];
