import Vue from 'vue'
import serverService from '../../../services/server-service'

export default class NeDiService {
    retrieve(configName, callback) {
        serverService.retrieve('nedi', configName ? ('config?name=' + configName) : 'config').then(response => {
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
        serverService.post('nedi', 'config', data).then(response => {
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
        serverService.post('nedi', 'test', data, [what]).then(response => {
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