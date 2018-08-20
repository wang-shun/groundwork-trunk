<template>
    <div class="container-fluid">
        <h3>Cloudhub</h3>
        <br />
        <div class="row text-center">
            <router-link to="/configuration/cloudera?name=cloudhub-cloudera-16.xml" class="btn btn-success">Configure Cloudera</router-link>
            <router-link to="/metrics/cloudera?name=cloudhub-cloudera-16.xml&profile=b574ee95-e0c1-4545-bc3d-8e65c2091a8d" class="btn btn-success">Metrics</router-link>
        </div>
        <br />
        <div class="row text-center">
            <router-link to="/configuration/azure?name=cloudhub-azure-21.xml" class="btn btn-success">Configure Azure</router-link>
            <router-link to="/metrics/azure?name=cloudhub-azure-21.xml" class="btn btn-success">Metrics</router-link>
        </div>
        <br />
        <div class="row text-center">
            <router-link to="/configuration/nedi?name=cloudhub-nedi-21.xml&profile=df74dfe7-b362-4caf-8e37-a649c31295e7" class="btn btn-success">Configure NeDi</router-link>
            <router-link to="/metrics/nedi?name=cloudhub-nedi-21.xml&profile=df74dfe7-b362-4caf-8e37-a649c31295e7" class="btn btn-success">Metrics</router-link>
        </div>
    </div>
</template>

<script>
export default {
  name: 'Home',
  data () {
    return {
    }
  }
}
</script>

<style scoped>
</style>
