/**
 * To customize Jaspersoft Report PieChart so that 
 * legend colors are assigned consistently to GWos Host/Service status.
 */
package com.gwos.reporting;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.PieDataset;

import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRChartCustomizer;

public class PieChartCustomizer implements JRChartCustomizer {
	
	public void customize(JFreeChart jfchart, JRChart jasperChart) {

		PiePlot3D plot = (PiePlot3D) jfchart.getPlot();			// Get PiePlot3D
		PieDataset dataSet = plot.getDataset();					// Get the Pie chart dataset
		
		List<Comparable<?>> keys = dataSet.getKeys();
		for(Comparable<?> key : keys) {
			Paint assignedPaint = getCustomPaint(key);
			if (assignedPaint != null) {
				// set the new paint to the renderer
				plot.setSectionPaint(key, assignedPaint);
			}
		}
	}

	/* The following color codes were defined in inital reports, 
	 * they are mapped to different colors while review is needed.  
	 * 	<plot backgroundAlpha="0.5" foregroundAlpha="0.8">
			<seriesColor seriesOrder="0" color="#FF0000"/>	// -> new Color(0, 204, 0)
			<seriesColor seriesOrder="1" color="#00CC00"/>	// -> Color.RED
			<seriesColor seriesOrder="2" color="#3399FF"/>	// -> new Color(51, 153, 255)
			<seriesColor seriesOrder="3" color="#F78617"/>	// -> new Color(247, 134, 23)
			<seriesColor seriesOrder="4" color="#808000"/>	// -> new Color(128, 128, 0)
			<seriesColor seriesOrder="5" color="#FF0000"/>	// -> Color.RED
			<seriesColor seriesOrder="6" color="#FFFF33"/>	// -> new Color(255, 255, 51)
		</plot>
	 */
	private Paint getCustomPaint(Comparable<?> rowKey) {
		String key = (String) rowKey;	// name column value of monitorstatus table
		
		Paint defaultColor = Color.LIGHT_GRAY;
		switch (key) {
		case "OK":
			defaultColor = new Color(0, 204, 0);
			break;
			
		case "CRITICAL":
			defaultColor = Color.RED;
			break;
			
		case "PENDING":
			defaultColor = new Color(51, 153, 255);
			break;
			
		case "SCHEDULED DOWN":
			defaultColor = new Color(247, 134, 23);
			break;
			
		case "UNKNOWN":
			defaultColor = new Color(128, 128, 0);
			break;
			
		case "UNSCHEDULED DOWN":
			defaultColor = Color.RED;
			break;
			
		case "WARNING":
			defaultColor = new Color(255, 255, 51);
			break;
		}
		return defaultColor;
	}

}
