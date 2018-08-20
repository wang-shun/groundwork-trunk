/**
 * 
 */
package com.groundwork.report.birt.eventhandler;

import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.api.script.element.IReportDesign;
import org.eclipse.birt.report.engine.api.script.eventadapter.ReportEventAdapter;
import org.eclipse.birt.chart.model.ChartWithAxes;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.eclipse.birt.chart.model.attribute.*;
import org.eclipse.birt.chart.model.attribute.impl.*;
import org.eclipse.birt.chart.model.component.*;
import org.eclipse.birt.chart.model.data.*;
import org.eclipse.birt.chart.model.data.impl.*;
import org.eclipse.birt.chart.model.type.*;
import org.eclipse.birt.chart.model.type.impl.*;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.extension.*;

//

/**
 * @author defeinsmith
 *
 */

public class EPRMultiVariableReportEventAdapter extends ReportEventAdapter
{
	MarkerType	marker_types[] =
	{
	 	MarkerType.BOX_LITERAL,
	 	MarkerType.CIRCLE_LITERAL,
	 	MarkerType.COLUMN_LITERAL,
	 	MarkerType.CROSS_LITERAL,
	 	MarkerType.CROSSHAIR_LITERAL,
	 	MarkerType.DIAMOND_LITERAL,
	 	MarkerType.ELLIPSE_LITERAL,
	 	MarkerType.FOUR_DIAMONDS_LITERAL,
	 	MarkerType.HEXAGON_LITERAL,
	 	MarkerType.NABLA_LITERAL,
	 	MarkerType.RECTANGLE_LITERAL,
	 	MarkerType.SEMI_CIRCLE_LITERAL,
	 	MarkerType.STAR_LITERAL,
	 	MarkerType.TRIANGLE_LITERAL
	};
	
	String marker_descriptions[] =
	{
			"Box Shaped",
			"Circular",
			"Columnar",
			"Cross Shaped",
			"Crosshair Shaped",
			"Diamond Shaped",
			"Elliptical",
			"Four Diamond Shaped",
			"Hexagonal",
			"Nabla Shaped",
			"Rectangular",
			"Semicircular",
			"Star Shaped",
			"Triangular"
	};
	
	LineStyle	line_styles[] =
	{
		LineStyle.DASH_DOTTED_LITERAL,
		LineStyle.DASHED_LITERAL,
		LineStyle.DOTTED_LITERAL,
		LineStyle.SOLID_LITERAL
	};
	
	static int	colors_array_left[][] =
	{
		{ 48, 128, 200 },		
		{ 0, 0, 205 },	
		{ 255, 0, 0 },	
		{ 65, 105, 225 },
		{ 255, 128, 200 },	
		{ 34, 139, 34 },	
		{ 148, 0, 211 }
	};
	
	static int	colors_array_right[][] =
	{
		{ 255, 128, 200 },	
		{ 48, 128, 200 },		
		{ 0, 0, 205 },	
		{ 255, 0, 0 },	
		{ 65, 105, 225 },		
		{ 34, 139, 34 },	
		{ 148, 0, 211 }
	};
	
