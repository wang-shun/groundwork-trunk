import Vue from 'vue'
import serverService from './server-service'

export default class MetricsService {
    retrieve(connector, configName, profileName, callback) {
        if (connector == 'aws')
            connector = 'amazon';

        serverService.retrieve(connector, configName ? ('metrics?name=' + configName + '&profile=' + profileName) : 'metrics').then(response => {
            for(var viewName in response.body.views) {
                var view = response.body.views[viewName], active = 0, synthetic = 0;

                for(var j = 0, jLimit = view.metrics.length; j < jLimit; j++) {
                    var metric = view.metrics[j];

                    if(metric.monitored === true) {
                        active++;
                    }

                    if(metric.computeType === "synthetic") {
                        synthetic++;
                    }
                }

                view.active = active;
                view.synthetic = synthetic;
            }

            callback(response.body);
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('Cloudhub metrics couldn\'t be loaded due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }

    getAmazonConfig(configName, callback) {
        serverService.retrieve('amazon', configName ? ('config?name=' + configName) : 'config').then(response => {
            callback(response.body);
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('Cloudhub configuration couldn\'t be loaded due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }

    checkforupdates(connector, configName, profileName, callback) {
        if (connector == 'aws')
            connector = 'amazon';

        serverService.retrieve(connector, configName ? ('checkforupdates?name=' + configName + '&profile=' + profileName) : 'checkforupdates').then(response => {
            callback(response.body.count && response.body.count > 0);
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('Cloudhub metrics couldn\'t be loaded due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }

    save(connector, data, callback) {
        if (connector == 'aws')
            connector = 'amazon';

        serverService.post(connector, 'metrics', data).then(response => {
            Vue.toast('Cloudhub metrics were saved', {
                id: 'toast-success',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });

            if(callback) {
                callback();
            }
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('Cloudhub metrics couldn\'t be saved due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }

    update(connector, configName, profileName, callback) {
        if (connector == 'aws')
            connector = 'amazon';

        serverService.retrieve(connector, configName ? ('update?name=' + configName + '&profile=' + profileName) : 'update').then(response => {
            Vue.toast('Cloudhub metrics were updated', {
                id: 'toast-success',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });

            if(callback) {
                callback(response.body);
            }
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('Cloudhub metrics couldn\'t be loaded due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }

    getNames(connector, serviceType, configName, callback) {
        if (connector == 'aws')
            connector = 'amazon';

        serverService.retrieve(connector, 'metricnames?serviceType=' + serviceType + '&name=' + configName).then(response => {
            callback(response.body);
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('Metric names couldn\'t be retrieved due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }

    getHealthCheckNames(connector, serviceType, configName, callback) {
        if (connector == 'aws')
            connector = 'amazon';

        serverService.retrieve(connector, 'healthchecknames?serviceType=' + serviceType + '&name=' + configName).then(response => {
            callback(response.body);
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('Health check names couldn\'t be retrieved due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }

    getInputMetrics(connector, expression, profile, inputType, serviceType, callback) {
        if (connector == 'aws')
            connector = 'amazon';

        serverService.retrieve(connector, 'variables?expression=' + encodeURIComponent(expression) + '&profile=' + profile + '&inputType=' + inputType + '&serviceType=' + serviceType).then(response => {
            var inputMetrics = [];

            for(var metric in response.body) {
                inputMetrics.push({name: metric, value: response.body[metric], overrideValue: ''});
            }

            inputMetrics.sort(function(a, b) {
                return (a.name.toLowerCase() > b.name.toLowerCase()) ? 1 : -1;
            });

            callback(inputMetrics);
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('Input metrics could not be calculated: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }

    getGWFunctionNames(connector, callback) {
        if (connector == 'aws')
            connector = 'amazon';

        serverService.retrieve(connector, 'gwfunctions').then(response => {            
            callback(response.body);
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('Groundwork functions could not be retrieved: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });            
        });
    }

    evaluate(connector, data, callback) {
        if (connector == 'aws')
            connector = 'amazon';

        serverService.post(connector, 'evaluate', data).then(response => {
            if(response.body.success) {
                callback(response.body.result);
            }
            else {
                Vue.toast(response.body.error, {
                    id: 'toast-error',
                    horizontalPosition: 'center',
                    verticalPosition: 'top',
                    duration: 5000,
                    mode: 'queue'
                });
            }
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast(response.body.error || 'There was an error evaluating your expression: \'' + errorMessage + '\', please retry later', {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }

    refreshCustom(connector, configName, profileName, callback) {
        if (connector == 'aws')
            connector = 'amazon';

        serverService.retrieve(connector, configName ? ('refreshcustommetrics?name=' + configName + '&profile=' + profileName) : 'refreshcustommetrics').then(response => {
            if(response && response.body && response.body.views && response.body.views.custom && response.body.views.custom.metrics && response.body.views.custom.metrics.length) {
                Vue.toast(response.body.views.custom.metrics.length + ' custom metrics were found', {
                    id: 'toast-success',
                    horizontalPosition: 'center',
                    verticalPosition: 'top',
                    duration: 5000,
                    mode: 'queue'
                });
            }

            if(callback) {
                callback(response.body);
            }
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('Cloudhub metrics couldn\'t be loaded due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }
};
