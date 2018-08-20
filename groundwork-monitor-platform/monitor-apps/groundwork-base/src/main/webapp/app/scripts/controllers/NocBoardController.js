'use strict';

/* Controllers: */
angular.module('myApp.controllers')
    .controller('NocBoardController', function ($scope, $q, $timeout, $modal, DataService, TextMessages, PortletService) {

        $scope.isOpen = true;
        this.showFilters = false;
        $scope.isLoading = true;
        
        var initialized = false;

        var cellTemplateHostSingle = '<div class="nServicesCell">{{row.entity.host}}</div>';
        var cellTemplateHostStatus = '<div class="statusCell"><div class="status-message" ng-class="{\'status-error\': ((row.entity.hostStatus == \'UNSCHEDULED CRITICAL\') || (row.entity.hostStatus == \'SCHEDULED CRITICAL\')), \'status-warning\': row.entity.hostStatus == \'WARNING\', \'status-pending\': row.entity.hostStatus == \'PENDING\', \'status-unknown\': row.entity.hostStatus == \'UNKNOWN\'}"></div></div>';
        var cellTemplateService  = '<div class="hostCell"><input type="checkbox" />&nbsp;<div class="ngCellText">{{row.entity.service}}</div><div class="host-message ngCellText" ng-class="{\'status-error\': ((row.entity.status == \'UNSCHEDULED CRITICAL\') || (row.entity.status == \'SCHEDULED CRITICAL\')), \'status-warning\': row.entity.status == \'WARNING\', \'status-pending\': row.entity.status == \'PENDING\', \'status-unknown\': row.entity.status == \'UNKNOWN\'}" title="{{row.entity.statusText}}">{{row.entity.statusText}}</div></div></div>';
        var cellTemplateStatus = '<div class="statusCell"><div class="status-message" ng-class="{\'status-error\': ((row.entity.status == \'UNSCHEDULED CRITICAL\') || (row.entity.status == \'SCHEDULED CRITICAL\')), \'status-warning\': row.entity.status == \'WARNING\', \'status-pending\': row.entity.status == \'PENDING\', \'status-unknown\': row.entity.status == \'UNKNOWN\'}"></div></div>';
        var cellTemplateTimeDown = '<div class="timedownCell"><div class="timedown-message">{{row.entity.duration}}</div>Since<div class="timesince-message">{{row.entity.sinceDate}}</div></div>';
        var cellTemplateAck = '<div class="ackCell" ng-click="grid.appScope.showAck(row.entity)" ng-class="{\'ack-active\': row.entity.ack}"><div class="ack-message" tooltip-placement="top" tooltip-append-to-body="true" tooltip-class="hittooltip" tooltip="{{row.entity.acknowledgeMessage}}">{{row.entity.ackYesNo}}</div></div>';
        var cellTemplateAvailability = '<div class="availabilityCell" ng-class="{\'availability-danger\': row.entity.availability < row.entity.sla}">{{row.entity.availability | number:0}}%</div>';
        var cellTemplateComments = '<div class="notificationsCell" ng-class="{\'notification-active\': row.entity.comments > 0}"><div class="comment-block"><div ng-click="grid.appScope.showComments(row.entity)">{{row.entity.comments}}</div></div></div>';

        var cellTemplateMaintenance =
            '<div class="maintenanceCell" ng-class="\'maintenanceCell\' + row.entity.maintenanceStatus">' +
              '<div class="maintenance-message">{{row.entity.maintenanceStatus == \'Active\' ? \'Yes\' : \'No\'}}</div>' +
              '<div class="maintenance-text">{{row.entity.maintenanceMessage}}</div>' +
              '<progress><bar ng-repeat="maintenanceWindow in row.entity.maintenanceWindows" value="maintenanceWindow.percentage" type="{{maintenanceWindow.status}}" ng-attr-title="{{maintenanceWindow.status}} - {{maintenanceWindow.percentage}}%"></bar></progress>' +
            '</div>';

        $scope.serviceData = { serviceGroup: '', hosts: [], prefs: {} };
        $scope.serviceTableData = [];

        $scope.viewDetails = function(item) {
            if(!window.location.origin) {
                window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
            }

            var url = window.location.origin + '/portal-statusviewer/urlmap?host=' + encodeURIComponent(item.entity.hostName || item.entity.name) + (item.entity.hostName ? ('&service=' + encodeURIComponent(item.entity.name)) : '');

            window.location = url;
        };

        function isColumnShown(columnData, columnName) {
            for(var i = 0, iLimit = columnData.length; i < iLimit; i++) {
                var column = columnData[i];

                if(column.name === columnName) {
                    return column.checked;
                }
            }

            return false;
        }

        $scope.init = function (nocBoardURL) {
            $scope.readResourceURL = nocBoardURL;

            PortletService.computeNocBoard(nocBoardURL).then(
                function success(result, status) {
                    if (!result.success) {
                        var msg = "Failed retrieving NOC Board: " + result.message;
                        $scope.isLoading = false;
                        $scope.addFailureAlert(msg, status);
                        return;
                    }
                    if (!!result.message) {
                        $scope.addFailureAlert(result.message, 500);
                    }
                    $.extend($scope.serviceData, $scope.serviceData, result);
                    $scope.serviceTableData = [];

                    $scope.isOpen = $scope.serviceData.autoExpand;
                    $scope.gridOptionsServices.columnDefs.splice(0);

                    if(isColumnShown(result.prefs.columns, 'Host'))
                        $scope.gridOptionsServices.columnDefs.push({field: 'host', displayName: 'Host', width: '15%', cellTemplate: cellTemplateHostSingle, enableColumnMenu: false, enableFiltering: false});
                    if(isColumnShown(result.prefs.columns, 'Status'))
                        $scope.gridOptionsServices.columnDefs.push({field: 'hostStatus', displayName: 'Status', width: 70, cellTemplate: cellTemplateHostStatus, enableColumnMenu: false, enableFiltering: false});
                    if(isColumnShown(result.prefs.columns, 'Service Name'))
                        $scope.gridOptionsServices.columnDefs.push({field: 'service', displayName: 'Service / Detail', cellTemplate: cellTemplateService, enableColumnMenu: false, enableFiltering: false});
                    if(isColumnShown(result.prefs.columns, 'Status'))
                        $scope.gridOptionsServices.columnDefs.push({field: 'status', displayName: 'Status', width: 70, cellTemplate: cellTemplateStatus, enableColumnMenu: false, enableFiltering: false});
                    if(isColumnShown(result.prefs.columns, 'Time Down'))
                        $scope.gridOptionsServices.columnDefs.push({field: 'timeStarted', displayName: 'Duration', width: 110, cellTemplate: cellTemplateTimeDown, enableColumnMenu: false, enableFiltering: false});
                    if(isColumnShown(result.prefs.columns, 'Maintenance'))
                        $scope.gridOptionsServices.columnDefs.push({field: 'maintenanceMessage', displayName: 'Maintenance', width: 110, cellTemplate: cellTemplateMaintenance, enableColumnMenu: false, enableFiltering: false});
                    if(isColumnShown(result.prefs.columns, 'Ack'))
                        $scope.gridOptionsServices.columnDefs.push({field: 'ackYesNo', displayName: 'Ack', width: 50, cellTemplate: cellTemplateAck, enableColumnMenu: false, enableFiltering: false});
                    if(isColumnShown(result.prefs.columns, 'Availability'))
                        $scope.gridOptionsServices.columnDefs.push({field: 'availability', displayName: 'Availability', width: 95, cellTemplate: cellTemplateAvailability, enableColumnMenu: false, enableFiltering: false});
                    if(isColumnShown(result.prefs.columns, 'Comment'))
                        $scope.gridOptionsServices.columnDefs.push({field: 'comments', displayName: 'Comments', width: 160, cellTemplate: cellTemplateComments, enableColumnMenu: false, enableFiltering: false});

                    $scope.gridOptionsServices.paginationPageSize = result.prefs.rows;
                    $scope.gridOptionsServices.paginationPageSizes = [result.prefs.rows];

                    var i = 0;
                    for(var hostName in $scope.serviceData.hosts) {
                        var host = $scope.serviceData.hosts[hostName];

                        for(var j = 0, jLimit = host.services.length; j < jLimit; j++) {
                            var service = host.services[j];
                            var ackMessage = ''; 

                            if (service['ackBool'] && service['acknowledger']) {
                                ackMessage += service['acknowledger'];
                                if (service['acknowledgeComment']) {
                                    ackMessage += " -- ";
                                    ackMessage += service['acknowledgeComment'];
                                }
                            }

                            $scope.serviceTableData.push({
                                host: (service['hostName']),
                                appType: (service['appType']),
                                //hostDisplay: (j ? '-' : service['hostName']),
                                hostStatus: host['status'],
                                service: service['name'],
                                statusText: service['statusText'],
                                status: service['status'],
                                duration: service['duration'],
                                timeStarted: service['timeStarted'],
                                sinceDate: moment(service['timeStarted']).format('M-D-YYYY hh:mm:ss A'),
                                maintenanceStatus: service['maintenanceStatus'],
                                maintenanceMessage: service['maintenanceMessage'],
                                maintenancePercent: service['maintenancePercent'],
                                maintenanceWindows: service['maintenanceWindows'],
                                ack: service['ackBool'],
                                ackYesNo: (service['ackBool']) ? 'Yes' : 'No',
                                acknowledger: service['acknowledger'],
                                acknowledgeComment: service['acknowledgeComment'],
                                acknowledgeMessage: ackMessage,
                                availability: service['availability'],
                                sla: $scope.serviceData.prefs.percentageSLA,
                                comments: service['numComments'],
                                commentsList: service['commentsList']
                            });

                            i++;
                        }
                    }

                    $scope.isLoading = false;

                    if(!initialized) {
                        initialized = true;

                        // Restore previously saved state.
                        restoreState();
                    }

                    if(result.prefs.refreshSeconds && (result.prefs.refreshSeconds > 10)) {
                        $timeout(refresh, result.prefs.refreshSeconds * 1000);
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

        // Aux methods for UI:
        function msToTime(milliseconds) {
            var seconds =  (milliseconds / 1000) % 60;
            var minutes = ((milliseconds / (1000 * 60)) % 60);
            var hours   = ((milliseconds / (1000 * 60 * 60)) % 24);

            return hours + 'h:' + minutes + 'm:' + seconds + 's';
        }

        $scope.hostStatusCount = function() {
            var hostStatuses = $scope.serviceData.hostStatusCounts, count = 0;

            for(var status in hostStatuses) {
                count += hostStatuses[status];
            }

            return count;
        };

        $scope.hostUpCount = function() {
            var hostStatuses = $scope.serviceData.hostStatusCounts, count = 0;

            for(var status in hostStatuses) {
                if(status == "UP") {
                    count += hostStatuses[status];
                }
            }

            return count;
        };

        $scope.hostDownCount = function() {
            var hostStatuses = $scope.serviceData.hostStatusCounts, count = 0;

            for(var status in hostStatuses) {
                if((status == "UNSCHEDULED DOWN") || (status == "SCHEDULED DOWN") || (status == "UNREACHABLE")) {
                    count += hostStatuses[status];
                }
            }

            return count;
        };

        $scope.hostWarningCount = function() {
            var hostStatuses = $scope.serviceData.hostStatusCounts, count = 0;

            for(var status in hostStatuses) {
                if(status == "WARNING") {
                    count += hostStatuses[status];
                }
            }

            return count;
        };

        $scope.hostPendingCount = function() {
            var hostStatuses = $scope.serviceData.hostStatusCounts, count = 0;

            for(var status in hostStatuses) {
                if(status == "PENDING") {
                    count += hostStatuses[status];
                }
            }

            return count;
        };

        $scope.serviceStatusCount = function() {
            var serviceStatuses = $scope.serviceData.serviceStatusCounts, count = 0;

            for(var status in serviceStatuses) {
                count += serviceStatuses[status];
            }

            return count;
        };

        $scope.serviceUpCount = function() {
            var serviceStatuses = $scope.serviceData.serviceStatusCounts, count = 0;

            for(var status in serviceStatuses) {
                if(status == "OK") {
                    count += serviceStatuses[status];
                }
            }

            return count;
        };

        $scope.serviceProblemCount = function() {
            var serviceStatuses = $scope.serviceData.serviceStatusCounts, count = 0;

            for(var status in serviceStatuses) {
                if(status != "OK") {
                    count += serviceStatuses[status];
                }
            }

            return count;
        };

        $scope.serviceCriticalCount = function() {
            var serviceStatuses = $scope.serviceData.serviceStatusCounts, count = 0;

            for(var status in serviceStatuses) {
                if((status == "UNSCHEDULED CRITICAL") || (status == "SCHEDULED CRITICAL")) {
                    count += serviceStatuses[status];
                }
            }

            return count;
        };

        $scope.serviceWarningCount = function() {
            var serviceStatuses = $scope.serviceData.serviceStatusCounts, count = 0;

            for(var status in serviceStatuses) {
                if(status == "WARNING") {
                    count += serviceStatuses[status];
                }
            }

            return count;
        };

        $scope.servicePendingCount = function() {
            var serviceStatuses = $scope.serviceData.serviceStatusCounts, count = 0;

            for(var status in serviceStatuses) {
                if(status == "PENDING") {
                    count += serviceStatuses[status];
                }
            }

            return count;
        };

        $scope.serviceUnknownCount = function() {
            var serviceStatuses = $scope.serviceData.serviceStatusCounts, count = 0;

            for(var status in serviceStatuses) {
                if(status == "UNKNOWN") {
                    count += serviceStatuses[status];
                }
            }

            return count;
        };

        $scope.isSlaMet = function(){
            var met = $scope.serviceData.slaMet;

            if( met == true){
                return "";
            }
            return "NOT ";

        };
        $scope.getSLAColor = function(){
            var met = $scope.serviceData.slaMet;

            if( met == true){
                return "";
            }
            return '#FF2F0B';

        };

        $scope.serviceStatuses = function() {
            var states = $scope.serviceData.prefs.states, names = [];

            for(var i = 0, iLimit = states ? states.length : 0; i < iLimit; i++) {
                if(states[i].checked) {
                    names.push(states[i].name);
                }
            }

            return names.join(" and ");
        };

        $scope.downtimeStatuses = function() {
            var states = $scope.serviceData.prefs.downTimeFilters, names = [];

            for(var i = 0, iLimit = states ? states.length : 0; i < iLimit; i++) {
                if(states[i].checked) {
                    names.push(states[i].name);
                }
            }

            return names.join(" and ");
        };

        $scope.acknowledgementStatuses = function() {
            var states = $scope.serviceData.prefs.ackFilters, names = [];

            for(var i = 0, iLimit = states ? states.length : 0; i < iLimit; i++) {
                if(states[i].checked) {
                    names.push(states[i].name);
                }
            }

            return names.join(" and ");
        };

        $scope.getFontSize = function() {
            var count = $scope.serviceProblemCount(), ndigits = (count + '').length;

            switch(ndigits) {
                case 1:
                    return 130;

                case 2:
                    return 90;

                case 3:
                    return 60;

                case 4:
                    return 45;

                case 5:
                    return 40;
            }
        };

        $scope.getBackgroundColor = function() {
            var count = $scope.serviceProblemCount();

            if(count > 0){
                return '#FF2F0B';
            }
            else{
                return '#008000';
            }
        };

        var self = this;

        $scope.getShowHideFilters = function() {
            if(self.showFilters) {
                return "hide";
            }
            else {
                return "show";
            }
        };

        $scope.gridApi = null;

        this.gridOptionsServices = $scope.gridOptionsServices = {
            multiSelect: false,
            rowHeight: 54,
            paginationPageSizes: [10],
            paginationPageSize: 10,
            data: 'serviceTableData',
            columnDefs: [],
            enableColumnResize: true,
            plugins: [new ngGridFlexibleHeightPlugin()],
            onRegisterApi: function(gridApi) {
                // Keep a reference to the gridApi.
                $scope.gridApi = gridApi;

                // Setup events so we're notified when grid state changes.
                $scope.gridApi.core.on.columnVisibilityChanged($scope, saveState);
                $scope.gridApi.core.on.filterChanged($scope, saveState);
                $scope.gridApi.core.on.sortChanged($scope, saveState);
            }
        };

        function saveState() {
            var state = $scope.gridApi.saveState.save();
            localStorage.setItem('gridState', JSON.stringify(state));
        }

        function restoreState() {
            $timeout(function() {
                var state = localStorage.getItem('gridState');
                if (state) $scope.gridApi.saveState.restore($scope, JSON.parse(state));
            });
        }

        $scope.showComments = function (data) {
            var modalInstance = $modal.open({
                animation: true,
                ariaLabelledBy: 'modal-title',
                ariaDescribedBy: 'modal-body',
                templateUrl: 'commentsModal.html',
                controller: CommentsModalInstanceCtrl,
                controllerAs: '$ctrl',
                size: '',
                appendTo: undefined,
                resolve: {
                    data: function() { return data; },
                    comments: function() { return data.commentsList; },
                    username: function() { return $scope.serviceData.username; },
                    parentScope: function() { return $scope; },
                    PortletService: function() { return PortletService; }
                }
            });

            modalInstance.result.then(function () {
            }, function () {
            });
        };

        $scope.showAck = function (data) {
            var modalInstance = $modal.open({
                animation: true,
                ariaLabelledBy: 'modal-title',
                ariaDescribedBy: 'modal-body',
                templateUrl: 'ackModal.html',
                controller: AckModalInstanceCtrl,
                controllerAs: '$ctrl',
                size: '',
                appendTo: undefined,
                resolve: {
                    data: function() { return data; },
                    username: function() { return $scope.serviceData.username; },
                    parentScope: function() { return $scope; },
                    PortletService: function() { return PortletService; }
                }
            });

            modalInstance.result.then(function () {
            }, function () {
            });
        };
    })
    .controller('NocBoardEditController', function ($scope, $q, $timeout, DataService, TextMessages, PortletService) {
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
                    $scope.nocBoardForm.$setPristine();
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
            $scope.nocBoardForm.$setPristine();
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


var CommentsModalInstanceCtrl = function ($scope, $modalInstance, $modal, data, comments, username, parentScope, PortletService) {

    $scope.comments = comments || [];
    $scope.comment = {notes: ''};
    $scope.username = username;
    $scope.alerts = [];

    $scope.postComment = function(url) {
        var comment = {host: data.host, service: data.service,
                        commentText: $scope.comment.notes, commentDate: (new Date().getTime()), commentUser: $scope.username };

        PortletService.postComments(url, comment).then(
            function success(result, status) {
                if (!result.success) {
                    var msg = "Failed storing NOC Board comment: " + result.message;
                    $scope.addFailureAlert(msg, status);
                    return;
                }
                comment.commentID = result.message;
                $scope.comments.push(comment);
                if(!data.commentList)
                    data.commentList = [];
                data.commentList.push(comment);
                $scope.comment.notes = '';

                if(data.appType === "NAGIOS") {
                    $scope.addInfoAlert("Comment has been queued up to Nagios server for processing");
                }
                else {
                    data.comments++;
                }
            },
            function error(msg, status) {
                $scope.addFailureAlert(msg, status);
            }
        )
    };

    $scope.deleteComment = function(url, commentID) {

        var comment = {host: data.host, service: data.service,
                        commentID: commentID, commentUser: $scope.username  };

        PortletService.deleteComment(url, comment).then(
            function success(result, status) {
                if (!result.success) {
                    var msg = "Failed deleting NOC Board comment: " + result.message;
                    $scope.addFailureAlert(msg, status);
                    return;
                }
                $scope.comments = $scope.comments.filter(function(el) {
                    return el.commentID !== comment.commentID;
                });
                data.comments--;
                data["commentsList"] =  data.commentsList.filter(function(el) {
                    return el.commentID !== comment.commentID;
                });



            },
            function error(msg, status) {
                $scope.addFailureAlert(msg, status);
            }
        )
    };

    $scope.cancel = function() {
        $modalInstance.dismiss();
    };

    $scope.addInfoAlert = function(message) {
        $scope.alerts.length = 0;
        $scope.alerts.push({type: 'info', msg: message});
    };

    $scope.addFailureAlert = function(message) {
        $scope.alerts.length = 0;
        $scope.alerts.push({type: 'danger', msg: message});
    };

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

}

var AckModalInstanceCtrl = function ($scope, $modalInstance, $modal, data, username, parentScope, PortletService) {

    $scope.comment = {notes: ''};
    $scope.username = username;
    $scope.alerts = [];

    $scope.postAck = function(url) {
        var ackRecord = { host: data.host, service: data.service, ackBool: true, acknowledger: $scope.username,
                          acknowledgeComment: $scope.comment.notes };
        PortletService.postAck(url, ackRecord).then(
            function success(result, status) {
                if (!result.success) {
                    var msg = "Failed posting Acknowledgement: " + result.message;
                    $scope.addFailureAlert(msg, status);
                    return;
                }
                data.ack = true;
                data.ackBool = true;
                data.acknowledgeComment = $scope.comment.notes;
                data.acknowledger = $scope.username;
                data.acknowledgeMessage = data.acknowledger + ' -- ' + data.acknowledgeComment;
                data.ackYesNo = 'Yes';
                $scope.comment.notes = '';
                $modalInstance.dismiss();
            },
            function error(msg, status) {
                $scope.addFailureAlert(msg, status);
            }
        )
    };

    $scope.cancel = function() {
        $modalInstance.dismiss();
    };

    $scope.addFailureAlert = function(message) {
        $scope.alerts.length = 0;
        $scope.alerts.push({type: 'danger', msg: message});
    };

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

}
/*
dApp = angular.module "dApp", []
dApp.directive "collapse", () ->
  restrict: "E"
  transclude: true
  replace: true
  scope:
    title: "@"
  controller: ($scope, $element) ->
    $scope.opened = true
    $scope.toggle = () ->
      $scope.opened = !$scope.opened
  template: """
    <div class="collapsible">
    <header ng-click="toggle()">
      <h4>{{title}}</h4>
    </header>
    <section ng-transclude ng-class="{opened: opened}"></section>
  </div>
  """

$(".collapsible-jq").on "click", ">header", (e) ->
  container = $(@).parents(".collapsible-jq:first").find(">section").css("height", "auto").slideToggle()
  */