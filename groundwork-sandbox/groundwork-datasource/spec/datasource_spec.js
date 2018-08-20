import {Datasource} from "../module";
import Q from "q";

describe('GenericDatasource', function() {
  var ctx = {};
  beforeEach(function() {
    ctx.$q = Q;
    ctx.backendSrv = {};
    ctx.templateSrv = {replace: val => val};
    ctx.ds = new Datasource({}, ctx.$q, ctx.backendSrv, ctx.templateSrv);
  });

  it('should return an empty array when no targets are set', function(done) {
    ctx.ds.query({targets: []}).then(function(result) {
      expect(result.data).to.have.length(0);
      done();
    });
  });

  it('should return the server results when a target is set', function(done) {
    ctx.backendSrv.datasourceRequest = function(request) {
      return ctx.$q.when({
        _request: request,
        data: {
          "serverName":"localhost",
          "serviceName":"local_cpu_java",
          "startTime":1461000000,
          "endTime":1461933880709,
          "interval":1000,
          "perfDataTimeSeriesValues":[
          {"valueType":"metric", "timestamp":1461086688000, "value":0 },
          {"valueType":"metric", "timestamp":1461087288000, "value":0 },
          {"valueType":"metric", "timestamp":1461087888000, "value":0 },
          {"valueType":"metric_wn", "timestamp":1461086688000, "value":40 },
          {"valueType":"metric_wn", "timestamp":1461087288000, "value":40 },
          {"valueType":"metric_wn", "timestamp":1461087888000, "value":40 },
          {"valueType":"metric_cr", "timestamp":1461086688000, "value":50 },
          {"valueType":"metric_cr", "timestamp":1461087288000, "value":50 },
          {"valueType":"metric_cr", "timestamp":1461087888000, "value":50 }]
        },
        "status":200,
        "config": {
          "method":"GET",
          "transformRequest":[null],
          "transformResponse":[null],
          "url":"/grafana/api/datasources/proxy/15/api/perfdata",
          "headers": {
            "GWOS-API-TOKEN":"854bc7eb-089b-437d-b059-497d986512ae",
            "GWOS-APP-NAME":"monitor-dashboard",
            "Accept":"application/json,text/plain,*/*"
          },
          "params": {
            "serverName":"localhost",
            "serviceName":"local_cpu_java",
            "startTime":1461000000,
            "interval":1000
          },
          "retry":0
        },
        "statusText":"OK"
      });
    };

    var testQuery = {
      "panelId":1,
      "range": {
        "from":"2016-04-27T14:25:52.523Z",
        "to":"2016-04-29T14:25:52.531Z"
      },
      "rangeRaw": {
        "from":"2016-04-27T14:25:52.523Z",
        "to":"2016-04-29T14:25:52.531Z"
      },
      "interval":"2m",
      "targets":[{
        "$$hashKey":"object:287",
        "refId":"A",
        "host":"localhost",
        "hostgroup":"Linux Servers",
        "hide":false,
        "queryType":'byHost',
        "service":"local_cpu_java"}],
        "format":"json",
        "maxDataPoints":868
    };
    ctx.ds.query(testQuery).then(function(result) {
      //TODO: This is verifying the request itself
      //expect(result._request.data.targets).to.have.length(3);

      var series = result.data[0];
      expect(series.target).to.equal('localhost.local_cpu_java');
      expect(series.datapoints).to.have.length(3);
      done();
    });
  });
});
