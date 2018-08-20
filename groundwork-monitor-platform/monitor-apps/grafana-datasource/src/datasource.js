/* 
Revision history
v 1.0 : 2016 RG first version for OpenTSDB prototype, based on simple json datasource code example
v 1.1 : 3/2017 DN added independent REST API session token management; added service group handling
v 1.2 : 6/2017 DN changed format of returned series so that if querying byHost, the labels don't prefix with  host.service.
v 1.3 : 6/2017 DN added audit log annotation handling - enter 'auditlogs' in annotation query.
v 1.4 : 8/2017 DN GWMON-13101 
v 1.5 : 5/2018 MS GWMON-13300 double values and no prefix for query by host, put prefix back in for query by host
*/

import _ from "lodash";

export class GenericDatasource {

  constructor(instanceSettings, $q, backendSrv, templateSrv) {

      this.groundworkDebug("In Groundwork datasources constructor()");

      this.q = $q;
      this.backendSrv = backendSrv;
      this.templateSrv = templateSrv;
  
      // These come from partials/config.html etc
      instanceSettings.jsonData = instanceSettings.jsonData || {};
      this.url = instanceSettings.jsonData.url;
      this.username = instanceSettings.jsonData.username;
      this.password = instanceSettings.jsonData.password;
      this.debug = instanceSettings.jsonData.gwdebug; 
      this.persist = instanceSettings.jsonData.persist; 
      
      // Define the GroundWork REST API session token cookie name.
      this.groundworkGrafanaCookieName = "GroundWorkGrafana";
  
      // An app name is required by the GroundWork REST API request headers
      this.gwosAppName = this.groundworkGrafanaCookieName;
  
      // Test for presence of GroundWork REST API session token cookie, and set it if necessary. 
      // This also validates an existing one at this document's path, to see if it expired and renews it.
      if ( ! this.groundworkTestAndSetCookie() ) {
	  this.groundworkDebug( "Constructor() : failed to test/set REST auth token cookie"); // TBD check this makes sense
	  return false;
      }
  
      // Create request headers
      this.grafanaCookieValue = this.groundworkGetCookieValue( this.groundworkGrafanaCookieName );
      this.headers = {
        'GWOS-API-TOKEN': this.grafanaCookieValue,
        'GWOS-APP-NAME' : this.gwosAppName
      };
  
  }


