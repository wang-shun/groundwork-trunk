<template>
    <div class="container-fluid">
        <nav class="text-left text-nav navbar-fixed-top">
            <a href="/cloudhub/mvc/home/listAllConfigurations"><span>Home</span></a>
        </nav>

        <div class="row margin-top90">
            <div class="col-sm-12 col-md-6">
                <form class="form-horizontal" >
                    <div class="form-group">
                        <label for="display-name" class="col-sm-2 control-label">Display Name</label>
                        <div class="col-sm-6">
                            <input type="text" class="form-control" id="display-name" name="display-name" v-model="displayName" readonly />
                        </div>

                        <div class="col-sm-2 text-right" v-if="suspended">
                            <div class="red-circle pull-left"></div><p class="form-control-static">Suspended</p>
                        </div>
                        <div class="col-sm-2 text-right" v-if="!suspended && (monitorExceptionCount > 0)">
                            <div class="yellow-circle pull-left"></div><p class="form-control-static">Running with errors</p>
                        </div>
                        <div class="col-sm-2 text-right" v-if="!suspended && !monitorExceptionCount">
                            <div class="green-circle pull-left"></div><p class="form-control-static">Running</p>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="config-name" class="col-sm-2 control-label">Name</label>
                        <div class="col-sm-6">
                            <input type="text" class="form-control" id="config-name" name="config-name" v-model="configName" readonly />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="agent-id" class="col-sm-2 control-label">Agent ID</label>
                        <div class="col-sm-6">
                            <input type="text" class="form-control" id="agent-id" name="agent-id" v-model="agentId" readonly />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="agent-id" class="col-sm-2 control-label">Connection State</label>
                        <div class="col-sm-6">
                            <input type="text" class="form-control" id="agent-id" name="agent-id" v-model="connectionState" readonly />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="retry-limit" class="col-sm-2 control-label">Retry Limit</label>
                        <div class="col-sm-2">
                            <input type="number" class="form-control" id="retry-limit" name="retry-limit" v-model="connectionRetries" readonly />
                        </div>

                        <label for="check-interval" class="col-sm-2 control-label">Check Interval</label>
                        <div class="col-sm-2">
                            <input type="number" class="form-control" id="check-interval" name="check-interval" v-model="checkIntervalMinutes" readonly />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="connection-type" class="col-sm-2 control-label">Connection Type</label>
                        <div class="col-sm-6">
                            <input type="text" class="form-control" id="connection-type" name="connection-type" v-model="connectorType" readonly />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="groundwork-errors" class="col-sm-2 control-label">Groundwork Errors</label>
                        <div class="col-sm-2">
                            <input type="number" class="form-control" id="groundwork-errors" name="groundwork-errors" v-model="groundworkExceptionCount" readonly />
                        </div>

                        <label for="monitoring-errors" class="col-sm-2 control-label">Monitoring Errors</label>
                        <div class="col-sm-2">
                            <input type="number" class="form-control" id="monitoring-errors" name="monitoring-errors" v-model="monitorExceptionCount" readonly />
                        </div>
                    </div>

                    <div class="form-group" v-if="lastError">
                        <label for="connection-type" class="col-sm-2 control-label">Last Error</label>
                        <div class="col-sm-6">
                            <p class="form-control-static text-left">{{lastError.message}}</p>
                        </div>
                        <div class="col-sm-3">
                            <p class="form-control-static">{{lastMessageDateTime}}</p>
                        </div>
                    </div>

                    <div class="panel-group text-left" id="errors-accordion" role="tablist" v-if="errors && errors.length">
                        <div class="panel panel-default">
                            <div class="panel-heading" role="tab">
                                <h4 class="panel-title">
                                    <a class="collapsed" role="button" data-toggle="collapse" data-parent="#accordion" href="#errors-panel" aria-expanded="false" v-bind:aria-controls="errors-panel">Errors</a>
                                </h4>
                            </div>
                            <div id="errors-panel" class="panel-collapse collapse" role="tabpanel">
                                <div class="panel-body">
                                    <p class="form-control-static" v-for="error in errors">{{error.message}} {{messageDateTime(error.timestamp)}}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<script>
import statusService from '../services/status-service';

export default {
  name: 'Status',
  data () {
    return {
      service: new statusService(),
      configName: '',
      name: '',
      agentId: '',
      connectorType: '',
      applicationType: '',
      connectionState: '',
      groundworkServer: '',
      checkIntervalMinutes: 1,
      connectionRetries: -1,
      monitorServer: "",
      errors: [],
      lastError: null,
      groundworkExceptionCount: 0,
      monitorExceptionCount: 0,
      suspended: false,
      mergeHosts: false
    }
  },
  mounted: function() {
    this.configName = this.$route.query.name || '';
    this.retrieve();
  },
  computed: {
    lastMessageDateTime: function() { 
      if(!this.lastError || !this.lastError.timestamp) {
        return '';
      }

      return new Date(this.lastError.timestamp).toLocaleString();
    }
  },
  methods: {
    retrieve: function() {
      var self = this;

      this.service.retrieve(this.configName, function(statusData)
      {
        self.displayName = statusData.displayName;
        self.name = statusData.name;
        self.agentId = statusData.agentId;
        self.connectorType = statusData.connectorType;
        self.applicationType = statusData.applicationType;
        self.connectionState = statusData.connectionState;
        self.groundworkServer = statusData.groundworkServer;
        self.checkIntervalMinutes = statusData.checkIntervalMinutes;
        self.connectionRetries = statusData.connectionRetries;
        self.monitorServer = statusData.monitorServer;
        self.errors = statusData.errors;
        self.lastError = statusData.lastError;
        self.groundworkExceptionCount = statusData.groundworkExceptionCount;
        self.monitorExceptionCount = statusData.monitorExceptionCount;
        self.suspended = statusData.suspended;
      });
    },

    messageDateTime: function(timestamp) { 
      if(!timestamp) {
        return '';
      }

      return new Date(timestamp).toLocaleString();
    }
  }
}
</script>

<style>
.text-right {
    text-align: right;
}

.red-circle {
    width: 32px;
    height: 32px;
    border-radius: 16px;
    background-color: darkred;
    margin-right: 10px;
}

.yellow-circle {
    width: 32px;
    height: 32px;
    border-radius: 16px;
    background-color: yellow;
    margin-right: 10px;
}

.green-circle {
    width: 32px;
    height: 32px;
    border-radius: 16px;
    background-color: green;
    margin-right: 10px;
}
</style>