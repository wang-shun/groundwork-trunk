'use strict';

/* Directives: */
angular.module('myApp.directives', [])
    .directive('gwpStatusIcon', ['$timeout', function ($timeout) {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var self = $(element);

                $timeout(function() {
                    if(self.hasClass('status-yellow')) {
                        self.append('<div class="glyphicon glyphicon-warning-sign"></div>');
                    }
                });
            }
        }
    }])
    .directive('aDisabled', function() {
        return {
            compile: function(tElement, tAttrs, transclude) {
                //Disable ngClick
                tAttrs["ngClick"] = "!("+tAttrs["aDisabled"]+") && ("+tAttrs["ngClick"]+")";

                //return a link function
                return function (scope, iElement, iAttrs) {
                    //Toggle "disabled" to class when aDisabled becomes true
                    scope.$watch(iAttrs["aDisabled"], function(newValue) {
                        if (newValue !== undefined) {
                            iElement.toggleClass("disabled", newValue);
                        }
                    });

                    //Disable href on click
                    iElement.on("click", function(e) {
                        if (scope.$eval(iAttrs["aDisabled"])) {
                            e.preventDefault();
                        }
                    });
                };
            }
        };
    })
    .directive('gwpStatusHover', ['DataService', function (DataService) {
        return {
            restrict: 'A',
            link : function(scope, element, attrs) {
                var self = $(element),
                    item = scope.item,
                    body = $(document.body);

                self.hover(function() {
                        $('#infohover').remove();

                        var position = self.offset();

                        DataService.getEventsByHost(item.hostName)
                            .then(function(data) {
                                var events = data,
                                    highAlerts = [1, 2, 3], avgAlerts = [1, 2, 3], lowAlerts = [1, 2, 3];

                                for(var i = 0, iLimit = events.length; i < iLimit; i++) {
                                    var event = events[i];

                                    switch(event.monitorStatus) {
                                        case 'SCHEDULED CRITICAL':
                                        case 'SCHEDULED DOWN':
                                        case 'UNREACHABLE':
                                        case 'UNSCHEDULED DOWN':
                                        case 'UNSCHEDULED CRITICAL':
                                        case 'ACKNOWLEDGEMENT (CRITICAL)':
                                        case 'ACKNOWLEDGEMENT (DOWN)':
                                        case 'ACKNOWLEDGEMENT (UNREACHABLE)':
                                        case 'CRITICAL':
                                        case 'DOWN':
                                            highAlerts.push(event);
                                            break;

                                        case 'WARNING':
                                        case 'ACKNOWLEDGEMENT (WARNING)':
                                        case 'ACKNOWLEDGEMENT (MAINTENANCE)':
                                        case 'MAINTENANCE':
                                            avgAlerts.push(event);
                                            break;

                                        case 'UP':
                                        case 'OK':
                                        case 'ACKNOWLEDGEMENT (UP)':
                                        case 'ACKNOWLEDGEMENT (OK)':
                                        case 'UNKNOWN':
                                        case 'PENDING':
                                        case 'ACKNOWLEDGEMENT (UNKNOWN)':
                                        case 'ACKNOWLEDGEMENT (PENDING)':
                                        case 'SUSPENDED':
                                        default:
                                            lowAlerts.push(event);
                                            break;
                                    }
                                }

                                var alertHtml = '';

                                if(highAlerts.length || avgAlerts.length || lowAlerts.length) {
                                    alertHtml = '<hr />';

                                    if(highAlerts.length) {
                                        alertHtml += '<h4><img class="icon icon-alert" src="/portal-groundwork-base/app/images/status/host-red.gif" />Critical alerts (' + highAlerts.length + ')</h4>';
                                        /*
                                         for(i = 0, iLimit = highAlerts.length; i < iLimit; i++) {
                                         alertHtml += '<p>' + highAlerts[i].textMessage + '</p>';
                                         }
                                         */
                                        if(avgAlerts.length || lowAlerts.length) {
                                            alertHtml += '<hr />';
                                        }
                                    }

                                    if(avgAlerts.length) {
                                        alertHtml += '<h4><img class="icon" src="/portal-groundwork-base/app/images/status/host-yellow.gif" />Warning alerts (' + avgAlerts.length + ')</h4>';
                                        /*
                                         for(i = 0, iLimit = avgAlerts.length; i < iLimit; i++) {
                                         alertHtml += '<p>' + avgAlerts[i].textMessage + '</p>';
                                         }
                                         */
                                        if(lowAlerts.length) {
                                            alertHtml += '<hr />';
                                        }
                                    }

                                    if(lowAlerts.length) {
                                        alertHtml += '<h4><img class="icon" src="/portal-groundwork-base/app/images/status/host-green.gif" />Info alerts (' + lowAlerts.length + ')</h4>';
                                        /*
                                         for(i = 0, iLimit = lowAlerts.length; i < iLimit; i++) {
                                         alertHtml += '<p>' + lowAlerts[i].textMessage + '</p>';
                                         }
                                         */
                                    }
                                }

                                $('#infohover').remove();

                                var bodyWidth = body.width();
                                var infoHover = $('<div id="infohover"><h3>' + item.hostName + '</h3><p class="timestamp">' + moment(item.lastCheckTime).format('dddd, MMMM Do YYYY, h:mm:ss a') + '</p>' + alertHtml + '</div>');
                                body.append(infoHover);

                                var left = position.left + self.outerWidth()  / 1.5;

                                if((left + infoHover.width()) > bodyWidth) {
                                    left = position.left + self.outerWidth() / 3 - infoHover.width();
                                }

                                infoHover.css({
                                    top:  position.top  + self.outerHeight() / 1.5,
                                    left: left
                                });
                            },
                            function() {
                            });
                    },
                    function() {
                        $('#infohover').remove();
                    });
                    
                    self.closest('tab-content').hover(function() {}, function() {
                        $('#infohover').remove();
                    });
            }
        }
    }])
    .directive('gwpEventHover', ['DataService', function (DataService) {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var self = $(element),
                    event = scope.event,
                    body = $(document.body);

                self.hover(function() {
                        $('#eventhover').remove();

                        var position = self.offset();

                        var eventHover = $('<div id="eventhover"><p>Updated ' + event.lastDate.format('dddd, MMMM Do YYYY, h:mm:ss a') + ', click to view this alert.</p><p>' + event.event.textMessage + '</p></div>');

                        body.append(eventHover);

                        eventHover.css({
                            top:  position.top  + self.outerHeight(),
                            left: position.left + 100
                        });
                    },
                    function() {
                        $('#eventhover').remove();
                    });
            }
        }
    }])
    .directive('gwpDatepicker', function () {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var self = $(element), path = attrs.gwpDatepicker.split('.');

                self.parent().datetimepicker({format: 'YYYY-MM-DD h:mm a'});

                self.parent().on("dp.change", function (e)
                {
                    var variable = scope;

                    for(var i = 0, iLimit = path.length - 1; i < iLimit; i++)
                    {
                        variable = variable[path[i]];
                    }

                    variable[path[i]] = e.date.format("YYYY-MM-DD h:mm a");
                });
            }
        }
    })
    .directive('gwpSlidingDialog', function () {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var self = $(element), dialog = self.closest('.modal');
                dialog.addClass('slide-from-right');

                var baseGrid = $('#grid.base-grid');
                if(baseGrid.length) {
                    var top = baseGrid.offset().top;

                    dialog.css('margin-top', top + 'px');
                }
            }
        }
    })
    .directive('gwpAutofocus', function () {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var self = $(element), dialog = self.closest('.modal');

                dialog.on('shown.bs.modal', function () {
                    setTimeout(function() {
                        element.focus();
                    },
                    100);
                });

                setTimeout(function() {
                    element.focus();
                },
                100);
            }
        }
    })
    .directive('gwpAccordionState', function () {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var self = $(element), id = self.attr("id"), accordion = self.parent();

                scope.$watch('isOpen', function(data) {
                    localStorage.setItem("accordion-" + id, data);
                });

                var state = localStorage.getItem("accordion-" + id) || true;

                if((state === "false") || !state) {
                    state = false;
                }
                else {
                    state = true;
                }

                setTimeout(function() {
                    if (!scope.$$phase) {
                        scope.$apply(function(){
                            scope.isOpen = state;
                        });
                    }
                }, 1);
            }
        }
    })
    .directive('gwpAccordionStateSpecial', function () {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var self = $(element), id = self.attr("id"), accordion = self.parent();

                scope.$watch('noc.showFilters', function(data) {
                    localStorage.setItem("accordion-" + id, data);
                });

                var state = localStorage.getItem("accordion-" + id) || false;

                if((state === "false") || !state) {
                    state = false;
                }
                else {
                    state = true;
                }

                setTimeout(function() {
                    if (!scope.$$phase) {
                        scope.$apply(function(){
                            scope.noc.showFilters = state;
                        });
                    }
                }, 1);
            }
        }
    })
    .directive('gwpNoSpaces', function () {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var self = $(element);

                self.on('keypress', function(e) {
                    if(e.which === 32) {
                        if(e.preventDefault) {
                            e.preventDefault();
                        }

                        return false;
                    }
                });

                self.on('paste', function () {
                    setTimeout(function () {
                        var text = self.val().replace(/\s/g, "");

                        self.val(text);
                    },
                    100);
                });
            }
        }
    })
    .directive('gwpSafeName', function () {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var self = $(element);

                self.on('keypress', function(e) {
                    var regex = new RegExp("^[a-zA-Z0-9\-\x08]+$");
                    var key = String.fromCharCode(!event.charCode ? event.which : event.charCode);

                    if (!regex.test(key)) {
                        if(e.preventDefault) {
                            e.preventDefault();
                        }

                        return false;
                    }
                });

                self.on('paste', function () {
                    setTimeout(function () {
                        var text = self.val().replace(/[^a-zA-Z0-9\-]/g, "");

                        self.val(text);
                    },
                    100);
                });
            }
        }
    })
    .directive('gwpFitGrid', ['$timeout', function ($timeout) {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var container = $(element);

                $timeout(function() {
                    var nav = container.find('.audit-nav-container'),
                        grid = container.find('.panel-grid'),
                        viewport = grid.find('.ngViewport');

                    grid.css({height: (container.height() - nav.height()) + 'px'});
                    viewport.css({height: (container.height() - nav.height() - grid.find('.ngHeaderContainer').height()) + 'px'});
                });
            }
        }
    }])
    .directive('gwpAdjustGrid', ['$timeout', function ($timeout) {
        return {
            restrict: 'A',
            link : function(scope, element, attrs){
                var container = $(element);

                scope.$watch('data', function(data) {
                    if(!data || !data.prefs || !data.prefs.rows)
                        return;

                    $timeout(function() {
                        var viewport = container.find('.ngViewport');
                        var height = ((viewport.find(".ngCell:first").height() || 30) * data.prefs.rows) + 'px';

                        viewport.css({'height': height, 'min-height': height});
                    });
                });
            }
        }
    }])
    .directive('gwpHostName', ['$timeout', function ($timeout) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link : function(scope, element, attrs, ctrl) {
                var hostNameRegEx = /^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-_]{0,61}[a-zA-Z0-9])(\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-_]{0,61}[a-zA-Z0-9]))*$/;
                var debounceTimer = null;

                ctrl.$parsers.unshift(function(value) {
                    if (!!debounceTimer) {
                        $timeout.cancel(debounceTimer);
                        debounceTimer = null;
                    }
                    var valid = (!value || ipaddr.isValid(value) || hostNameRegEx.test(value));
                    if (valid) {
                        ctrl.$setValidity('hostName', true);
                    } else {
                        debounceTimer = $timeout(function (){
                            ctrl.$setValidity('hostName', false);
                        }, 300);
                    }
                    return valid ? value : undefined;
                });

                ctrl.$formatters.unshift(function(value) {
                    if (!!debounceTimer) {
                        $timeout.cancel(debounceTimer);
                        debounceTimer = null;
                    }
                    var valid = (!value || ipaddr.isValid(value) || hostNameRegEx.test(value));
                    ctrl.$setValidity('hostName', valid);
                    return value;
                });
            }
        }
    }]);

