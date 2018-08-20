import Vue from 'vue'
import VueResource from 'vue-resource'
import VueCookie from 'vue-cookie'

Vue.use(VueResource);
Vue.use(VueCookie);

export default {
    port: 8090,

    api: function(connector, endPoint, ...params) {
        var port = "";

        if (!!window.location.port) {
             port = ":" + ((window.location.port == "4100") ? this.port : window.location.port);
        }

        var origin = window.location.protocol + "//" + window.location.hostname + port,
            url = origin + '/cloudhub/api/' + (connector ? (connector + '/') : '') + endPoint;

        for(var i = 0, iLimit = params.length; i < iLimit; i++) {
            url += '/' + params[i];
        }

        return url;
    },

    retrieve: function(connector, endPoint, ...params) {
        var url = this.api(connector, endPoint, ...params);

        var headers = {}, token = Vue.cookie.get('FoundationToken');
        Vue.http.headers.common['Content-Type'] = 'application/json;charset=utf-8';

        if(token) {
            Vue.http.headers.common['GWOS-API-TOKEN'] = token;
        }
        else {
            delete Vue.http.headers.common['GWOS-API-TOKEN'];
        }

        return Vue.http.get(url, {}, {headers: headers});
    },

    post: function(connector, endPoint, data, ...params) {
        var url = this.api(connector, endPoint, ...params);

        var headers = {}, token = Vue.cookie.get('FoundationToken');
        Vue.http.headers.common['Content-Type'] = 'application/json;charset=utf-8';

        if(token) {
            Vue.http.headers.common['GWOS-API-TOKEN'] = token;
        }
        else {
            delete Vue.http.headers.common['GWOS-API-TOKEN'];
        }

        return Vue.http.post(url, data, {headers: headers});
    }
};
