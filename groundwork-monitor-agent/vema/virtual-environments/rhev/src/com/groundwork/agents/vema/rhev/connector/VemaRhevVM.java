package com.groundwork.agents.vema.rhev.connector;

import com.groundwork.agents.vema.base.VemaBaseMetric;
import com.groundwork.agents.vema.base.VemaBaseQuery;
import com.groundwork.agents.vema.base.VemaBaseSynthetic;
import com.groundwork.agents.vema.base.VemaBaseVM;

import org.apache.log4j.Logger;

public final class VemaRhevVM extends VemaBaseVM
{
	private static org.apache.log4j.Logger log = Logger.getLogger( VemaRhevVM.class );
	
    public VemaRhevVM( String vmName )
    {
        super( vmName );
    }

    private static final VemaBaseQuery[] baseMetricList =
    { 
//                  ---------------------------------------------------------------------------------
//                  - parameter                              warning  critical  graph  monitor      -
//                  ---------------------------------------------------------------------------------
//new VemaBaseQuery( "stat.cpu.current.guest.description",         0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.guest.id",                  0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.guest.name",                0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.guest.type",                0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.guest.unit",                0,        0, false, false ),
  new VemaBaseQuery( "stat.cpu.current.guest.value",               0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.hypervisor.description",    0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.hypervisor.id",             0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.hypervisor.name",           0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.hypervisor.type",           0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.hypervisor.unit",           0,        0, false, false ),
  new VemaBaseQuery( "stat.cpu.current.hypervisor.value",          0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.total.description",         0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.total.id",                  0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.total.name",                0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.total.type",                0,        0, false, false ),
//new VemaBaseQuery( "stat.cpu.current.total.unit",                0,        0, false, false ),
  new VemaBaseQuery( "stat.cpu.current.total.value",               0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.installed.description",          0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.installed.id",                   0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.installed.name",                 0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.installed.type",                 0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.installed.unit",                 0,        0, false, false ),
  new VemaBaseQuery( "stat.memory.installed.value",                0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.used.description",               0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.used.id",                        0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.used.name",                      0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.used.type",                      0,        0, false, false ),
//new VemaBaseQuery( "stat.memory.used.unit",                      0,        0, false, false ),
  new VemaBaseQuery( "stat.memory.used.value",                     0,        0, false, false ),
    };

    private static final VemaBaseQuery[] baseConfigList =
    { 
//                  ---------------------------------------------------------------------------------
//                  - parameter                              warning  critical  graph  monitor      -
//                  ---------------------------------------------------------------------------------
//new VemaBaseQuery( "cluster.id",                                 0,        0, false, false ),
  new VemaBaseQuery( "cluster.name",                               0,        0, false, false ),
  new VemaBaseQuery( "cpu.cores",                                  0,        0, false, false ),
  new VemaBaseQuery( "disk[0].actual_size",                        0,        0, false, false ),
//new VemaBaseQuery( "disk[0].id",                                 0,        0, false, false ),
  new VemaBaseQuery( "disk[0].name",                               0,        0, false, false ),
  new VemaBaseQuery( "disk[0].provisioned_size",                   0,        0, false, false ),
  new VemaBaseQuery( "disk[0].size",                               0,        0, false, false ),
  new VemaBaseQuery( "disk[0].status.state",                       0,        0, false, false ),
//new VemaBaseQuery( "disk[1].actual_size",                        0,        0, false, false ),
//new VemaBaseQuery( "disk[1].id",                                 0,        0, false, false ),
//new VemaBaseQuery( "disk[1].name",                               0,        0, false, false ),
//new VemaBaseQuery( "disk[1].provisioned_size",                   0,        0, false, false ),
//new VemaBaseQuery( "disk[1].size",                               0,        0, false, false ),
//new VemaBaseQuery( "disk[1].status.state",                       0,        0, false, false ),
//new VemaBaseQuery( "disk[2].actual_size",                        0,        0, false, false ),
//new VemaBaseQuery( "disk[2].id",                                 0,        0, false, false ),
//new VemaBaseQuery( "disk[2].name",                               0,        0, false, false ),
//new VemaBaseQuery( "disk[2].provisioned_size",                   0,        0, false, false ),
//new VemaBaseQuery( "disk[2].size",                               0,        0, false, false ),
//new VemaBaseQuery( "disk[2].status.state",                       0,        0, false, false ),
//new VemaBaseQuery( "display.address",                            0,        0, false, false ),
//new VemaBaseQuery( "display.monitors",                           0,        0, false, false ),
//new VemaBaseQuery( "display.port",                               0,        0, false, false ),
//new VemaBaseQuery( "display.secure_port",                        0,        0, false, false ),
//new VemaBaseQuery( "display.type",                               0,        0, false, false ),
//new VemaBaseQuery( "host.id",                                    0,        0, false, false ),
  new VemaBaseQuery( "host.name",                                  0,        0, false, false ),
//new VemaBaseQuery( "id",                                         0,        0, false, false ),
  new VemaBaseQuery( "memory",                                     0,        0, false, false ),
//new VemaBaseQuery( "memory_policy.guaranteed",                   0,        0, false, false ),
  new VemaBaseQuery( "name",                                       0,        0, false, false ),
//new VemaBaseQuery( "nic[0].active",                              0,        0, false, false ),
//new VemaBaseQuery( "nic[0].id",                                  0,        0, false, false ),
  new VemaBaseQuery( "nic[0].mac",                                 0,        0, false, false ),
  new VemaBaseQuery( "nic[0].name",                                0,        0, false, false ),
//new VemaBaseQuery( "nic[0].network.id",                          0,        0, false, false ),
//new VemaBaseQuery( "nic[0].vm.id",                               0,        0, false, false ),
//new VemaBaseQuery( "origin",                                     0,        0, false, false ),
  new VemaBaseQuery( "os.type",                                    0,        0, false, false ),
  new VemaBaseQuery( "start_time",                                 0,        0, false, false ),
  new VemaBaseQuery( "status.detail",                              0,        0, false, false ),
  new VemaBaseQuery( "status.state",                               0,        0, false, false ),
//new VemaBaseQuery( "template.id",                                0,        0, false, false ),
//new VemaBaseQuery( "template.name",                              0,        0, false, false ),
//new VemaBaseQuery( "type",                                       0,        0, false, false ),
//new VemaBaseQuery( "vmpool.id",                                  0,        0, false, false ),
  new VemaBaseQuery( "vmpool.name",                                0,        0, false, false ),
    };

    private static final VemaBaseQuery[] baseSyntheticList = 
    {
//                  ---------------------------------------------------------------------------------
//                  - parameter                              warning  critical  graph  monitor      -
//                  ---------------------------------------------------------------------------------
  new VemaBaseQuery( "syn.vm.cpu.used",                            75,       90, false, false ),
//new VemaBaseQuery( "syn.vm.cpu.unused",                          25,       10, false, false ),
  new VemaBaseQuery( "syn.vm.mem.used",                            80,       95, false, false ),
//new VemaBaseQuery( "syn.vm.mem.unused",                          20,       5   false, false ),
    };

    private static VemaBaseSynthetic[] baseSyntheticMaster = 
    {
        new VemaBaseSynthetic(  "syn.vm.cpu.used",
                                "stat.cpu.current.guest.value", 1.0, 
                                "stat.cpu.current.total.value", false, true ),


        new VemaBaseSynthetic(  "syn.vm.cpu.unused",
                                "stat.cpu.current.guest.value", 1.0, 
                                "stat.cpu.current.total.value", true, true ),

        new VemaBaseSynthetic(  "syn.vm.mem.used",
                                "stat.memory.used.value",      1.0,
                                "stat.memory.installed.value", false, true ),

        new VemaBaseSynthetic(  "syn.vm.mem.unused",
                                "stat.memory.used.value",      1.0,
                                "stat.memory.installed.value", true,  true ),
  
        new VemaBaseSynthetic(  "syn.vm.disk[0].actual",
                                "disk[0].actual_size",           1.0,
                                "disk[0].size",                  false,  true ),
  
        new VemaBaseSynthetic(  "syn.vm.disk[1].actual",
                                "disk[1].actual_size",           1.0,
                                "disk[1].size",                  false,  true ),
  
        new VemaBaseSynthetic(  "syn.vm.disk[2].actual",
                                "disk[2].actual_size",           1.0,
                                "disk[2].size",                  false,  true ),
  
    };
  

    public VemaBaseQuery[] getDefaultSyntheticList() { return baseSyntheticList;   }
    public VemaBaseQuery[] getDefaultMetricList()    { return baseMetricList;      }
    public VemaBaseQuery[] getDefaultConfigList()    { return baseConfigList;      }
    public VemaBaseSynthetic[] getSyntheticMaster()  { return baseSyntheticMaster; }

    public VemaBaseSynthetic   getSynthetic( String handle )
    {
        // I know... sequential searching sucks.  But it should always be short list.
        // ......... in future, make it a hash, I believe.
        //
        for( VemaBaseSynthetic v : baseSyntheticMaster )
            if( v.getHandle().equals( handle ) )
                return v;

        return null;
    }
    
    /**
     * getMonitorState()
     * 
     * turns a number of retrieved VIM25 properties into a 'runtime state' 
     * of the virtual machine.  Kind of defensive code, that could be made 
     * more trim, and less suspicious.
     * 
     * MODES:
     * powerState   guestState   connectionState     result    extra
     * ----------------------------------------------------------------------
     *     null           x                  x       UNKNOWN   no powerstate
     *        x        null                  x       UNKNOWN   no gueststate
     *        x           x               null       UNKNOWN   no connectionstate
     * ----------------------------------------------------------------------
     *  powered_on   running          connected      UP
     *  powered_on   suspended        connected      SUSPENDED
     *  powered_on        ?           connected      UP        {guestState}
     *  powered_on        ?           suspended      SUSPENDED
     *  powered_on   suspended              ?        SUSPENDED
     *  powered_on   notrunning             ?        UNSCHED DOWN
     *  powered_on        ?                 ?        UNSCHED DOWN      {guestState}
     * ----------------------------------------------------------------------
     *  suspended         ?                 ?        SUSPENDED
     *  powered_off       ?                 ?        SCHEDULED_DOWN
     *        ?           ?                 ?        UNKNOWN   {powerState}
     * ----------------------------------------------------------------------
     * @return
     */
    private static final String sUnknown     = "UNKNOWN";
    private static final String sUp          = "UP";
    private static final String sPending     = "PENDING";
    private static final String sSuspended   = "SUSPENDED";
    private static final String sUnschedDown = "UNSCHEDULED DOWN";
    private static final String sSchedDown   = "SCHEDULED DOWN";

    public String getMonitorState()
    {
    	String           statusState = null;
    	String          statusDetail = null;
    	VemaBaseMetric        metric = null;
    	String                     r = null; // receives       state information
    	String                     x = null; // receives extra state information

        if((metric = getConfig( "status.state" )) != null     // take no chances!
        || (metric = getMetric( "status.state" )) != null )   // could be in either pot
    		statusState = metric.getCurrValue();
    	
    	if((metric = getConfig( "status.detail" )) != null    // same here
        || (metric = getMetric( "status.detail" )) != null )
    		statusDetail = metric.getCurrValue();
        else
            statusDetail = "";
    	
    	if(      statusState     == null )                            { r = sUnknown;     x = "no state"; }
    	else if( statusDetail    == null )                            { r = sUnknown;     x = "no detail"; }
        else if( statusState.equalsIgnoreCase( "unassigned"        )) { r = sSchedDown;   x = statusDetail;  }
        else if( statusState.equalsIgnoreCase( "down"              )) { r = sSchedDown;   x = statusDetail;  }
        else if( statusState.equalsIgnoreCase( "up"                )) { r = sUp;          x = "";  }
        else if( statusState.equalsIgnoreCase( "powering_up"       )) { r = sPending;     x = "Powering up";  }
        else if( statusState.equalsIgnoreCase( "powered_down"      )) { r = sSchedDown;   x = "Powered down";  }
        else if( statusState.equalsIgnoreCase( "paused"            )) { r = sSuspended;   x = "Paused";  }
        else if( statusState.equalsIgnoreCase( "migrating"         )) { r = sSuspended;   x = "Migrating";  }
        else if( statusState.equalsIgnoreCase( "unknown"           )) { r = sUnknown;     x = statusDetail;  }
        else if( statusState.equalsIgnoreCase( "not_responding"    )) { r = sUnschedDown; x = "Not responding";  }
        else if( statusState.equalsIgnoreCase( "wait_for_launch"   )) { r = sPending;     x = "Waiting for launch";  }
        else if( statusState.equalsIgnoreCase( "reboot_in_progress")) { r = sPending;     x = "Rebooting";  }
        else if( statusState.equalsIgnoreCase( "saving_state"      )) { r = sPending;     x = "Saving state";  }
        else if( statusState.equalsIgnoreCase( "restoring_state"   )) { r = sPending;     x = "Restoring state";  }
        else if( statusState.equalsIgnoreCase( "suspended"         )) { r = sSuspended;   x = statusDetail;  }
        else if( statusState.equalsIgnoreCase( "image_locked"      )) { r = sPending;     x = "Image locked";  }
        else if( statusState.equalsIgnoreCase( "powering_down"     )) { r = sPending;     x = "Powering down";  }
        else	                                                      { r = sUnknown;     x = statusDetail;  }

    	this.setRunExtra( x == null ? "" : x );
    	return r;
    }
}
