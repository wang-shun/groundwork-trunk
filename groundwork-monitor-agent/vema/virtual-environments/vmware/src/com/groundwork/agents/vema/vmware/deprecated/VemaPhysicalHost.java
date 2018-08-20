package com.groundwork.agents.vema.vmware.deprecated;

import java.util.ArrayList;

public final class VemaPhysicalHost
{
    private static String            hostId;
    private static String            hostGroup;
    private static ArrayList<String> vmList;

    VemaPhysicalHost( )
    {
        hostId = null;
        vmList = null;
        hostGroup = null;
    }

    VemaPhysicalHost( String host )
    {
    	hostId = host;
    	vmList = null;
    	hostGroup = null;
    }
    
    VemaPhysicalHost( String host, ArrayList<String> vmlist )
    {
        hostId = host;
        vmList.addAll( vmlist );
        hostGroup = null;
    }

    VemaPhysicalHost( String host, ArrayList<String> vmlist, String group )
    {
        hostId = host;
        vmList.addAll( vmlist );
        hostGroup = group;
    }

    public  String            getHostId() { return hostId; }
    public  String         getHostGroup() { return hostGroup; }
    public  ArrayList<String> getVmList() { return vmList; }

    public  void     addVm(   String vm ) { vmList.add( vm ); }
    public  void     clearVm( String vm ) { vmList.clear(); }
    public  String   getVm(   int index ) { return vmList.get(index); }
}
