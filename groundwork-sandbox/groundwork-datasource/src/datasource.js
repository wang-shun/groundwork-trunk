/* Version history

1.0 2017   RG Original version (starting at a working copy with InfluxDB backend via GW API)
1.1 3/9/17 DN GroundWork REST API token obtained via cookie for integration with GroundWork portal


*/ 


import _ from "lodash";

export class GenericDatasource {

  constructor(instanceSettings, $q, backendSrv, templateSrv) {
    this.q = $q;
    this.backendSrv = backendSrv;
    this.templateSrv = templateSrv;
    instanceSettings.jsonData = instanceSettings.jsonData || {};
    this.url = instanceSettings.jsonData.url;
    this.username = instanceSettings.jsonData.username;
    this.password = instanceSettings.jsonData.password;
    /* v 1.1  
       this.headers GWOS-API-TOKEN
           Previously GWOS-API-TOKEN was set to null. Setting token to null requires a call to testDataSource() 
           in order to instantiate a GroundWork REST API session token. This version requires that the Grafana UI be 
           integrated with the GroundWork portal, where the Grafana servlet creates a session and stores the token in 
           a cookie. This cookie is then retrieved and it's token used in GroundWork REST API headers. 
       this.gwosAppName
           Previously this was set to 'grafana'. However, the token created by the Grafana servlet is for application
           name 'monitor-dashboard', so for that token to work here, the same app name needs to be used.
    */
    this.gwosAppName = 'monitor-dashboard';
    this.headers = {
      'GWOS-API-TOKEN': this.groundworkGetCookie('FoundationToken'), 
      'GWOS-APP-NAME' : this.gwosAppName
    };
  }

   groundworkGetCookie(cookieName) { 	
       var cookieMatch = (' '+document.cookie+';').match(' '+cookieName+'=([^;]*);');
       return (!!cookieMatch ? cookieMatch[1] : undefined);
   }

  // Called once per panel (graph)
  query(options) {
   
    var parameters = this.buildQueryParameters(options);

    if (parameters.targets.length <= 0) {
      return this.q.when({data: []});
    }

    var start = new Date(parameters.range.from).getTime();
    var end = new Date(parameters.range.to).getTime();

    var queries =
      _.reduce(parameters.targets, (result, target) => {
        if (!target.hide) {
          var services = this.templateSrv.replace(target.service, options.scopedVars, 'pipe').split('|');
          if (target.queryType === 'byHostGroup') {
            var hostgroups = this.templateSrv.replace(target.hostgroup, options.scopedVars, 'pipe').split('|');
            _.forEach(hostgroups, hostgroup => {
              _.forEach(services, service => {
                result.push(this.queryGroup(hostgroup, service, target.enableThresholds, start, end));
              })
            });
          } else {
            var hosts = this.templateSrv.replace(target.host, options.scopedVars, 'pipe').split('|');
            _.forEach(hosts, host => {
              _.forEach(services, service => {
                result.push(this.querySingle(host, service, target.enableThresholds, start, end));
              })
            });
          }
        }
        return result;
      }, []);


    return this.q.all(queries).then(responses => ({ data: _.flatten(responses, true) }));
  }

  groundworkHostsByHostGroup(hostgroup) {
    var request = {
      url: this.url + '/api/hostgroups/' + hostgroup,
      method: 'GET',
      headers: this.headers
    };
    return this.backendSrv.datasourceRequest(request);
  }

  queryGroup(hostgroup, service, enableThresholds, start, end) {
    return this.groundworkHostsByHostGroup(hostgroup)
      .then(response => {
        var queries = _.map(response.data.hosts, host => this.querySingle(host.hostName, service, enableThresholds, start, end));
        return this.q.all(queries).then(responses => _.flatten(responses, true));
      });
  }

  querySingle(host, service, enableThresholds, start, end) {
    var targetPrefix = host + '.' + service;
    return this.groundworkPerfData(host, service, start, end).then(
        response =>
        _.chain(response.data.perfDataTimeSeriesValues)
        .filter(datapoint => enableThresholds || !(datapoint.valueType.endsWith('_wn') || datapoint.valueType.endsWith('_cr')))
        .groupBy(datapoint => (datapoint.valueType === 'metric' ? targetPrefix : targetPrefix + '.' + datapoint.valueType))
        .map ((rawData, target) =>
          ({target: target, datapoints: _.map(rawData, datapoint => [datapoint.value, datapoint.timestamp])}))
        .value(),
        errResponse => []
        );
  }

  groundworkLogin() {
    
    var request = {
      url: this.url + '/api/auth/login',
      method: 'POST',
      headers: { 'Accept':'text/plain', 'Content-Type':'application/x-www-form-urlencoded' },
      data: "user=" + btoa(this.username) + "&password=" + this.password + "&gwos-app-name=" + this.gwosAppName
    };
    return this.backendSrv.datasourceRequest(request);
  }

  groundworkPerfData(host, service, start, end) {
    var params = {
      serverName: host,
      serviceName: service,
      startTime: start,
      endTime: end,
      interval: 1000
    };
    var request = {
      url: this.url + '/api/perfdata',
      method: 'GET',
      headers: this.headers,
      params: params
    };
    return this.backendSrv.datasourceRequest(request);
  }

  // Required
  // Used for testing datasource in datasource configuration pange
  testDatasource() {
    return this.groundworkLogin().then(
        response => {
          this.headers["GWOS-API-TOKEN"] = response.data;
          var result = {
            status: 'success',
            message: 'Data source is working',
            title: 'Success' };
          return result;
        },
        errResponse => ({
          status: 'error',
          message: 'ERROR: unable to communicate with groundwork',
          title: 'Error' }));
  }

