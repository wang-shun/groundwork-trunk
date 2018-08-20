import Vue from 'vue'
import serverService from '../../../services/server-service'

export default class VMwareService {
    retrieve(configName, callback) {
        serverService.retrieve('vmware', configName ? ('config?name=' + configName) : 'config').then(response => {
            callback(response.body);
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('VMware configuration couldn\'t be loaded due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }

    save(data, callback) {
        serverService.post('vmware', 'config', data).then(response => {
            Vue.toast('VMware configuration was saved', {
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

            Vue.toast('VMware configuration couldn\'t be saved due to server error: ' + errorMessage, {
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

        serverService.post('vmware', 'upload', data).then(response => {
            callback(response.bodyText);
        }, response => {
            console.log(response.status);
            console.log(response.statusText);

            var errorMessage = response.body.error || "(no message provided)";

            Vue.toast('VMware configuration file upload failed due to server error: ' + errorMessage, {
                id: 'toast-error',
                horizontalPosition: 'center',
                verticalPosition: 'top',
                duration: 5000,
                mode: 'queue'
            });
        });
    }

    test(data, what, callbackSuccess, callbackError) {
        serverService.post('vmware', 'test', data, [what]).then(response => {
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

            Vue.toast('VMware configuration testing failed due to server error: ' + errorMessage, {
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
