/* jshint indent:false, unused:false */
/* jshint -W087 */
/* global appConfig:true, _:true */
'use strict';

var ServerService = function ($cookies) {

    var apiHttpConfig = {
        rootPath : "http://localhost/api",
        withCredentials: false,
        headers: {
            'GWOS-API-TOKEN': '6CB95F7FFA7B4B0ABF92216D822E4ECD',
            'GWOS-APP-NAME' : 'monitor-dashboard'
        }
    }

    var PATH_SEPARATOR = "/";
    var PORT_SEPARATOR = ":";

    function endsWith(str, suffix) {
        return str.indexOf(suffix, str.length - suffix.length) !== -1;
    }

    function startsWith(str, prefix) {
        return str.indexOf(prefix) === 0;
    }

    function concatenatePaths(base, path) {
        var result = "";
        if (base === null) base = "";
        if (path === null) path = "";
        result = result + base;
        if (endsWith(base, PATH_SEPARATOR)) {
            if (startsWith(path, PATH_SEPARATOR)) {
                result = result + path.substring(1);
            }
            else
                result = result + path;
        }
        else {
            if (startsWith(path, PATH_SEPARATOR) || startsWith(path, PORT_SEPARATOR))
                result = result + path;
            else {
                result = result  + PATH_SEPARATOR;
                result = result + path;
            }
        }
        return result;
    }


    return {
        api: function (endPoint, pathParam, pathParam2, pathParam3, pathParam4) {
//            var rootPath = appConfig.rootPath || '/';
//            var url = location.protocol + '//' + location.hostname + ':' + appConfig.apiPort + ((rootPath !== '/') ? rootPath : '') + '/api' + endPoint;
            var token = $cookies.FoundationToken;
            if (!_.isUndefined(token)) {
                apiHttpConfig.headers['GWOS-API-TOKEN'] = token;
            }

            // GWMON-12079: DO NOT USE FoundationRestService endpoint - it is not updated by installer as previously thought
            // there should always be an /api proxy pass configured on installed systems
            //var root = angular.fromJson($cookies.FoundationRestService);
            if (!window.location.origin) {
                window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
            }

            var url = window.location.origin + '/api' + endPoint;

            // var url = apiHttpConfig.rootPath + endPoint;
            if (pathParam !== undefined) {
                url += '/' + pathParam;
            }
            if (pathParam2 !== undefined) {
                url += '/' + pathParam2;
            }
            if (pathParam3 !== undefined) {
                url += '/' + pathParam3;
            }
            if (pathParam4 !== undefined) {
                url += '/' + pathParam4;
            }
            return url;
        },

        apiConfig: function () {
            return _.clone(apiHttpConfig);
        }
    }
};

