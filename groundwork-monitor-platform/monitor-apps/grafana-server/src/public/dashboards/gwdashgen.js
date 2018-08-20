/* global _ */

/*
 * This script generates a dashboard object that Grafana can load. 
 * It takes a number of user supplied URL parameters (in the ARGS variable).
 * It returns a function which takes a single callback function as argument,
 * This callback function is then called with the dashboard object.
 *
 * This script is called by networkView.js.
 * This script is incomplete. It was an exploration into integrating Grafana dashboards into GW 710.
 *
 * DN June 2016
 *
 * DN June 2017 - overrides updated to reflect InfluxDB vs OpenTSDB (warning and critical thresholds regexes)
 *              -- heights of panels/rows adjusted/enlarged
 *              -- fill's under threshold lines turned off (they should have been off but wasn't inheriting perhaps?)
 *         6/27 - split the single annotation for service up into 3 (CRITICAL/WARNING/OK severity) to color code them
 */

'use strict';

// accessible variables in this scope
var window, document, 
    ARGS, $, jQuery, service,
    argHostName, argDataSource, argHostGroupName, argMinOccurs, argDashType, queryType, argDebug, argShowLegends, argShowAnnotations;

//alert( JSON.stringify( ARGS ) );

// parse the params - this needs collapsing
for ( var arg in ARGS ) {
    //console.error("arg = " + arg + " = " + ARGS[arg]);
    switch( arg ) {
	case "hostname":
  	    argHostName = ARGS[arg];
  	    queryType = 'byHost';
  	    service = '$service';
	    break;
	case "servicename":
  	    service = ARGS[arg];
	    break;
	case "hostgroup":
  	    argHostGroupName = ARGS[arg];
  	    queryType = 'byHostGroup';
  	    service = '$service';
	    break;
	case "minoccurs":
  	    argMinOccurs = ARGS[arg];
	    break;
	case "datasource":
  	    argDataSource = ARGS[arg];
	    break;
	case "dashtype":
  	    argDashType = ARGS[arg];
	    break;
	case "debug":
  	    argDebug = ARGS[arg];
	    break;
	case "showlegends":
  	    argShowLegends = ARGS[arg];
	    break;
	case "showannotations":
  	    argShowAnnotations = ARGS[arg];
	    break;
    }
}

if ( _.isUndefined(argShowLegends) ) {
    argShowLegends = false;
}
else {
    argShowLegends = ( argShowLegends === 'true' ) ? true : false;
}

if ( _.isUndefined(argShowAnnotations) ) {
    argShowAnnotations = false;
}
else {
    argShowAnnotations = ( argShowAnnotations === 'true' ) ? true : false;
}

// set up defaults 
if ( _.isUndefined(argMinOccurs) ) {
    argMinOccurs = 1; // show everything if minoccurs not supplied
}
if ( _.isUndefined(argDataSource) ) {
    argDataSource = 'GroundWork';
}

// check for missing required arg cases
// TBD


return function(callback) {

    // generates and returns the dashboard generating function 
 
    var dashBoard = {};

    $.ajax({
        method: 'GET',
        url: '/'
    })
    .done( function(result) {

    dashBoard = {
          "id": 1,
          "title": genTitle( ),
          "originalTitle": "",
          "tags": [],
          "style": "light", // this doesn't seem to be observed in 3.0.2
          "timezone": "browser",
          "editable": true,
          "hideControls": false,
          "sharedCrosshair": false,
          "rows": genRows( ),
          "time": {
            "from": "now-24h",
            "to": "now"
          },
          "timepicker": {
            "refresh_intervals": [ "5s", "10s", "30s", "1m", "5m", "15m", "30m", "1h", "2h", "1d" ],
            "time_options": [ "5m", "15m", "1h", "6h", "12h", "24h", "2d", "7d", "30d" ]
          },
          "templating": { "list": genTemplateVars( ) },
          "annotations": { "list": genAnnotations( ) }, 
          "refresh": "1m",
          "schemaVersion": 12,
          "version": 0,
          "links": []
        };

        if ( !_.isUndefined( argDebug ) ) {
	    console.log(JSON.stringify( dashBoard, null, 2));
        }
       
        // when dashboard is composed call the callback function and pass the dashboard
        callback(dashBoard);

    });
}


