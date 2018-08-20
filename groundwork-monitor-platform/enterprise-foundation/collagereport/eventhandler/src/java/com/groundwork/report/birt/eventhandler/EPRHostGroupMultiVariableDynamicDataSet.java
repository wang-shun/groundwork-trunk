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

public class EPRHostGroupMultiVariableDynamicDataSet extends DataSetEventAdapter
{
	public void beforeOpen(IDataSetInstance dataSet, IReportContext reportContext)
	{
		Object[]	yleft_performanceIndicators,
					yright_performanceIndicators;
		String 		queryText;
		String		pdl_name, lpd_name, ycol_name, indicator_name;
		int 		num_yleft, num_yright, i;	
		
		try
		{
			yleft_performanceIndicators = (Object[]) reportContext.getParameterValue("PerformanceIndicator");
			yright_performanceIndicators = (Object[]) reportContext.getParameterValue("PerformanceIndicator_Right");
			num_yleft = yleft_performanceIndicators.length;
			num_yright = yright_performanceIndicators.length;
			
			//
			//	Initialize Query.
			//
			
			queryText = "SELECT ";
			
			//
			//	Loop through the left performance indicator
			//	parameters specified in the parameter entry UI.
			//
			
			for (i=0; i < num_yleft;)
			{				
				pdl_name = "yl" + String.valueOf(i) + "_pdl";
				lpd_name = "yl" + String.valueOf(i) + "_lpd";
				ycol_name = "yleft" + String.valueOf(i);
				indicator_name = (String)yleft_performanceIndicators[i];
				
				queryText += "(SELECT avg(" + lpd_name + ".Average) FROM PerformanceDataLabel " + pdl_name + " ";
				queryText += "INNER JOIN LogPerformanceData " + lpd_name + " ON " + lpd_name + ".PerformanceDataLabelID = " + pdl_name + ".PerformanceDataLabelID ";
				queryText += "WHERE " + pdl_name + ".PerformanceName='" + indicator_name + "' ";
				queryText += "AND " + lpd_name + ".LastCheckTime <= lpd.LastCheckTime AND to_days(LastCheckTime) > to_days(lpd.LastCheckTime)-2) AS " + ycol_name;
				if (++i < num_yleft)
					queryText += ",";
			}
			
			//
			//	Now, loop through the right performance indicator
			//	parameters specified in the parameter entry UI.
			//
			
			queryText += ",";
			for (i=0; i < num_yright;)
			{				
				pdl_name = "yr" + String.valueOf(i) + "_pdl";
				lpd_name = "yr" + String.valueOf(i) + "_lpd";
				ycol_name = "yright" + String.valueOf(i);
				indicator_name = (String)yright_performanceIndicators[i];
				
				queryText += "(SELECT avg(" + lpd_name + ".Average) FROM PerformanceDataLabel " + pdl_name + " ";
				queryText += "INNER JOIN LogPerformanceData " + lpd_name + " ON " + lpd_name + ".PerformanceDataLabelID = " + pdl_name + ".PerformanceDataLabelID ";
				queryText += "WHERE " + pdl_name + ".PerformanceName='" + indicator_name + "' ";
				queryText += "AND " + lpd_name + ".LastCheckTime <= lpd.LastCheckTime AND to_days(LastCheckTime) > to_days(lpd.LastCheckTime)-2) AS " + ycol_name;
				
				if (++i < num_yright)
					queryText += ",";
			}
			
			//
			//	Now, add other columns to the query.
			//
			
				queryText += ",";
				queryText += "lpd.LastCheckTime ";
			
			//
			//	Add the source statements.
			//
			
			queryText += " ";
			queryText += "FROM PerformanceDataLabel pdl ";
			queryText += "INNER JOIN LogPerformanceData lpd ON lpd.PerformanceDataLabelID = pdl.PerformanceDataLabelID ";
			queryText += "INNER JOIN ServiceStatus ss ON lpd.ServiceStatusID = ss.ServiceStatusID ";
			queryText += "INNER JOIN Host h ON ss.HostID = h.HostID ";
			queryText += "INNER JOIN HostGroupCollection hgc ON h.HostID = hgc.HostID ";
			queryText += "INNER JOIN HostGroup hg ON hgc.HostGroupID = hg.HostGroupID ";
			
			//
			//	Add WHERE clauses to match against parameters entered.
			//
			
			Date	dateStart, dateEnd;
			String 	dateStartComparitor, dateEndComparitor;
			
			dateStart			= (Date) reportContext.getParameterValue("DateStart");
			dateEnd				= (Date) reportContext.getParameterValue("DateEnd");
			dateStartComparitor	= (dateStart.getYear()+1900) + "-" + (dateStart.getMonth()+1) + "-" + dateStart.getDate();
			dateEndComparitor	= (dateEnd.getYear()+1900) + "-" + (dateEnd.getMonth()+1) + "-" + dateEnd.getDate();
			//debug_window("dates", "dateStart=" + dateStart + ",dateEnd=" + dateEnd + "[" + dateStartComparitor + "," + dateEndComparitor + "]");
			queryText += "WHERE (hg.Name = '" + (String) reportContext.getParameterValue("HostGroup") + "' OR hg.Name = '" + (String) reportContext.getParameterValue("HostGroup_Right") + "') ";
			queryText += "AND lpd.LastCheckTime >= '" + dateStartComparitor + "' ";
			queryText += "AND lpd.LastCheckTime <= '" + dateEndComparitor + "' ";

			//
			//	Add Grouping and Ordering.
			//

			//queryText += "GROUP BY dayofyear(lpd.LastCheckTime), pdl.PerformanceName, hg.HostGroupID ";
			queryText += "GROUP BY dayofyear(lpd.LastCheckTime), hg.HostGroupID ";
			queryText += "ORDER BY dayofyear(lpd.LastCheckTime) " + reportContext.getParameterValue("SortOrder") + " ";
			// YYZ: The ORDER BY ABOVE is incorrect and needs to be fixed.
			
			//
			//	Finalize the query.
			//
			
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


