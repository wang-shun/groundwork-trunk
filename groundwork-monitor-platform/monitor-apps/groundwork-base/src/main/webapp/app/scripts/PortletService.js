var PortletService = function ($http, $q, ServerService) {

    var service = {

        config: {
            withCredentials: true
        },

        lookupPreferences: function (url) {
            var self = this, deferred = $q.defer();
            $http.get(url, service.config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function error(data, status) {
                    deferred.reject(data);
                });
            return deferred.promise;
        },
        storePreferences: function (url, prefs) {
            var self = this, deferred = $q.defer();
            $http.post(url, prefs, service.config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function error(data, status) {
                    deferred.reject(data);
                });
            return deferred.promise;
        },
        computeHitList: function (url) {
            var self = this, deferred = $q.defer();
            $http.get(url, service.config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function error(data, status) {
                    deferred.reject(data);
                });
            return deferred.promise;
        },
        computeNocBoard: function (url) {
            var self = this, deferred = $q.defer();
            $http.get(url, service.config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function error(data, status) {
                    deferred.reject(data);
                });
            return deferred.promise;
        },
        postComments: function (url, comments) {
            var self = this, deferred = $q.defer();
            $http.post(url, comments, service.config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function error(data, status) {
                    deferred.reject(data);
                });
            return deferred.promise;
        },
        deleteComment: function (url, comment) {
            var self = this, deferred = $q.defer();
            $http.post(url, comment, service.config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function error(data, status) {
                    deferred.reject(data);
                });
            return deferred.promise;
        },
        postAck: function (url, ackRecord) {
            var self = this, deferred = $q.defer();
            $http.post(url, ackRecord, service.config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function error(data, status) {
                    deferred.reject(data);
                });
            return deferred.promise;
        },
        postNotification: function (url, notification) {
            var self = this, deferred = $q.defer();
            $http.post(url, notification, service.config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function error(data, status) {
                    deferred.reject(data);
                });
            return deferred.promise;
        },
        getBoards: function (url) {
            var self = this, deferred = $q.defer();
            $http.get(url, service.config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function error(data, status) {
                    deferred.reject(data);
                });
            return deferred.promise;
        },
        lookupBoard: function (url) {
            var self = this, deferred = $q.defer();
            $http.get(url, service.config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function error(data, status) {
                    deferred.reject(data);
                });
            return deferred.promise;
        },
        saveBoard: function (url, board) {
            var self = this, deferred = $q.defer();
            $http.post(url, board, service.config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function error(data, status) {
                    deferred.reject(data);
                });
            return deferred.promise;
        },
        deleteBoard: function (url) {
            var self = this, deferred = $q.defer();
            $http.post(url, service.config)
                .success(function success(data, status, headers, config) {
                    deferred.resolve(data);
                })
                .error(function error(data, status) {
                    deferred.reject(data);
                });
            return deferred.promise;
        },

    }
    return service;
}
