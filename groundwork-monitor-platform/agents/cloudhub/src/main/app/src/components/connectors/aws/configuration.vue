<template>
    <div class="container-fluid">
        <nav class="text-left text-nav navbar-fixed-top">
            <a rel="/cloudhub/mvc/home/listAllConfigurations" href="javascript:void(0)" v-on:click="goHome"><span>Home</span></a>&nbsp;/&nbsp;<span><b>Configuration</b></span><span v-show="configName.length"> (also: <router-link v-bind:to="metricsLink"><span>Metrics</span></router-link>)</span>
            <router-link class="btn btn-default pull-right" id="connectornext" v-bind:to="metricsLink" v-show="configName.length"><span>Next</span></router-link>
            <button type="button" class="btn btn-default pull-right" v-on:click="save" :disabled="errors.any()">Save</button>
        </nav>
        <div class="row margin-top90">
            <div class="col-md-4">
                <p><strong>GroundWork Server</strong></p>
                <hr />
                <form class="form-horizontal">
                    <div class="form-group">
                        <label for="config-version" class="col-sm-3 control-label">Version</label>
                        <div class="col-sm-9">
                            <select class="form-control" id="config-version" v-model="gwos.gwosVersion" :disabled="!common.canAccessMultipleVersions">
                                <option value="7.1">7.1</option>
                                <option value="7.0">7.0</option>
                            </select>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="config-hostname" class="col-sm-3 control-label">Hostname</label>
                        <div class="col-sm-9">
                            <input type="text" :class="{'form-control': true, 'invalid': errors.has('config-hostname') }" id="config-hostname" name="config-hostname" placeholder="Hostname" v-model="gwos.gwosServer" v-validate.initial="{ rules: { required: true, regex: /^[a-zA-Z0-9_\.\-\:]*$/ } }" />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="config-username" class="col-sm-3 control-label">Username</label>
                        <div class="col-sm-9">
                            <input autocomplete="new-password" type="text" :class="{'form-control': true, 'invalid': errors.has('config-username') }" id="config-username" name="config-username" placeholder="Username" v-model="gwos.wsUsername" v-validate.initial="{ rules: { required: true } }" />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="config-token" class="col-sm-3 control-label">Token</label>
                        <div class="col-sm-9">
                            <input autocomplete="new-password" type="password" :class="{'form-control': true, 'invalid': errors.has('config-token') }" id="config-token" name="config-token" placeholder="Token" v-model="gwos.wsPassword" v-validate.initial="{ rules: { required: true } }" />
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-sm-4 col-xs-12">
                            <div class="checkbox">
                                <label>
                                  <input id="config-ssl" name="config-ssl" type="checkbox" v-model="gwos.gwosSSLEnabled"> SSL
                                </label>
                            </div>
                        </div>
                        <div class="col-sm-4 col-xs-12">
                            <div class="checkbox">
                                <label>
                                  <input id="config-merge" name="config-merge" type="checkbox" v-model="gwos.mergeHosts"> Merge Hosts
                                </label>
                            </div>
                        </div>
                        <div class="col-sm-4 col-xs-12">
                            <div class="checkbox">
                                <label>
                                  <input id="config-monitor" name="config-monitor" type="checkbox" v-model="gwos.monitor"> Monitor
                                </label>
                            </div>
                        </div>
                    </div>

                    <hr />

                  <div class="row">
                    <div class="col-sm-6 col-xs-6">
                      <div class="form-group">
                          <label class="col-sm-8 col-xs-8 control-label">Connection Status</label>
                          <div class="col-sm-4 col-xs-4">
                              <div id="config-connection-status" class="status" :class="{ 'status-error': !state.connected,'status-success': state.connected }"></div>
                          </div>
                      </div>
                    </div>

                    <div class="col-sm-4 col-xs-6">
                      <div class="form-group text-center">
                        <div class="col-sm-12">
                            <button type="button" class="btn btn-default" v-on:click="testGW">Test</button>
                        </div>
                      </div>
                    </div>
                  </div>

                  <hr class="visible-xs-block visible-sm-block" />
                </form>
            </div>
            <div class="col-md-4">
                <p><strong>AWS Connector</strong></p>
                <hr />
                <form class="form-horizontal">
                    <div class="form-group">
                        <label for="config-displayName" class="col-sm-4 control-label">Display Name</label>
                        <div class="col-sm-8">
                            <input type="text" :class="{'form-control': true, 'invalid': errors.has('config-displayName') }" id="config-displayName" name="config-displayName" placeholder="Display Name" v-model="common.displayName" v-validate.initial="{ rules: { required: true } }" />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="config-region-endpoint" class="col-sm-4 control-label">AWS Region Endpoint</label>
                        <div class="col-sm-8">
                            <input type="text" :class="{'form-control': true, 'invalid': errors.has('config-region-endpoint') }" id="config-region-endpoint" name="config-region-endpoint" placeholder="AWS Region Endpoint" v-model="connection.server" v-validate="{ rules: { required: true } }" />
                        </div>
                    </div>

                    <div class="well">
                        <div class="form-group">
                            <div class="col-sm-12 col-xs-12">
                                <div class="checkbox">
                                    <label>
                                        <input id="config-aim" name="config-aim" type="checkbox" v-model="connection.enableIAMRoles"> Enable AIM Roles
                                    </label>
                                </div>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="config-region-endpoint" class="col-sm-4 control-label">AWS Access Key ID</label>
                            <div class="col-sm-8">
                                <input type="text" :class="{'form-control': true }" placeholder="AWS Access Key ID" v-model="connection.username" :disabled="connection.enableIAMRoles" />
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="config-region-endpoint" class="col-sm-4 control-label">AWS Secret Key ID</label>
                            <div class="col-sm-8">
                                <input type="text" :class="{'form-control': true }" placeholder="AWS Secret Key ID" v-model="connection.password" :disabled="connection.enableIAMRoles" />
                            </div>
                        </div>
                    </div>

                    <div class="well">
                        <div class="form-group">
                            <div class="col-sm-12 col-xs-12">
                                <div class="checkbox">
                                    <label>
                                        <input id="config-tagging" name="config-tagging" type="checkbox" v-model="common.enableGroupTag"> Enable Hostgroup Tagging
                                    </label>
                                </div>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="config-region-endpoint" class="col-sm-4 control-label">Hostgroup Tag Name</label>
                            <div class="col-sm-8">
                                <input type="text" :class="{'form-control': true, 'invalid': errors.has('config-enableGroupTag') }" id="config-enableGroupTag" name="config-enableGroupTag" placeholder="Hostgroup Tag Name" v-model="common.groupTag" :disabled="!common.enableGroupTag" />
                            </div>
                        </div>
                    </div>

                    <div class="form-group form-vcentered">
                        <div class="col-sm-5 col-xs-6">
                            <div class="checkbox">
                                <label>
                                  <input id="config-aws-ssl" name="config-aws-ssl" type="checkbox" v-model="gwos.gwosSSLEnabled"> Enable SSL
                                </label>
                            </div>
                        </div>
                        <div class="col-sm-7 col-xs-6">
                          <div class="checkbox">
                              <span class="pull-left hidden-xs">Cloudhub Interval (min):</span>
                              <span class="col-xs-12 visible-xs">Cloudhub Interval (min):</span>
                              <span class="col-sm-6 col-xs-12 no-padding-right short-padding-left">
                                <input id="config-check-interval" name="config-check-interval" type="number" :class="{'form-control': true, 'invalid': errors.has('config-check-interval') }" min="1" max="9999" v-model="common.checkIntervalMinutes" v-validate="{ rules: { required: true } }" />
                              </span>
                          </div>
                        </div>
                    </div>

                    <div class="form-group form-vcentered">
                        <div class="col-sm-5 col-xs-6">
                          <div class="checkbox">
                            <label class="hidden-xs">
                              <input id="config-infinite-retries-1" name="config-infinite-retries-1" type="checkbox" v-model="common.connectionRetries" v-bind:true-value="-1" v-bind:false-value="10" v-validate> Infinite Retries
                            </label>
                            <span class="col-xs-12 visible-xs">Infinite Retries:</span>
                            <label class="col-xs-12 visible-xs">
                              <input id="config-infinite-retries-2" name="config-infinite-retries-2" type="checkbox" v-model="common.connectionRetries" v-bind:true-value="-1" v-bind:false-value="10" class="checkbox-margin" v-validate> Yes
                            </label>
                          </div>
                        </div>
                        <div class="col-sm-7 col-xs-6">
                          <div class="checkbox">
                              <span class="pull-left col-sm-6 hidden-xs">Retry Limit:</span>
                              <span class="col-xs-12 visible-xs">Retry Limit:</span>
                              <span class="col-sm-6 col-xs-12 no-padding-right short-padding-left align-retry-limit">
                                <input id="config-retry-limit" name="config-retry-limit" type="number" :class="{'form-control': true, 'invalid': errors.has('config-retry-limit') }" min="-1" max="999" :disabled="common.connectionRetries == -1" v-model="common.connectionRetries" v-validate="{ rules: { required: true } }" />
                              </span>
                          </div>
                        </div>
                    </div>

                    <hr class="no-margin-top" />

                    <div class="row">
                      <div class="col-sm-6 col-xs-6">
                        <div class="form-group">
                            <label class="col-sm-8 col-xs-8 control-label">Connection Status</label>
                            <div class="col-sm-4 col-xs-4">
                                <div id="config-server-connection-status"  class="status" :class="{ 'status-error': !state.connected,'status-success': state.connected }"></div>
                            </div>
                        </div>
                      </div>

                      <div class="col-sm-4 col-xs-6">
                        <div class="form-group text-center">
                          <div class="col-sm-12">
                              <button type="button" class="btn btn-default" v-on:click="testConnection">Test</button>
                          </div>
                        </div>
                      </div>
                    </div>

                    <hr class="visible-xs-block visible-sm-block" />

                </form>
            </div>

            <div class="col-md-4">
                <p><strong>Views</strong></p>
                <hr />
                    <div class="form-group form-vcentered">
                        <div class="col-sm-12 col-xs-12" v-for="view in views">
                          <div class="checkbox">
                            <label class="hidden-xs">
                              <input type="checkbox" v-model="view.enabled"> {{view.name == 'storageView' ? 'Storage View' : (view.name == 'networkView' ? 'Network View' : (view.name == 'customView' ? 'Custom View' : view.name))}}
                            </label>
                            <span class="col-xs-12 visible-xs">{{view.name == 'storageView' ? 'Storage View' : (view.name == 'networkView' ? 'Network View' : (view.name == 'customView' ? 'Custom View' : view.name))}}:</span>
                            <label class="col-xs-12 visible-xs">
                              <input type="checkbox" v-model="view.enabled" class="checkbox-margin"> Yes
                            </label>
                          </div>
                        </div>
                    </div>
            </div>
        </div>

        <hsy-dialog class="info" v-model="testing" :clickMask2Close="false">
            <div slot="title">Test: {{testSubject}}</div>
            <div slot="body" class="test-dialog">
                <div v-show="testingStatus == 'testing'">Testing...</div>
                <div class="success" v-show="testingStatus == 'success'">Success.</div>
                <div class="failure" v-show="testingStatus == 'failure'">Failed: {{testError}}</div>
            </div>
        </hsy-dialog>

        <hsy-dialog class="confirm" v-model="prefix">
            <div slot="title">Prefix Service Names with Cluster?</div>
            <div slot="body">
                <div>Cluster Prefix has changed. Changing the prefix will rename all of your Hostnames for NeDi Services.<br /><br />Are you sure you want to proceed?</div><br />
                <div>
                    <button @click="prefixYes">Yes</button>&nbsp;&nbsp;
                    <button @click="prefixNo">No</button>
                </div>
            </div>
        </hsy-dialog>

        <hsy-dialog class="confirm" v-model="visible">
            <div slot="title">Are you sure?</div>
            <div slot="body">
                <div>If you leave now, you will lose all your changes.<br />Are you sure you want to navigate away?</div><br />
                <div>
                    <button @click="handleYes">Yes</button>&nbsp;&nbsp;
                    <button @click="handleNo">No</button>
                </div>
            </div>
        </hsy-dialog>
    </div>
