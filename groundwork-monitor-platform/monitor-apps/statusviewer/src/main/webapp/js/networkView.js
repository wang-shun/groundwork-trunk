/**
 * networkView.js - Network View Javascript Library
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */

/**
 * Render performance measurement graph. Invoked post DOM render.
 *
 * @hostName host name of measure to graph
 * @serviceName service name of measure to graph
 * @startTime start time to graph, (unix time, seconds since epoch)
 * @endTime end time to graph, (unix time, seconds since epoch)
 * @applicationType application type of measure or null for all, (e.g. NAGIOS)
 * @width width of graph to render, (pixels)
 * @targetDivId div tag id target
 */

function renderPerformanceMeasurementGraph(hostName, serviceName, startTime, endTime, applicationType, width, targetDivId, showLegends, showAnnotations, hostGroup, dashboardType, minOccurs) {

    // Grafana integration with scripted and embedded dashboards
    // DN June 2016/17

    //console.error(JSON.parse(JSON.stringify(arguments) )  ); // debug all function args

    var url, route, html, objectHeight, baseUrl, params, divId, objectId;

    baseUrl = window.location.origin;
    
    // ms epoch times eg from=1462507200000&to=1462593599999
    //                        1462447598       1460460413
    startTime = startTime * 1000; 
    endTime   = endTime   * 1000;

    // check dashboardType supplied
    if ( typeof dashboardType == "undefined" ) {
	// If no dashboardType, assume coming directly from service view
        dashboardType = 'service';
    }
    
    // check dashboardType value valid
    // TBD - should be service, host or group ( for hostgroup )

    // construct the url based on the dashboard type being requested
    switch( dashboardType ) {
	case "service":
        	// the dashboard-solo route is used if this is being used to create just a service level graph.  Using dashboard-solo
        	// produces a clean embedded graph panel without the grafana navbar.
        	route = 'dashboard-solo';
		objectHeight = 300; 
		// TBD check serviceName is given and valid
                url = baseUrl + '/grafana/' + route + '/script/gwdashgen.js?dashtype=' + dashboardType + '&notitles=1' + '&hostname=' + hostName + '&servicename=' + serviceName;
                url += '&panelId=1'; // this is required for embedding (ie using the dashboard-solo route), else grafana throws an error 'Panel not found'
		break;

	case "host":
        	// for host and group types of dashboards, use the usual fully functional dashboard route
        	route = 'dashboard';
		objectHeight = 700;
		// TBD check hostName is given and valid
                url = baseUrl + '/grafana/' + route + '/script/gwdashgen.js?dashtype=' + dashboardType + '&notitles=1' + '&hostname=' + hostName ;
		break;

	case "group":
        	// for host and group types of dashboards, use the usual fully functional dashboard route
        	route = 'dashboard';
		objectHeight = 700;
		// TBD check hostGroup is given and valid
                url = baseUrl + '/grafana/' + route + '/script/gwdashgen.js?dashtype=' + dashboardType + '&hostgroup=' + hostGroup ;
    		if ( typeof minOccurs !== "undefined" ) {
			// TBD check hostGroup is given and valid
			url += '&minoccurs=' + minOccurs ;
		}
                
		break;
    }

    if ( typeof startTime !== "undefined" ) {
        url += '&from=' + startTime ; 
    }

    if ( typeof endTime !== "undefined" ) {
        url += '&to=' + endTime ; 
    }

    if ( typeof showLegends !== "undefined" ) {
	url += '&showlegends=' + showLegends; // will be 'true' or 'false' so just pass it through
    }

    if ( typeof showAnnotations !== "undefined" ) {
	url += '&showannotations=' + showAnnotations; // will be 'true' or 'false' so just pass it through
    }
    
    /* At this point, have a url to the javascript dashboard generator.
     * Embed the output of that js into the page. Use <object ...> to do the embedding.
     * The object definition html is constructed and then the targetDivId's innerHTML updated with it.
     * The object is tagged with a unique div so can access it's #document later to extract it's inner DOM stuff.
     * The object is wrapped inside a div which can be resized by this code automatically to allow 
     * fitting of grafana legends on the fly. The object height is set to 100% to fill the div.
     * All a bit clunky but does the trick.
     * Host Availability & Performance Measurement and Service Availability & Performance Measurement viewing modes
     */

    // create the id's for the wrapping div and the object
    divId = targetDivId + '.plotId';
    objectId = targetDivId + '.objectId';
    objectId = objectId.replace(/\./g, "_"); // underscores won't need CSS escaping in querySelector later

    // create the html to put in the targetdiv
    html = '<div id="' + divId + '" style="height:' + objectHeight + 'px"> <object id = "' + objectId + '" type="text/html" width="700" height="100%" data="' + url + '" ></object> </div>' ;
    //html = '<p>targetDivId = ' + targetDivId + ', url = ' + url + '</p>';  // debugging  - replaces graphic with this text

    // set the targetdiv's html and so essentially embedding the output of the grafana scripted dashboard
    document.getElementById(targetDivId).innerHTML = html;  // object html

    // Resize the enclosing object div to enlarge to fit the legend.
    // I reach into the embedded object's #document DOM to get the grafana panel size. It takes a while to render so have to wait for it.
    // That size is then used to resize the object-wrapping div.
    // Only try to resize embedded object if legends are enabled
    // Seems like a good case for using Promises perhaps.
    if ( showLegends === 'true' ) {

       var existCondition = setInterval(  function( divId, objectId ) {

       // To avoid throwing unhandled exceptions, first need to wait for the existence of elements of class 'graph-wrapper' to be created by grafana. 
       if ( typeof document.querySelector("#" + objectId).contentDocument.getElementsByClassName("graph-wrapper")[0] != 'undefined' ) {

           // Once some elements exist, need to wait for clientHeight to exist and be non zero - I was also using offsetHeight for a while for reference
           if ( document.querySelector("#" + objectId).contentDocument.getElementsByClassName("graph-wrapper")[0].clientHeight ) {
              clearInterval(existCondition);
              var theHeight = document.querySelector("#" + objectId).contentDocument.getElementsByClassName("graph-wrapper")[0].clientHeight;
              theHeight += 25; // found need to expand the height a little still to avoid inner scrollbar still
              document.getElementById( divId ).style.height = theHeight + "px";
           }
        }
       }, 500, divId, objectId); // check every 500ms 

    }
    return; 


    // Old Dimple.js integration below here.



    var interval = Math.max(Math.floor((endTime-startTime)/width+0.999), 1);
    getPerfData(hostName, serviceName, startTime, endTime, interval, applicationType,
        function(data, status) {
            jQuery('div[id='+targetDivId+']').empty();
            if (!!data.perfDataTimeSeriesValues && !!data.perfDataTimeSeriesValues.length) {
                cssEscapedTargetDivId = targetDivId.replace(/([ #;?%&,.+*~\':"!^$[\]()=>|\/@])/g,'\\$1');
                drawChart(cssEscapedTargetDivId, data.perfDataTimeSeriesValues, width);
            } else {
                jQuery('div[id='+targetDivId+']').append('<p class="error_msg">No Performance Measurement Data</p>');
            }
        },
        function(message, status) {
            jQuery('div[id='+targetDivId+']').empty();
            jQuery('div[id='+targetDivId+']').append('<p class="error_msg">Error loading data: '+message+', (http status '+status+')</p>');
            jQuery('div[id='+targetDivId+']').append('<p class="error_msg">'+((new Date()).toLocaleTimeString())+':'+
                ' hostName='+hostName+
                ' serviceName='+serviceName+
                ' startTime='+startTime+
                ' endTime='+endTime+
                ' applicationType='+applicationType+
                ' width='+width+
                ' targetDivId='+targetDivId+'</p>');
        });
}


/**
 * Get performance measurement time series data.
 *
 * @param hostName host name of measure
 * @param serviceName service/metric name of measure
 * @param startTime start time for data range, (unix time, seconds since epoch)
 * @param endTime end time for data range, (unix time, seconds since epoch)
 * @param interval downsampling interval, (seconds)
 * @param applicationType application type data filter or null for all
 * @param success callback function accepting JSON data and HTTP status code parameters
 * @param error callback function accepting message and HTTP status code parameters
 */
function getPerfData(hostName, serviceName, startTime, endTime, interval, applicationType, success, error) {
    var matchApiToken = /FoundationToken=([^;]*);/.exec(document.cookie);
    var apiToken = (!!matchApiToken ? matchApiToken[1] : null);
    var baseApiUrl = window.location.origin+'/api';
    jQuery.ajax({
        url: baseApiUrl+'/perfdata?'+
            (!!applicationType ? 'appType='+encodeURIComponent(applicationType)+'&' : '')+
            'serverName='+encodeURIComponent(hostName)+'&'+
            'serviceName='+encodeURIComponent(serviceName)+'&'+
            'startTime='+startTime*1000+'&'+
            'endTime='+endTime*1000+'&'+
            'interval='+interval*1000,
        dataType: 'json',
        beforeSend: function(xhr) {
            xhr.setRequestHeader('GWOS-API-TOKEN', apiToken);
            xhr.setRequestHeader('GWOS-APP-NAME', 'monitor-dashboard');
        },
        success: function(data) {
            if (!!success) {
                success(data, 200);
            }
        },
        error: function(xhr) {
            var status = xhr.status;
            var statusText = xhr.statusText;
            var data = xhr.response;
            if ((status == 404) && (!!success)) {
                var notFoundData = {
                    serverName: hostName,
                    serviceName: serviceName,
                    startTime: startTime,
                    endTime: endTime,
                    interval: interval,
                    perfDataTimeSeriesValues: []
                };
                if (!!applicationType) {
                    notFoundData.applicationType = applicationType;
                }
                success(notFoundData, status);
            } else if (xhr.getResponseHeader('Content-Type') == 'application/json') {
                data = JSON.parse(data);
                if (!data.error && !!success) {
                    success(data, status);
                } else {
                    if (!!error) {
                        error(data.error, status);
                    }
                }
            } else if (!!error) {
                error(statusText, status);
            }
        }
    });
}

/**
 * Render chart in target element.
 *
 * @param containerId id of target element
 * @param seriesValues performance measurement time series data
 * @param width width of chart, (pixels)
 */
function drawChart(containerId, seriesValues, width)
{
    // Calculate the min/max bounds:
    var min, max, minStamp, maxStamp,
        minValue, maxValue, minTw, maxTw, minTc, maxTc;

    for(var i = 0, iLimit = seriesValues.length; i < iLimit; i++)
    {
        var item = seriesValues[i], value = item.value, timestamp = item.timestamp;

        if(typeof min == "undefined")
        {
            min = value;
        }
        else
        {
            if(min > value)
                min = value;
        }

        if(typeof minStamp == "undefined")
        {
            minStamp = timestamp;
        }
        else
        {
            if(minStamp > timestamp)
                minStamp = timestamp;
        }

        if(typeof max == "undefined")
        {
            max = value;
        }
        else
        {
            if(max < value)
                max = value;
        }

        if(typeof maxStamp == "undefined")
        {
            maxStamp = timestamp;
        }
        else
        {
            if(maxStamp < timestamp)
                maxStamp = timestamp;
        }

        switch(item.valueType)
        {
            case "value":
                if(typeof minValue == "undefined")
                {
                    minValue = value;
                }
                else
                {
                    if(minValue > value)
                        minValue = value;
                }

                if(typeof maxValue == "undefined")
                {
                    maxValue = value;
                }
                else
                {
                    if(maxValue < value)
                        maxValue = value;
                }
                break;

            case "thold-w":
                if(typeof minTw == "undefined")
                {
                    minTw = value;
                }
                else
                {
                    if(minTw > value)
                        minTw = value;
                }

                if(typeof maxTw == "undefined")
                {
                    maxTw = value;
                }
                else
                {
                    if(maxTw < value)
                        maxTw = value;
                }
                break;

            case "thold-c":
                if(typeof minTc == "undefined")
                {
                    minTc = value;
                }
                else
                {
                    if(minTc > value)
                        minTc = value;
                }

                if(typeof maxTc == "undefined")
                {
                    maxTc = value;
                }
                else
                {
                    if(maxTc < value)
                        maxTc = value;
                }
                break;
        }
    }

    min -= min * 0.05;
    max += max * 0.05;

    var dateSpan = (minStamp == maxStamp) ? 1 : ((maxStamp - minStamp) / (1000 * 60 * 60 * 24));

    //console.log(dateSpan);

    var dateMin = new Date(minStamp), dateMax = new Date(maxStamp),
        datelabel = ((minStamp == maxStamp) ?
            (dateMin.getMonth() + "/" + dateMin.getDate() + "/" + dateMin.getFullYear()) :
            (dateMin.getMonth() + "/" + dateMin.getDate() + "/" + dateMin.getFullYear() + " - " + dateMax.getMonth() + "/" + dateMax.getDate() + "/" + dateMax.getFullYear()));

    // Draw the chart:
    var svg = dimple.newSvg("#" + containerId, width, width / 1.75);

    var myChart = new dimple.chart(svg, seriesValues);
    myChart.setBounds(60, 30, width - 85, width / 1.75 - 175);

    var x = myChart.addCategoryAxis("x", "timestamp");
    x.addOrderRule("timestamp");
    x.timeField = "timestamp";
    x.tickFormat = (dateSpan <= 2) ? "%I:%M:%S %p" : ((dateSpan <= 4) ? "%m/%d/%Y %I:%M:%S %p" : "%m/%d/%Y"); // "%m/%d/%Y %I:%M:%S %p"
    x.dateParseFormat = null;
    x.title = (dateSpan <= 2) ? datelabel : null;
    x.fontFamily = "'Helvetica Neue',Helvetica,Arial,sans-serif";
    x.fontSize = "12px";
    x.showGridlines = true;

    var y = myChart.addMeasureAxis("y", "value");
    y.title = null;
    y.fontFamily = "'Helvetica Neue',Helvetica,Arial,sans-serif";
    y.fontSize = "12px";
    y.showGridlines = true;
    y.overrideMax = max;
    y.overrideMin = min;

    var s = myChart.addSeries("valueType", dimple.plot.line, [x, y]);

    myChart.assignColor("value", "green");
    myChart.assignColor("thold-w", "orange");
    myChart.assignColor("thold-c", "red");

    var legend = myChart.addLegend(0, 10, 500, 20, "right");
    legend.fontFamily = "'Helvetica Neue',Helvetica,Arial,sans-serif";
    legend.fontSize = "12px";

    myChart.draw();

    // Write the min-max values to the legend & correct the legend item positions:
    var labels = legend.shapes[0], w2, w3, x1, x2, x1_, x2_;

    w2 = labels[1].firstChild.offsetWidth + labels[1].children[1].offsetWidth;
    w3 = labels[2].firstChild.offsetWidth + labels[2].children[1].offsetWidth;

    x1 = +labels[0].firstChild.getAttribute("x");
    x2 = +labels[1].firstChild.getAttribute("x");

    x1_ = +labels[0].children[1].getAttribute("x");
    x2_ = +labels[1].children[1].getAttribute("x");

    labels[0].firstChild.textContent = "Critical (" + ((minTc == maxTc)       ? minTc    : ("min: " + minTc + ", max: " + maxTc)) + ")";
    labels[1].firstChild.textContent = "Warning ("  + ((minTw == maxTw)       ? minTw    : ("min: " + minTw + ", max: " + maxTw)) + ")";
    labels[2].firstChild.textContent = "Value ("    + ((minValue == maxValue) ? minValue : ("min: " + minValue + ", max: " + maxValue)) + ")";

    w2_ = labels[1].firstChild.offsetWidth + labels[1].children[1].offsetWidth;
    w3_ = labels[2].firstChild.offsetWidth + labels[2].children[1].offsetWidth;

    labels[0].firstChild.setAttribute("x", x1 + (w2_ - w2) + (w3_ - w3));
    labels[1].firstChild.setAttribute("x", x2 + (w3_ - w3));

    labels[0].children[1].setAttribute("x", x1_ + (w2_ - w2) + (w3_ - w3));
    labels[1].children[1].setAttribute("x", x2_ + (w3_ - w3));
}
