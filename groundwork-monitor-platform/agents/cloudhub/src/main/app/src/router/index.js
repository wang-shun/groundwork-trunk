import Vue from 'vue'
import Router from 'vue-router'
import Home from '@/components/Home'
import Configuration from '@/components/Configuration'
import Metrics from '@/components/Metrics'
import Status from '@/components/Status'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/cloudhub/',
      name: 'Cloudhub',
      component: Home
    },
    {
      path: '/configuration/:connector',
      name: 'Cloudhub Configuration',
      component: Configuration
    },
    {
      path: '/metrics/:connector',
      name: 'Cloudhub Metrics',
      component: Metrics
    },
    {
      path: '/status',
      name: 'Cloudhub Status',
      component: Status
    },
    { path: '*', redirect: '/cloudhub/' }
  ]
})
