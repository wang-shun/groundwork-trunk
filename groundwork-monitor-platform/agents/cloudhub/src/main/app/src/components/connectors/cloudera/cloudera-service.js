import Vue from 'vue'
import serverService from '../../../services/server-service'

export default class ClouderaService {
    retrieve(configName, callback) {
        serverService.retrieve('cloudera', configName ? ('config?name=' + configName) : 'config').then(response => {
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

    save(data, callback) {
        serverService.post('cloudera', 'config', data).then(response => {
            Vue.toast('Cloudhub configuration was saved', {
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

            Vue.toast('Cloudhub configuration couldn\'t be saved due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }

    test(data, what, callbackSuccess, callbackError) {
        serverService.post('cloudera', 'test', data, [what]).then(response => {
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

            Vue.toast('Cloudhub configuration testing failed due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });

            callbackError(false);
        });
    }
}; 