function genTitle ( ) {

    // Generates a dashboard title depending on simple logic
    // Returns the title string

    var title ;

    switch ( argDashType ) { 
	case 'group':
	    title = argHostGroupName + " services (minimum host count = " + argMinOccurs + ")";
	    break;
	case 'host':
	    title = argHostName + " services";
	    break;
	case 'service':
            // nothing because title isn't shown 
	    break;
    }

    return title;
}


function genRows( ) {

    // Generates row(s) for dashboard - expects groundwork datasource type
    // Returns an array of row(s) objects

    var rows, row;

    rows = [ ] ;

    row = {
      "collapse": false,
      "editable": false,
      "panels": [
        {
          "aliasColors": {},
          "bars": false,
          "datasource": argDataSource,
          "editable": false,
          "error": false,
          "fill": 3, // at least 2 for light theme and light colors
          "grid": {
            "threshold1": null,
            "threshold1Color": "rgba(216, 200, 27, 0.27)",
            "threshold2": null,
            "threshold2Color": "rgba(234, 112, 112, 0.22)"
          },
          "height": "300px",
          "id": 1,
          "isNew": true,
          "legend": { 
		"avg": false, 
		"current": true, 
		"max": false, 
		"min": false, 
		"show": argShowLegends, 
		"total": false, 
		"values": true, 
		"alignAsTable": ( argDashType == "group" ? true : false )
          },
          "lines": true,
          "linewidth": 2,
          "links": [],
          "nullPointMode": "connected",
          "percentage": false,
          "pointradius": 5,
          "points": false,
          "renderer": "flot",
          "seriesOverrides": genSeriesOverrides(),
          "span": 12,
          "stack": false,
          "steppedLine": false,
          "targets": [
            {
              //"alias": "the alias", // the opentsdb way - this won't work but is a placeholder to remind us the legend is hard coded to metric.host in our ds
	      "enableThresholds": ( argDashType == "host" || argDashType == "service" ? true : false ),
              "host": argHostName,
              "hostgroup": argHostGroupName,
              "queryType": queryType,
              "refId": "A",
              "service": service
            }
          ],
          "timeFrom": null,
          "timeShift": "", // TBD sync problem - status waterfalls are not aligned - UTC on opentsdb, tz on postgres so api needs to convert to UTC and status view use browser time ?
          "title": ( argDashType == "service" ? null : "$service") ,
          "tooltip": { "msResolution": false, "shared": true, "value_type": "cumulative" },
          "transparent": true,
          "type": "graph",
          "xaxis": { "show": true },
          "yaxes": [ { "format": "short", "label": null, "logBase": 1, "max": null, "min": null, "show": true }, { "format": "short", "label": null, "logBase": 1, "max": null, "min": null, "show": true } ]
        }
      ],
      "repeat": "service",
      "title": ( argDashType == "service" ? service : "$service") 
    };

    rows.push ( row );  

    return rows;
}


function genSeriesOverrides( ) {

    // Generates series overrides which control things like whether something shows on the legend, or whether it's filled or not etc
    // Returns an array of override objects
    var overrides = [];

    // something to remove the tholds from the legend, and not have fill
   
    if ( argDashType != 'group' ) {
        overrides.push( 
            {
              //"alias": "/^.*\\.thold-[wc]$/", // selects thold-w/c , eg demo4.ssh_load.thold-w
              "alias": "/^.*(_wn|_cr)$/", // selects thold-w/c , eg 	localhost.local_cpu_java.local_cpu_java_%CPU_wn  or ... _cr
              "legend": false, // remove these from the legend
              "fill": 0 // don't fill area under line for these either
            },
            {
              //"alias": "/^.*\\.thold-w$/", // select warning thold
              "alias": "/^.*_wn$/", // select warning thold
              "color": "#EAB839", // set color
              "linewidth": 1, // set thin line
              "fill": 0 // don't fill area under line for these either - had to add this here to take effect
            },
            {
              //"alias": "/^.*\\.thold-c$/", // select crit thold
              "alias": "/^.*_cr$/", // select crit thold
              //"color": "#E24D42", // set color
              "color": "#E0752D", // set color
              "linewidth": 1, // set thin line
              "fill": 0 // don't fill area under line for these either - had to add this here to take effect
            }
        );
    }

    return overrides;

}



