'use strict';

/* Services */

var RestService = function ($http, $q, ServerService) {

    var service = {

        vms: [],
        hypervisors: [],
        networks: [],
        storage: [],
        events: {},
        allEvents: [],
        groupedEvents: {},
        hostGroups: {},
        serviceGroups: {},
        hostCategories: {},
        serviceCategories: {},
        hostServiceNames: {},

        StatusPriority: {
            "UP": 4,
            "OK": 4,
            "UNKNOWN": 3,
            "SCHEDULED CRITICAL": 1,
            "PENDING": 3,
            "SCHEDULED DOWN": 1,
            "UNREACHABLE": 1,
            "UNSCHEDULED DOWN": 1,
            "WARNING": 2,
            "UNSCHEDULED CRITICAL": 1,
            "ACKNOWLEDGEMENT (WARNING)": 2,
            "ACKNOWLEDGEMENT (CRITICAL)": 1,
            "ACKNOWLEDGEMENT (DOWN)": 1,
            "ACKNOWLEDGEMENT (UP)": 4,
            "ACKNOWLEDGEMENT (OK)": 4,
            "ACKNOWLEDGEMENT (UNREACHABLE)": 1,
            "ACKNOWLEDGEMENT (UNKNOWN)": 3,
            "ACKNOWLEDGEMENT (PENDING)": 3,
            "ACKNOWLEDGEMENT (MAINTENANCE)": 2,
            "CRITICAL": 1,
            "DOWN": 1,
            "MAINTENANCE": 2,
            "SUSPENDED": 3
        },

        buildCache: function(groups, hosts, sortOrder) {
            service.vms = [];
            service.hypervisors = [];
            service.networks = [];
            service.storage = [];
            _.forEach(hosts, function(host) {
                if (host.appType == 'VEMA' || host.appType == 'NAGIOS') {
                    if (host.hostName.substring(0, 4) === "NET-") {
                        //console.log('NET: host = ' + host.hostName);
                        service.networks.push(host);
                    }
                    else if (host.hostName.substring(0, 5) === "STOR-") {
                        //console.log('storage = ' + host.hostName);
                        service.storage.push(host);
                    }
                    else { // hypervisor or vm

                        if (~_.findIndex(groups, {'hostName': host.hostName})) {
                            service.hypervisors.push(host);
                            //console.log('hypervisor = ' + host.hostName);
                        }
                        else { // default to VM
                            service.vms.push(host);
                            //console.log('vm = ' + host.hostName);
                        }
                    }
                }
            })
            if (sortOrder === undefined || sortOrder === 'name') {
                service.vms = service.doSortByName(service.vms);
                service.hypervisors = service.doSortByName(service.hypervisors);
                service.networks = service.doSortByName(service.networks);
                service.storage = service.doSortByName(service.storage);
            }
            else {
                service.vms = service.doSortByStatus(service.vms);
                service.hypervisors = service.doSortByStatus(service.hypervisors);
                service.networks = service.doSortByStatus(service.networks);
                service.storage = service.doSortByStatus(service.storage);
            }
            service.hostGroups = {};
            service.serviceGroups = {};
            service.hostCategories = {};
            service.serviceCategories = {};
            service.hostServiceNames = {};
        },

        doSortByName: function(array) {
            var result = array.sort(function (a, b) {
                if (!a) return 1;
                if (!b) return -1;
                var a_ = a.hostName;
                var b_ = b.hostName;
                if (a_ && !b_) return -1;
                if (!a_ && b_) return 1;
                a_ = a_.toLowerCase();
                b_ = b_.toLowerCase();
                if (a_ > b_) return 1;
                return -1;
            });
            return result;
        },

        doSortByStatus: function(array) {
            var result = array.sort(function (a, b) {
                if (!a) return 1;
                if (!b) return -1;
                var a_ = service.StatusPriority[a.monitorStatus];
                var b_ = service.StatusPriority[b.monitorStatus];
                if (a_ && !b_) return -1;
                if (!a_ && b_) return 1;
                if (a_ > b_) return 1;
                return -1;
            });
            return result;
        },

        doQueries: function(sortOrder) {
            var deferred = $q.defer();
            var self = this;
            var url = ServerService.api('/hostgroups');
            var config = ServerService.apiConfig();
            config.params = {
                "query" : "name like 'ESX%' or name like 'VSS%'"
            };
            $http.get(url, config)
                .success(function success(hgData, status, headers, config) {
                    _.forEach(hgData.hostGroups, function(hg) {
                        hg.hostName = hg.name.substring(4);
                    });
                    var url = ServerService.api('/hosts');
                    var config = ServerService.apiConfig();
                    config.params = {
                        "query" : "serviceStatuses.metricType is not null"
                        //"query" : "appType = 'VEMA'"
                    };
                    $http.get(url, config)
                        .success(function success(hostData, status, headers, config) {
                            service.buildCache(hgData.hostGroups, hostData.hosts, sortOrder);
                            deferred.resolve(service);
                        })
                        .error(function error(data, status) {
                            if (status == 404) {
                                service.buildCache(hgData.hostGroups, [], sortOrder);
                                deferred.resolve(service);
                            }
                            else {
                                rejectWithMessage(deferred, data);
                            }
                        }
                    );
                })
                .error(function error(data, status) {
                    if (status == 404) {
                        service.buildCache([], [], sortOrder); // TODO: still retrieve host data here
                        deferred.resolve(service);
                    }
                    else {
                        rejectWithMessage(deferred, data);
                    }
                });
            return deferred.promise;
        },

        getVmData: function (sortOrder) {
            var self = this;
            if (service.vms.length > 0) {
                var deferred = $q.defer();
                deferred.resolve(service);
                return deferred.promise;
            }
            var deferred = $q.defer();
            service.doQueries(sortOrder).then(
                function success(data, status) {
                    deferred.resolve(data);
                },
                function error(data, status) {
                    rejectWithMessage(deferred, data);
                }
            );
            return deferred.promise;
        },

        getNetworkData: function (sortOrder) {
            var self = this;
            if (service.vms.length > 0) {
                var deferred = $q.defer();
                deferred.resolve(service);
                return deferred.promise;
            }
            var deferred = $q.defer();
            service.doQueries(sortOrder).then(
                function success(data, status) {
                    deferred.resolve(data);
                },
                function error(data, status) {
                    rejectWithMessage(deferred, data);
                }
            );
            return deferred.promise;
        },

        getDataStoreData: function (sortOrder) {
            var self = this;
            if (service.vms.length > 0) {
                var deferred = $q.defer();
                deferred.resolve(service);
                return deferred.promise;
            }
            var deferred = $q.defer();
            service.doQueries(sortOrder).then(
                function success(data, status) {
                    deferred.resolve(data);
                },
                function error(data, status) {
                    rejectWithMessage(deferred, data);
                }
            );
            return deferred.promise;
        },

        getHostsData: function (sortOrder) {
            var self = this;
            if (service.vms.length > 0) {
                var deferred = $q.defer();
                deferred.resolve(service);
                return deferred.promise;
            }
            var deferred = $q.defer();
            service.doQueries(sortOrder).then(
                function(data, status) {
                    deferred.resolve(data);
                },
                function error(data, status) {
                    rejectWithMessage(deferred, data);
                }
            );
            return deferred.promise;

        },

        getEventsByHost: function (host) {
            if (!_.isEmpty(service.events)) {
                if (!_.isEmpty(service.events[host])) {
                    var deferred = $q.defer();
                    deferred.resolve(service.events[host]);
                    return deferred.promise;
                }
            }
            var deferred = $q.defer();
            var url = ServerService.api('/events');
            var config = ServerService.apiConfig();
            config.params = {
                "query" : "host = '" + host + "'"
            };
            $http.get(url, config)
                .success(function success(eventData, status, headers, config) {
                    service.events[host] = [];
                    _.forEach(eventData.events, function(event) {
                        service.events[host].push(event);
                    });
                    deferred.resolve(service.events[host]);
                })
                .error(function error(data, status) {
                    rejectWithMessage(deferred, data);
                }
            );
            return deferred.promise;
        },

        getEventData: function () {
            var deferred = $q.defer();
            var url = ServerService.api('/events');
            var config = ServerService.apiConfig();
            config.params = {
                "query" : "appType = 'VEMA' and operationStatus = 'OPEN'"
            };
            $http.get(url, config)
                .success(function success(eventData, status, headers, config) {
                    service.allEvents = [];
                    service.events = {};
                    _.forEach(eventData.events, function(event) {
                        if (!!event.service) {
                            service.allEvents.push(event);
                        }
                    });
                    deferred.resolve(service.allEvents);
                })
                .error(function error(data, status) {
                    rejectWithMessage(deferred, data);
                }
            );
            return deferred.promise;
        },

        getGroupedEventData: function () {
            var deferred = $q.defer();
            service.getEventData().then(
                function success(eventData, status) {
                    service.groupedEvents = {};

                    _.forEach(eventData, function(event) {
                        var serviceName = $.trim(event.service),
                            key = event.monitorStatus  + '-' + serviceName,
                            group = service.groupedEvents[key];

                        if (!group) {
                            service.groupedEvents[key] = {
                                event: event,
                                service: serviceName,
                                monitorStatus: event.monitorStatus,
                                lastDate: moment(event.lastInsertDate),
                                count: 1
                            };
                        }
                        else {
                            var eventDate = moment(event.lastInsertDate);

                            if(eventDate.isAfter(group.lastDate)) {
                                group.event = event;
                                group.lastDate = eventDate;
                            }

                            group.count++;
                        }
                    });
                    deferred.resolve(service.groupedEvents);
                }
            );
            return deferred.promise;
        },

        getServiceData: function (serviceType) {
            //var array = service.services[serviceType];
            var result = [];
            var deferred = $q.defer();
            var url = ServerService.api('/services');

            var config = ServerService.apiConfig();
            config.params = {
                "query" : "description = '" + serviceType + "'" // 'syn.vm.cpu.cpuToMax.used'
                //"query" : "description = 'syn.vm.cpu.cpuToMax.used'"
            };
            $http.get(url, config)
                .success(function success(serviceData, status, headers, config) {
                    _.forEach(serviceData.services, function(serv) {
                        result.push(serv);
                    });
                    deferred.resolve(result);
                })
                .error(function error(data, status) {
                    rejectWithMessage(deferred, data);
                }
            );
            return deferred.promise;
        },

        /**
         * Get array of all host names.
         *
         * @returns promise rejected or resolved with host names
         */
        getHostNames: function () {
            var deferred = $q.defer();
            service.getHostServiceNames().then(
                function success(hostServiceNames) {
                    deferred.resolve(_.keys(hostServiceNames));
                },
                function error(message) {
                    deferred.reject(message);
                });
            return deferred.promise;
        },

        /**
         * Get object with all host names as fields, each with all
         * host service descriptions.
         *
         * @returns promise rejected or resolved with host service names object
         */
        getHostServiceNames: function () {
            var deferred = $q.defer();
            if (!_.isEmpty(service.hostServiceNames)) {
                deferred.resolve(service.hostServiceNames);
            } else {
                /*
                // get all hosts with deep depth to get nested services
                var url = ServerService.api('/hosts');
                url += '?depth=deep';
                $http.get(url, ServerService.apiConfig())
                    .success(function success(data, status) {
                        service.hostServiceNames = {};
                        if (!!data && !_.isEmpty(data.hosts)) {
                            _.forEach(data.hosts, function(host) {
                                if (!_.isEmpty(host.services)) {
                                    var hostServiceNames = [];
                                    _.forEach(host.services, function(service) {
                                        hostServiceNames.push(service.description);
                                    });
                                    service.hostServiceNames[host.hostName] = hostServiceNames;
                                }
                            });
                        }
                        deferred.resolve(service.hostServiceNames);
                    })
                    .error(function error(message, status) {
                        if (status == 404) {
                            service.hostServiceNames = {};
                            deferred.resolve(service.hostServiceNames);
                        } else {
                            rejectWithMessage(deferred, message);
                        }
                    }
                );
                */
                // get all authorized hosts and services
                var url = ServerService.api('/biz/getauthorizedservices');
                $http.get(url, ServerService.apiConfig())
                    .success(function success(data, status) {
                        service.hostServiceNames = {};
                        if (!!data && !!data.serviceHostNames) {
                            // invert service hosts map to build host services map
                            service.hostServiceNames = _.transform(data.serviceHostNames, function(result, hostNames, serviceName) {
                                _.forEach(hostNames, function(hostName) {
                                    if (!result[hostName]) {
                                        result[hostName] = [];
                                    }
                                    result[hostName].push(serviceName);
                                });
                            });
                        }
                        deferred.resolve(service.hostServiceNames);
                    })
                    .error(function error(message, status) {
                        if (status == 404) {
                            service.hostServiceNames = {};
                            deferred.resolve(service.hostServiceNames);
                        } else {
                            rejectWithMessage(deferred, message);
                        }
                    }
                );
            }
            return deferred.promise;
        },

        /**
         * Get and cache all HostGroups.
         *
         * @returns promise rejected or resolved with HostGroups data object
         */
        getHostGroups: function () {
            var deferred = $q.defer();
            if (!_.isEmpty(service.hostGroups)) {
                deferred.resolve(service.hostGroups);
            } else {
                var url = ServerService.api('/hostgroups');
                $http.get(url, ServerService.apiConfig())
                    .success(function success(data, status) {
                        service.hostGroups = data;
                        deferred.resolve(service.hostGroups);
                    })
                    .error(function error(message, status) {
                        if (status == 404) {
                            service.hostGroups = {hostGroups: []};
                            deferred.resolve(service.hostGroups);
                        } else {
                            rejectWithMessage(deferred, message);
                        }
                    }
                );
            }
            return deferred.promise;
        },

        /**
         * Lookup a HostGroup by name.
         *
         * @param name host group name
         * @returns promise rejected or resolved with a HostGroup object.
         */
        getHostGroup: function(name) {
            var deferred = $q.defer();
            var url = ServerService.api('/hostgroups', encodeURIComponent(name));
            $http.get(url, ServerService.apiConfig())
                .success(function success(data, status) {
                    deferred.resolve(data);
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Create or update HostGroup. Operation is considered a create if the
         * host group name is unique and the id is undefined.
         *
         * @param hostGroup HostGroup instance to create or update
         * @returns promise rejected or resolved with entity string
         */
        createOrUpdateHostGroup: function(hostGroup) {
            var deferred = $q.defer();
            var url = ServerService.api('/hostgroups');
            // create or update post closure
            var createOrUpdatePost = function() {
                $http.post(url, {hostGroups: [hostGroup]}, ServerService.apiConfig())
                    .success(function success(data, status) {
                        resolveWithEntityOrRejectWithMessage(deferred, data);
                        service.hostGroups = {};
                    })
                    .error(function error(message, status) {
                        rejectWithMessage(deferred, message);
                    }
                );
            }
            // clear host group before update to support replace/put update
            // mode, (update is normally an upsert/post)
            if (!!hostGroup.id) {
                var httpConfig = {
                    method: 'DELETE',
                    url: url,
                    headers: {'Content-Type': 'application/json'},
                    data: {hostGroups: [_.pick(hostGroup, ['id', 'name'])]},
                    params: {clear: true}
                };
                _.merge(httpConfig, ServerService.apiConfig());
                $http(httpConfig)
                    .success(function success(data, status) {
                        if (!!data && (data.successful === 1)) {
                            // update host group
                            createOrUpdatePost();
                        } else {
                            // clear host group fails for update
                            resolveWithEntityOrRejectWithMessage(deferred, data);
                        }
                    })
                    .error(function error(message, status) {
                        rejectWithMessage(deferred, message);
                    }
                );
            } else {
                // create host group
                createOrUpdatePost();
            }
            return deferred.promise;
        },

        /**
         * Delete HostGroups.
         *
         * @param hostGroups array of HostGroup instances to delete
         * @returns promise rejected or resolved
         */
        deleteHostGroups: function(hostGroups) {
            var deferred = $q.defer();
            var hostGroupIdAndNames = [];
            _.forEach(hostGroups, function(hostGroup) {
                hostGroupIdAndNames.push(_.pick(hostGroup, ['id', 'name']));
            });
            var httpConfig = {
                method: 'DELETE',
                url: ServerService.api('/hostgroups'),
                headers: {'Content-Type': 'application/json'},
                data: {hostGroups: hostGroupIdAndNames}
            };
            _.merge(httpConfig, ServerService.apiConfig());
            $http(httpConfig)
                .success(function success(data, status) {
                    if (!!data && (data.successful === hostGroups.length)) {
                        deferred.resolve(undefined);
                    } else {
                        deferred.reject("Host groups not deleted.");
                    }
                    service.hostGroups = {};
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Get and cache all ServiceGroups.
         *
         * @returns promise rejected or resolved with ServiceGroups data object
         */
        getServiceGroups: function() {
            var deferred = $q.defer();
            if (!_.isEmpty(service.serviceGroups)) {
                deferred.resolve(service.serviceGroups);
            } else {
                var url = ServerService.api('/servicegroups');
                $http.get(url, ServerService.apiConfig())
                    .success(function success(data, status) {
                        service.serviceGroups = data;
                        deferred.resolve(service.serviceGroups);
                    })
                    .error(function error(message, status) {
                        if (status == 404) {
                            service.serviceGroups = {serviceGroups: []};
                            deferred.resolve(service.serviceGroups);
                        } else {
                            rejectWithMessage(deferred, message);
                        }
                    }
                );
            }
            return deferred.promise;
        },

        /**
         * Lookup a ServiceGroup by name.
         *
         * @param name service group name
         * @returns promise rejected or resolved with a ServiceGroup object.
         */
        getServiceGroup: function(name) {
            var deferred = $q.defer();
            var url = ServerService.api('/servicegroups', encodeURIComponent(name));
            $http.get(url, ServerService.apiConfig())
                .success(function success(data, status) {
                    deferred.resolve(data);
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Create or update ServiceGroup. Operation is considered a create if
         * the service group name is unique and the id is undefined.
         *
         * @param serviceGroup ServiceGroup instance to create or update
         * @returns promise rejected or resolved with entity string
         */
        createOrUpdateServiceGroup: function(serviceGroup) {
            var deferred = $q.defer();
            var url = ServerService.api('/servicegroups');
            var serviceGroupUpdate = _.pick(serviceGroup, ['name', 'description', "appType", "agentId"]);
            serviceGroupUpdate.services = [];
            _.forEach(serviceGroup.services, function(service) {
                serviceGroupUpdate.services.push({
                    host: service.hostName,
                    service: service.description
                });
            });
            $http.post(url, {serviceGroups: [serviceGroupUpdate]}, ServerService.apiConfig())
                .success(function success(data, status) {
                    resolveWithEntityOrRejectWithMessage(deferred, data);
                    service.serviceGroups = {};
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Delete ServiceGroups.
         *
         * @param serviceGroups array of ServiceGroup instances to delete
         * @returns promise rejected or resolved
         */
        deleteServiceGroups: function(serviceGroups) {
            var deferred = $q.defer();
            var serviceGroupUpdates = [];
            _.forEach(serviceGroups, function(serviceGroup) {
                serviceGroupUpdates.push(_.pick(serviceGroup, ['name']));
            });
            var httpConfig = {
                method: 'DELETE',
                url: ServerService.api('/servicegroups'),
                headers: {'Content-Type': 'application/json'},
                data: {serviceGroups: serviceGroupUpdates}
            };
            _.merge(httpConfig, ServerService.apiConfig());
            $http(httpConfig)
                .success(function success(data, status) {
                    if (!!data && (data.successful === serviceGroups.length)) {
                        deferred.resolve(undefined);
                    } else {
                        deferred.reject("Service groups not deleted.");
                    }
                    service.serviceGroups = {};
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Get and cache all HostCategory root categories.
         *
         * @returns promise rejected or resolved with categories data object
         */
        getHostCategories: function() {
            var deferred = $q.defer();
            if (!_.isEmpty(service.hostCategories)) {
                deferred.resolve(service.hostCategories);
            } else {
                service.getCategoryHierarchyRoots('HOST_CATEGORY').then(
                    function success(categories) {
                        service.hostCategories = categories;
                        deferred.resolve(service.hostCategories);
                    },
                    function error(message) {
                        deferred.reject(message);
                    });
            }
            return deferred.promise;
        },

        /**
         * Get and cache all ServiceCategory root categories.
         *
         * @returns promise rejected or resolved with categories data object
         */
        getServiceCategories: function() {
            var deferred = $q.defer();
            if (!_.isEmpty(service.serviceCategories)) {
                deferred.resolve(service.serviceCategories);
            } else {
                service.getCategoryHierarchyRoots('SERVICE_CATEGORY').then(
                    function success(categories) {
                        service.serviceCategories = categories;
                        deferred.resolve(service.serviceCategories);
                    },
                    function error(message) {
                        deferred.reject(message);
                    });
            }
            return deferred.promise;
        },

        /**
         * Get AuditLog data via query. The query is specified as an object with the
         * following fields;
         *
         * 'subsystem' : matches subsystem
         * 'action' : matches action
         * 'description' : contains within description
         * 'username' : matches username
         * 'maxtimestamp' : matches timestamps before
         * 'mintimestamp' : matches timestamps after
         * 'hostname' : matches host name
         * 'servicedescription' : matches service description
         * 'hostgroupname' : matches host group name
         * 'servicegroupname' : matches service group name
         *
         * AuditLog data is returned in descending timestamp order, so most recent are
         * returned first. All matches are case insensitive.
         *
         * The hostname, hostname/servicedescription, hostgroupname, and servicegroupname
         * are mutually exclusive, so these are queried as ORs. The rest of the fields
         * are queried as ANDs.
         *
         * @param queryObject optional query object, defaults to {}
         * @param first optional first data item index, defaults to 0
         * @param count optional count to return, defaults to 25
         * @returns promise rejected or resolved with a wrapped list of AuditLog objects.
         */
        getAuditData: function(queryObject, first, count) {
            // gather/default parameters
            queryObject = (!!queryObject ? queryObject : {});
            first = (!!first ? first : 0);
            count = (!!count ? count : 25);

            // construct query with ordering
            var query = '';
            var hostNamePredicate;
            var serviceDescriptionPredicate;
            var hostGroupNamePredicate;
            var serviceGroupNamePredicate;
            _.forOwn(queryObject, function(value, field) {
                if (_.isDate(value)) {
                    value = formatTimestamp(value);
                } else if (_.isString(value)) {
                    value = value.toLowerCase();
                }
                switch (field.toLowerCase()) {
                    case 'subsystem' : query += (!!query ? ' AND ' : '') + 'lower(subsystem) = \''+value+'\''; break;
                    case 'action' : query += (!!query ? ' AND ' : '') + 'lower(action) = \''+value+'\''; break;
                    case 'description' : query += (!!query ? ' AND ' : '') + 'lower(description) like \'%'+value+'%\''; break;
                    case 'username' : query += (!!query ? ' AND ' : '') + 'lower(username) = \''+value+'\''; break;
                    case 'maxtimestamp' : query += (!!query ? ' AND ' : '') + 'timestamp <= \''+value+'\''; break;
                    case 'mintimestamp' : query += (!!query ? ' AND ' : '') + 'timestamp >= \''+value+'\''; break;
                    case 'hostname' : hostNamePredicate = 'lower(hostName) = \''+value+'\''; break;
                    case 'servicedescription' : serviceDescriptionPredicate = 'lower(serviceDescription) = \''+value+'\''; break;
                    case 'hostgroupname' : hostGroupNamePredicate = 'lower(hostGroupName) = \''+value+'\''; break;
                    case 'servicegroupname' : serviceGroupNamePredicate = 'lower(serviceGroupName) = \''+value+'\''; break;
                }
            });
            if (!!hostNamePredicate || !!serviceDescriptionPredicate || !!hostGroupNamePredicate || !!serviceGroupNamePredicate) {
                query += (!!query ? ' AND ' : '')+'( ';
                if (!!hostNamePredicate && !!serviceDescriptionPredicate) {
                    query += '( '+hostNamePredicate+' AND '+serviceDescriptionPredicate+' ) ';
                } else if (!!hostNamePredicate) {
                    query += '( '+hostNamePredicate+' AND serviceDescription IS NULL ) ';
                } else if (!!serviceDescriptionPredicate) {
                    query += '( hostName IS NOT NULL AND '+serviceDescriptionPredicate+' ) ';
                }
                if (!!hostGroupNamePredicate) {
                    query += (!~query.indexOf('( ', query.length-2) ? 'OR ' : '')+hostGroupNamePredicate+' ';
                }
                if (!!serviceGroupNamePredicate) {
                    query += (!~query.indexOf('( ', query.length-2) ? 'OR ' : '')+serviceGroupNamePredicate+' ';
                }
                query += ')';
            }
            if (!!query) {
                query += ' ORDER BY timestamp DESC, auditLogId DESC';
            }

            // make auditlogs query
            var deferred = $q.defer();
            var url = ServerService.api('/auditlogs');
            var config = ServerService.apiConfig();
            config.params = {first: first, count: count};
            if (!!query) {
                config.params.query = query;
            }
            $http.get(url, config)
                .success(function success(data, status) {
                    deferred.resolve(data);
                })
                .error(function error(message, status) {
                    if (status === 404) {
                        deferred.resolve({auditLogs: []});
                    } else {
                        rejectWithMessage(deferred, message);
                    }
                }
            );
            return deferred.promise;
        },

        /**
         * Get HostIdentities via query. The query is specified as an object with the
         * following fields;
         *
         * 'id' : matches host identity id
         * 'hostname' : contains within host name
         * 'hostnames' : contains within host names
         *
         * HostIdentities are returned in ascending host name order. All matches are
         * case insensitive.
         *
         * @param queryObject optional query object, defaults to {}
         * @param first optional first data item index, defaults to 0
         * @param count optional count to return, defaults to 25
         * @returns promise rejected or resolved with a wrapped list of HostIdentity objects.
         */
        getHostIdentities: function(queryObject, first, count) {
            // gather/default parameters
            queryObject = (!!queryObject ? queryObject : {});
            first = (!!first ? first : 0);
            count = (!!count ? count : 25);

            // construct query with ordering
            var query = '';
            _.forOwn(queryObject, function(value, field) {
                query = ((!!query) ? query+' AND ' : '');
                if (_.isString(value)) {
                    value = value.toLowerCase();
                }
                switch (field.toLowerCase()) {
                    case 'id' : query += 'hostIdentityId = \''+value+'\''; break;
                    case 'hostname' : query += 'lower(hostName) like \'%'+value+'%\''; break;
                    case 'hostnames' : query += 'lower(hostNames.id) like \'%'+value+'%\''; break;
                }
            });
            if (!!query) {
                query += ' ORDER BY hostName ASC';
            }

            // make hostidentities query
            var deferred = $q.defer();
            var url = ServerService.api('/hostidentities');
            var config = ServerService.apiConfig();
            config.params = {first: first, count: count};
            if (!!query) {
                config.params.query = query;
            }
            $http.get(url, config)
                .success(function success(data, status) {
                    deferred.resolve(data);
                })
                .error(function error(message, status) {
                    if (status === 404) {
                        deferred.resolve({hostIdentities: []});
                    } else {
                        rejectWithMessage(deferred, message);
                    }
                }
            );
            return deferred.promise;
        },

        /**
         * Lookup a HostIdentity by id or host names. Matches are case insensitive.
         *
         * @param idOrHostName host identity id or host name
         * @returns promise rejected or resolved with a HostIdentity object.
         */
        getHostIdentity: function(idOrHostName) {
            var deferred = $q.defer();
            var url = ServerService.api('/hostidentities', encodeURIComponent(idOrHostName));
            $http.get(url, ServerService.apiConfig())
                .success(function success(data, status) {
                    deferred.resolve(data);
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Create or update host identity. Operation is considered a create if the
         * host name is unique and the id is undefined.
         *
         * @param hostIdentity host identity to create or update
         * @returns promise rejected or resolved with entity string
         */
        createOrUpdateHostIdentity: function(hostIdentity) {
            var deferred = $q.defer();
            var url = ServerService.api('/hostidentities');
            // create or update post closure
            var createOrUpdatePost = function() {
                $http.post(url, {hostIdentities: [hostIdentity]}, ServerService.apiConfig())
                    .success(function success(data, status) {
                        resolveWithEntityOrRejectWithMessage(deferred, data);
                    })
                    .error(function error(message, status) {
                        rejectWithMessage(deferred, message);
                    }
                );
            }
            // clear host identity before update to support replace/put update
            // mode, (update is normally an upsert/post)
            if (!!hostIdentity.hostIdentityId) {
                var httpConfig = {
                    method: 'DELETE',
                    url: url,
                    headers: {'Content-Type': 'application/json'},
                    data: {hostIdentities: [_.pick(hostIdentity, ['hostIdentityId', 'hostName'])]},
                    params: {clear: true}
                };
                _.merge(httpConfig, ServerService.apiConfig());
                $http(httpConfig)
                    .success(function success(data, status) {
                        if (!!data && (data.successful === 1)) {
                            // update host identity
                            createOrUpdatePost();
                        } else {
                            // clear host identity fails for update
                            resolveWithEntityOrRejectWithMessage(deferred, data);
                        }
                    })
                    .error(function error(message, status) {
                        rejectWithMessage(deferred, message);
                    }
                );
            } else {
                // create host identity
                createOrUpdatePost();
            }
            return deferred.promise;
        },

        /**
         * Delete host identities.
         *
         * @param hostIdentities array of host identities to delete
         * @returns promise rejected or resolved
         */
        deleteHostIdentities: function(hostIdentities) {
            var deferred = $q.defer();
            var hostIdentityIdAndHostNames = [];
            _.forEach(hostIdentities, function(hostIdentity) {
                hostIdentityIdAndHostNames.push(_.pick(hostIdentity, ['hostIdentityId', 'hostName']));
            });
            var httpConfig = {
                method: 'DELETE',
                url: ServerService.api('/hostidentities'),
                headers: {'Content-Type': 'application/json'},
                data: {hostIdentities: hostIdentityIdAndHostNames}
            };
            _.merge(httpConfig, ServerService.apiConfig());
            $http(httpConfig)
                .success(function success(data, status) {
                    if (!!data && (data.successful === hostIdentities.length)) {
                        deferred.resolve(undefined);
                    } else {
                        deferred.reject("Host identities not deleted.");
                    }
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Get HostBlacklists via query. The query is specified as an object with the
         * following fields;
         *
         * 'id' : matches host blacklist id
         * 'hostname' : contains within host name regular expression
         *
         * HostBlacklists are returned in ascending host name regular expression order.
         * All matches are case insensitive.
         *
         * @param queryObject optional query object, defaults to {}
         * @param first optional first data item index, defaults to 0
         * @param count optional count to return, defaults to 25
         * @returns promise rejected or resolved with a wrapped list of HostBlacklist objects.
         */
        getHostBlacklists: function(queryObject, first, count) {
            // gather/default parameters
            queryObject = (!!queryObject ? queryObject : {});
            first = (!!first ? first : 0);
            count = (!!count ? count : 25);

            // construct query with ordering
            var query = '';
            _.forOwn(queryObject, function(value, field) {
                query = ((!!query) ? query+' AND ' : '');
                if (_.isString(value)) {
                    value = value.toLowerCase();
                }
                switch (field.toLowerCase()) {
                    case 'id' : query += 'hostBlacklistId = \''+value+'\''; break;
                    case 'hostname' : query += 'lower(hostName) like \'%'+value+'%\''; break;
                }
            });
            if (!!query) {
                query += ' ORDER BY hostName ASC';
            }

            // make hostblacklists query
            var deferred = $q.defer();
            var url = ServerService.api('/hostblacklists');
            var config = ServerService.apiConfig();
            config.params = {first: first, count: count};
            if (!!query) {
                config.params.query = query;
            }
            $http.get(url, config)
                .success(function success(data, status) {
                    deferred.resolve(data);
                })
                .error(function error(message, status) {
                    if (status === 404) {
                        deferred.resolve({hostBlacklists: []});
                    } else {
                        rejectWithMessage(deferred, message);
                    }
                }
            );
            return deferred.promise;
        },

        /**
         * Lookup a HostBlacklist by host name regular expression.
         *
         * @param hostName host blacklist host name regular expression
         * @returns promise rejected or resolved with a HostBlacklist object.
         */
        getHostBlacklist: function(hostName) {
            var deferred = $q.defer();
            var url = ServerService.api('/hostblacklists', encodeURIComponent(hostName));
            $http.get(url, ServerService.apiConfig())
                .success(function success(data, status) {
                    deferred.resolve(data);
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Create or update host blacklist. Operation is considered a create if the
         * host name is unique and the id is undefined.
         *
         * @param hostBlacklist host blacklist to create or update
         * @returns promise rejected or resolved with entity string
         */
        createOrUpdateHostBlacklist: function(hostBlacklist) {
            var deferred = $q.defer();
            var url = ServerService.api('/hostblacklists');
            $http.post(url, {hostBlacklists: [hostBlacklist]}, ServerService.apiConfig())
                .success(function success(data, status) {
                    resolveWithEntityOrRejectWithMessage(deferred, data);
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Delete host blacklists.
         *
         * @param hostBlacklists array of host blacklists to delete
         * @returns promise rejected or resolved
         */
        deleteHostBlacklists: function(hostBlacklists) {
            var deferred = $q.defer();
            var hostBlacklistIdAndHostNames = [];
            _.forEach(hostBlacklists, function(hostBlacklist) {
                hostBlacklistIdAndHostNames.push(_.pick(hostBlacklist, ['hostBlacklistId', 'hostName']));
            });
            var httpConfig = {
                method: 'DELETE',
                url: ServerService.api('/hostblacklists'),
                headers: {'Content-Type': 'application/json'},
                data: {hostBlacklists: hostBlacklistIdAndHostNames}
            };
            _.merge(httpConfig, ServerService.apiConfig());
            $http(httpConfig)
                .success(function success(data, status) {
                    if (!!data && (data.successful === hostBlacklists.length)) {
                        deferred.resolve(undefined);
                    } else {
                        deferred.reject("Host blacklists not deleted.");
                    }
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Get DeviceTemplateProfiles via query. The query is specified as an object with the
         * following fields;
         *
         * 'id' : matches device template profile id
         * 'device' : contains within device identification
         * 'description' : contains within device description
         * 'template' : contains within Cacti host template
         * 'profile' : contains within Monarch host profile
         * 'maxtimestamp' : matches timestamps before
         * 'mintimestamp' : matches timestamps after
         *
         * DeviceTemplateProfiles are returned in ascending device identification order.
         * All matches are case insensitive.
         *
         * @param queryObject optional query object, defaults to {}
         * @param first optional first data item index, defaults to 0
         * @param count optional count to return, defaults to 25
         * @returns promise rejected or resolved with a wrapped list of DeviceTemplateProfile objects.
         */
        getDeviceTemplateProfiles: function(queryObject, first, count) {
            // gather/default parameters
            queryObject = (!!queryObject ? queryObject : {});
            first = (!!first ? first : 0);
            count = (!!count ? count : 25);

            // construct query with ordering
            var query = '';
            _.forOwn(queryObject, function(value, field) {
                query = ((!!query) ? query+' AND ' : '');
                if (_.isDate(value)) {
                    value = formatTimestamp(value);
                } else if (_.isString(value)) {
                    value = value.toLowerCase();
                }
                switch (field.toLowerCase()) {
                    case 'id' : query += 'deviceTemplateProfileId = \''+value+'\''; break;
                    case 'device' : query += 'lower(deviceIdentification) like \'%'+value+'%\''; break;
                    case 'description' : query += 'lower(deviceDescription) like \'%'+value+'%\''; break;
                    case 'template' : query += 'lower(cactiHostTemplate) like \'%'+value+'%\''; break;
                    case 'profile' : query += 'lower(monarchHostProfile) like \'%'+value+'%\''; break;
                    case 'maxtimestamp' : query += 'timestamp <= \''+value+'\''; break;
                    case 'mintimestamp' : query += 'timestamp >= \''+value+'\''; break;
                }
            });
            if (!!query) {
                query += ' ORDER BY deviceIdentification ASC';
            }

            // make devicetemplateprofiles query
            var deferred = $q.defer();
            var url = ServerService.api('/devicetemplateprofiles');
            var config = ServerService.apiConfig();
            config.params = {first: first, count: count};
            if (!!query) {
                config.params.query = query;
            }
            $http.get(url, config)
                .success(function success(data, status) {
                    deferred.resolve(data);
                })
                .error(function error(message, status) {
                    if (status === 404) {
                        deferred.resolve({deviceTemplateProfiles: []});
                    } else {
                        rejectWithMessage(deferred, message);
                    }
                }
            );
            return deferred.promise;
        },

        /**
         * Lookup a DeviceTemplateProfile by device identification.
         *
         * @param deviceIdentification device template profile device identification
         * @returns promise rejected or resolved with a DeviceTemplateProfile object.
         */
        getDeviceTemplateProfile: function(deviceIdentification) {
            var deferred = $q.defer();
            var url = ServerService.api('/devicetemplateprofiles', encodeURIComponent(deviceIdentification));
            $http.get(url, ServerService.apiConfig())
                .success(function success(data, status) {
                    deferred.resolve(data);
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Create or update device template profile. Operation is considered a create if the
         * device identification is unique and the id is undefined.
         *
         * @param deviceTemplateProfile device template profile to create or update
         * @returns promise rejected or resolved with entity string
         */
        createOrUpdateDeviceTemplateProfile: function(deviceTemplateProfile) {
            var deferred = $q.defer();
            var url = ServerService.api('/devicetemplateprofiles');
            // create or update post closure
            var createOrUpdatePost = function() {
                $http.post(url, {deviceTemplateProfiles: [deviceTemplateProfile]}, ServerService.apiConfig())
                    .success(function success(data, status) {
                        resolveWithEntityOrRejectWithMessage(deferred, data);
                    })
                    .error(function error(message, status) {
                        rejectWithMessage(deferred, message);
                    }
                );
            };
            // clear device template profile before update to support replace/put
            // update mode, (update is normally an upsert/post)
            if (!!deviceTemplateProfile.deviceTemplateProfileId) {
                var httpConfig = {
                    method: 'DELETE',
                    url: url,
                    headers: {'Content-Type': 'application/json'},
                    data: {deviceTemplateProfiles: [_.pick(deviceTemplateProfile, ['deviceTemplateProfileId', 'deviceIdentification'])]},
                    params: {clear: true}
                };
                _.merge(httpConfig, ServerService.apiConfig());
                $http(httpConfig)
                    .success(function success(data, status) {
                        if (!!data && (data.successful === 1)) {
                            // update device template profile
                            createOrUpdatePost();
                        } else {
                            // clear device template profile fails for update
                            resolveWithEntityOrRejectWithMessage(deferred, data);
                        }
                    })
                    .error(function error(message, status) {
                        rejectWithMessage(deferred, message);
                    }
                );
            } else {
                // create device template profile
                createOrUpdatePost();
            }
            return deferred.promise;
        },

        /**
         * Delete device template profiles.
         *
         * @param deviceTemplateProfiles array of device template profiles to delete
         * @returns promise rejected or resolved
         */
        deleteDeviceTemplateProfiles: function(deviceTemplateProfiles) {
            var deferred = $q.defer();
            var deviceTemplateProfileIdAndDeviceIdentifications = [];
            _.forEach(deviceTemplateProfiles, function(deviceTemplateProfile) {
                deviceTemplateProfileIdAndDeviceIdentifications.push(_.pick(deviceTemplateProfile, ['deviceTemplateProfileId', 'deviceIdentification']));
            });
            var httpConfig = {
                method: 'DELETE',
                url: ServerService.api('/devicetemplateprofiles'),
                headers: {'Content-Type': 'application/json'},
                data: {deviceTemplateProfiles: deviceTemplateProfileIdAndDeviceIdentifications}
            };
            _.merge(httpConfig, ServerService.apiConfig());
            $http(httpConfig)
                .success(function success(data, status) {
                    if (!!data && (data.successful === deviceTemplateProfiles.length)) {
                        deferred.resolve(undefined);
                    } else {
                        deferred.reject("Device template profiles not deleted.");
                    }
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Get all entity types supported by categories.
         *
         * @returns promise rejected or resolved
         */
        getCategoryEntityTypes: function() {
            // hard coded because the entity types supported by categories
            // cannot be queried from all entity types
            var deferred = $q.defer();
            deferred.resolve({
                entityTypes: [ {
                    id: 2,
                    name: "SERVICE_STATUS",
                    description: "com.groundwork.collage.model.impl.ServiceStatus",
                    logicalEntity: false
                }, {
                    id: 5,
                    name: "HOST",
                    description: "com.groundwork.collage.model.impl.Host",
                    logicalEntity: false
                }, {
                    id: 6,
                    name: "HOSTGROUP",
                    description: "com.groundwork.collage.model.impl.HostGroup",
                    logicalEntity: false
                }, {
                    id: 23,
                    name: "SERVICE_GROUP",
                    description: "com.groundwork.collage.model.impl.ServiceGroup",
                    logicalEntity: true
                }, {
                    id: 24,
                    name: "CUSTOM_GROUP",
                    description: "com.groundwork.collage.model.impl.CustomGroup",
                    logicalEntity: true
                }, {
                    id: 25,
                    name: "HOST_CATEGORY",
                    description: "com.groundwork.collage.model.impl.HostCategory",
                    logicalEntity: true
                }, {
                    id: 26,
                    name: "SERVICE_CATEGORY",
                    description: "com.groundwork.collage.model.impl.ServiceCategory",
                    logicalEntity: true
                } ]
            });
            return deferred.promise;
        },

        /**
         * Get all category hierarchy roots for a specified entity type.
         *
         * @param entityTypeName category entity type name
         * @returns promise rejected or resolved
         */
        getCategoryHierarchyRoots: function(entityTypeName) {
            // get category hierarchy roots
            var deferred = $q.defer();
            var url = ServerService.api('/categories');
            url += '?entityTypeName='+encodeURIComponent(entityTypeName)+'&roots=true';
            $http.get(url, ServerService.apiConfig())
                .success(function success(data, status) {
                    deferred.resolve(data);
                })
                .error(function error(message, status) {
                    if(status == 404) {
                        deferred.resolve({categories: []});
                    }
                    else {
                        rejectWithMessage(deferred, message);
                    }
                }
            );
            return deferred.promise;
        },

        /**
         * Get all categories in a hierarchy from the specified category.
         * This is normally invoked passing root categories, but any
         * category can be specified. The returned categories include
         * the specified category and all deep children of it, including
         * categories that are shared with other hierarchies. Categories
         * are returned as nested children of specified category.
         *
         * @param category hierarchy category
         * @returns promise rejected or resolved
         */
        getCategoryHierarchy: function(category) {
            // extract category name and entity type name
            var name = category.name;
            var entityTypeName = category.entityTypeName;
            // get nested category hierarchy via full depth
            var deferred = $q.defer();
            var url = ServerService.api('/categories', encodeURIComponent(name), encodeURIComponent(entityTypeName));
            url += '?depth=full';
            $http.get(url, ServerService.apiConfig())
                .success(function success(data, status) {
                    deferred.resolve(data);
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Get a category by name and entity type. Does not include
         * parents, children, ancestors, or entities.
         *
         * @param name category name
         * @param entityTypeName category entity type name
         * @returns promise rejected or resolved
         */
        getCategory: function(name, entityTypeName) {
            // get category
            var deferred = $q.defer();
            var url = ServerService.api('/categories', encodeURIComponent(name), encodeURIComponent(entityTypeName));
            $http.get(url, ServerService.apiConfig())
                .success(function success(data, status) {
                    deferred.resolve(data);
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Create a root category.
         *
         * @param category category object to create
         * @returns promise rejected or resolved
         */
        createRootCategory: function(category) {
            // construct category update
            var categoryUpdate = {
                create: 'AS_ROOT',
                categoryName: category.name
            };
            _.merge(categoryUpdate, _.pick(category, ['entityTypeName', 'description', 'appType', 'agentId']));
            // update category
            return updateCategory(categoryUpdate);
        },

        /**
         * Create a child category. The child category can be optionally
         * inserted into the hierarchy by adopting parent's children.
         *
         * @param category category object to create
         * @param parent parent category object
         * @param insert insert child into hierarchy
         * @returns promise rejected or resolved
         */
        createCategory: function(category, parent, insert) {
            // construct category update
            var categoryUpdate = {
                create: (!!insert ? 'AS_CHILD_WITH_PARENT_CHILDREN' : 'AS_CHILD'),
                categoryName: category.name,
                parentName: parent.name
            };
            _.merge(categoryUpdate, _.pick(category, ['entityTypeName', 'description', 'appType', 'agentId']));
            // update category
            return updateCategory(categoryUpdate);
        },

        /**
         * Delete a leaf category. Category will only be deleted if it
         * has no children.
         *
         * @param category category object to delete
         * @returns promise rejected or resolved
         */
        deleteLeafCategory: function(category) {
            // construct category update
            var categoryUpdate = {
                delete: 'LEAF_ONLY',
                categoryName: category.name,
                entityTypeName: category.entityTypeName
            };
            // update category
            return updateCategory(categoryUpdate);
        },

        /**
         * Delete a category from its hierarchy. Children of category
         * can optionally be adopted by parent or become root categories.
         *
         * @param category category object to delete
         * @param orphanAsRoot children are to become root categories
         * @returns promise rejected or resolved
         */
        deleteCategory: function(category, orphanAsRoot) {
            // construct category update
            var categoryUpdate = {
                delete: (!!orphanAsRoot ? 'ORPHAN_CHILDREN_AS_ROOTS' : 'ADD_CHILDREN_TO_PARENTS'),
                categoryName: category.name,
                entityTypeName: category.entityTypeName
            };
            // update category
            return updateCategory(categoryUpdate);
        },

        /**
         * Cascade delete a category and its hierarchy. All children can
         * optionally be deleted even if shared in other hierarchies; by
         * default, only the specified category and deep children it
         * exclusively references are deleted.
         *
         * @param category category object to delete
         * @param allChildren cascade delete all children
         * @returns promise rejected or resolved
         */
        cascadeDeleteCategoryHierarchy: function(category, allChildren) {
            // construct category update
            var categoryUpdate = {
                delete: (!!allChildren ? 'CASCADE_ALL' : 'CASCADE'),
                categoryName: category.name,
                entityTypeName: category.entityTypeName
            };
            // update category
            return updateCategory(categoryUpdate);
        },

        /**
         * Make category root of its own hierarchy.
         *
         * @param category category object to make root
         * @param removeAllParents remove from all parents in addition
         * @returns promise rejected or resolved
         */
        rootCategory: function(category, removeAllParents) {
            // construct category update
            var categoryUpdate = {
                modify: (!!removeAllParents ? 'ROOT_REMOVE_PARENTS' : 'ROOT'),
                categoryName: category.name,
                entityTypeName: category.entityTypeName
            };
            // update category
            return updateCategory(categoryUpdate);
        },

        /**
         * Remove category from root categories that is also a child member of
         * another category. Note that this will not delete a root category if
         * it is a standalone category root: it will remain a root category.
         *
         * @param category category object to remove from roots
         * @returns promise rejected or resolved
         */
        unrootCategory: function(category) {
            // construct category update
            var categoryUpdate = {
                modify: 'UNROOT',
                categoryName: category.name,
                entityTypeName: category.entityTypeName
            };
            // update category
            return updateCategory(categoryUpdate);
        },

        /**
         * Add category as a child to a parent category. Operation can
         * optionally move child by removing it from any existing parents.
         *
         * @param category category object to add or move
         * @param parent parent category object
         * @param move move child instead of adding
         * @returns promise rejected or resolved
         */
        addCategory: function(category, parent, move) {
            // construct category update
            var categoryUpdate = {
                modify: (!!move ? 'MOVE_CHILD' : 'ADD_CHILD'),
                categoryName: category.name,
                entityTypeName: category.entityTypeName,
                otherCategoryNames: [parent.name]
            };
            // update category
            return updateCategory(categoryUpdate);
        },

        /**
         * Update category. Description, agent id, application type, and
         * root state can be updated.
         *
         * @param category category to be updated
         * @returns promise rejected or resolved
         */
        updateCategory: function(category) {
            // update category
            var deferred = $q.defer();
            var url = ServerService.api('/categories');
            $http.post(url, {categories: [category]}, ServerService.apiConfig())
                .success(function success(data, status) {
                    resolveWithEntityOrRejectWithMessage(deferred, data);
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Add entities to category. Entity ids can be specified as a single
         * numeric id or an array of ids.
         *
         * @param category category object to update
         * @param entityIds single or array of entity ids, (numbers), to add
         * @param entityTypeName entity entity type name to add
         * @returns promise rejected or resolved
         */
        addCategoryEntities: function(category, entityIds, entityTypeName) {
            // add category members
            var categoryMemberUpdate = makeCategoryMemberUpdate(category, entityIds, entityTypeName);
            var deferred = $q.defer();
            var url = ServerService.api('/categories/addmembers');
            $http.put(url, categoryMemberUpdate, ServerService.apiConfig())
                .success(function success(data, status) {
                    resolveWithEntityOrRejectWithMessage(deferred, data);
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Delete entities from category. Entity ids can be specified as a single
         * numeric id or an array of ids. Entities themselves are not deleted.
         *
         * @param category category object to update
         * @param entityIds single or array of entity ids, (numbers), to delete
         * @param entityTypeName entity entity type name to delete
         * @returns promise rejected or resolved
         */
        deleteCategoryEntities: function(category, entityIds, entityTypeName) {
            // delete category members
            var categoryMemberUpdate = makeCategoryMemberUpdate(category, entityIds, entityTypeName);
            var deferred = $q.defer();
            var url = ServerService.api('/categories/deletemembers');
            $http.put(url, categoryMemberUpdate, ServerService.apiConfig())
                .success(function success(data, status) {
                    resolveWithEntityOrRejectWithMessage(deferred, data);
                })
                .error(function error(message, status) {
                    rejectWithMessage(deferred, message);
                }
            );
            return deferred.promise;
        },

        /**
         * Modify category entities. Entity ids can be specified as a single
         * numeric id or an array of ids. Entities themselves are not deleted.
         *
         * @param category category object to update
         * @param deleteEntityIds single or array of entity ids, (numbers), to delete
         * @param addEntityIds single or array of entity ids, (numbers), to add
         * @param entityTypeName entity entity type name to add or delete
         * @returns promise rejected or resolved
         */
        modifyCategoryEntities: function(category, deleteEntityIds, addEntityIds, entityTypeName) {
            // trivially invoke either deleteCategoryEntities or addCategoryEntities
            if (!_.isNumber(addEntityIds) && _.isEmpty(addEntityIds)) {
                return service.deleteCategoryEntities(category, deleteEntityIds, entityTypeName);
            }
            if (!_.isNumber(deleteEntityIds) && _.isEmpty(deleteEntityIds)) {
                return service.addCategoryEntities(category, addEntityIds, entityTypeName);
            }
            // invoke both deleteCategoryEntities and addCategoryEntities
            var deferred = $q.defer();
            service.deleteCategoryEntities(category, deleteEntityIds, entityTypeName).then(
                function success() {
                    service.addCategoryEntities(category, addEntityIds, entityTypeName).then(
                        function success(entity) {
                            deferred.resolve(entity);
                        },
                        function error(message) {
                            deferred.reject(message);
                        }
                    );
                },
                function error(message) {
                    deferred.reject(message);
                }
            );
            return deferred.promise;
        },

        /**
         * Get autocomplete names for prefix. Total counts are not returned by
         * this function, (see suggestions() below). A null, undefined, blank,
         * or '*' wildcard prefix matches all names. The limit parameter
         * sets a limit on the returned names; if it is negative, the number of
         * names returned is unlimited. The number of names returned can exceed
         * the limit if canonical names are returned: the limit is applied to
         * unique canonical names in that case.
         *
         * Supported entity types include: 'HOST', 'SERVICE', 'HOSTGROUP',
         * 'SERVICE_GROUP', and 'CUSTOM_GROUP'. Host names include host and
         * host identity names.
         *
         * @param prefix names prefix
         * @param limit returned names limit, (or -1 for unlimited)
         * @param entityTypeName entity type name
         * @returns promise rejected or resolved with array of names objects with
         * name and canonicalName members.
         */
        autocomplete: function(prefix, limit, entityTypeName) {
            var deferred = $q.defer();
            prefix = (!_.isEmpty(prefix) ? prefix : '*');
            var autocompleteApiRoot;
            switch (entityTypeName) {
                case 'HOST' : autocompleteApiRoot = '/hostidentities'; break;
                case 'SERVICE' : autocompleteApiRoot = '/services'; break;
                case 'HOSTGROUP' : autocompleteApiRoot = '/hostgroups'; break;
                case 'SERVICE_GROUP' : autocompleteApiRoot = '/servicegroups'; break;
                case 'CUSTOM_GROUP' : autocompleteApiRoot = '/customgroups'; break;
            }
            if (!limit || !autocompleteApiRoot) {
                deferred.resolve([]);
            } else {
                var url = ServerService.api(autocompleteApiRoot, 'autocomplete', encodeURIComponent(prefix));
                var config = ServerService.apiConfig();
                config.params = {
                    "limit" : limit
                };
                $http.get(url, config)
                    .success(function success(data, status) {
                        deferred.resolve(data.names);
                    })
                    .error(function error(message, status) {
                        if (status == 404) {
                            deferred.resolve([]);
                        } else {
                            rejectWithMessage(deferred, message);
                        }
                    }
                );
            }
            return deferred.promise;
        },

        /**
         * Get suggestion names and total count for pattern. Pattern can include
         * '*' or '?' wildcards. Prefix queries can be performed by appending the
         * '*' wildcard to the prefix. A null, undefined, or blank pattern is
         * will match all names. The limit parameter sets a limit on the returned
         * names; if it is negative, the number of names returned is unlimited.
         *
         * Supported entity types include: 'HOST', 'SERVICE', 'HOSTGROUP',
         * 'SERVICE_GROUP', and 'CUSTOM_GROUP'. Host suggestions include host and
         * host identity names.
         *
         * @param pattern names pattern
         * @param limit returned names limit, (or -1 for unlimited)
         * @param entityTypeName entity type name
         * @returns promise rejected or resolved with object containing names array and total count
         */
        suggestions: function(pattern, limit, entityTypeName) {
            var deferred = $q.defer();
            if (!limit || !entityTypeName) {
                deferred.resolve({names: [], count: 0});
            } else {
                var url = ServerService.api('/suggestions/query', entityTypeName,
                    (!_.isEmpty(pattern) ? encodeURIComponent(pattern) : undefined));
                var config = ServerService.apiConfig();
                config.params = {
                    "limit" : limit
                };
                $http.get(url, config)
                    .success(function success(data, status) {
                        if (_.has(data, 'suggestions') && _.has(data, 'count')) {
                            deferred.resolve({names: _.pluck(data.suggestions, 'name'), count: data.count});
                        } else {
                            deferred.resolve({names: [], count: 0});
                        }
                    })
                    .error(function error(message, status) {
                        if (status == 404) {
                            deferred.resolve({names: [], count: 0});
                        } else {
                            rejectWithMessage(deferred, message);
                        }
                    }
                );
            }
            return deferred.promise;
        },

        /**
         * Get service descriptions for specified host.
         *
         * @param hostName host name
         * @returns promise rejected or resolved with names array
         */
        hostServiceDescriptions: function(hostName) {
            var deferred = $q.defer();
            if (_.isEmpty(hostName)) {
                deferred.resolve([]);
            } else {
                var url = ServerService.api('/suggestions/services', encodeURIComponent(hostName));
                $http.get(url, ServerService.apiConfig())
                    .success(function success(data, status) {
                        deferred.resolve(_.pluck(data.names, 'name'));
                    })
                    .error(function error(message, status) {
                        if (status == 404) {
                            deferred.resolve([]);
                        } else {
                            rejectWithMessage(deferred, message);
                        }
                    }
                );
            }
            return deferred.promise;
        },

        /**
         * Get names of all hosts with specified service.
         *
         * @param serviceDescription service description
         * @returns promise rejected or resolved with array of names objects with
         * name and canonicalName members.
         */
        serviceHostNames: function(serviceDescription) {
            var deferred = $q.defer();
            if (_.isEmpty(serviceDescription)) {
                deferred.resolve([]);
            } else {
                var url = ServerService.api('/suggestions/hosts', encodeURIComponent(serviceDescription));
                $http.get(url, ServerService.apiConfig())
                    .success(function success(data, status) {
                        deferred.resolve(data.names);
                    })
                    .error(function error(message, status) {
                        if (status == 404) {
                            deferred.resolve([]);
                        } else {
                            rejectWithMessage(deferred, message);
                        }
                    }
                );
            }
            return deferred.promise;
        },

        /**
         * Get all non-nagios host names.
         *
         * @return promise rejected or resolved with array of host names
         */
        getNonNagiosHostNames: function() {
            var deferred = $q.defer();
            var url = ServerService.api('/hosts');
            var config = ServerService.apiConfig();
            config.params = {query: 'appType != \'NAGIOS\'', depth: 'simple'};
            $http.get(url, config)
                .success(function success(data, status) {
                    if (!!data && !_.isEmpty(data.hosts)) {
                        var hostNames = _.pluck(data.hosts, 'hostName');
                        deferred.resolve(hostNames);
                    } else {
                        deferred.resolve([]);
                    }
                })
                .error(function error(message, status) {
                    if (status === 404) {
                        deferred.resolve([]);
                    } else {
                        rejectWithMessage(deferred, message);
                    }
                }
            );
            return deferred.promise;
        },

        /**
         * Delete specified hosts.
         *
         * @param hostNames host names to delete
         * @return promise rejected or resolved
         */
        deleteHosts: function(hostNames) {
            var deferred = $q.defer();
            if (_.isEmpty(hostNames)) {
                deferred.resolve();
            } else {
                var hosts = [];
                _.forEach(hostNames, function(hostName) {
                    hosts.push({hostName: hostName});
                });
                var httpConfig = {
                    method: 'DELETE',
                    url: ServerService.api('/hosts'),
                    headers: {'Content-Type': 'application/json'},
                    data: {hosts: hosts}
                };
                _.merge(httpConfig, ServerService.apiConfig());
                $http(httpConfig)
                    .success(function success(data, status) {
                        deferred.resolve();
                    })
                    .error(function error(message, status) {
                        rejectWithMessage(deferred, message);
                    }
                );
            }
            return deferred.promise;
        }
    };

    /**
     * Format Date into SQL string.
     *
     * @param date Date instance
     * @returns formatted SQL string
     */
    function formatTimestamp(date) {
        return date.getFullYear()+'-'+('0'+(date.getMonth()+1)).slice(-2)+'-'+('0'+date.getDate()).slice(-2)+' '+('0'+date.getHours()).slice(-2)+':'+('0'+date.getMinutes()).slice(-2)+':'+('0'+date.getSeconds()).slice(-2);
    }

    /**
     * Update category. Category update parameter describes the update
     * operation: create, clone, modify, or delete.
     *
     * @param categoryUpdate category update operation
     * @returns promise rejected or resolved
     */
    function updateCategory(categoryUpdate) {
        // update category
        var deferred = $q.defer();
        var url = ServerService.api('/categories');
        $http.put(url, {categoryUpdates: [categoryUpdate]}, ServerService.apiConfig())
            .success(function success(data, status) {
                resolveWithEntityOrRejectWithMessage(deferred, data);
            })
            .error(function error(message, status) {
                rejectWithMessage(deferred, message);
            }
        );
        return deferred.promise;
    }

    /**
     * Resolve with entity or reject with message.
     *
     * @param deferred deferred prommise
     * @param data results data
     */
    function resolveWithEntityOrRejectWithMessage(deferred, data) {
        if (!!data && (data.successful === 1)) {
            var entity = ((!!data && !!data.results && data.results.length) ? data.results[0].entity : undefined);
            deferred.resolve(entity);
        } else {
            var message = ((!!data && !!data.results && data.results.length) ? data.results[0].message : undefined);
            deferred.reject(message);
        }
    }

    /**
     * Reject with message.
     *
     * @param deferred deferred promise
     * @param message results data or message
     */
    function rejectWithMessage(deferred, message) {
        if (_.isObject(message)) {
            message = message.error;
        }
        deferred.reject(message);
    }

    /**
     * Construct category member update object.
     *
     * @param category category object to update
     * @param entityIds single or array of entity ids, (numbers)
     * @param entityTypeName entity entity type name
     * @returns category member update object
     */
    function makeCategoryMemberUpdate(category, entityIds, entityTypeName) {
        var categoryMemberUpdate = {
            name: category.name,
            entityTypeName: category.entityTypeName,
            entities: []
        };
        if (_.isArray(entityIds)) {
            _.forEach(entityIds, function(entityId) {
                categoryMemberUpdate.entities.push({
                    objectID: entityId,
                    entityTypeName: entityTypeName
                });
            });
        } else if (_.isNumber(entityIds)) {
            categoryMemberUpdate.entities.push({
                objectID: entityIds,
                entityTypeName: entityTypeName
            });
        }
        return categoryMemberUpdate;
    }

    return service;
};

// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('myApp.services', []).
    value('version', '0.1');
