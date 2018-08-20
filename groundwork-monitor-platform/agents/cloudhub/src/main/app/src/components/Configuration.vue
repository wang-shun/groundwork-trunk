<template>
    <component v-bind:is="$route.params.connector"></component>
</template>

<script>
import AzureConnector from './connectors/azure/configuration'
import ClouderaConnector from './connectors/cloudera/configuration'
import NediConnector from './connectors/nedi/configuration'
import VMwareConnector from './connectors/vmware/configuration'
import DockerConnector from './connectors/docker/configuration'
import AWSConnector from './connectors/aws/configuration'

export default {
  name: 'Configuration',
  components: {
    azure: AzureConnector,
    cloudera: ClouderaConnector,
    nedi: NediConnector,
    vmware: VMwareConnector,
    docker: DockerConnector,
    aws: AWSConnector
  },
  beforeRouteLeave: function(to, from, next) {
    var view = this.$children[0];

    if (view.$vnode.componentOptions.Ctor.options.beforeRouteLeave &&
        view.$vnode.componentOptions.Ctor.options.beforeRouteLeave[0] &&
        typeof view.$vnode.componentOptions.Ctor.options.beforeRouteLeave[0] == 'function') {
        view.$vnode.componentOptions.Ctor.options.beforeRouteLeave[0].apply(view, [to, from, next]);
    }
  }
}
</script>
