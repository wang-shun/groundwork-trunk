package com.groundwork.agents.vema.utils;

import java.util.Map;

import org.apache.log4j.Logger;

import com.groundwork.agents.vema.base.VemaBaseHost;
import com.groundwork.agents.vema.base.VemaBaseVM;
import com.groundwork.agents.vema.api.Vema;
import com.groundwork.agents.vema.base.VemaBaseQuery;
import com.groundwork.agents.vema.base.VemaBaseMetric;
import org.apache.log4j.Logger;

public class VemaInformation
{
	private static Logger log = Logger.getLogger(VemaInformation.class);

	public VemaInformation() {}
	
	public static String listHostInformationToString(Vema vema, Map <String, VemaBaseHost> hostlist)
	{
		return listHostInformationToString( vema, hostlist, true, true, true, true, true, true, true );
	}
			
    public static String listHostInformationToString(
    		Vema vema,
			Map <String, VemaBaseHost> hostlist, 
			boolean pDecorations, // for object 'global' variables 
			boolean pConfigs,     // for config type data
			boolean pMetrics,     // for metric type data
			boolean pDebug,       // for debugging the metrics...
			boolean pTimes,       // for timestamps too...
			boolean pInternals,   // for vema-object internal stuff.
			boolean pStates       // for displaying state-change subsystem
			)
    {
    	StringBuffer output = new StringBuffer( 50000 );  // large initial size
    	aPad a = new aPad();
    	String temp = null;
    	
    	output.append( "\n" );
    	
    	if( pInternals )
    		output.append((( temp = vema.getInternals()) == null) ? "" : temp );

        for( String hostkey : hostlist.keySet() )
        {
        	VemaBaseHost host = hostlist.get(hostkey);
            
            output.append(a.Pad("]", 
            		"Host handle[ " + hostkey + " ] ... " 
            		+ ( pStates 
            			? host.getRunState() + " (" + host.getRunExtra() + ")" + " isChanged=" + host.isStateChange()
            			: "" ) 
            		+ "\n"));

            if( pDecorations )
            {
	        	output.append(a.Pad("]", "Host ID    [ " + host.getHostName()      + " ]\n"));
	            output.append(a.Pad("]", "Host Grp   [ " + host.getHostGroup()     + " ]\n"));
	            output.append(a.Pad("]", "Host IP    [ " + host.getIpAddress()     + " ]\n"));
	            output.append(a.Pad("]", "Host Mac   [ " + host.getMacAddress()    + " ]\n"));
	            output.append(a.Pad("]", "Host Descr [ " + host.getDescription()   + " ]\n"));
	            output.append(a.Pad("]", "Host Bootd [ " + host.getBootDate()      + " ]\n"));
	            output.append(a.Pad("]", "Host LastD [ " + host.getLastUpdate()    + " ]\n"));
	            output.append(a.Pad("]", "Host State [ " + host.getRunState()      + " ]\n"));
	            output.append(a.Pad("]", "Host State+[ " + host.getRunExtra()      + " ]\n"));
	            output.append(a.Pad("]", "Prev State [ " + host.getPrevRunState()  + " ]\n"));
            }
            if( pConfigs )
	            for( String key : host.getConfigPool().keySet() )
	            {
                    VemaBaseMetric vbm = host.getConfig( key );

	            	output.append(a.Pad("]", "Host Config=[ " + key + " ] = '" + vbm.getCurrValue()         + "'\n" ));
	            	output.append(a.Pad("]", "Host isChgd?[ " + key + " ] = '" + vbm.isStateChange()        + "'\n" ));
	            	output.append(a.Pad("]", "Host State  [ " + key + " ] = '" + vbm.getCurrState()         + "'\n" ));
	            	output.append(a.Pad("]", "Last State  [ " + key + " ] = '" + vbm.getLastState()         + "'\n" ));
	            	if( !pDebug )
	            		continue;
	            	output.append(a.Pad("]", "Date Created[ " + key + " ] = '" + vbm.getDateCreated()       + "'\n" ));
	            	output.append(a.Pad("]", "Date Value  [ " + key + " ] = '" + vbm.getDateValue()         + "'\n" ));
	            	output.append(a.Pad("]", "Last State  [ " + key + " ] = '" + vbm.getLastState()         + "'\n" ));
	            	output.append(a.Pad("]", "Last Value  [ " + key + " ] = '" + vbm.getLastValue()         + "'\n" ));
	            	output.append(a.Pad("]", "Host Thresh.[ " + key + " ] = '" + vbm.getThresholdCritical() + "'\n" ));
	            	output.append(a.Pad("]", "Host Warning[ " + key + " ] = '" + vbm.getThresholdWarning()  + "'\n" ));
	            	output.append(a.Pad("]", "Host isWarn [ " + key + " ] = '" + vbm.isWarning()            + "'\n" ));
	            	output.append(a.Pad("]", "Host isCrit [ " + key + " ] = '" + vbm.isCritical()           + "'\n" ));
	            	output.append(a.Pad("]", "Host isDefnk[ " + key + " ] = '" + vbm.isDefunct()            + "'\n" ));
	            	output.append(a.Pad("]", "Monitored   [ " + key + " ] = '" + (vbm.isMonitored() ? "true" : "false") + "'\n"));
	            	output.append(a.Pad("]", "Graphed     [ " + key + " ] = '" + (vbm.isGraphed()   ? "true" : "false") + "'\n"));
	            	output.append(a.Pad("]", "Traced      [ " + key + " ] = '" + (vbm.isTraced()    ? "true" : "false") + "'\n"));
	            }

            if( pMetrics )
            	for( String key : host.getMetricPool().keySet() )
            	{
                    VemaBaseMetric vbm = host.getMetric( key );

            		output.append(a.Pad("]", "Host Metric=[ " + key + " ] = '" + vbm.getCurrValue()          + "'\n" ));
	            	output.append(a.Pad("]", "Mtrk isChgd?[ " + key + " ] = '" + vbm.isStateChange()         + "'\n" ));
	            	output.append(a.Pad("]", "Host State  [ " + key + " ] = '" + vbm.getCurrState()          + "'\n" ));
	            	output.append(a.Pad("]", "Last State  [ " + key + " ] = '" + vbm.getLastState()          + "'\n" ));
	            	if( !pDebug )
	            		continue;
	            	output.append(a.Pad("]", "Date Created[ " + key + " ] = '" + vbm.getDateCreated()        + "'\n" ));
	            	output.append(a.Pad("]", "Date Value  [ " + key + " ] = '" + vbm.getDateValue()          + "'\n" ));
	            	output.append(a.Pad("]", "Last State  [ " + key + " ] = '" + vbm.getLastState()          + "'\n" ));
	            	output.append(a.Pad("]", "Last Value  [ " + key + " ] = '" + vbm.getLastValue()          + "'\n" ));
	            	output.append(a.Pad("]", "Mtrk Thresh.[ " + key + " ] = '" + vbm.getThresholdCritical()  + "'\n" ));
	            	output.append(a.Pad("]", "Mtrk Warning[ " + key + " ] = '" + vbm.getThresholdWarning()   + "'\n" ));
	            	output.append(a.Pad("]", "Mtrk isWarn [ " + key + " ] = '" + vbm.isWarning()             + "'\n" ));
	            	output.append(a.Pad("]", "Mtrk isCrit [ " + key + " ] = '" + vbm.isCritical()            + "'\n" ));
	            	output.append(a.Pad("]", "Mtrk isDefnk[ " + key + " ] = '" + vbm.isDefunct()             + "'\n" ));
	            	output.append(a.Pad("]", "Monitored   [ " + key + " ] = '" + (vbm.isMonitored() ? "true" : "false") + "'\n"));
	            	output.append(a.Pad("]", "Graphed     [ " + key + " ] = '" + (vbm.isGraphed()   ? "true" : "false") + "'\n"));
	            	output.append(a.Pad("]", "Traced      [ " + key + " ] = '" + (vbm.isTraced()    ? "true" : "false") + "'\n"));
            	}

            for( String vmkey : host.getVMPool().keySet() )
            {
            	VemaBaseVM vm = host.getVM(vmkey);
            	
                output.append(a.Pad("]", 
                        "VM handle[ " + vmkey + " ] ... " 
                        + ( pStates 
                            ? vm.getRunState() + " (" + vm.getRunExtra() + ")" + " isChanged=" + vm.isStateChange()
                            : "" ) 
                        + "\n"));

                if( pDecorations )
                {
	            	output.append(a.Pad("]", "VM ID    [ " + vm.getVMName()      + " ]\n"));
	                output.append(a.Pad("]", "VM Hyper [ " + vm.getHypervisor()  + " ]\n"));
	                output.append(a.Pad("]", "VM Guest [ " + vm.getGuestState()  + " ]\n"));
	                output.append(a.Pad("]", "VM IP    [ " + vm.getIpAddress()   + " ]\n"));
	                output.append(a.Pad("]", "VM Mac   [ " + vm.getMacAddress()  + " ]\n"));
	                output.append(a.Pad("]", "VM Bootd [ " + vm.getBootDate()    + " ]\n"));
	                output.append(a.Pad("]", "VM LastD [ " + vm.getLastUpdate()  + " ]\n"));
	                output.append(a.Pad("]", "VM State [ " + vm.getRunState()    + " ]\n"));
	                output.append(a.Pad("]", "VM State+[ " + vm.getRunExtra()    + " ]\n"));
                }

                if( pConfigs )
    	            for( String key : vm.getConfigPool().keySet() )
    	            {
                        VemaBaseMetric vbm = vm.getConfig( key );

    	            	output.append(a.Pad("]", "VM   Config=[ " + key + " ] = '" + vbm.getCurrValue()         + "'\n" ));
    	            	output.append(a.Pad("]", "VM   isChgd?[ " + key + " ] = '" + vbm.isStateChange()        + "'\n" ));
    	            	output.append(a.Pad("]", "VM   State  [ " + key + " ] = '" + vbm.getCurrState()         + "'\n" ));
    	            	output.append(a.Pad("]", "Last State  [ " + key + " ] = '" + vbm.getLastState()         + "'\n" ));
    	            	if( !pDebug )
    	            		continue;
    	            	output.append(a.Pad("]", "Date Created[ " + key + " ] = '" + vbm.getDateCreated()       + "'\n" ));
    	            	output.append(a.Pad("]", "Date Value  [ " + key + " ] = '" + vbm.getDateValue()         + "'\n" ));
    	            	output.append(a.Pad("]", "Last State  [ " + key + " ] = '" + vbm.getLastState()         + "'\n" ));
    	            	output.append(a.Pad("]", "Last Value  [ " + key + " ] = '" + vbm.getLastValue()         + "'\n" ));
    	            	output.append(a.Pad("]", "VM   Thresh.[ " + key + " ] = '" + vbm.getThresholdCritical() + "'\n" ));
    	            	output.append(a.Pad("]", "VM   Warning[ " + key + " ] = '" + vbm.getThresholdWarning()  + "'\n" ));
    	            	output.append(a.Pad("]", "VM   isWarn [ " + key + " ] = '" + vbm.isWarning()            + "'\n" ));
    	            	output.append(a.Pad("]", "VM   isCrit [ " + key + " ] = '" + vbm.isCritical()           + "'\n" ));
    	            	output.append(a.Pad("]", "VM   isDefnk[ " + key + " ] = '" + vbm.isDefunct()            + "'\n" ));
                        output.append(a.Pad("]", "Monitored   [ " + key + " ] = '" + (vbm.isMonitored() ? "true" : "false") + "'\n"));
                        output.append(a.Pad("]", "Graphed     [ " + key + " ] = '" + (vbm.isGraphed()   ? "true" : "false") + "'\n"));
                        output.append(a.Pad("]", "Traced      [ " + key + " ] = '" + (vbm.isTraced()    ? "true" : "false") + "'\n"));
    	            }

                if( pMetrics )
                	for( String key : vm.getMetricPool().keySet() )
                	{
                        VemaBaseMetric vbm = vm.getMetric( key );

                		output.append(a.Pad("]", "VM   Metric=[ " + key + " ] = '" + vbm.getCurrValue()         + "'\n" ));
    	            	output.append(a.Pad("]", "Mtrk isChgd?[ " + key + " ] = '" + vbm.isStateChange()        + "'\n" ));
    	            	output.append(a.Pad("]", "VM   State  [ " + key + " ] = '" + vbm.getCurrState()         + "'\n" ));
    	            	output.append(a.Pad("]", "Last State  [ " + key + " ] = '" + vbm.getLastState()         + "'\n" ));
    	            	if( !pDebug )
    	            		continue;
    	            	output.append(a.Pad("]", "Date Created[ " + key + " ] = '" + vbm.getDateCreated()       + "'\n" ));
    	            	output.append(a.Pad("]", "Date Value  [ " + key + " ] = '" + vbm.getDateValue()         + "'\n" ));
    	            	output.append(a.Pad("]", "Last State  [ " + key + " ] = '" + vbm.getLastState()         + "'\n" ));
    	            	output.append(a.Pad("]", "Last Value  [ " + key + " ] = '" + vbm.getLastValue()         + "'\n" ));
    	            	output.append(a.Pad("]", "Mtrk Thresh.[ " + key + " ] = '" + vbm.getThresholdCritical() + "'\n" ));
    	            	output.append(a.Pad("]", "Mtrk Warning[ " + key + " ] = '" + vbm.getThresholdWarning()  + "'\n" ));
    	            	output.append(a.Pad("]", "Mtrk isWarn [ " + key + " ] = '" + vbm.isWarning()            + "'\n" ));
    	            	output.append(a.Pad("]", "Mtrk isCrit [ " + key + " ] = '" + vbm.isCritical()           + "'\n" ));
    	            	output.append(a.Pad("]", "Mtrk isDefnk[ " + key + " ] = '" + vbm.isDefunct()            + "'\n" ));
                        output.append(a.Pad("]", "Monitored   [ " + key + " ] = '" + (vbm.isMonitored() ? "true" : "false") + "'\n"));
                        output.append(a.Pad("]", "Graphed     [ " + key + " ] = '" + (vbm.isGraphed()   ? "true" : "false") + "'\n"));
                        output.append(a.Pad("]", "Traced      [ " + key + " ] = '" + (vbm.isTraced()    ? "true" : "false") + "'\n"));
                	}
            }
        }
        return output.toString();
    }
}
