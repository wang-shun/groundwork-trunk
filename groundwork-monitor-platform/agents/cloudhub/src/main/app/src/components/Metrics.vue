<template>
    <div class="container-fluid">
        <nav class="text-left text-nav navbar-fixed-top">
            <a rel="/cloudhub/mvc/home/listAllConfigurations" href="javascript:void(0)" v-on:click="goHome"><span>Home</span></a>&nbsp;/&nbsp;<span><b>Metrics</b></span> (also: <router-link v-bind:to="configLink"><span>Configuration</span></router-link>)
            <a rel="/cloudhub/mvc/home/listAllConfigurations" href="javascript:void(0)" v-on:click="goHome" class="btn btn-default pull-right metrics-btn"><span>Next</span></a>
            <button class="btn btn-default pull-right metrics-btn" @click="save">Save</button>
            <button class="btn btn-default pull-right metrics-btn" @click="checkupdates" v-show="connectorName == 'vmware' || connectorName == 'aws'">Check for Updates</button>
            <button class="btn btn-default pull-right" @click="refreshcustom" v-show="connectorName == 'aws' && hasCustomViews">Refresh Custom</button>
        </nav>
        <div class="margin-top90 panel-group text-left" id="accordion" role="tablist">
            <div class="panel panel-default" v-for="section in sortedSections">
                <div class="panel-heading" role="tab">
                    <h4 class="panel-title">
                        <a class="collapsed" role="button" data-toggle="collapse" data-parent="#accordion" v-bind:href="'#' + section.name.replace(/\W+/g, '_')" aria-expanded="false" v-bind:aria-controls="section.name.replace(/\W+/g, '_')">
                        {{section.displayName}} (<span class="text-danger">{{section.metrics.length}}</span> metrics, <span class="text-warning">{{section.active}}</span> active, <span class="text-info">{{section.synthetic}}</span> synthetic)
                        </a>
                    </h4>
                </div>
                <div v-bind:id="section.name.replace(/\W+/g, '_')" class="panel-collapse collapse" role="tabpanel">
                    <div class="panel-body">
                        <v-client-table :data="section.metrics" :columns="columns" :options="options"></v-client-table>
                        <button type="button" class="btn btn-success btn-add-normal" @click="addNormalMetric(section.name)">Add Normal Metric</button>&nbsp;&nbsp;
                        <button type="button" class="btn btn-success btn-add-synthetic" @click="addSyntheticMetric(section.name)">Add Synthetic Metric</button>
                    </div>
                </div>
            </div>
        </div>

        <hsy-dialog class="edit" v-model="editModalShown" :clickMask2Close="false">
            <div slot="title">{{editOptions.editTitle}}</div>
            <div slot="body">
                <div>
                    <form class="form-horizontal">
                        <div class="form-group">
                            <label for="normalMetricNameEdit" class="col-sm-3 control-label">Metric Name</label>
                            <div class="col-sm-9">
                                <typeahead id="normalMetricNameEdit" v-model="editOptions.editData.name" :data="metricNames" placeholder="Metric Name"></typeahead>
                                <span class="help-block" v-show="!editOptions.editData.name || !editOptions.editData.name.trim().length">Metric name cannot be blank.</span>
                                <span class="help-block" v-show="editOptions.editData.name && !editOptions.editData.customName && !isUnique(editOptions.editData)">Metric name must be unique.</span>
                                <span class="help-block" v-show="editOptions.editData.name && containsDashes(editOptions.editData.name)">Metric name cannot contain dashes.</span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="normalMetricFormatEdit" class="col-sm-3 control-label">Metric Format String</label>
                            <div class="col-sm-9">
                                <input type="text" class="form-control" id="normalMetricFormatEdit" placeholder="Metric Format" v-model="editOptions.editData.format" autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false" />
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="normalDisplayNameEdit" class="col-sm-3 control-label">Display Name</label>
                            <div class="col-sm-9">
                                <input type="text" class="form-control" id="normalDisplayNameEdit" placeholder="Display Name" v-model="editOptions.editData.customName" autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false" />
                                <span class="help-block" v-show="editOptions.editData.customName && !isUnique(editOptions.editData)">Metric name must be unique.</span>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="col-sm-offset-3 col-sm-2">
                                <div class="checkbox">
                                    <label>
                                        <input type="checkbox" v-model="editOptions.editData.monitored"> Monitor
                                    </label>
                                </div>
                            </div>
                            <div class="col-sm-2">
                                <div class="checkbox">
                                    <label>
                                        <input type="checkbox" v-model="editOptions.editData.graphed"> Graph
                                    </label>
                                </div>
                            </div>
                            <div class="col-sm-3">
                                <div class="checkbox">
                                    <label>
                                        <input id="health" name="health" type="checkbox" v-model="editOptions.editData.computeType" v-bind:true-value="'health'" /> Health Check
                                    </label>
                                </div>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="normalWarningThresholdEdit" class="col-sm-3 control-label">Default Warning Threshold</label>
                            <div class="col-sm-9">
                                <input type="number" class="form-control" id="normalWarningThresholdEdit" v-model="editOptions.editData.warningThreshold" />
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="normalCriticalThresholdEdit" class="col-sm-3 control-label">Default Critical Threshold</label>
                            <div class="col-sm-9">
                                <input type="number" class="form-control" id="normalCriticalThresholdEdit" v-model="editOptions.editData.criticalThreshold" />
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="normalDescriptionEdit" class="col-sm-3 control-label">Description</label>
                            <div class="col-sm-9">
                                <input type="text" class="form-control" id="normalDescriptionEdit" placeholder="Description" v-model="editOptions.editData.description" />
                            </div>
                        </div>
                    </form>
                </div>
                <br />
                <div>
                    <button :disabled="!editOptions.editData.name || !editOptions.editData.name.trim().length || !isUnique(editOptions.editData) || containsDashes(editOptions.editData.name)" @click="editYes">Save</button>&nbsp;&nbsp;
                    <button @click="editNo">Cancel</button>
                </div>
            </div>
        </hsy-dialog>

        <hsy-dialog class="edit" v-model="editSyntheticModalShown" :clickMask2Close="false">
            <div slot="title">{{editOptions.editTitle}}</div>
            <div slot="body">
                <div>
                    <form class="form-horizontal">
                        <div class="form-group">
                            <label for="syntheticMetricNameEdit" class="col-sm-3 control-label">Metric Name</label>
                            <div class="col-sm-9">
                                <input id="syntheticMetricNameEdit" class="form-control" v-model="editOptions.editData.name" placeholder="Metric Name" autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false" />
                                <span class="help-block" v-show="!editOptions.editData.name || !editOptions.editData.name.trim().length">Metric name cannot be blank.</span>
                                <span class="help-block" v-show="editOptions.editData.name && !editOptions.editData.customName && !isUnique(editOptions.editData)">Metric name must be unique.</span>
                                <span class="help-block" v-show="editOptions.editData.name && containsDashes(editOptions.editData.name)">Metric name cannot contain dashes.</span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="syntheticMetricFormatEdit" class="col-sm-3 control-label">Metric Format String</label>
                            <div class="col-sm-9">
                                <input type="text" class="form-control" id="syntheticMetricFormatEdit" placeholder="Metric Format" v-model="editOptions.editData.format" autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false" />
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="expressionEdit" class="col-sm-3 control-label">Metric Expression</label>
                            <div class="col-sm-9">
                                <typeahead id="expressionEdit" v-model="editOptions.editData.expression" :data="currentMetrics" :blur="getInputMetrics" placeholder="Metric Expression" autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false"></typeahead>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="syntheticDisplayNameEdit" class="col-sm-3 control-label">Display Name</label>
                            <div class="col-sm-9">
                                <input type="text" class="form-control" id="syntheticDisplayNameEdit" placeholder="Display Name" v-model="editOptions.editData.customName" autocomplete="off" autocorrect="off" autocapitalize="off" spellcheck="false" />
                                <span class="help-block" v-show="editOptions.editData.customName && !isUnique(editOptions.editData)">Metric name must be unique.</span>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="col-sm-offset-3 col-sm-2">
                                <div class="checkbox">
                                    <label>
                                        <input type="checkbox" v-model="editOptions.editData.monitored"> Monitor
                                    </label>
                                </div>
                            </div>
                            <div class="col-sm-2">
                                <div class="checkbox">
                                    <label>
                                        <input type="checkbox" v-model="editOptions.editData.graphed"> Graph
                                    </label>
                                </div>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="syntheticWarningThresholdEdit" class="col-sm-3 control-label">Default Warning Threshold</label>
                            <div class="col-sm-9">
                                <input type="number" class="form-control" id="syntheticWarningThresholdEdit" v-model="editOptions.editData.warningThreshold" />
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="syntheticCriticalThresholdEdit" class="col-sm-3 control-label">Default Critical Threshold</label>
                            <div class="col-sm-9">
                                <input type="number" class="form-control" id="syntheticCriticalThresholdEdit" v-model="editOptions.editData.criticalThreshold" />
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="syntheticDescriptionEdit" class="col-sm-3 control-label">Description</label>
                            <div class="col-sm-9">
                                <input type="text" class="form-control" id="syntheticDescriptionEdit" placeholder="Description" v-model="editOptions.editData.description" />
                            </div>
                        </div>

                        <hr />

                        <div class="row">
                            <div class="col-sm-9 border-right">
                                <p>Values:</p>

                                <div class="form-group col-sm-12">
                                    <label class="col-sm-4 control-label">{{editOptions.editData.name}}</label>
                                    <div class="col-sm-4">
                                        <p class="form-control-static">= {{outputValue}}</p>
                                    </div>
                                    <div class="col-sm-4">
                                        <button @click="evaluate" type="button" class="btn btn-info">Evaluate</button>
                                    </div>
                                </div>

                                <div class="col-sm-8 no-padding-left"><p>Input Metric Values:</p></div>
                                <div class="col-sm-4 no-padding-left"><p>Override Value:</p></div>

                                <div class="form-group col-sm-12" v-for="inputMetric in inputMetrics">
                                    <label class="col-sm-4 control-label">{{inputMetric.name}}</label>
                                    <div class="col-sm-4">
                                        <p class="form-control-static">= {{inputMetric.value}}</p>
                                    </div>
                                    <div class="col-sm-4">
                                        <input type="number" class="form-control" v-model="inputMetric.overrideValue" />
                                    </div>
                                </div>
                            </div>
                            <div class="col-sm-3 padding-left30">
                                <div class="form-group">
                                    <div class="radio">
                                        <label>
                                            <input type="radio" name="syntheticValueType" value="warning" v-model="valueType" v-on:click="getInputMetrics"> Warning Threshold
                                        </label>
                                    </div>
                                    <div class="radio">
                                        <label>
                                            <input type="radio" name="syntheticValueType" value="critical" v-model="valueType" v-on:click="getInputMetrics"> Critical Threshold
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
                <br />
                <div>
                    <button :disabled="!editOptions.editData.name || !editOptions.editData.name.trim().length || !isUnique(editOptions.editData) || containsDashes(editOptions.editData.name)" @click="editYes">Save</button>&nbsp;&nbsp;
                    <button @click="editNo">Cancel</button>
                </div>
            </div>
        </hsy-dialog>

        <hsy-dialog class="confirm" v-model="removeModalShown">
            <div slot="title">Remove metric</div>
            <div slot="body">
                <div class="remove-dialog">Are you sure you want to remove the metric <b>{{removeOptions[0]}}</b> from <b>{{removeOptions[1]}}</b>?</div><br />
                <div>
                    <button @click="removeYes">Yes</button>&nbsp;&nbsp;
                    <button @click="removeNo">No</button>
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

        <hsy-dialog class="confirm" v-model="needsUpdate">
            <div slot="body">
                <div>Do you want to update your metric profile now?</div><br />
                <div>
                    <button @click="handleUpdateYes">Yes</button>&nbsp;&nbsp;
                    <button @click="handleUpdateNo">No</button>
                </div>
            </div>
        </hsy-dialog>

        <hsy-dialog class="confirm" v-model="needsNoUpdate">
            <div slot="body">
                <div>No updates are available at this time</div><br />
                <div>
                    <button @click="handleUpdateNo">Ok</button>
                </div>
            </div>
        </hsy-dialog>

    </div>
