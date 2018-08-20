package org.groundwork.cloudhub.connectors.rhev;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseSynthetic;
import org.groundwork.cloudhub.metrics.BaseVM;

public final class RhevVM extends BaseVM
{
	private static Logger log = Logger.getLogger( RhevVM.class );
	
    public RhevVM(String vmName)
    {
        super( vmName );
    }

    private static final BaseQuery[] baseMetricList =
    { 
//                  ---------------------------------------------------------------------------------
//                  - parameter                              warning  critical  graph  monitor      -
//                  ---------------------------------------------------------------------------------
//new BaseQuery( "stat.cpu.current.guest.description",         0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.guest.id",                  0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.guest.name",                0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.guest.type",                0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.guest.unit",                0,        0, false, false ),
  new BaseQuery( "stat.cpu.current.guest.value",               0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.hypervisor.description",    0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.hypervisor.id",             0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.hypervisor.name",           0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.hypervisor.type",           0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.hypervisor.unit",           0,        0, false, false ),
  new BaseQuery( "stat.cpu.current.hypervisor.value",          0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.total.description",         0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.total.id",                  0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.total.name",                0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.total.type",                0,        0, false, false ),
//new BaseQuery( "stat.cpu.current.total.unit",                0,        0, false, false ),
  new BaseQuery( "stat.cpu.current.total.value",               0,        0, false, false ),
//new BaseQuery( "stat.memory.installed.description",          0,        0, false, false ),
//new BaseQuery( "stat.memory.installed.id",                   0,        0, false, false ),
//new BaseQuery( "stat.memory.installed.name",                 0,        0, false, false ),
//new BaseQuery( "stat.memory.installed.type",                 0,        0, false, false ),
//new BaseQuery( "stat.memory.installed.unit",                 0,        0, false, false ),
  new BaseQuery( "stat.memory.installed.value",                0,        0, false, false ),
//new BaseQuery( "stat.memory.used.description",               0,        0, false, false ),
//new BaseQuery( "stat.memory.used.id",                        0,        0, false, false ),
//new BaseQuery( "stat.memory.used.name",                      0,        0, false, false ),
//new BaseQuery( "stat.memory.used.type",                      0,        0, false, false ),
//new BaseQuery( "stat.memory.used.unit",                      0,        0, false, false ),
  new BaseQuery( "stat.memory.used.value",                     0,        0, false, false ),
    };

    private static final BaseQuery[] baseConfigList =
    { 
//                  ---------------------------------------------------------------------------------
//                  - parameter                              warning  critical  graph  monitor      -
//                  ---------------------------------------------------------------------------------
//new BaseQuery( "cluster.id",                                 0,        0, false, false ),
  new BaseQuery( "cluster.name",                               0,        0, false, false ),
  new BaseQuery( "cpu.cores",                                  0,        0, false, false ),
  new BaseQuery( "disk[0].actual_size",                        0,        0, false, false ),
//new BaseQuery( "disk[0].id",                                 0,        0, false, false ),
  new BaseQuery( "disk[0].name",                               0,        0, false, false ),
  new BaseQuery( "disk[0].provisioned_size",                   0,        0, false, false ),
  new BaseQuery( "disk[0].size",                               0,        0, false, false ),
  new BaseQuery( "disk[0].status.state",                       0,        0, false, false ),
//new BaseQuery( "disk[1].actual_size",                        0,        0, false, false ),
//new BaseQuery( "disk[1].id",                                 0,        0, false, false ),
//new BaseQuery( "disk[1].name",                               0,        0, false, false ),
//new BaseQuery( "disk[1].provisioned_size",                   0,        0, false, false ),
//new BaseQuery( "disk[1].size",                               0,        0, false, false ),
//new BaseQuery( "disk[1].status.state",                       0,        0, false, false ),
//new BaseQuery( "disk[2].actual_size",                        0,        0, false, false ),
//new BaseQuery( "disk[2].id",                                 0,        0, false, false ),
//new BaseQuery( "disk[2].name",                               0,        0, false, false ),
//new BaseQuery( "disk[2].provisioned_size",                   0,        0, false, false ),
//new BaseQuery( "disk[2].size",                               0,        0, false, false ),
//new BaseQuery( "disk[2].status.state",                       0,        0, false, false ),
//new BaseQuery( "display.address",                            0,        0, false, false ),
//new BaseQuery( "display.monitors",                           0,        0, false, false ),
//new BaseQuery( "display.port",                               0,        0, false, false ),
//new BaseQuery( "display.secure_port",                        0,        0, false, false ),
//new BaseQuery( "display.type",                               0,        0, false, false ),
//new BaseQuery( "host.id",                                    0,        0, false, false ),
  new BaseQuery( "host.name",                                  0,        0, false, false ),
//new BaseQuery( "id",                                         0,        0, false, false ),
  new BaseQuery( "memory",                                     0,        0, false, false ),
//new BaseQuery( "memory_policy.guaranteed",                   0,        0, false, false ),
  new BaseQuery( "name",                                       0,        0, false, false ),
//new BaseQuery( "nic[0].active",                              0,        0, false, false ),
//new BaseQuery( "nic[0].id",                                  0,        0, false, false ),
  new BaseQuery( "nic[0].mac",                                 0,        0, false, false ),
  new BaseQuery( "nic[0].name",                                0,        0, false, false ),
//new BaseQuery( "nic[0].network.id",                          0,        0, false, false ),
//new BaseQuery( "nic[0].vm.id",                               0,        0, false, false ),
//new BaseQuery( "origin",                                     0,        0, false, false ),
  new BaseQuery( "os.type",                                    0,        0, false, false ),
  new BaseQuery( "start_time",                                 0,        0, false, false ),
  new BaseQuery( "status.detail",                              0,        0, false, false ),
  new BaseQuery( "status.state",                               0,        0, false, false ),
//new BaseQuery( "template.id",                                0,        0, false, false ),
//new BaseQuery( "template.name",                              0,        0, false, false ),
//new BaseQuery( "type",                                       0,        0, false, false ),
//new BaseQuery( "vmpool.id",                                  0,        0, false, false ),
  new BaseQuery( "vmpool.name",                                0,        0, false, false ),
    };

    private static final BaseQuery[] baseSyntheticList =
    {
//                  ---------------------------------------------------------------------------------
//                  - parameter                              warning  critical  graph  monitor      -
//                  ---------------------------------------------------------------------------------
  new BaseQuery( "syn.vm.cpu.used",                            75,       90, false, false ),
//new BaseQuery( "syn.vm.cpu.unused",                          25,       10, false, false ),
  new BaseQuery( "syn.vm.mem.used",                            80,       95, false, false ),
//new BaseQuery( "syn.vm.mem.unused",                          20,       5   false, false ),
    };

    private static BaseSynthetic[] baseSyntheticMaster =
    {
        new BaseSynthetic(  "syn.vm.cpu.used",
                                "stat.cpu.current.guest.value", 1.0, 
                                "stat.cpu.current.total.value", false, true ),


        new BaseSynthetic(  "syn.vm.cpu.unused",
                                "stat.cpu.current.guest.value", 1.0, 
                                "stat.cpu.current.total.value", true, true ),

        new BaseSynthetic(  "syn.vm.mem.used",
                                "stat.memory.used.value",      1.0,
                                "stat.memory.installed.value", false, true ),

        new BaseSynthetic(  "syn.vm.mem.unused",
                                "stat.memory.used.value",      1.0,
                                "stat.memory.installed.value", true,  true ),
  
        new BaseSynthetic(  "syn.vm.disk[0].actual",
                                "disk[0].actual_size",           1.0,
                                "disk[0].size",                  false,  true ),
  
        new BaseSynthetic(  "syn.vm.disk[1].actual",
                                "disk[1].actual_size",           1.0,
                                "disk[1].size",                  false,  true ),
  
        new BaseSynthetic(  "syn.vm.disk[2].actual",
                                "disk[2].actual_size",           1.0,
                                "disk[2].size",                  false,  true ),
  
    };
  

    public BaseQuery[] getDefaultSyntheticList() { return baseSyntheticList;   }
    public BaseQuery[] getDefaultMetricList()    { return baseMetricList;      }
    public BaseQuery[] getDefaultConfigList()    { return baseConfigList;      }
    public BaseSynthetic[] getSyntheticMaster()  { return baseSyntheticMaster; }

    public BaseSynthetic   getSynthetic( String handle )
    {
        // I know... sequential searching sucks.  But it should always be short list.
        // ......... in future, make it a hash, I believe.
        //
        for( BaseSynthetic v : baseSyntheticMaster )
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
     *     null           x                  x       UNREACHABLE   no powerstate
     *        x        null                  x       UNREACHABLE   no gueststate
     *        x           x               null       UNREACHABLE   no connectionstate
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
     *        ?           ?                 ?        UNREACHABLE   {powerState}
     * ----------------------------------------------------------------------
     * @return
     */
    public static final String sUnreachable = "UNREACHABLE";
    private static final String sUp          = "UP";
    private static final String sPending     = "PENDING";
    private static final String sSuspended   = "SUSPENDED";
    private static final String sUnschedDown = "UNSCHEDULED DOWN";
    private static final String sSchedDown   = "SCHEDULED DOWN";

    public String getMonitorState()
    {
    	String           statusState = null;
    	String          statusDetail = null;
    	BaseMetric metric = null;
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
    	
    	if(      statusState     == null )                            { r = sUnreachable;     x = "no state"; }
    	else if( statusDetail    == null )                            { r = sUnreachable;     x = "no detail"; }
        else if( statusState.equalsIgnoreCase( "unassigned"        )) { r = sSchedDown;   x = statusDetail;  }
        else if( statusState.equalsIgnoreCase( "down"              )) { r = sSchedDown;   x = statusDetail;  }
        else if( statusState.equalsIgnoreCase( "up"                )) { r = sUp;          x = "";  }
        else if( statusState.equalsIgnoreCase( "powering_up"       )) { r = sPending;     x = "Powering up";  }
        else if( statusState.equalsIgnoreCase( "powered_down"      )) { r = sSchedDown;   x = "Powered down";  }
        else if( statusState.equalsIgnoreCase( "paused"            )) { r = sSuspended;   x = "Paused";  }
        else if( statusState.equalsIgnoreCase( "migrating"         )) { r = sSuspended;   x = "Migrating";  }
        else if( statusState.equalsIgnoreCase( "unknown"           )) { r = sUnreachable; x = statusDetail;  }
        else if( statusState.equalsIgnoreCase( "not_responding"    )) { r = sUnschedDown; x = "Not responding";  }
        else if( statusState.equalsIgnoreCase( "wait_for_launch"   )) { r = sPending;     x = "Waiting for launch";  }
        else if( statusState.equalsIgnoreCase( "reboot_in_progress")) { r = sPending;     x = "Rebooting";  }
        else if( statusState.equalsIgnoreCase( "saving_state"      )) { r = sPending;     x = "Saving state";  }
        else if( statusState.equalsIgnoreCase( "restoring_state"   )) { r = sPending;     x = "Restoring state";  }
        else if( statusState.equalsIgnoreCase( "suspended"         )) { r = sSuspended;   x = statusDetail;  }
        else if( statusState.equalsIgnoreCase( "image_locked"      )) { r = sPending;     x = "Image locked";  }
        else if( statusState.equalsIgnoreCase( "powering_down"     )) { r = sPending;     x = "Powering down";  }
        else	                                                      { r = sUnreachable; x = statusDetail;  }

    	this.setRunExtra( x == null ? "" : x );
    	return r;
    }
}
