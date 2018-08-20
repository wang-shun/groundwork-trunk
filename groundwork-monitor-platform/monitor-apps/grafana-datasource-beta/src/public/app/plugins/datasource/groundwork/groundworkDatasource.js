define([
    'angular',
    'lodash',
    './public/vendor/xml-to-json/xml',
    './datasource',
    './groundworkQueryCtrl'
],
function (angular, _) {
    'use strict';

    var module = angular.module('grafana.services');

    module.factory('GroundWorkOpenTSDBDatasource', function($q, $http, backendSrv, templateSrv, OpenTSDBDatasource) {

        var DEBUG = true;

        var GW_SSO_UID_COOKIE = 'gwuid';
        var GW_API_TOKEN_COOKIE = 'FoundationToken';
        var GW_APP_NAME = 'monitor-dashboard';

        var OPENTSDB_GROUP_WILDCARD = '*';
        var GW_OPENTSDB_HOSTNAME_TAG = 'hostname';

        function GroundWorkOpenTSDBDatasource(datasource) {
            this.openTSDBDatasource = new OpenTSDBDatasource(datasource);
            this.gwUid = __getCookie(GW_SSO_UID_COOKIE);
            this.gwApiHttpConfig = {
                withCredentials: false,
                headers: {
                    'GWOS-API-TOKEN': __getCookie(GW_API_TOKEN_COOKIE),
                    'GWOS-APP-NAME' : GW_APP_NAME
                }
            };
            this.gwXMLApiHttpConfig = _.defaults({
                transformResponse : function(data) {
                    return window.xml.xmlToJSON(data);
                }}, this.gwApiHttpConfig);
            this.gwApiRootUrl = window.location.origin;
            this.gwHostGroups = [];
            this.gwServiceGroups = [];
            this.gwAuthorizedServices = {};
            this.gwAuthorizedServicesPromise = this.__initGWAuthorizedServices();
        }
        
        GroundWorkOpenTSDBDatasource.prototype.query = function(options) {
            if (DEBUG) {
                console.log(">>>>>>>>>>>>>>>>>> GroundWorkOpenTSDBDatasource.query()");
            }
            var thisDatasource = this;
            var deferred = $q.defer();
            thisDatasource.__hasGWAuthorizedServices().then(function success(hasAuthorizedServices) {
                if (DEBUG) {
                    console.log(">>>>>>>>>>>>>>>>>> GroundWorkOpenTSDBDatasource.query() options: " +
                        JSON.stringify(options));
                }
                options = thisDatasource.__filterQueryOptions(hasAuthorizedServices, options);
                if (DEBUG) {
                    console.log(">>>>>>>>>>>>>>>>>> GroundWorkOpenTSDBDatasource.query() filtered options: " +
                        JSON.stringify(options));
                }
                thisDatasource.openTSDBDatasource.query(options).then(function success(data) {
                    deferred.resolve(data);
                }, function error(message) {
                    deferred.reject(message);
                });
            }, function error(message) {
                deferred(message);
            });
            return deferred.promise;
        };

        GroundWorkOpenTSDBDatasource.prototype.metricFindQuery = function(query) {
            if (DEBUG) {
                console.log(">>>>>>>>>>>>>>>>>> GroundWorkOpenTSDBDatasource.metricFindQuery()");
            }
            var thisDatasource = this;
            var deferred = $q.defer();
            thisDatasource.__hasGWAuthorizedServices().then(function success(hasAuthorizedServices) {
                thisDatasource.openTSDBDatasource.metricFindQuery(query).then(function success(data) {
                    if (DEBUG) {
                        console.log(">>>>>>>>>>>>>>>>>> GroundWorkOpenTSDBDatasource.metricFindQuery() query: " +
                            JSON.stringify(query));
                        console.log(">>>>>>>>>>>>>>>>>> GroundWorkOpenTSDBDatasource.metricFindQuery() metadata: " +
                            JSON.stringify(data));
                    }
                    data = thisDatasource.__filterMetricMetadata(hasAuthorizedServices, query, data);
                    if (DEBUG) {
                        console.log(">>>>>>>>>>>>>>>>>> GroundWorkOpenTSDBDatasource.metricFindQuery() filtered metadata: " +
                            JSON.stringify(data));
                    }
                    data = thisDatasource.__sortMetricMetadata(data);
                    if (DEBUG) {
                        console.log(">>>>>>>>>>>>>>>>>> GroundWorkOpenTSDBDatasource.metricFindQuery() sorted filtered metadata: " +
                            JSON.stringify(data));
                    }
                    deferred.resolve(data);
                }, function error(message) {
                    deferred.reject(message);
                });
            }, function error(message) {
                deferred(message);
            });
            return deferred.promise;
        };
        
        GroundWorkOpenTSDBDatasource.prototype.testDatasource = function() {
            if (DEBUG) {
                console.log(">>>>>>>>>>>>>>>>>> GroundWorkOpenTSDBDatasource.testDatasource()");
            }
            return this.openTSDBDatasource.testDatasource();
        };

        /**
         * Filter query options based on authorized services and hosts.
         *
         * @param hasAuthorizedServices has authorized services flag
         * @param options query options
         * @returns filtered query options clone
         */
        GroundWorkOpenTSDBDatasource.prototype.__filterQueryOptions = function(hasAuthorizedServices, options) {
            // filter query options targets based on service authorizations
            var thisDatasource = this;
            var filteredOptions = _.cloneDeep(_.omit(options, 'targets'));
            filteredOptions.targets = [];
            _.forEach(options.targets, function (target) {
                target = _.cloneDeep(target);
                if (!!target.metric) {
                    // validate hostname query tag
                    if (!target.tags) {
                        target.tags = {};
                    }
                    var hostTag = target.tags[GW_OPENTSDB_HOSTNAME_TAG];
                    if (!hostTag) {
                        // force wildcard grouping of hostname if tag not specified
                        hostTag = target.tags[GW_OPENTSDB_HOSTNAME_TAG] = OPENTSDB_GROUP_WILDCARD;
                    }
                    // filtering based on authorized services
                    if (hasAuthorizedServices) {
                        // get authorized service hosts for metric
                        var authorizedHosts = thisDatasource.gwAuthorizedServices[target.metric];
                        if (!!authorizedHosts) {
                            // filter/transform hostname query tag
                            if (hostTag === OPENTSDB_GROUP_WILDCARD) {
                                // hostname wildcard grouping into multiple authorized queries
                                _.forEach(authorizedHosts, function (host) {
                                    var hostTarget = _.cloneDeep(target);
                                    hostTarget.tags[GW_OPENTSDB_HOSTNAME_TAG] = host;
                                    filteredOptions.targets.push(hostTarget);
                                });
                            } else if (_.contains(authorizedHosts, hostTag)) {
                                // hostname tag authorized
                                filteredOptions.targets.push(target);
                            }
                        }
                    } else {
                        // hostname tag verified
                        filteredOptions.targets.push(target);
                    }
                }
            });
            return filteredOptions;
        };

        /**
         * Filter metadata based on authorized services and hosts.
         *
         * @param hasAuthorizedServices has authorized services flag
         * @param query metadata query
         * @param data metric metadata
         * @returns filtered metric metadata
         */
        GroundWorkOpenTSDBDatasource.prototype.__filterMetricMetadata = function(hasAuthorizedServices, query, data) {
            var thisDatasource = this;
            // filtering only performed if authorized services set
            if (hasAuthorizedServices) {
                var filteredData = [];
                // filter metrics query results
                if (!query.lastIndexOf('metrics(', 0)) {
                    // filter metrics metadata based on service authorizations
                    _.forEach(data, function (element) {
                        if (!!thisDatasource.gwAuthorizedServices[element.text]) {
                            filteredData.push(element);
                        }
                    });
                    return filteredData;
                }
                // filter tag values query results
                if (!query.lastIndexOf('tag_values(', 0)) {
                    var queryParams = query.match(/tag_values\(([^,]*),([^\)]*)\)/);
                    if (!!queryParams && (queryParams.length === 3)) {
                        var metric = queryParams[1];
                        var tag = queryParams[2];
                        if (tag === GW_OPENTSDB_HOSTNAME_TAG) {
                            // filter hostname metadata based on service authorizations
                            var authorizedHosts = thisDatasource.gwAuthorizedServices[metric];
                            if (!!authorizedHosts) {
                                _.forEach(data, function (element) {
                                    if (_.contains(authorizedHosts, element.text)) {
                                        filteredData.push(element);
                                    }
                                });
                            }
                            return filteredData;
                        }
                    }
                }
            }
            return data;
        };

        /**
         * Sort and dedupe metadata.
         *
         * @param data raw metadata
         * @returns sorted/deduped metadata
         */
        GroundWorkOpenTSDBDatasource.prototype.__sortMetricMetadata = function(data) {
            var sortedData = [];
            _.forEach(data, function (element) {
                var index = _.sortedIndex(sortedData, element, 'text');
                if (index < sortedData.length) {
                    if (sortedData[index].text !== element.text) {
                        sortedData.splice(index, 0, element);
                    }
                } else {
                    sortedData.push(element);
                }
            });
            return sortedData;
        };

        /**
         * Check GroundWork authorized services.
         *
         * @returns promise resolved with flag
         */
        GroundWorkOpenTSDBDatasource.prototype.__hasGWAuthorizedServices = function() {
            var thisDatasource = this;
            var deferred = $q.defer();
            thisDatasource.gwAuthorizedServicesPromise.then(function success() {
                deferred.resolve(!_.isEmpty(thisDatasource.gwAuthorizedServices));
            }, function error(message) {
                deferred.reject(message);
            });
            return deferred.promise;
        };

        /**
         * Initialize GroundWork authorized services.
         *
         * @returns promise resolved when complete
         */
        GroundWorkOpenTSDBDatasource.prototype.__initGWAuthorizedServices = function() {
            var thisDatasource = this;
            var deferred = $q.defer();
            // lookup GroundWork roles by user
            thisDatasource.__getGWRolesByUser().then(function success(data) {
                // extract all host and service groups
                _.forEach(data.extendedrole_list.extendedrole, function (extendedRole) {
                    if (!!extendedRole.hgList) {
                        _.forEach(extendedRole.hgList.trim().split(/\s*[,\/]\s*/), function (hostGroup) {
                            if (!_.contains(thisDatasource.gwHostGroups, hostGroup)) {
                                thisDatasource.gwHostGroups.push(hostGroup);
                            }
                        });
                    }
                    if (!!extendedRole.sgList) {
                        _.forEach(extendedRole.sgList.trim().split(/\s*[,\/]\s*/), function (serviceGroup) {
                            if (!_.contains(thisDatasource.gwServiceGroups, serviceGroup)) {
                                thisDatasource.gwServiceGroups.push(serviceGroup);
                            }
                        });
                    }
                });
                if (DEBUG) {
                    console.log(">>>>>>>>>>>>>>>>>> GroundWorkOpenTSDBDatasource.__initGWAuthorizedServices() gwHostGroups: " +
                        JSON.stringify(thisDatasource.gwHostGroups));
                    console.log(">>>>>>>>>>>>>>>>>> GroundWorkOpenTSDBDatasource.__initGWAuthorizedServices() gwServiceGroups: " +
                        JSON.stringify(thisDatasource.gwServiceGroups));
                }
                // lookup GroundWork authorized hosts and services
                return thisDatasource.__getGWAuthorizedServices();
            }).then(function success(data) {
                if (!!data.serviceHostNames) {
                    thisDatasource.gwAuthorizedServices = data.serviceHostNames;
                    if (DEBUG) {
                        console.log(">>>>>>>>>>>>>>>>>> GroundWorkOpenTSDBDatasource.__initGWAuthorizedServices() gwAuthorizedServices: " +
                            JSON.stringify(thisDatasource.gwAuthorizedServices));
                    }
                }
                deferred.resolve();
            }, function error(message) {
                deferred.reject(message);
            });
            return deferred.promise;
        };

        /**
         * Get GroundWork extended roles for current user.
         *
         * @returns promise resolved with extended roles list
         */
        GroundWorkOpenTSDBDatasource.prototype.__getGWRolesByUser = function() {
            var deferred = $q.defer();
            var url = this.gwApiRootUrl+'/rest/extendedrole/findrolesbyuser';
            var config = _.clone(this.gwXMLApiHttpConfig);
            config.params = {userName: this.gwUid};
            $http.get(url, config).success(function success(data) {
                if (!!data && !!data.extendedrole_list && !!data.extendedrole_list.extendedrole) {
                    // cleanup XML to JSON singleton array mappings
                    if (!_.isArray(data.extendedrole_list.extendedrole)) {
                        data.extendedrole_list.extendedrole = [data.extendedrole_list.extendedrole];
                    }
                    _.forEach(data.extendedrole_list.extendedrole, function (extendedRole) {
                        if (!!extendedRole.permissions && !!extendedRole.permissions.permission &&
                            !_.isArray(extendedRole.permissions.permission)) {
                            extendedRole.permissions.permission = [extendedRole.permissions.permission];
                        }
                    });
                    deferred.resolve(data);
                } else {
                    deferred.resolve({extendedrole_list: {extendedrole: []}});
                }
            }).error(function error(message, status) {
                if (status == 404) {
                    deferred.resolve({extendedrole_list: {extendedrole: []}});
                } else {
                    __rejectWithMessage(deferred, message);
                }
            });
            return deferred.promise;
        };

        /**
         * Get GroundWork authorized hosts and services
         *
         * @returns promise resolved with authorized services
         */
        GroundWorkOpenTSDBDatasource.prototype.__getGWAuthorizedServices = function() {
            var deferred = $q.defer();
            var url = this.gwApiRootUrl+'/api/biz/getauthorizedservices';
            var config = _.clone(this.gwApiHttpConfig);
            var postData = {};
            if (!_.isEmpty(this.gwHostGroups)) {
                postData.hostGroupNames = this.gwHostGroups;
            }
            if (!_.isEmpty(this.gwServiceGroups)) {
                postData.serviceGroupNames = this.gwServiceGroups;
            }
            $http.post(url, postData, config).success(function success(data) {
                // clean name keys using same transformation used to write
                // performance data into OpenTSDB
                if (!!data && !!data.hostNames) {
                    data.hostNames = _.transform(data.hostNames, function(result, hostName) {
                        result.push(__cleanNameKey(hostName));
                    });
                }
                if (!!data && !!data.serviceHostNames) {
                    data.serviceHostNames = _.transform(data.serviceHostNames, function(result, hostNames, serviceName) {
                        serviceName = __cleanNameKey(serviceName);
                        hostNames = _.transform(hostNames, function(result, hostName) {
                            result.push(__cleanNameKey(hostName));
                        });
                        result[serviceName] = hostNames;
                    });
                }
                deferred.resolve(data);
            }).error(function error(message, status) {
                if (status == 404) {
                    deferred.resolve({});
                } else {
                    __rejectWithMessage(deferred, message);
                }
            });
            return deferred.promise;
        };

        /**
         * Get cookie value with specified name.
         *
         * @param name cookie name
         * @returns cookie value or undefined
         */
        function __getCookie(name) {
            var cookieMatch = (' '+document.cookie+';').match(' '+name+'=([^;]*);');
            return (!!cookieMatch ? cookieMatch[1] : undefined);
        }

        /**
         * Reject with message.
         *
         * @param deferred deferred promise
         * @param message results data or message
         */
        function __rejectWithMessage(deferred, message) {
            if (_.isObject(message)) {
                message = message.error;
            }
            deferred.reject(message);
        }

        /**
         * Clean metric name and tag key/values replacing illegal characters with
         * underscores. Must be kept in sync with client-side code performing same
         * operation, (see OpenTSDBPerfDataWriter).
         *
         * @param nameKey input name key
         * @returns clean name key
         */
        function __cleanNameKey(nameKey) {
            return nameKey.split(/[^-a-zA-Z0-9_./]/).join('_');
        }

        return GroundWorkOpenTSDBDatasource;
    });
});
