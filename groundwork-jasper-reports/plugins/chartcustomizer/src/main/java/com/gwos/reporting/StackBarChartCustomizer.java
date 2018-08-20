package com.gwos.reporting;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;

import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRChartCustomizer;

public class StackBarChartCustomizer implements JRChartCustomizer {
	
	@Override
	public void customize(JFreeChart jfchart, JRChart jasperChart) {
		CategoryPlot plot = jfchart.getCategoryPlot();		// Get CategoryPlot
		CategoryDataset dataSet = plot.getDataset();		// Get the CategoryDataset dataset

		List<Comparable<?>> keys = dataSet.getRowKeys();
		for(Comparable<?> key : keys) {
			Paint assignedPaint = getCustomPaint(key);
			if (assignedPaint != null) {
				int idx = dataSet.getRowIndex(key);
				plot.getRenderer().setSeriesPaint(idx, assignedPaint);
			}
		}
	}
	
	private Paint getCustomPaint(Comparable<?> rowKey) {
		String key = (String) rowKey;	
		
		Paint defaultColor = Color.LIGHT_GRAY;
		switch (key.toLowerCase()) {
		case "% total time up":						// host
		case "% total time ok":						// service
			defaultColor = new Color(0, 204, 0);
			break;
			
		case "% time down scheduled":				// host
		case "% known time critical scheduled":		// service
			defaultColor = Color.RED;
			break;
			
		case "% time down unscheduled":				// host
		case "% known time critical unscheduled":	// service	
			defaultColor = new Color(51, 153, 255);
			break;
			
		case "% time other":						// host and service
			defaultColor = Color.YELLOW;
			break;

		case "% known time warning scheduled":		// service
			defaultColor = new Color(247, 134, 23);
			break;
			
		case "% known time warning unscheduled":	// service	
			defaultColor = new Color(51, 153, 255);
			break;
			
		}
		return defaultColor;
	}

}
