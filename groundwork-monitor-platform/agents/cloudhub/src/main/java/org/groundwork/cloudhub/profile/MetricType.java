package org.groundwork.cloudhub.profile;

public enum  MetricType {
    hypervisor,
    vm,
    netController,
    netSwitch,
    monitor,
    testEngine,
    test,
    custom;

    @Override
    public String toString() {
        return this.name();
    }

}
