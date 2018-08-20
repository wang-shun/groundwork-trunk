'use strict';

/* Controllers: */
angular.module('myApp.controllers')
    .controller('NocBoardConfigController', function ($scope, $q, $timeout, $modal, DataService, TextMessages, PortletService) {
        $scope.isOpen = true;
        $scope.boards = [];
        $scope.selectedBoardName = null;

        function listBoards() {
            PortletService.getBoards($scope.listURL).then(
                function success(result, status) {
                    $scope.boards = result;
                    
                    if(!$scope.selectedBoardName && $scope.boards.length) {
                        $scope.selectedBoardName = $scope.boards[0].name;
                    }
                },
                function error(msg, status) {
                    console.log(msg);
                }
            );
        }

        $scope.init = function (listURL, lookupURL, saveURL, removeURL, existsURL, isJBoss) {
            $scope.listURL = listURL;
            $scope.lookupURL = lookupURL;
            $scope.saveURL = saveURL;
            $scope.removeURL = removeURL;
            $scope.existsURL = existsURL;
            $scope.isJBoss = isJBoss;
            listBoards();
        };

        $scope.newBoard = function () {
            var modalInstance = $modal.open({
                templateUrl: '/portal-groundwork-base/app/views/modals/noc-detail.html',
                controller: NocBoardModalInstanceCtrl,
                backdrop: true,
                resolve: {
                    boardName: function() { return null; },
                    boardNames:  function() { return $.map($scope.boards, function(val, index) { return val.name } ); },
                    PortletService: function() { return PortletService; },
                    lookupURL: function() { return $scope.lookupURL; },
                    saveURL: function() { return $scope.saveURL; },
                    removeURL: function() { return $scope.removeURL; },
                    existsURL: function() { return $scope.existsURL; },
                    isJBoss: function () { return $scope.isJBoss; }
                }
            });

            modalInstance.result.then(function (board) {
                listBoards();
            }, function () {
            });
        };

        $scope.editBoard = function (selectedBoardName) {
            var modalInstance = $modal.open({
                templateUrl: '/portal-groundwork-base/app/views/modals/noc-detail.html',
                controller: NocBoardModalInstanceCtrl,
                backdrop: true,
                resolve: {
                    boardName: function() { return selectedBoardName; },
                    boardNames:  function() { return $.map($scope.boards, function(val, index) { return val.name } ); },
                    PortletService: function() { return PortletService; },
                    lookupURL: function() { return $scope.lookupURL; },
                    saveURL: function() { return $scope.saveURL; },
                    removeURL: function() { return $scope.removeURL; },
                    existsURL: function() { return $scope.existsURL; },
                    isJBoss: function () { return $scope.isJBoss; }
                }
            });

            modalInstance.result.then(function (board) {
                if(board === true) {
                    $scope.selectedBoardName = null;
                }

                listBoards();
            }, function () {
            });
        };
    });

var NocBoardModalInstanceCtrl = function ($scope, $modalInstance, $modal, PortletService, lookupURL, saveURL, removeURL, existsURL, boardName, boardNames, isJBoss) {
    $scope.isNew = !boardName;
    $scope.boardInstance = null;
    $scope.isJBoss = (isJBoss === 'true');
    var paramDelimiter = ($scope.isJBoss) ? "&" : "?";

    PortletService.lookupBoard(lookupURL + (boardName ? (paramDelimiter + "name=" + encodeURIComponent(boardName)) : '')).then(
        function success(result, status) {
            $scope.hostGroups = result.hostGroups ? result.hostGroups.slice() : [];
            $scope.serviceGroups = result.serviceGroups ? result.serviceGroups.slice(): [];

            $scope.boardInstance = $.extend({}, result);

            delete $scope.boardInstance.hostGroups;
            delete $scope.boardInstance.serviceGroups;
        },
        function error(msg, status) {
            console.log(msg);
            $scope.addFailureAlert(msg, status);
        }
    );

    $scope.isValidName = function() {
        return $scope.boardInstance && $scope.boardInstance.name && $scope.boardInstance.name.length && ($scope.isNew ? (boardNames.indexOf($scope.boardInstance.name) === -1) : true);
    };

    $scope.isValidTitle = function() {
        return $scope.boardInstance && $scope.boardInstance.title && $scope.boardInstance.title.length;
    };

    $scope.isValidBoard = function() {
        return $scope.isValidName() && $scope.isValidTitle() && ($scope.boardInstance.hostGroup || $scope.boardInstance.serviceGroup) &&
              ($scope.boardInstance.availabilityHours >= 1) && ($scope.boardInstance.availabilityHours <= $scope.boardInstance.maxAvailabilityWindow);
    };

    $scope.save = function() {
        PortletService.saveBoard(saveURL, $scope.boardInstance).then(
            function success(result, status) {
                $modalInstance.close($scope.boardInstance);
            },
            function error(msg, status) {
                console.log(msg);
                $scope.addFailureAlert(msg, status);
            }
        )
     };

    $scope.delete = function() {
        var modalInstance = $modal.open({
            templateUrl: '/portal-groundwork-base/app/views/modals/noc-delete.html',
            controller: DeleteNocBoardInstanceController,
            resolve: {
                name: function() { return $scope.boardInstance.name; }
            }
        });

        modalInstance.result.then(function () {
            var paramDelimiter = ($scope.isJBoss) ? "&" : "?";
            PortletService.deleteBoard(removeURL + paramDelimiter + "name=" + encodeURIComponent($scope.boardInstance.name)).then(
                function success(result, status) {
                    $modalInstance.close(true);
                },
                function error(msg, status) {
                    console.log(msg);
                    $scope.addFailureAlert(msg, status);
                }
            )
        }, function () {
        });
    };

    $scope.close = function() {
        $modalInstance.dismiss();
    };

    $scope.alerts = [];

    $scope.addFailureAlert = function(message) {
        $scope.alerts.length = 0;
        $scope.alerts.push({type: 'danger', msg: message});
    };

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };
};

NocBoardModalInstanceCtrl.$inject = ['$scope', '$modalInstance', '$modal', 'PortletService', 'lookupURL', 'saveURL', 'removeURL', 'existsURL', 'boardName', 'boardNames', 'isJBoss'];

var DeleteNocBoardInstanceController = function ($scope, $modalInstance, name) {
    $scope.name = name;

    $scope.deleteBoard = function() {
        $modalInstance.close();
    };

    $scope.close = function() {
        $modalInstance.dismiss();
    };
};

DeleteNocBoardInstanceController.$inject = ['$scope', '$modalInstance', 'name'];
