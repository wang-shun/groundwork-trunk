/**
 * 
 */
package com.groundwork.report.birt.eventhandler;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.eventadapter.DataSetEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

/**
 * @author defeinsmith
 *
 */

public class EPRHostGroupMultiVariableDynamicDataSetForTable extends DataSetEventAdapter
{
	public void beforeOpen(IDataSetInstance dataSet, IReportContext reportContext)
	{
		Object[]	yleft_performanceIndicators,
					yright_performanceIndicators;
		String 		queryText;
		String		hg_name_left, hg_name_right, sort_field, sort_order;
		int 		num_yleft, num_yright, i;	
		
		try
		{
			yleft_performanceIndicators = (Object[]) reportContext.getParameterValue("PerformanceIndicator");
			yright_performanceIndicators = (Object[]) reportContext.getParameterValue("PerformanceIndicator_Right");
			hg_name_left = (String) reportContext.getParameterValue("HostGroup");
			hg_name_right = (String) reportContext.getParameterValue("HostGroup_Right");
			sort_field = (String) reportContext.getParameterValue("SortField");
			sort_order = (String) reportContext.getParameterValue("SortOrder");
			num_yleft = yleft_performanceIndicators.length;
			num_yright = yright_performanceIndicators.length;
			
			//
			//	Query Prologue
			//
			
			queryText = "select CAST(lpd.LastCheckTime AS date) AS LastCheckTime, min(lpd.Minimum) as Minimum, avg(lpd.Average) as Average,";
			queryText += "max(lpd.Maximum) as Maximum,pdl.PerformanceName,h.HostName as HostName,hg.Name as HostGroup from PerformanceDataLabel pdl ";
			queryText += "inner join LogPerformanceData lpd ON lpd.PerformanceDataLabelID = pdl.PerformanceDataLabelID ";
			queryText += "inner join ServiceStatus ss ON lpd.ServiceStatusID = ss.ServiceStatusID ";
			queryText += "inner join Host h ON ss.HostID = h.HostID ";
			queryText += "inner join HostGroupCollection hgc on h.HostID = hgc.HostID ";
			queryText += "inner join HostGroup hg on hgc.HostGroupID = hg.HostGroupID ";
			queryText += "where (hg.Name = '" + hg_name_left + "' OR hg.Name = '" + hg_name_right + "') AND ";
			
			//
			//	Add data specific for parameter values.
			//
			
			for (i=0; i < num_yleft; i++)
				queryText += "pdl.PerformanceName = '" + (String)yleft_performanceIndicators[i] + "' OR ";
			
			for (i=0; i < num_yright;)
			{
				queryText += "pdl.PerformanceName = '" + (String)yright_performanceIndicators[i] + "' ";
				if (++i < num_yright)
					queryText += "OR ";
			}
			
			// And put the footer on the query.
			
			queryText += "group by dayofyear(lpd.LastCheckTime), pdl.PerformanceName, hg.HostGroupID ";
			queryText += "order by " + sort_field + " " + sort_order;
			
			// Finalize the Query.
			
			queryText += ";";
			
			//
			//	And finally, set the query text for this data set.
			//
			
			//debug_window("EPRHostGroupMultiVariableDynamicDataSet: beforeOpen", queryText);
			dataSet.setQueryText(queryText);
		}
		catch (ScriptException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void debug_window(String title, String text)
	{
		JFrame	frame = new JFrame(title);
		JTextArea textArea = new JTextArea();

		frame.setSize(300,300);
		textArea.setEditable(false);
        textArea.setRows(20);
        textArea.setColumns(50);
        textArea.setLineWrap(true);
        textArea.setFont(new Font("Sans Serif", Font.PLAIN, 10));
        frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        
        textArea.append(new String(text));
	}
}

