<%@ page contentType="text/html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<portlet:defineObjects/>
<portlet:resourceURL var="readPrefs" id="readPrefs" escapeXml="false" />

<style>
    .hostNameFormat {
        font-size: 150%;
        font-weight: bold
    }
</style>

<script>

    function showHosts() {
        $JQ("#hostsBlock").show();
        $JQ("#servicesBlock").hide();
    }

    function showService(hostName) {
        $JQ("#hostsBlock").hide();
        $JQ("#servicesBlock").show();
        $JQ('#hostNameField').html(hostName);
        console.log(hostName);
        $JQ('#servicesTable').dataTable( {
            "columns": [
                { "data": "description" },
                { "data": "monitorStatus" },
                { "data": "lastCheckTime" },
                { "data": "properties.LastPluginOutput"}
            ],
            "ajax": function (data, callback, settings) {
                var foundationToken = $JQ.cookie("FoundationToken");
                var foundationRestService = $JQ.cookie("FoundationRestService");
                //var serviceUrl = foundationRestService + "/services?hostName=" + hostName;
                var serviceUrl = window.location.origin + '/api/services?hostName=' + hostName;
                $JQ.ajax({
                    url: serviceUrl,
                    dataType: "json",
                    headers: {
                        'GWOS-API-TOKEN': foundationToken,
                        'GWOS-APP-NAME' : 'monitor-dashboard'
                    },
                    type: "GET",
                    success: function( data ) {
                        console.log("success " + data);
                        var wrapped = {};
                        wrapped.data = data.services;
                        callback(wrapped);
                    },
                    error: function(e) {
                        console.log("Error retrieving Services " + e.status + ", " + e.statusText);
                    }
                });
            }
        } );

    }

    function buildHostTable(prefs) {
        $JQ('#hostsTable').dataTable( {
            "pageLength": prefs.rows,
            "columns": [
                { "data": "hostName" },
                { "data": "monitorStatus" },
                { "data": "appType" },
                { "data": "properties.LastPluginOutput"}
            ],
            "ajax": function (data, callback, settings) {
                var foundationToken = $JQ.cookie("FoundationToken");
                var foundationRestService = $JQ.cookie("FoundationRestService");
                var hostUrl = window.location.origin + '/api/hosts';
                $JQ.ajax({
                    url: hostUrl,
                    dataType: "json",
                    headers: {
                        'GWOS-API-TOKEN': foundationToken,
                        'GWOS-APP-NAME' : 'monitor-dashboard'
                    },
                    type: "GET",
                    success: function( data ) {
                        console.log("success " + data);
                        for ( var ix=0, ixlen=data.hosts.length; ix<ixlen ; ix++ ) {
                            data.hosts[ix].hostName = "<a href='javascript:showService(\"" + data.hosts[ix].hostName + "\")'>"
                            + data.hosts[ix].hostName + "</a>";
                        }
                        var wrapped = {};
                        wrapped.data = data.hosts;
                        callback(wrapped);
                    },
                    error: function(e) {
                        console.log("Error retrieving Hosts " + e.status + ", " + e.statusText);
                    }
                });
            }
        } );
    }

    function refreshHosts() {
        var prefsEndPoint = '<%=renderResponse.encodeURL(readPrefs.toString())%>';
        $JQ.ajax({
            url: prefsEndPoint,
            dataType: "json",
            success: function (prefs) {
                buildHostTable(prefs);
            },
            error: function (e) {
                console.log("Error Retrieving Prefs " + e.status + ", " + e.statusText);
            }
        });

    };

    $JQ(document).ready(function() {
        refreshHosts();
    });

</script>

<div id='hostsBlock'>
    <table id="hostsTable" width="100%" >
        <thead>
        <tr>
            <th>Host Name</th>
            <th>Monitor Status</th>
            <th>App Type</th>
            <th>Last Plugin Output</th>
        </tr>
        </thead>
    </table>
</div>

<div id='servicesBlock' style="display: none">
    <br/>
    <div id="servicesInfo" class='hostNameFormat'> Services for Host: <span class='hostNameFormat' id='hostNameField'/></div>
    <div id="returnToList"><a href="javascript:showHosts()"> Return to List Hosts</a></div>
    <br/>

    <table id="servicesTable" width="100%" >
        <thead>
        <tr>
            <th>Service Name</th>
            <th>Service Status</th>
            <th>Last Check Time</th>
            <th>Last Plugin Output</th>
        </tr>
        </thead>
    </table>
</div>