  // Called once per panel (graph)
  // Required method.
  // TBD refactor so this is readable / maintainable.
  query(options) {
    this.groundworkDebug("In query()");
    this.groundworkDebug(options);
   
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
          } else if (target.queryType === 'byHost') {
            var hosts = this.templateSrv.replace(target.host, options.scopedVars, 'pipe').split('|');
            _.forEach(hosts, host => {
              _.forEach(services, service => {
                result.push(this.querySingle(host, service, target.enableThresholds, start, end, target.queryType));
              })
            });
          } else if (target.queryType === 'byServiceGroup') {
            var servicegroups = this.templateSrv.replace(target.servicegroup, options.scopedVars, 'pipe').split('|');
            _.forEach(servicegroups, servicegroup => {
              _.forEach(services, service => {
                result.push(this.queryServiceGroup(servicegroup, service, target.enableThresholds, start, end));
              })
            });

          }
          
        }
        return result;
      }, []);

    return this.q.all(queries).then(responses => ({ data: _.flatten(responses, true) }));
  }

  // TBD add description of how this is used
  groundworkHostsByHostGroup(hostgroup) {
    this.groundworkDebug("In groundworkHostsByHostGroup()");
    var params = {
      depth: 'shallow'
    };
    var request = {
      url: this.url + '/api/hostgroups/' + hostgroup,
      method: 'GET',
      headers: this.headers,
      params: params
    };
    return this.backendSrv.datasourceRequest(request);
  }

  // TBD add description of how this is used
  groundworkHostsByServiceGroup(servicegroup) {
    this.groundworkDebug("In groundworkHostsByServiceGroup(" + servicegroup + ")");
    var request = {
      url: this.url + '/api/servicegroups/' + servicegroup,
      method: 'GET',
      headers: this.headers
    };
    return this.backendSrv.datasourceRequest(request);
  }

  // TBD add description of how this is used
  queryGroup(hostgroup, service, enableThresholds, start, end) {
    this.groundworkDebug("In queryGroup()");
    return this.groundworkHostsByHostGroup(hostgroup)
      .then(response => {
        var queries = _.map(response.data.hosts, host => this.querySingle(host.hostName, service, enableThresholds, start, end));
        return this.q.all(queries).then(responses => _.flatten(responses, true));
      });
  }

  // TBD add description of how this is used
  queryServiceGroup(servicegroup, service, enableThresholds, start, end) {
    var _this3 = this;

    this.groundworkDebug("In queryServiceGroup()");

    return this.groundworkHostsByServiceGroup(servicegroup).then(function (response) {
      var queries = _.map(response.data.services, function (host) {
        return _this3.querySingle(host.hostName, service, enableThresholds, start, end);
      });
      return _this3.q.all(queries).then(function (responses) {
        var flatResponses = _.flatten(responses, true);
        // remove dup items fix to double values GWMON-13300
        var uniqResponses = _.uniqBy(flatResponses, function(item) {
            return item.target;
        });
        return uniqResponses;
      });
    });
  }



  // TBD add description of how this is used
  // TBD refactor so this is readable / maintainable.
  querySingle(host, service, enableThresholds, start, end, qType) {
    this.groundworkDebug("In querySingle()");
    // v 1.2 
    // If the query is by host, the label (or whatever it's called officially) on the returned series shouldn't prefix with 
    // hostname.servicename, because it gets really hard to read. Instead the label should just be the metric name.
    // Eg localhost.local_cpu_httpd.local_cpu_httpd_%CPU should be local_cpu_httpd_%CPU
    // For other query types (by hg and by sg), it's ok to use hostname.servicename.metric tho
    //var targetPrefix = host + '.' + service;
    // v 1.5
    // put prefix back in for query by host: GWMON-13300
    var targetPrefix ;
    //if ( qType == "byHost" ) { 
    //   targetPrefix = "";  // don't set a prefex
    //} else { 
       targetPrefix = host + '.' + service + '.'; // set prefex to 'host.service.'
    //}

    //console.error("qType = " + qType); console.error("prefix = " + targetPrefix);

    return this.groundworkPerfData(host, service, start, end).then(
        response =>
        _.chain(response.data.perfDataTimeSeriesValues)
        .filter(datapoint => enableThresholds || !(datapoint.valueType.endsWith('_wn') || datapoint.valueType.endsWith('_cr')))
        .groupBy(datapoint => (datapoint.valueType === 'metric' ? targetPrefix : targetPrefix + datapoint.valueType))
        .map ((rawData, target) =>
          ({target: target, datapoints: _.map(rawData, datapoint => [datapoint.value, datapoint.timestamp])}))
        .value(),
        errResponse => []
        );
  }

  // TBD add description of how this is used
  groundworkLogin() {
    this.groundworkDebug("In groundworkLogin()");
    
    var request = {
      url: this.url + '/api/auth/login',
      method: 'POST',
      headers: { 'Accept':'text/plain', 'Content-Type':'application/x-www-form-urlencoded' },
      data: "user=" + btoa(this.username) + "&password=" + this.password + "&gwos-app-name=" + this.gwosAppName
    };
    return this.backendSrv.datasourceRequest(request);
  }

  // takes an appname and a token and logouts out from the GW api closing the session 
  groundworkLogout( appname, token) {
    var ajaxRequestResult = jQuery.ajax( {
        url: '/api/auth/logout',
        headers: { 'Accept':'text/plain', 'Content-Type':'application/x-www-form-urlencoded' },
        data: "gwos-api-token=" + token + "&gwos-app-name=" + appname ,
        //async: false, // works ok sync'y
        type: 'POST'
    });
  }

  // TBD add description of how this is used
  groundworkPerfData(host, service, start, end) {
    this.groundworkDebug("In groundworkPerfData()");

    // The "hammer" approach... if the token has expired, need to renew it. This is done here 
    // with a NOC dashboard in mind. It adds some overhead to achieve this goal. There are prob'y better
    // ways of doing this, and this is probably required in other methods too.
    // Test for cookie, and set it if necessary. This also validates existing one.
    if ( ! this.groundworkTestAndSetCookie() ) {
        this.groundworkDebug( "Constructor() : failed to test/set REST auth token cookie"); // TBD check this makes sense
        return false;
    }

    var params = {
      serverName: host,
      serviceName: service,
      startTime: start,
      endTime: end
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
    this.groundworkDebug("In testDatasource()");
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

  // TBD add description of how this is used
  groundworkEvents(query, start, end) {
    this.groundworkDebug("In groundworkEvents()");
    var params = { query: "(reportDate between '" + start + "' and '" + end + "') and (" + query + ")" };
    var request = {
      url: this.url + '/api/events',
      method: 'GET',
      headers: this.headers,
      params: params
    };
    return this.backendSrv.datasourceRequest(request);
  }

  // TBD add description of how this is used
  groundworkAuditLogs(query, start, end) {
    this.groundworkDebug("In groundworkAuditLogs()");

    var auditLogsQuery = "(logtimestamp between '" + start + "' and '" + end + "')"; // get all by default
    // If query was auditlogs <audit logs query>, then use that too
    if ( query ) {
        auditLogsQuery = auditLogsQuery + " and (" + query + ")";
    }
    var params = { query: auditLogsQuery };
    var request = {
      url: this.url + '/api/auditlogs',
      method: 'GET',
      headers: this.headers,
      params: params
    };
    return this.backendSrv.datasourceRequest(request);
  }

  // TBD add description of how this is used
  annotationQuery(options) {
    this.groundworkDebug("In annotationQuery()");
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

    if ( interpolated.match(/^auditlogs/) ) { 
      var query = interpolated.replace(/^auditlogs\s*/,""); // strip off the auditlogs piece
      return this.groundworkAuditLogs(query, start, end).then(
        gwAuditLogs =>
        _.map(gwAuditLogs.data.auditLogs, gwAuditLog => ({
          title: "<b>Audit entry:</b> " + gwAuditLog.description,
          time: new Date(gwAuditLog.timestamp).getTime(),
          text:  "<b>Action:</b> " + gwAuditLog.action + "<br><b>Source:</b> " + gwAuditLog.subsystem + "<br><b>User:</b> " + 
                 gwAuditLog.username  + this.groundworkAuditAnnotationText( gwAuditLog.hostName, gwAuditLog.hostGroupName, gwAuditLog.serviceGroupName, gwAuditLog.serviceDescription ),
          annotation: options.annotation
        })),
        errResponse => []
        );
    }
    else {
      return this.groundworkEvents(interpolated, start, end).then(
        gwEvents =>
        _.map(gwEvents.data.events, gwEvent => ({
          //title: "gwEvent.host + '.' + gwEvent.service + ' [Event Id:' + gwEvent.id + ']',
          //title: "<b>GroundWork event:</b> " + gwEvent.host + '.' + gwEvent.service + ' [Event Id:' + gwEvent.id + ']',
          title: gwEvent.textMessage,
          time: new Date(gwEvent.reportDate).getTime(),
          text: "<b>Host:</b> " + gwEvent.host +  "<br><b>Service:</b> " + gwEvent.service + "<br><b>Severity:</b> " + gwEvent.severity + "<br><b>Event ID:</b>" + gwEvent.id,
          annotation: options.annotation
        })),
        errResponse => []
        );
    }
  }

  groundworkAuditAnnotationText( hostName, hostGroupName, serviceGroupName, serviceDescription ) {
     var text = '';
     if ( typeof hostName !== 'undefined' ) {
        text = text + "<br><b>Host:</b> " + hostName;
     }
     if (typeof serviceDescription !== 'undefined') {
          text = text + "<br><b>Service :</b> " + serviceDescription;
     }
     if ( typeof hostGroupName !== 'undefined' ) {
        text = text + "<br><b>Host group:</b> " + hostGroupName;
     }
     if ( typeof serviceGroupName !== 'undefined' ) {
        text = text + "<br><b>Service group:</b> " + hostGroupName;
     }

     return text;
  }

  // TBD add description of how this is used
  groundworkHostGroups() {
    this.groundworkDebug("In groundworkHostGroups()");
    var params = {
      depth: 'simple'
    };
    var request = {
      url: this.url + '/api/hostgroups',
      method: 'GET',
      headers: this.headers,
      params: params
    };
    return this.backendSrv.datasourceRequest(request);
  }

  // TBD add description of how this is used
  groundworkServiceGroups() {
    this.groundworkDebug("In groundworkServiceGroups()");
    var request = {
      //url: this.url + '/api/categories?entityTypeName=SERVICE_GROUP',
      url: this.url + '/api/servicegroups',
      method: 'GET',
      headers: this.headers
    };
    return this.backendSrv.datasourceRequest(request);
  }



  // TBD add description of how this is used
  groundworkHosts() {
    this.groundworkDebug("In groundworkHosts()");

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

  // TBD add description of how this is used
  groundworkServicesByHostGroup(hostgroup) {
    this.groundworkDebug("In groundworkServicesByHostGroup(), hostgroup = " + hostgroup);
    var params = {
        query: "(hostGroup='" + hostgroup + "')",
        depth: 'simple'
    };
    var request = {
      url: this.url + '/api/services',
      method: 'GET',
      headers: this.headers,
      params: params
    };
    return this.backendSrv.datasourceRequest(request);
  }

  // TBD add description of how this is used
  groundworkServicesByServiceGroup(servicegroup) {
    this.groundworkDebug("In groundworkServicesByServiceGroup(), servicegroup = " + servicegroup);

    var params = { };
    var request = {
      url: this.url + '/api/servicegroups/' + servicegroup,
      method: 'GET',
      headers: this.headers,
      params: params
    };
    return this.backendSrv.datasourceRequest(request);

  }



  // TBD add description of how this is used
  groundworkAllServices() {
    this.groundworkDebug("In groundworkAllServices()");
    var request = {
      url: this.url + '/api/services',
      method: 'GET',
      headers: this.headers
    };
    return this.backendSrv.datasourceRequest(request);
  }

  // TBD add description of how this is used
  groundworkServicesByHost(host) {
    this.groundworkDebug("In groundworkServicesByHost()");
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

  // TBD add description of how this is used
  metricFindQuery(query) {
    this.groundworkDebug("In metricFindQuery()");
    if (!query) { return this.q.when([]); }
    var interpolated;
    try {
      interpolated = this.templateSrv.replace(query);
    }
    catch (err) {
      return this.q.reject(err);
    }

    var service_groups_query = interpolated.match(/servicegroups/);
    if (service_groups_query) {
      return this.metricFindServiceGroupQuery();
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

  // TBD add description of how this is used
  metricFindServiceGroupQuery(options) {
    this.groundworkDebug("In metricFindServiceGroupQuery()");
    return this.groundworkServiceGroups().then(result => _.map(result.data.serviceGroups, servicegroup => ({text: servicegroup.name})));
  }

  // TBD add description of how this is used
  metricFindHostGroupQuery(options) {
    this.groundworkDebug("In metricFindHostGroupQuery()");
    return this.groundworkHostGroups().then(result => _.map(result.data.hostGroups, hostgroup => ({text: hostgroup.name})));
  }

  // TBD add description of how this is used
  metricFindHostQuery(options) {
    this.groundworkDebug("In metricFindHostQuery()");
    return this.groundworkHosts().then(result => _.map(result.data.hosts, host => ({text: host.hostName})));
  }

  // TBD add description of how this is used
  metricFindServiceQuery(options) {

    this.groundworkDebug("In metricFindServiceQuery()");
    this.groundworkDebug(options);

    var minOccurs = 0;
    var services = this.groundworkAllServices();

    if (options && options.minOccurs) {
      minOccurs = options.minOccurs;
    }

    if (options && options.queryType) {

      switch( options.queryType ) {
        case 'byHostGroup':
          services = this.groundworkServicesByHostGroup( options.hostgroup );
          break;
        case 'byHost':
          services = this.groundworkServicesByHost( options.host );
          break;
        case 'byServiceGroup':
          services = this.groundworkServicesByServiceGroup( options.servicegroup );
          break;
        default:
          this.groundworkError("Unhandled query type in metricFindHostQuery()");
      }
     
      /*
      services = (options.queryType === 'byHostGroup' ?
          this.groundworkServicesByHostGroup(options.hostgroup) :
          this.groundworkServicesByHost(options.host));
      */

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


  // TBD add description of how this is used
  buildQueryParameters(options) {
    this.groundworkDebug("In buildQueryParameters()");
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

  // TBD add description of how this is used
  pruneInfrequents(items, minOccurs) {
    this.groundworkDebug("In pruneInfrequents()");
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


  groundworkTestAndSetCookie() {

    // If there's a cookie and it validates, we're done. That's the most common case, and is handled first to attempt to be effecient.
    // If there was no cookie, or it didn't validate, just get a new token, put it in the cookie (and optionally validate it) and we're done.
    // Also if the cookie was updated, then this.headers is updated here.
    // 
    // Returns:
    // - true : cookie was present and valid, or was created and valid
    // - false : something went wrong getting a cookie, 

    this.groundworkDebug("In groundworkTestAndSetCookie()");

    // check for existing portal session - if none, the redirects to login page if session not active
    if ( ! this.persist ) { 
        this.groundworkPortalInSession();
    }

    var groundworkRESTAPISessionToken, tokenResultObject;

    // Try to get the cookie with the session token in it. This will be undefined if not found, or a token if found.
    groundworkRESTAPISessionToken = this.groundworkGetCookieValue( this.groundworkGrafanaCookieName ); 

    // The most common case is the cookie is present, and contains a valid token. Handle this case first quickly.
    if ( groundworkRESTAPISessionToken  &&  this.groundworkValidateToken( groundworkRESTAPISessionToken, this.gwosAppName ) )  {
	this.groundworkDebug( "Found a valid '" + this.groundworkGrafanaCookieName + "' REST API token cookie" );
	return true;
    }

    // if there was no cookie, or it didn't validate, just get a new token, put it in the cookie (and optionally validate it) and we're done
    else {

	this.groundworkDebug( "No valid '" + this.groundworkGrafanaCookieName + "' cookie was found." );

	// Try to get a new REST API session token object { token:..., errors:... }
  	tokenResultObject = this.groundworkGetRESTAPIToken( ); 

        // Check for errors 
        if ( tokenResultObject.errors ) {
	    this.groundworkError("Unable to get a GroundWork REST API session token :");
	    this.groundworkError( tokenResultObject.errors ); 
	    return false;
	}

        // Got a token - try to store it in the cookie
        //if ( ! this.groundworkSetCookie( this.groundworkGrafanaCookieName, tokenResultObject.token, "/portal/classic/dashboard" )  ) {
        if ( ! this.groundworkSetCookie( this.groundworkGrafanaCookieName, tokenResultObject.token )  ) {
	    this.groundworkError( "Failed to store the token in the cookie");
	    return false;
	}

        // Validate the newly obtained token.
        // It's unlikely the token just created is invalid or corrupted but easy to check. 
        // The validation routine provides details on issues if invalid.
        // TBD consider yanking this out later.
        if ( ! this.groundworkValidateToken( tokenResultObject.token, this.gwosAppName ) ) {
	    this.groundworkError("The freshly created REST API session token was invalid or was unable to be validated.");  
            return false;
        }

        // Refresh the request headers with the new token
        this.headers = {
          'GWOS-API-TOKEN': tokenResultObject.token,
          'GWOS-APP-NAME' : this.gwosAppName
        };

        // Token obtained and stored in cookie 
	return true;
    }

  }


  groundworkGetRESTAPIToken( ) {

    // Tries to get a GroundWork REST API session token. 
    // Returns an object : { token: "..." }, or { errors: "..." }

    this.groundworkDebug("In groundworkGetRESTAPIToken()");

    var requestResult, errorObject, ajaxRequestResult, params, apiRes, result;

    params = {
        url: this.url + '/api/auth/login', 
        data: { 'user' : btoa(this.username ) , 'password': this.password ,  'gwos-app-name' : this.gwosAppName }, 
        type: 'POST'
    };

    // Call the REST API to get the token
    apiRes = this.groundworkRESTAPI( params );

    // TBD improve error notification here 
    if ( apiRes.errors ) { 
        // failed to get token - return errors
	result = { 'errors' : apiRes.errors };
    }
    else {
        // Got a token - return it via response property
	result = { 'token' : apiRes.response };
    }

    return result;

  }


  groundworkRESTAPI( params ) {

    // Provides interface to calling REST API using JQuery.ajax().
    // It does this in a synchronous fashion in order to get the results
    // from the call and process them.
    // There might be a much better and prefered way to do this with promises
    // and the Grafana backend. TBD investigate that.

    this.groundworkDebug("In groundworkRESTAPI()");    

    var result = { response: null, errors: null };

    var ajaxRequestResult = jQuery.ajax( {

        // params for .ajax()
        url: params.url,
        data: params.data,
        type: params.type,

        // Turn off async here so can grab the results right now - this means this is blocking 
        async: false,  

        // on successful api call, result will be in data , which will get 
        // convert into a string to make life a lot easier later when testing it 
        // espy in the case of validation where have true or false
        success: function( data ) { 
            result.response = _.toString(data);
        },

        // error in POST, GET, etc - grab the details (using the jqXHR object properties directly since they're 
        // more useful than textStatus and errorThrown)
        error: function( jqXHR, textStatus, errorThrown ) {
	    // this.groundworkDebug(jqXHR);
            result.errors = { 
                EerrorThrown : errorThrown,
            	resultText : jqXHR.responseText,
            	statusNumber : jqXHR.status,
            	statusText : jqXHR.statusText 
            }
        }
    } );

    return result;

  }


  groundworkPortalInSession( ) {
    // checks if portal session active. if not, logouts out the GW API so token is no longer usable, purges the token cookie
    // and redirects page to main login page
    this.groundworkDebug( "In groundworkPortalInSession()" );

    var logout = this.groundworkLogout; // will call this function from in the success scope below
    var params = {
        url: '/portal/classic/dashboard/grafana',
        data: { },
        type: 'GET' ,
        token: this.groundworkGetCookieValue( this.groundworkGrafanaCookieName ),
        lpage: this.url,
        appName: this.gwosAppName
    };

    var ajaxRequestResult = jQuery.ajax( {
        url: params.url,
        data: params.data,
        type: params.type,

        // If there's no open GroundWork portal session, then hitting the url will result
        // in a redirect (302) back to the login page. The ajax request will auto follow
        // the 302 (an alternative here is to use window.fetch), and the final response 
        // will contain a loginForm id. 
        success: function( data, textStatus, xhr ) { 
            if ( jQuery("#loginForm", data).length > 0) {  // ! this results in an error drop_cookies is not defined

                // logout of the GW api to make the session token not work any more in case it's taken from the cookie 
                logout( params.appName, params.token );  

                // delete the api session cookie - make it expire 
                document.cookie = params.cname + '=xxx;expires=Thu, 01 Jan 1970 00:00:01 GMT;'; // need to set value to something it seems

                // redirect page to portal login page
                window.top.location = params.lpage;
            }
        },

        // error in , GET - grab the details (using the jqXHR object properties directly since they're 
        // more useful than textStatus and errorThrown)
        error: function( jqXHR, textStatus, errorThrown ) {
	    this.groundworkError(jqXHR); // for debugging
            result.errors = { 
                errorThrown : errorThrown,
            	resultText : jqXHR.responseText,
            	statusNumber : jqXHR.status,
            	statusText : jqXHR.statusText 
            }
            window.top.location = params.lpage;
        }  

    } );

  }


  groundworkSetCookie( cookieName, token ) {

    // Creates a session cookie called cookieName with value token.
    // It's possible to end up with multiple session cookies but with different paths from pages where this code
    // was invoked:
    // - From within GroundWork portal : Reports -> Grafana -> <dashboard> => session cookie with path=/grafana
    // - From within GroundWork portal : IFrame portlet  <dashboard> => session cookie with path=/grafana/dashboard/db
    // - From outside of GroundWork portal : Grafana -> <dashboard> => session cookie with path=/dashboard/db, or /.
    // The path is deliberately not defined, allowing multiple same-named cookies with different paths to coexist
    // to support the same dashboard being usable properly from different pages.
    // See also the partner groundworkGetCookie() method.

    this.groundworkDebug("In groundworkStoreTokenInCookie()");

    // Token cannot be empty
    if ( ! token ) { 
        this.groundworkError("The REST API token cannot be empty.");
        return false;
    }

    // Other token checks  - eg must not have a comma in it
    // TBD

    // Try to create a session cookie with the token in it
    this.groundworkDebug("Writing cookie : " + cookieName + "=" + token );
    document.cookie = cookieName + "=" + token ;

    // Check that the cookie exists ie was created
    if ( ! this.groundworkGetCookieValue( cookieName ) ) {
        this.groundworkError("Failed to create the cookie");
        return false;
    }

    return true; 

  }

  groundworkGetCookieValue( cookieName ) { 	
  
     // Takes a cookie name, and searches for it's value.
     // Returns the cookie value, or undefined.

     // There can be multiple GroundWorkGrafana cookies with different paths. That's ok because
     // document.cookie appears to get the one for this page's path (ie dirname window.location.pathname)

     this.groundworkDebug("In groundworkGetCookieValue()");

     // Get the list of all cookies, prepend with space, and tack on a ;. . Eg :
     // Before: "document.cookies : "username=John Doe; grafana_remember=fbb6c98361c1a29ce2320e1b75f76b96287c6f821c81cf4e; grafana_user=admin; updates="
     // After:  " document.cookies : "username=John Doe; grafana_remember=fbb6c98361c1a29ce2320e1b75f76b96287c6f821c81cf4e; grafana_user=admin; updates=;"

     // Then do a string match regex on that string looking for a match on the cookieName. 
     // Match example :  cookieMath = [" username=John Doe;", "John Doe"]
     // No match : null

     var cookieMatch = ( ' ' + document.cookie + ';' ).match( ' ' + cookieName + '=([^;]*);' );

     // return (!!cookieMatch ? cookieMatch[1] : undefined); // Please don't use bang bang boolean !! notation.

     if ( Boolean( cookieMatch ) ) {  // If there was a match, cookieMatch will be truthy (ie a populated list)
	return cookieMatch[1]; // return the value from the match list
     } else {
	return undefined; // no match 
     }

  }


  groundworkValidateToken( token , appName ) {

    // Validates a GW REST API session token from the cookie
    //
    // Possible results :
    // - pass validation (validation returns true)
    // - fail validation 
    //   -- due to expired or corrupt token value (validation returns false)
    //   -- due to wrong app name - not sure why this would happen here (validation returns false)
    //   -- the validation POST fails (eg rest api down or malfunctioning) (validation POST errors)
    //
    // Returns 
    // - true only if the token validation worked and responded with true
    // - false if failed or errored

    this.groundworkDebug("In groundworkValidateToken()");

    var validationResult, errorResText, errorStatusText, postRequestResult, params , apiRes;

    params = {
        url: this.url + '/api/auth/validatetoken', 
        data: { 'gwos-api-token' : token , 'gwos-app-name': appName },
        type: 'POST'
    };

    apiRes = this.groundworkRESTAPI( params );

    // Validation endpoint successfully invoked, and return true - a valid token
    if ( apiRes.response == "true" ) { 
       	this.groundworkDebug("Token " + token + " validated successfully");	
	return true;
    }

    // Validation endpoint successfully invoked, and return false - an invalid token
    // most probably due to expiration, but also possibly due to corruption somehow.
    else if ( apiRes.response == "false" ) { 
       	this.groundworkWarning("Token validation *unsuccessful* - it may have expired");	 
	return false; 
    }

    // Validation endpoint not successfully invoked
    else if ( apiRes.errors ) { 
	this.groundworkError("Token validation call failed - something went wrong");
	this.groundworkError("-- jqXHR.responseText : " + apiRes.errors.resultText );
	this.groundworkError("-- jqXHR.statusText : " + apiRes.errors.statusText );
	return false; 
    }

    // Something else not yet thought of
    else {
	this.groundworkError("Unhandled error condition in groundworkValidateToken()");
        return false;
    }

  }


  // TBD DRY / consolidate logging functions here eg with OTS module 
  groundworkDebug( message ) {
    // TBD need to improve this - don't like stringifying
    if ( this.debug ) { 
        if ( _.isObject( message ) ) {
	    console.log( "GroundWork DEBUG [" + Date() + "]: " + JSON.stringify( message) ); 
	} else {
	    console.log( "GroundWork DEBUG [" + Date() + "]: "  + message);
        }
    }
  }
  groundworkError( message ) {
    if ( _.isObject( message ) ) {
        console.error( "GroundWork ERROR [" + Date() + "]: " + JSON.stringify( message) ); 
    } else {
	console.error( "GroundWork ERROR [" + Date() + "]: "  + message);
    }
  }
  groundworkWarning( message ) {
    if ( _.isObject( message ) ) {
        console.warn( "GroundWork WARNING [" + Date() + "]: " + JSON.stringify( message) ); 
    } else {
	console.warn( "GroundWork WARNING [" + Date() + "]: "  + message);
    }
  }

}