	public void	beforeFactory(IReportDesign design, IReportContext reportContext)
	{
		try
		{
			Object[]			yleft_performanceIndicators,
								yright_performanceIndicators;
			DesignElementHandle	designHandle;
			ExtendedItemHandle 	chart_eih;
			ChartWithAxes		chart;
			Axis				xAxis, yAxisLeft, yAxisRight;
			String				legendText, unit_left, unit_right;
			Integer				marker_size_left, marker_type_left, line_width_left, line_style_left,
								marker_size_right, marker_type_right, line_width_right, line_style_right;
			
			//
			//	First, acquire our user-entered parameters.
			//	Get first our performance indicators specified.
			//
			
			yleft_performanceIndicators = (Object[]) reportContext.getParameterValue("PerformanceIndicator");
			yright_performanceIndicators = (Object[]) reportContext.getParameterValue("PerformanceIndicator_Right");
			
			//
			//	Next, Get our display parameters.
			//
			
			marker_type_left = (Integer) reportContext.getParameterValue("MarkerType_Left");
			marker_size_left = (Integer) reportContext.getParameterValue("MarkerSize_Left");
			line_width_left = (Integer) reportContext.getParameterValue("LineWidth_Left");
			line_style_left = (Integer) reportContext.getParameterValue("LineStyle_Left");
			marker_type_right = (Integer) reportContext.getParameterValue("MarkerType_Right");
			marker_size_right = (Integer) reportContext.getParameterValue("MarkerSize_Right");
			line_style_right = (Integer) reportContext.getParameterValue("LineStyle_Right");
			line_width_right = (Integer) reportContext.getParameterValue("LineWidth_Right");
			unit_left = (String) reportContext.getParameterValue("Unit");
			unit_right = (String) reportContext.getParameterValue("Unit_Right");
			
			//
			//	Now, acquire the chart from the loaded report design.
			//
			
			designHandle = reportContext.getReportRunnable().getDesignHandle();
			chart_eih = (ExtendedItemHandle) designHandle.getDesignHandle().findElement("HGMultiVariableDynamicChart");		
			chart = (ChartWithAxes) chart_eih.getReportItem().getProperty("chart.instance");
			
			legendText = "Left Y Axis Is " + marker_descriptions[marker_type_left.intValue()] + "\n";
			legendText += "Right Y Axis Is " + marker_descriptions[marker_type_right.intValue()] + "\n";
			chart.getLegend().getTitle().getCaption().setValue(legendText);
			
			//
			//	Next, get the X Axis and
			//	the Left and Right Y Axes.
			//
			
		 	xAxis		= (Axis) chart.getAxes().get(0);
		 	yAxisLeft	= (Axis) xAxis.getAssociatedAxes().get(0);
		 	yAxisRight	= (Axis) xAxis.getAssociatedAxes().get(1);
		 	
		 	//
		 	//	Remove previous series definitions first.
		 	//
		 	
		 	yAxisLeft.getSeriesDefinitions().clear();
		 	yAxisRight.getSeriesDefinitions().clear();
		 	
		 	//
		 	//	Now create the series for the left Y axis
		 	//	and for the right Y axis.
		 	//
		 	
		 	yAxisLeft.getTitle().getCaption().setValue("(Units: " + unit_left + ")");
		 	yAxisRight.getTitle().getCaption().setValue("(Units: " + unit_right + ")");
		 	
		 	createSeries(yleft_performanceIndicators, yAxisLeft, "yleft", marker_types[marker_type_left.intValue()], marker_size_left.intValue(), line_styles[line_style_left.intValue()], line_width_left.intValue(), colors_array_left);
		 	createSeries(yright_performanceIndicators, yAxisRight, "yright", marker_types[marker_type_right.intValue()], marker_size_right.intValue(), line_styles[line_style_right.intValue()], line_width_right.intValue(), colors_array_right);
		 	
		 	//
		 	//	And we're done!
		 	//
		}
		catch (ExtendedElementException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createSeries(Object[] performanceIndicators, Axis yAxis, String series_prefix, MarkerType marker_type, int marker_size, LineStyle line_style, int line_width, int colors_array[][])
	{
		SeriesDefinition	sdNew;
		LineSeries			ls;
		Palette				palette;
		Fill				fill;
		ColorDefinition		color;
		Marker				marker;
		LineAttributes		line_attributes;
		Query				qry;
		String				ycol_name;
		int					num_indicators, i, color_index;

		num_indicators = performanceIndicators.length;		
	 	for(i=0; i < num_indicators; i++)
	 	{
	 		ycol_name 		= series_prefix + String.valueOf(i);
	 		sdNew			= SeriesDefinitionImpl.create();
			ls				= (LineSeries) LineSeriesImpl.create();	
			
			//
			// 	Set view options for new line series.
			//	specifically, we set:
			//		1. marker shape
			//		2. marker & line color
			//		3. legend label.
			//
			
			color_index		= (i % colors_array.length);
			fill			= get_color_definition(colors_array, color_index);			
			color			= get_color_definition(colors_array, color_index);
			palette			= PaletteImpl.create(fill);
			marker			= MarkerImpl.create(marker_type, marker_size);
			line_attributes	= LineAttributesImpl.create(color, line_style, line_width);
			
			ls.setConnectMissingValue(true);
			ls.setMarker(marker);
			ls.getLabel().setVisible(false);			
			ls.setSeriesIdentifier((String)performanceIndicators[i]); // + " [" + ycol_name + "]");
			ls.setTranslucent(true);
			ls.setLineAttributes(line_attributes);
			ls.setPaletteLineColor(true);
			sdNew.setSeriesPalette(palette);
			
			//
			// Associate Query.
			//
			
			qry = QueryImpl.create("row[\"" + ycol_name +   "\"]" );
			ls.getDataDefinition().add(qry);
			
			//
			//	Finally, add the new line series to the series
			//	definition, and add the series definition to
			//	the left Y axis.
			//
			
			sdNew.getSeries().add(ls);
			yAxis.getSeriesDefinitions().add(sdNew);
			//debug_window("# of SERIES DEFINITIONS on Y AXIS!=", String.valueOf(yAxis.getSeriesDefinitions().size()));
			
			//
			//	And, we're done adding the series.
			// 
		}
	}
	
	public ColorDefinition get_color_definition(int colors_array[][], int color_index)
	{
		return(ColorDefinitionImpl.create(colors_array[color_index][0], colors_array[color_index][1], colors_array[color_index][2]));
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
