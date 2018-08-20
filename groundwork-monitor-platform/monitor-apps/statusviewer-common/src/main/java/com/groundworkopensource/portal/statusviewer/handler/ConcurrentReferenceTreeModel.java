package com.groundworkopensource.portal.statusviewer.handler;

import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import java.util.concurrent.ConcurrentHashMap;

class ConcurrentReferenceTreeModel {

    private ConcurrentHashMap<Integer, NetworkMetaEntity> hostGroupMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, NetworkMetaEntity> hostMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, NetworkMetaEntity> serviceMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, NetworkMetaEntity> serviceGroupMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, NetworkMetaEntity> customGroupRootMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, NetworkMetaEntity> customGroupMap = new ConcurrentHashMap<>();

    public ConcurrentHashMap<Integer, NetworkMetaEntity> getHostGroupMap() {
        return hostGroupMap;
    }

    public ConcurrentHashMap<Integer, NetworkMetaEntity> getHostMap() {
        return hostMap;
    }

    public ConcurrentHashMap<Integer, NetworkMetaEntity> getServiceMap() {
        return serviceMap;
    }

    public ConcurrentHashMap<Integer, NetworkMetaEntity> getServiceGroupMap() {
        return serviceGroupMap;
    }

    public ConcurrentHashMap<Integer, NetworkMetaEntity> getCustomGroupRootMap() {
        return customGroupRootMap;
    }

    public ConcurrentHashMap<Integer, NetworkMetaEntity> getCustomGroupMap() {
        return customGroupMap;
    }

}