  groundworkEvents(query, start, end) {
    var params = { query: "(reportDate between '" + start + "' and '" + end + "') and (" + query + ")" };
    var request = {
      url: this.url + '/api/events',
      method: 'GET',
      headers: this.headers,
      params: params
    };
    return this.backendSrv.datasourceRequest(request);
  }

  annotationQuery(options) {
    var query = options.annotation.query;

    if (!query) { return this.q.when([]); }
    var interpolated;
    try {
      interpolated = this.templateSrv.replace(query, undefined, function(value) {
        if (typeof value === 'string') {
          return '\'' + value + '\'';
        }
        return '(' + _.map(value, val => '\'' + val + '\'') + ')';
      });
    }
    catch (err) {
      return this.q.reject(err);
    }

    var start = JSON.stringify(options.range.from);
    var end = JSON.stringify(options.range.to);

    return this.groundworkEvents(interpolated, start, end).then(
        gwEvents =>
        _.map(gwEvents.data.events, gwEvent => ({
          title: gwEvent.host + '.' + gwEvent.service + ' [' + gwEvent.id + ']',
          time: new Date(gwEvent.reportDate).getTime(),
          text: gwEvent.textMessage,
          annotation: options.annotation
        })),
        errResponse => []
        );
  }

  groundworkHostGroups() {
    var request = {
      url: this.url + '/api/hostgroups',
      method: 'GET',
      headers: this.headers
    };
    return this.backendSrv.datasourceRequest(request);
  }

  groundworkHosts() {
    var params = {
      depth: 'simple'
    };
    var request = {
      url: this.url + '/api/hosts',
      method: 'GET',
      headers: this.headers,
      params: params
    };
    return this.backendSrv.datasourceRequest(request);
  }

  groundworkServicesByHostGroup(hostgroup) {
    var params = { query: "(hostGroup='" + hostgroup + "')" };
    var request = {
      url: this.url + '/api/services',
      method: 'GET',
      headers: this.headers,
      params: params
    };
    return this.backendSrv.datasourceRequest(request);
  }

  groundworkAllServices() {
    var request = {
      url: this.url + '/api/services',
      method: 'GET',
      headers: this.headers
    };
    return this.backendSrv.datasourceRequest(request);
  }

  groundworkServicesByHost(host) {
    var params = {
      hostName: host
    }
    var request = {
      url: this.url + '/api/services',
      method: 'GET',
      headers: this.headers,
      params: params
    };
    return this.backendSrv.datasourceRequest(request);
  }

  metricFindQuery(query) {
    if (!query) { return this.q.when([]); }
    var interpolated;
    try {
      interpolated = this.templateSrv.replace(query);
    }
    catch (err) {
      return this.q.reject(err);
    }

    var groups_query = interpolated.match(/groups/);
    if (groups_query) {
      return this.metricFindHostGroupQuery();
    }

    var hosts_query = interpolated.match(/hosts\((.*)\)/);
    if (hosts_query) {
      if (hosts_query[1]) {
        var args = JSON.parse(hosts_query[1]);
        return this.groundworkHostsByHostGroup(args.hostgroup).then(
            response => _.map(response.data.hosts, host => ({text:host.hostName})));
      } else {
        return this.metricFindHostQuery();
      }
    }

    var services_query = interpolated.match(/services\((.*)\)/);
    if (services_query) {
      if (services_query[1]) {
        var args = JSON.parse(services_query[1]);
        return this.metricFindServiceQuery(args);
      } else {
        return this.metricFindServiceQuery();
      }
    }
    return this.q.when([]);
  }

  metricFindHostGroupQuery(options) {
    return this.groundworkHostGroups().then(result => _.map(result.data.hostGroups, hostgroup => ({text: hostgroup.name})));
  }

  metricFindHostQuery(options) {
    return this.groundworkHosts().then(result => _.map(result.data.hosts, host => ({text: host.hostName})));
  }

  metricFindServiceQuery(options) {
    var minOccurs = 0;
    var services = this.groundworkAllServices();
    if (options && options.minOccurs) {
      minOccurs = options.minOccurs;
    }
    if (options && options.queryType) {
      services = (options.queryType === 'byHostGroup' ?
          this.groundworkServicesByHostGroup(options.hostgroup) :
          this.groundworkServicesByHost(options.host));
    }
    return services.then(result => {
      var serviceList =
        _.chain(result.data.services)
        .map('description')
        .sortBy(service => service)
        .value();
      var prunedServices = this.pruneInfrequents(serviceList, minOccurs);
      return _.chain(prunedServices)
        .uniq(true)
        .map(service => ({text: service}))
        .value();
    });
  }

  buildQueryParameters(options) {
    //remove placeholder targets
    options.targets =
      _.filter(options.targets, target =>
          _.isString(target.service) &&
          target.service !== 'Select service' &&
          (target.queryType === 'byHostGroup' ?
           _.isString(target.hostgroup) :
           (_.isString(target.host) || target.host !== 'Select host')));
    return options;
  }

  pruneInfrequents(items, minOccurs) {
    if (minOccurs < 2) return items;
    var result =
      _.chain(items)
      .countBy()
      .map((count, text) => ({text:text, count:count}))
      .filter(val => {return val.count >= minOccurs})
      .map('text')
      .value();
    return result;
  }

}
