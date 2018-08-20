import Vue from 'vue'
import VueResource from 'vue-resource'
import VueCookie from 'vue-cookie'
import VeeValidate from 'vee-validate';
import App from './App'
import router from './router'
import serverService from './services/server-service'
import {ClientTable, Event} from 'vue-tables-2';
import Toast from 'vue-easy-toast'
import Dialog from 'hsy-vue-dialog'

Vue.config.productionTip = false;

/* eslint-disable no-new */
Vue.use(VueResource);
Vue.use(VueCookie);
Vue.use(ClientTable);
Vue.use(VeeValidate);
Vue.use(Toast);
Vue.use(Dialog);

new Vue({
  el: '#app',
  router,
  render: h => h(App)
})
