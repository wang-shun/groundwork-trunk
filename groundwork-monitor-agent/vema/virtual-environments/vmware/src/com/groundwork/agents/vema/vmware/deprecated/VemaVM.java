package com.groundwork.agents.vema.vmware.deprecated;


public final class VemaVM
{
    private static String            vmId;
    private static String            hostId;
    private static String            vmGroup;

    VemaVM( String name, String host, String group)
    {
        hostId  = host;
        vmId    = name;
        vmGroup = group;
    }

    public  String getHostId()                { return hostId;  }
    public  String getVmId()                  { return vmId;    }
    public  String getVmGroup()               { return vmGroup; }

    public  void   setHostId( String host )   { hostId  = host; }
    public  void   setVmId(   String vm   )   { vmId    = vm;   }
    public  void   setVmGroup(String group)   { vmGroup = group;}
    
    private static final String[] statAllowed = { 
    	"summary.guest.hostName",
    	"summary.guest.ipAddress",
    	"summary.quickStats.balloonedMemory",
    	"summary.quickStats.compressedMemory",
    	"summary.quickStats.consumedMemory",
    	"summary.quickStats.guestMemoryUsage",
    	"summary.quickStats.hostMemoryUsage",
    	"summary.quickStats.overallCpuDemand",
    	"summary.quickStats.overallCpuUsage",
    	"summary.quickStats.privateMemory",
    	"summary.quickStats.sharedMemory",
    	"summary.quickStats.ssdSwappedMemory",
    	"summary.quickStats.swappedMemory",
    	"summary.quickStats.uptimeSeconds",
    	"summary.config.memorySizeMB",
    	"summary.config.name",
    	"summary.config.numCpu",
    	"summary.config.numEthernetCards",
    	"summary.config.numVirtualDisks",
    	"summary.runtime.bootTime",
    	"summary.runtime.connectionState",
    	"summary.runtime.host",
    	"summary.runtime.memoryOverhead",
    	"summary.runtime.powerState",
    	"summary.storage.committed",
    	"summary.storage.uncomitted",
    	"guest.ipAddress",
    	"guest.net[0].macAddress",
    	"guest.guestState",
    	"guest.hostName",
    	"name",
    };
    
    public String[] getStatsAllowed()
    {
    	return statAllowed;
    }
    
    public boolean statIsOK( String stat )  // hate seq. search, but list is small
    {
    	int i;
    	for(i = 0; i < statAllowed.length; i++)
    		if( stat == statAllowed[i] )
    			return true;

    	return false;
    }
    
    public  String getStat( String stat )
    	throws Exception
    {
    	String[] statlist = stat.split("[.]");

    	if( !statIsOK( stat ) )
    		throw new IllegalArgumentException( "(" + stat + ") not on approved list" );

    	if(statlist[0] == "name")
    	{
    		return getStat( "summary.guest.hostName" );
    	}
    	else if(statlist[0] == "guest")
    	{
			return getStat( "summary." + stat );  // recursion instead of goto.
    	}
    	else if(statlist[0] == "summary")
    	{
    		if( statlist[1] == "guest" )
    		{
    			// VirtualMachineGuestSummary [.statlist[2]]
    		}
    		else if( statlist[1] == "quickStats" )
    		{
    			// VirtualMachineQuickStats [.statlist[2]]
    		}
    		else if( statlist[1] == "config" )
    		{
    			// VirtualMachineConfigSummary [.statlist[2]]
    		}
    		else if( statlist[1] == "runtime" )
    		{
    			// VirtualMachineRuntimeInfo [.statlist[2]]
    		}
    		else if( statlist[1] == "storage" )
    		{
    			// VirtualMachineStorageSummary [.statlist[2]]
    		}
    		else
    		{
    			throw new Exception( "program error: no handler for '" + statlist[1] + "'" );
    		}
    	}
    	else
    		throw new Exception( "Programming error - no code for '" + statlist[0] + "'");
    	
    	return "I'm a teapot";
    }
}