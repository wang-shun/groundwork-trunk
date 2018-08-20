import Vue from 'vue'
import serverService from '../../../services/server-service'

export default class AzureService {
    retrieve(configName, callback) {
        serverService.retrieve('azure', configName ? ('config?name=' + configName) : 'config').then(response => {
            callback(response.body);
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('Azure configuration couldn\'t be loaded due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }

    save(data, callback) {
        serverService.post('azure', 'config', data).then(response => {
            Vue.toast('Azure configuration was saved', {
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

            Vue.toast('Azure configuration couldn\'t be saved due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }

    upload(form, callback) {
        var data = new FormData(form);

        serverService.post('azure', 'upload', data).then(response => {
            callback(response.bodyText);
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('Azure configuration file upload failed due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }

    test(data, what, callbackSuccess, callbackError) {
        serverService.post('azure', 'test', data, [what]).then(response => {
            if(response.body.success) {
                callbackSuccess();
            }
            else {
                callbackError(response.body.error);
            }
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('Azure configuration testing failed due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });

            callbackError(false);
        });
    }

    getDiscoveryServices(params, callbackSuccess) {
        var query = [];
        for(var param in params) {
            query.push(param + '=' + params[param]);
        }

        serverService.retrieve('azure', 'discovery?' + query.join('&')).then(response => {
          console.log("good response: ", response);
            callbackSuccess(response.body);
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('Azure discoverable services couldn\'t be loaded due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }

    saveDiscoveryServices(services, callbackSuccess, callbackError) {
        serverService.post('azure', 'discovery', services).then(response => {
            console.log(response);
            callbackSuccess(response.body);
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('Azure discoverable services couldn\'t be loaded due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }
};
