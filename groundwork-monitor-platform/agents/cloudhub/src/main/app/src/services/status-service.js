import Vue from 'vue'
import serverService from './server-service'

export default class StatusService {
    retrieve(configName, callback) {
        serverService.retrieve(null, configName ? ('status?name=' + configName) : 'status').then(response => {
            console.log(response.body);
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
};