function genTemplateVars( ) {
    
   // Generates template variables.
   // Returns an array of template variable objects

    var templatingList = [];

    switch ( argDashType ) {
	case "group":
            templatingList.push ( { 
                "name": "service",
                "label": "Service(s)",
                "query": "services({\"minOccurs\":" + argMinOccurs + ", \"queryType\":\"byHostGroup\", \"hostGroup\":\"" + argHostGroupName + "\"})",
                "datasource": argDataSource,
                "hide": 0,
                "includeAll": true,
                "multi": true,
                "refresh": 1,
                "type": "query",
                "useTags": false
            } );
	    break;

	case "host":
            templatingList.push ( {
                "name": "service",
                "label": "Service(s)",
                "query": "services({ \"queryType\":\"byHost\", \"host\":\"" + argHostName + "\"})",
                "datasource": argDataSource,
                "hide": 0,
                "includeAll": true,
                "multi": true,
                "refresh": 1,
                "type": "query",
                "useTags": false
            } );
	    break;

	case "service":
	    // no template vars required for explicit host + service graph
	    break;
    }

    return templatingList;
}


function genAnnotations( ) {
    
   // Generates annotations - currently just for services
   // Returns an array of annotation object(s)

    var annotationsList = [];

    if ( argShowAnnotations === false ) {
            return annotationsList;
    }

    var annotation = {
            "datasource": argDataSource,
            "enable": true,
            "iconColor": "rgb(255, 159, 96)",
            "name": "Service events"
    };

    switch ( argDashType ) {
	case "service":

            // CRITICAL events
            annotationsList.push ( {
                "datasource": argDataSource,
                "enable": true,
                "hide": false,
                "iconColor": "rgb(255, 0, 0)",
                "limit": 100,
                "name": "Critical events",
                "query": "service in ('" + service + "') and host = '" + argHostName + "' and severity='CRITICAL'",
                "showIn": 0,
                "type": "alert"
            } );

            // WARNING events
            annotationsList.push ( {
                "datasource": argDataSource,
                "enable": true,
                "hide": false,
                "iconColor": "rgb(255, 164, 54)",
                "limit": 100,
                "name": "Warning events",
                "query": "service in ('" + service + "') and host = '" + argHostName + "' and severity='WARNING'",
                "showIn": 0,
                "type": "alert"
            } );

            // OK events
            annotationsList.push ( {
                "datasource": argDataSource,
                "enable": true,
                "hide": false,
                "iconColor": "rgb(25, 230, 53)",
                "limit": 100,
                "name": "Ok events",
                "query": "service in ('" + service + "') and host = '" + argHostName + "' and severity='OK'",
                "showIn": 0,
                "type": "alert"
            } );

            // Audit logs 
            annotationsList.push ( {
                "datasource": argDataSource,
                "enable": true,
                "hide": false,
                "iconColor": "rgb(96, 186, 255)",
                "limit": 100,
                "name": "Audit Log",
                //"query": "auditlogs",
                "query": "auditlogs service in ('" + service + "') and host = '" + argHostName + "'", // scoped for just this host/service
                "showIn": 0,
                "type": "alert"
            } );

            //annotation.query = "service in ('" + service + "') and host='" + argHostName+ "'" ; // Original all-in-one
            break;
	case "host":
            annotation.query = "service in $service and host='" + argHostName+ "'" ;
            annotationsList.push( annotation ) ;
            break;
	case "group":
            annotation.query = "service in $service and hostgroup='" + argHostGroupName+ "'" ;
            annotationsList.push( annotation ) ;
            break;
    }

    return annotationsList;
}