</template>

<script>
import {ClientTable, Event} from 'vue-tables-2';
import typeahead from './GWTypeahead';
import metricsService from '../services/metrics-service';

var metricComponentInstance = null;

function getMetricComponentInstance() {
    return metricComponentInstance;
}

function getMetricObject(serviceType, metricName) {
    var instance = getMetricComponentInstance();

    var section = instance.tableData[serviceType];

    for(var i = 0, iLimit = section.metrics.length; i < iLimit; i++) {
        var metric = section.metrics[i];

        if(metric.name == metricName) {
            return metric;
        }
    }

    return null;
}

function regexIndexOf(text, re, i) {
    var indexInSuffix = text.slice(i).search(re);
    return indexInSuffix < 0 ? indexInSuffix : indexInSuffix + i;
}

export default {
  name: 'Metrics',
  components: { 'typeahead': typeahead },
  data () {
    return {
      service: new metricsService(),
      columns: [
        'monitored',
        'graphed',
        'name',
        'customName',
        'warningThreshold',
        'criticalThreshold',
        'actions'
      ],
      connectorName: '',
      configName: '',
      tableData: {},
      hasCustomViews: false,
      metricNames: [],
      functionNames: [],
      metricData: {},
      metricsRemoved: [],
      editModalShown: false,
      editSyntheticModalShown: false,
      modalFlag: false,
      editOptions: {editTitle: 'Edit Metric', editIndex: 0, editData: {}, oldName: ''},
      inputMetrics: [],
      outputValue: '',
      valueType: 'warning',
      removeModalShown: false,
      removeOptions: [],
      configLink: '',
      excludes: null,
      visible: false,
      needsUpdate: false,
      needsNoUpdate: false,
      options: {
        perPage: 25,
        orderBy: {
            column: 'name',
            ascending: true
        },
        texts: {
          filter: "Filter Results: "
        },
        headings: {
          'monitored': 'Monitor?',
          'graphed': 'Graph?',
          'name': 'Metric Name',
          'customName': 'Display Name',
          'warningThreshold': 'Warning Threshold',
          'criticalThreshold': 'Critical Threshold',
          'actions': ''
        },
        rowClassCallback: function(row) {
          var classes = [];

          if(row.monitored) {
            classes.push('active');
          }
          else {
            classes.push('disabled');
          }

          if(row.computeType == 'synthetic') {
            classes.push('synthetic');
          }

          return classes.join(' ');
        },
        columnsClasses: {
          'name': 'metric-name',
          'customName': 'display-name',
          'warningThreshold': 'warning-threshold',
          'criticalThreshold': 'critical-threshold',
        },
        filterByColumn: false,
        filterable: false,
        templates: {
          'monitored': function(h, row) {
            var self = this;

            return h('input', {
                    attrs:{
                        'type': 'checkbox'
                    },
                    domProps: {
                        checked: row.monitored
                    },
                    on: {
                        change: function (event) {
                            row.monitored = event.target.checked;

                            var metric = getMetricObject(row.serviceType, row.name);
                            metric.monitored = event.target.checked;

                            self.$emit('change', event.target.checked);

                            var domrow = event.target;
                            while(domrow && domrow.nodeName.toLowerCase() != 'tr') {
                                domrow = domrow.parentNode;
                            }

                            domrow.className = row.monitored ? 'active' : 'disabled';

                            var instance = getMetricComponentInstance();

                            if(!row.monitored) {
                                instance.tableData[row.serviceType].active--;
                            }
                            else {
                                instance.tableData[row.serviceType].active++;
                            }
                        }
                    }
                });
          },
          'graphed': function(h, row) {
            var self = this;

            return h('input', {
                    attrs:{
                      'type': 'checkbox'
                    },
                    domProps: {
                        checked: row.graphed
                    },
                    on: {
                        change: function (event) {
                            row.graphed = event.target.checked;

                            var metric = getMetricObject(row.serviceType, row.name);
                            metric.graphed = event.target.checked;

                            self.$emit('change', event.target.checked);
                        }
                    }
            });
          },
          'name': function(h, row) {
            var self = this;

            return h('span', {
                    attrs:{
                        'title': row.description
                    }},
                    row.name
                );
          },
          'customName': function(h, row) {
            var self = this;

            return h('input', {
                    attrs:{
                        'type': 'text'
                    },
                    domProps: {
                        value: row.customName
                    },
                    on: {
                        keypress: function (event) {
                            if (event.which == 32) {
                                event.preventDefault();
                                return false;
                            }
                        },
                        input: function (event) {
                            row.customName = event.target.value;

                            var metric = getMetricObject(row.serviceType, row.name);
                            metric.customName = event.target.value;

                            self.$emit('input', event.target.value);
                        }
                    }
                });
          },
          'warningThreshold': function(h, row) {
            var self = this;

            return h('input', {
                    attrs:{
                        'type': 'number'
                    },
                    domProps: {
                        value: (row.warningThreshold < 0 ? '' : row.warningThreshold)
                    },
                    on: {
                        input: function (event) {
                            row.warningThreshold = +event.target.value;

                            var metric = getMetricObject(row.serviceType, row.name);
                            metric.warningThreshold = +event.target.value;

                            self.$emit('input', event.target.value);
                        }
                    }
                });
          },
          'criticalThreshold': function(h, row) {
            var self = this;

            return h('input', {
                    attrs:{
                        'type': 'number'
                    },
                    domProps: {
                        value: (row.criticalThreshold < 0 ? '' : row.criticalThreshold)
                    },
                    on: {
                        input: function (event) {
                            row.criticalThreshold = +event.target.value;

                            var metric = getMetricObject(row.serviceType, row.name);
                            metric.criticalThreshold = +event.target.value;

                            self.$emit('input', event.target.value);
                        }
                    }
                });
          },
          'actions': function(h, row) {
            var self = this;

            return [h('input', {
                    attrs:{
                      'type': 'button',
                      'class': 'btn btn-info btn-edit'
                    },
                    domProps: {
                        value: 'Edit'
                    },
                    on: {
                        click: function (event) {
                            var instance = getMetricComponentInstance();
                            var metric = getMetricObject(row.serviceType, row.name);

                            instance.inputMetrics = [];
                            instance.outputValue = '';

                            instance.editOptions.editTitle = 'Edit Metric';
                            instance.editOptions.editData = $.extend({}, metric);
                            instance.editOptions.oldName = metric.name;

                            var section = instance.tableData[row.serviceType];

                            for(var i = 0, iLimit = section.metrics.length; i < iLimit; i++) {
                                var metric = section.metrics[i];

                                if(metric.name == row.name) {
                                    instance.editOptions.editIndex = i;
                                    break;
                                }
                            }

                            instance.getInputMetrics();

                            if(metric.computeType == 'synthetic') {
                                instance.service.getGWFunctionNames(instance.$route.params.connector, function(functionNames) {
                                    instance.functionNames = functionNames;
                                    instance.editSyntheticModalShown = true;
                                });
                            }
                            else if(metric.computeType == 'health') {
                                instance.metricNames = [];

                                instance.service.getHealthCheckNames(instance.$route.params.connector, row.serviceType, instance.$route.query.name, function(names) {
                                    instance.metricNames = names;
                                    instance.editModalShown = true;
                                });
                            }
                            else {
                                instance.metricNames = [];

                                instance.service.getNames(instance.$route.params.connector, row.serviceType, instance.$route.query.name, function(names) {
                                    instance.metricNames = names;
                                    instance.editModalShown = true;
                                });
                            }

                            instance.closeTypeahead();
                        }
                    }
            }),
            h('input', {
                    attrs:{
                      'type': 'button',
                      'class': 'btn btn-danger btn-remove'
                    },
                    domProps: {
                        value: 'Remove'
                    },
                    on: {
                        click: function (event) {
                            var instance = getMetricComponentInstance();

                            instance.removeOptions = [row.name, row.serviceType];
                            instance.removeModalShown = true;

                            self.$emit('click', event.target.value);
                        }
                    }
            })];
          }
        }
      }
    }
  },
  mounted: function() {
    metricComponentInstance = this;

    this.connectorName = this.$route.params.connector;
    this.configName = this.$route.query.name || 'New';

    this.retrieve(this.$route.query.name, this.$route.query.profile);

    var self = this;
    this.$watch('editOptions.editData.computeType', function (newVal, oldVal) {
        self.changeHealthType();
    });
  },
  computed: {
    sortedSections: function() {
        var sorted = [];

        for(var index in this.tableData) {
            if((index != 'CLUSTER') && (index != 'HOST')) {
                sorted.push(this.tableData[index]);
            }
        }

        sorted.sort(function(a, b) {
            if(a.name && b.name) {
                return a.name.toLowerCase() > b.name.toLowerCase() ? 1 : -1;
            }
            else {
                return a.name > b.name ? 1 : -1;
            }
        });

        if(this.tableData.HOST) {
            sorted = [this.tableData.HOST].concat(sorted);
        }

        if(this.tableData.CLUSTER) {
            sorted = [this.tableData.CLUSTER].concat(sorted);
        }

        return sorted;
    },
    currentMetrics: function() {
        var section = this.tableData[this.editOptions.editData.serviceType];

        if(!section)
            return [];

        var metrics = [];

        for(var i = 0, iLimit = section.metrics.length; i < iLimit; i++) {
            var metric = section.metrics[i];

            if(metric.computeType !== 'health' && metric.computeType !== 'synthetic') {
                metrics.push(metric.customName || metric.name);
            }
        }

        metrics.sort(function(a, b) {
            if(a && b) {
                return (a.toLowerCase() < b.toLowerCase()) ? -1 : 1;
            }
            else {
                return (a < b) ? -1 : 1;
            }
        });

        metrics = metrics.concat(this.functionNames);
        return metrics;
    }
  },
  methods: {
    retrieve: function(configName, profileName) {
      var self = this;

      this.service.retrieve(this.$route.params.connector, configName, profileName, function(metricsData)
      {
        self.metricData = {
            agent: metricsData.agent,
            configFileName: metricsData.configFileName,
            configFilePath: metricsData.configFilePath,
            profileType: metricsData.profileType
        };

        var query = [];
        for(var param in self.$route.query) {
            query.push(param + '=' + self.$route.query[param]);
        }

        self.configLink = '/configuration/' + self.$route.params.connector + '?' + query.join('&');
        self.excludes = metricsData.excludes;

        self.tableInitial = JSON.parse(JSON.stringify(metricsData.views)),
        self.tableData = JSON.parse(JSON.stringify(metricsData.views));
      });

      if(this.$route.params.connector == 'aws')
      {
        this.service.getAmazonConfig(configName, function(config)
        {
          self.hasCustomViews = config.common.customView;
        });
      }
    },

    checkupdates: function() {
      var self = this;

      this.service.checkforupdates(this.$route.params.connector, this.$route.query.name, this.$route.query.profile, function(needsupdate)
      {
        if(needsupdate) {
          self.needsUpdate = true;
        }
        else {
          self.needsNoUpdate = true;
        }
      });
    },

    refreshcustom: function() {
      var self = this;

      this.service.refreshCustom(this.$route.params.connector, this.$route.query.name, this.$route.query.profile, function(metricsData)
      {
        self.metricData = {
            agent: metricsData.agent,
            configFileName: metricsData.configFileName,
            configFilePath: metricsData.configFilePath,
            profileType: metricsData.profileType
        };

        self.excludes = metricsData.excludes;

        self.tableInitial = JSON.parse(JSON.stringify(metricsData.views)),
        self.tableData = JSON.parse(JSON.stringify(metricsData.views));
      });
    },

    save: function() {
        var self = this;

        this.service.save(this.$route.params.connector, {
            agent: self.metricData.agent,
            configFileName: self.metricData.configFileName,
            configFilePath: self.metricData.configFilePath,
            profileType: self.metricData.profileType,
            state: {
              metricsRemoved: self.metricsRemoved
            },
            views: self.tableData,
            excludes: self.excludes
        },
        function() {
            self.metricsRemoved = [];

            self.tableInitial = JSON.parse(JSON.stringify(self.tableData));
        });
    },

    remove: function() {
        var instance = getMetricComponentInstance();

        if(!instance.removeOptions.length)
            return;

        var section = instance.tableData[instance.removeOptions[1]];

        for(var i = 0, iLimit = section.metrics.length; i < iLimit; i++) {
            var metric = section.metrics[i];

            if(metric.name == instance.removeOptions[0]) {
                instance.metricsRemoved.push({metric: (metric.customName || metric.name), serviceType: metric.serviceType});
                instance.removeOptions = [];

                section.metrics.splice(i, 1);
                return;
            }
        }
    },

    addNormalMetric: function(serviceType) {
        this.editOptions.editTitle = 'New Normal Metric';
        this.editOptions.editData = {
            monitored: true,
            graphed: true,
            computeType: '',
            name: '',
            format: '',
            customName: '',
            warningThreshold: '',
            criticalThreshold: '',
            description: '',
            serviceType: serviceType
        };
        this.editOptions.oldName = '';

        var self = this, section = this.tableData[serviceType];
        this.editOptions.editIndex = section.metrics.length;

        this.metricNames = [];

        this.service.getNames(this.$route.params.connector, serviceType, this.$route.query.name, function(names) {
            self.metricNames = names;
            self.editModalShown = true;
        });

        this.closeTypeahead();
    },

    addSyntheticMetric: function(serviceType) {
        this.editOptions.editTitle = 'New Synthetic Metric';
        this.editOptions.editData = {
            monitored: true,
            graphed: true,
            computeType: 'synthetic',
            name: '',
            format: '',
            expression: '',
            customName: '',
            warningThreshold: '',
            criticalThreshold: '',
            description: '',
            serviceType: serviceType
        };
        this.editOptions.oldName = '';

        var self = this, section = this.tableData[serviceType];
        this.editOptions.editIndex = section.metrics.length;
        this.inputMetrics = [];
        this.outputValue = '';

        this.service.getGWFunctionNames(this.$route.params.connector, function(functionNames) {
            self.functionNames = functionNames;
            self.editSyntheticModalShown = true;
        });

        this.closeTypeahead();
    },

    isUnique: function(metric) {
        var metricName = (metric.customName || metric.name);
        metricName = metricName.trim();

        var instance = getMetricComponentInstance();

        var section = instance.tableData[metric.serviceType];

        for(var i = 0, iLimit = section.metrics.length; i < iLimit; i++) {
            var metric_ = section.metrics[i];

            if(instance.editOptions.editIndex != i) {
                if(metricName == (metric_.customName || metric_.name)) {
                    return false;
                }
            }
        }

        return true;
    },

    containsDashes: function(name) {
        return (regexIndexOf(name, /\w\-\w/g, 0) > -1);
    },

    changeHealthType: function() {
        if(!metricComponentInstance.editModalShown || !metricComponentInstance || !metricComponentInstance.editOptions) {
            return;
        }

        metricComponentInstance.metricNames = [];

        if(metricComponentInstance.editOptions.editData.computeType == 'health') {
            metricComponentInstance.service.getHealthCheckNames(metricComponentInstance.$route.params.connector, metricComponentInstance.editOptions.editData.serviceType, metricComponentInstance.$route.query.name, function(names) {
                metricComponentInstance.metricNames = names;
            });
        }
        else {
            metricComponentInstance.service.getNames(metricComponentInstance.$route.params.connector, metricComponentInstance.editOptions.editData.serviceType, metricComponentInstance.$route.query.name, function(names) {
                metricComponentInstance.metricNames = names;
            });
        }
    },

    editYes: function() {
        this.editModalShown = false;
        this.editSyntheticModalShown = false;

        if(this.editOptions.editData.warningThreshold === '') {
            this.editOptions.editData.warningThreshold = -1;
        }
        else {
            this.editOptions.editData.warningThreshold = +this.editOptions.editData.warningThreshold;
        }

        if(this.editOptions.editData.criticalThreshold === '') {
            this.editOptions.editData.criticalThreshold = -1;
        }
        else {
            this.editOptions.editData.criticalThreshold = +this.editOptions.editData.criticalThreshold;
        }

        if(this.editOptions.editData.format === '') {
            delete this.editOptions.editData.format;
        }

        this.tableData[this.editOptions.editData.serviceType].metrics.splice(this.editOptions.editIndex, 1, this.editOptions.editData);

        if(this.editOptions.oldName && (this.editOptions.oldName !== this.editOptions.editData.name)) {
            this.metricsRemoved.push({metric: this.editOptions.oldName, serviceType: this.editOptions.editData.serviceType});
        }

        this.save();
    },

    editNo: function() {
        this.editModalShown = false;
        this.editSyntheticModalShown = false;
    },

    getInputMetrics: function() {
        var expression = this.editOptions.editData.expression || '';

        if(!expression) {
            this.inputMetrics = [];
            return;
        }

        var self = this;

        this.service.getInputMetrics(this.$route.params.connector, expression, this.$route.query.profile, this.valueType, this.editOptions.editData.serviceType, function(inputMetrics) {
            self.inputMetrics = inputMetrics;
        });
    },

    evaluate: function() {
        var data = {
            configName: this.$route.query.name,
            expression: this.editOptions.editData.expression,
            format: this.editOptions.editData.format,
            inputs: {}
        };

        for(var i = 0, iLimit = this.inputMetrics.length; i < iLimit; i++) {
            var inputMetric = this.inputMetrics[i];

            data.inputs[inputMetric.name] = inputMetric.overrideValue ? +inputMetric.overrideValue : inputMetric.value;
        }

        var self = this;

        this.service.evaluate(this.$route.params.connector, data, function(output) {
            self.outputValue = output;
        });
    },

    removeYes: function() {
        this.removeModalShown = false;
        this.remove();
    },

    removeNo: function() {
        this.removeModalShown = false;
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

        if(this.next && (typeof this.next === 'function')) {
            this.next(false);
        }
    },

    handleUpdateNo: function() {
        this.needsUpdate = false;
        this.needsNoUpdate = false;
    },

    handleUpdateYes: function() {
        this.needsUpdate = false;

        var self = this;

        this.service.update(this.$route.params.connector, this.$route.query.name, this.$route.query.profile, function()
        {
            self.retrieve(self.$route.query.name, self.$route.query.profile);
        });
    },

    closeTypeahead: function() {
        metricComponentInstance.modalFlag = true;

        var children = metricComponentInstance.$children;

        for(var i = 0, iLimit = children.length; i < iLimit; i++) {
            var child = children[i];

            if(typeof child.maskClicked != 'undefined') {
                if(child.$children && child.$children.length) {
                    child.$children[0].showDropdown = false;
                }
            }
        }
    },

    goHome: function(event) {
        var link = event.currentTarget,
            anyDirty = false;

        if(JSON.stringify(this.tableInitial) !== JSON.stringify(this.tableData)) {
            anyDirty = true;
        }

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
    var anyDirty = false;

    if(JSON.stringify(this.tableInitial) !== JSON.stringify(this.tableData)) {
        anyDirty = true;
    }

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

<style>
.VueTables table tbody tr td:nth-child(1), .VueTables table tbody tr td:nth-child(2) {
    text-align: center;
}

.VueTables table tbody tr.active td, .VueTables table tbody tr.active:hover td {
    background-color: #D3FFDC;
}

.VueTables table tbody tr.active.synthetic td, .VueTables table tbody tr.active.synthetic:hover td {
    background-color: #c6fbf4;
}

.VueTables table tbody tr.disabled td.warning-threshold, .VueTables table tbody tr.disabled td.critical-threshold {
    background-color: #DDDDDD;
}

.VueTables table tbody td.display-name input, .VueTables table tbody td.warning-threshold input, .VueTables table tbody td.critical-threshold input {
    padding: 5px 5px;
    width: 100%;
}

.VueTables table tbody tr.disabled td.warning-threshold input, .VueTables table tbody tr.disabled td.critical-threshold input {
    background-color: #E8E8E8;
    border: 1px solid #E0E0E0;
}

.VuePagination .VuePagination__pagination {
    margin: 0;
}

.VuePagination .VuePagination__count {
    float: right;
}

.metric-name, .display-name {
    font-weight: bold;
}

.VuePagination {
    margin-bottom: 10px;
}

.btn-remove {
  margin-bottom: 5%;
}

.btn-edit {
  margin-right: 5%;
  margin-bottom: 5%;
}

#metricNameHelpBlock {
    font-size: 0.9em;
    margin-bottom: 0px;
}

#metricNameHelpBlock.error {
    color: red;
}

.no-padding-left {
    padding-left: 0 !important;
}

.padding-left30 {
    padding-left: 30px;
}

.border-right {
    border-right: 1px solid lightgrey;
}

.radio > label > input {
    opacity: 1 !important;
}

.remove-dialog {
  overflow-y: hidden;
}

.metrics-btn {
    margin-left: 20px;
}
</style>