</template>

<script>
import AWSService from './aws-service'

export default {
  name: 'Configuration',
  data () {
    return {
      service: new AWSService(),
      configName: '',
      state: '',

      common: {
        displayName: '',
        enableGroupTag: false,
        groupTag: '',
        checkIntervalMinutes: 0
      },
      gwos: {
        gwosServer: '',
        wsUsername: '',
        wsPassword: '',
        monitor: false
      },
      connection: {
        enableIAMRoles: false
      },

      views: [],

      viewsInitial: [],
      prefixServiceNamesInitial: false,

      monitorInitial: false,
      gwosServerInitial: '',
      displayNameInitial: '',

      testing: false,
      testSubject: '',
      testingStatus: 'testing',
      testError: '',

      prefix: false,
      visible: false,
      next: null,
      metricsLink: ''
    }
  },
  mounted: function() {
    this.configName = this.$route.query.name || '';

    this.retrieve(this.configName);
  },
  methods: {
    retrieve: function(configName) {
      var self = this;
      this.service.retrieve(configName, function(configurationData)
      {
        self.common  = configurationData.common;
        self.gwos = configurationData.gwos;
        self.connection  = configurationData.connection;
        self.views = configurationData.views;
        self.state = configurationData.state;
        self.viewsInitial = JSON.parse(JSON.stringify(configurationData.views));
        self.prefixServiceNamesInitial = self.common.prefixServiceNames;

        self.monitorInitial = self.gwos.monitor;
        self.gwosServerInitial = self.gwos.gwosServer;
        self.displayNameInitial = self.common.displayName;

        self.metricsLink = '/metrics/aws?name=' + self.common.configurationFile + '&profile=' + self.common.agentId;
      });
    },

    getState: function() {
        var viewsRemoved = [], viewsAdded = [], initialHash = {};

        for(var i = 0, iLimit = this.viewsInitial.length; i < iLimit; i++) {
            var initial = this.viewsInitial[i];

            initialHash[initial.name] = initial.enabled;
        }

        for(var i = 0, iLimit = this.views.length; i < iLimit; i++) {
            var view = this.views[i];

            if (initialHash[view.name] && !view.enabled) {
                viewsRemoved.push(view.name);
            }
        }

        for(var i = 0, iLimit = this.views.length; i < iLimit; i++) {
            var view = this.views[i];

            if (!initialHash[view.name] && view.enabled) {
                viewsAdded.push(view.name);
            }
        }

        return {
            "viewsAdded": viewsAdded,
            "viewsRemoved": viewsRemoved,
            "prefixServiceNamesChanged": (this.prefixServiceNamesInitial !== this.common.prefixServiceNames),
            "monitorChanged": (this.monitorInitial !== this.gwos.monitor),
            "gwosServerChanged": (this.gwosServerInitial !== this.gwos.gwosServer),
            "displayNameChanged": (this.displayNameInitial !== this.common.displayName)
        };
    },

    save: function(event, skipCheck) {
        if (!skipCheck && !this.$route.query.name && (this.getState().prefixServiceNamesChanged)) {
            this.prefix = true;
            return;
        }

        // Correct view info:
        for(var i = 0, iLimit = this.views.length; i < iLimit; i++)
        {
            var view = this.views[i];

            switch(view.name)
            {
                case "networkView":
                    this.common.networkView = view.enabled;
                    break;

                case "storageView":
                    this.common.storageView = view.enabled;
                    break;

                case "customView":
                    this.common.customView = view.enabled;
                    break;
            }
        }

        var self = this;

        this.service.save({
            state: this.getState(),
            common: this.common,
            gwos: this.gwos,
            connection: this.connection
        },
        function(result) {
            self.configName = 'New';
            self.common.configurationFile = result.configuration.common.configurationFile;
            self.common.configurationPath = result.configuration.common.configurationPath;
            self.common.agentId = result.configuration.common.agentId;

            self.metricsLink = '/metrics/aws?name=' + self.common.configurationFile + '&profile=' + self.common.agentId;

            Object.keys(self.fields).forEach(field => {
              self.$validator.flag(field, {
                pristine: true,
                dirty: false,
                untouched: true,
                touched: false
              })
            });

            self.viewsInitial = JSON.parse(JSON.stringify(self.views));
            self.prefixServiceNamesInitial = self.common.prefixServiceNames;

            self.monitorInitial = self.gwos.monitor;
            self.gwosServerInitial = self.gwos.gwosServer;
            self.displayNameInitial = self.common.displayName;

            self.$router.push({ path: '/configuration/aws', query: { name: self.common.configurationFile }})
        });
    },

    testGW: function() {
        var self = this;
        this.testingStatus = 'testing';
        this.testSubject = 'groundwork';
        this.testing = true;

        this.service.test({
            state: this.getState(),
            common: this.common,
            gwos: this.gwos,
            connection: this.connection/*,
            views: this.views*/
        },
        'groundwork',
        function() {
            self.testingStatus = 'success';

            setTimeout(function() {
                self.testing = false;
            },
            10000);
        },
        function(error) {
            self.testError = (error === false) ? 'server error' : error;
            self.testingStatus = 'failure'

            setTimeout(function() {
                self.testing = false;
            },
            20000);
        });
    },

    testConnection: function() {
        var self = this;
        this.testingStatus = 'testing';
        this.testSubject = 'connection';
        this.testing = true;

        this.service.test({
            state: this.getState(),
            common: this.common,
            gwos: this.gwos,
            connection: this.connection/*,
            views: this.views*/
        },
        'connector',
        function() {
            self.testingStatus = 'success';

            setTimeout(function() {
                self.testing = false;
            },
            30000);
        },
        function(error) {
            self.testError = (error === false) ? 'server error' : error;
            self.testingStatus = 'failure'

            setTimeout(function() {
                self.testing = false;
            },
            20000);
        });
    },

    prefixYes: function() {
        this.prefix = false;

        this.save(null, true);
    },

    prefixNo: function() {
        this.prefix = false;
    },

    handleYes: function() {
        this.visible = false;
        var self = this;

        setTimeout(function() {
            if(self.next) {
                if(typeof self.next === 'function') {
                    self.next();
                }
                else {
                    window.location = self.next;
                }
            }

            self.next = null;
        },
        100);
    },

    handleNo: function() {
        this.visible = false;
        this.next(false);
    },

    anyDirty: function() {
        var anyDirty = false;

        for(var fieldName in this.fields) {
            if(this.fields[fieldName].dirty) {
                anyDirty = true;
                break;
            }
        }

        if(!anyDirty) {
            var initialHash = {};

            for(var i = 0, iLimit = this.viewsInitial.length; i < iLimit; i++) {
                var initial = this.viewsInitial[i];

                initialHash[initial.name] = initial.enabled;
            }

            for(var i = 0, iLimit = this.views.length; i < iLimit; i++) {
                var view = this.views[i], name = view.name, enabled = view.enabled;

                if (initialHash[name] != enabled) {
                    anyDirty = true;
                    break;
                }
            }
        }

        return anyDirty;
    },

    goHome: function(event) {
        var link = event.currentTarget,
            anyDirty = this.anyDirty();

        if(anyDirty) {
            this.visible = true;
            this.next = link.getAttribute('rel');
        }
        else {
            window.location = link.getAttribute('rel');
        }
    }
  },

  beforeRouteLeave: function(to, from, next) {
    var anyDirty = this.anyDirty();

    if(anyDirty) {
        this.visible = true;
        this.next = next;
    }
    else {
        next();
    }
  }
}
</script>

<style scoped>
.form-horizontal {
    text-align: left;
}

.status {
    height: 9px;
    width: 9px;
    border-radius: 4px;
}

form .status {
    position: relative;
    top: 12px;
}

.status.status-error {
    border: 1px solid red;
    background: red;
}

.status.status-success {
  border: 1px solid green;
  background: green;
}

.no-margin-top {
    margin-top: 0;
}

.no-padding-left {
    padding-left: 0 !important;
}

.no-padding-right {
    padding-right: 0 !important;
}

.short-padding-left {
    padding-left: 10px !important;
}

.checkbox-margin {
    margin-left: -5px !important;
}

.form-vcentered {
    line-height: 32px;
}

.form-vcentered input[type='checkbox'] {
    position: relative;
    top: 3px;
}

.align-retry-limit {

}

input.invalid {
    border: 1px solid red;
    background: #fee;
}

.test-dialog {
  overflow-y: hidden;
}

#connectornext {
    margin-left: 20px;
}
</style>
