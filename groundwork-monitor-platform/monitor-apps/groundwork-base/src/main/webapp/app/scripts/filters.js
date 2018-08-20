'use strict';

/* Filters */

angular.module('myApp.filters', []).
  filter('interpolate', ['version', function(version) {
    return function(text) {
      return String(text).replace(/\%VERSION\%/mg, version);
    };
  }])
  .filter('moment', function() {
    return function(text) {
      return moment(text).format('M-D-YYYY hh:mm:ss A');
    };
  });
