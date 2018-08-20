import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'my-table',
  templateUrl: './templates/table.html'
})

export class TableComponent implements OnInit {

  data: any[];
  cols: any[];

  constructor() { }

  ngOnInit() {
    this.cols = [
      {header: 'Monitor?', field: 'isMonitor', selected: false, selectable: true},
      {header: 'Graph?', field: 'isGraph', selected: false, selectable: true},
      {header: 'Metric Name', field: 'metricName', sort: 'asc'},
      {header: 'Display Name', field: 'displayName', sort: ''},
      {header: 'Warning Threshold', className: 'text-warning', field: 'warningThreshold'},
      {header: 'Critical Threshold', field: 'criticalThreshold'}
    ];

    this.data = [
    {
      'isMonitor': false,
      'isGraph': false,
      'metricName': 'Some Metric 1',
      'displayName': 'Some Cool Metric 1',
      'warningThreshold': 150.5,
      'criticalThreshold': 208.178
    },
    {
      'isMonitor': false,
      'isGraph': false,
      'metricName': 'Some Metric 2',
      'displayName': 'Some Cool Metric 2',
      'warningThreshold': 150.5,
      'criticalThreshold': 208.178
    },
    {
      'isMonitor': false,
      'isGraph': false,
      'metricName': 'Some Metric 3',
      'displayName': 'Some Cool Metric 3',
      'warningThreshold': 150.5,
      'criticalThreshold': 208.178
    },
    {
      'isMonitor': false,
      'isGraph': false,
      'metricName': 'Some Metric 4',
      'displayName': 'Some Cool Metric 4',
      'warningThreshold': 150.5,
      'criticalThreshold': 208.178
    },
    {
      'isMonitor': false,
      'isGraph': false,
      'metricName': 'Some Metric 5',
      'displayName': 'Some Cool Metric 5',
      'warningThreshold': 150.5,
      'criticalThreshold': 208.178
    },
    {
      'isMonitor': false,
      'isGraph': false,
      'metricName': 'Some Metric 6',
      'displayName': 'Some Cool Metric 6',
      'warningThreshold': 150.5,
      'criticalThreshold': 208.178
    },
    {
      'isMonitor': false,
      'isGraph': false,
      'metricName': 'Some Metric 7',
      'displayName': 'Some Cool Metric 7',
      'warningThreshold': 150.5,
      'criticalThreshold': 208.178
    },
    {
      'isMonitor': false,
      'isGraph': false,
      'metricName': 'Some Metric 8',
      'displayName': 'Some Cool Metric 8',
      'warningThreshold': 150.5,
      'criticalThreshold': 208.178
    },
    {
      'isMonitor': false,
      'isGraph': false,
      'metricName': 'Some Metric 9',
      'displayName': 'Some Cool Metric 9',
      'warningThreshold': 150.5,
      'criticalThreshold': 208.178
    },
    {
      'isMonitor': false,
      'isGraph': false,
      'metricName': 'Some Metric 10',
      'displayName': 'Some Cool Metric 10',
      'warningThreshold': 150.5,
      'criticalThreshold': 208.178
    },
    {
      'isMonitor': false,
      'isGraph': false,
      'metricName': 'Some Metric 11',
      'displayName': 'Some Cool Metric 11',
      'warningThreshold': 150.5,
      'criticalThreshold': 208.178
    },
    {
      'isMonitor': false,
      'isGraph': false,
      'metricName': 'Some Metric 12',
      'displayName': 'Some Cool Metric 12',
      'warningThreshold': 150.5,
      'criticalThreshold': 208.178
    },
    {
      'isMonitor': false,
      'isGraph': false,
      'metricName': 'Some Metric 13',
      'displayName': 'Some Cool Metric 13',
      'warningThreshold': 150.5,
      'criticalThreshold': 208.178
    },
    {
      'isMonitor': false,
      'isGraph': false,
      'metricName': 'Some Metric 14',
      'displayName': 'Some Cool Metric 14',
      'warningThreshold': 150.5,
      'criticalThreshold': 208.178
    },
    {
      'isMonitor': false,
      'isGraph': false,
      'metricName': 'Some Metric 15',
      'displayName': 'Some Cool Metric 15',
      'warningThreshold': 150.5,
      'criticalThreshold': 208.178
    }];
  }
}
