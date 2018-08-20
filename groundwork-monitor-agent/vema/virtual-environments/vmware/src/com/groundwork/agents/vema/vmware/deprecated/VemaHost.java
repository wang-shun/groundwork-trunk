package com.groundwork.agents.vema.vmware.deprecated;

import java.util.ArrayList;

public final class VemaHost
{
    private static String            hostId;
    private static String            hostGroup;
    private static ArrayList<String> vmList;

    VemaHost( String host )
    {
    	hostId = host;
    	vmList = null;
    	hostGroup = null;
    }
    
    VemaHost( String host, ArrayList<String> vmlist )
    {
        hostId = host;
        vmList.addAll( vmlist );
        hostGroup = null;
    }

    VemaHost( String host, ArrayList<String> vmlist, String group )
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
