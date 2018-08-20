/**
 * 
 */
package com.groundwork.report.birt.eventhandler;


import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.eventadapter.DataSetEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

/**
 * @author dfeinsmith
 *
 */
public class EPRPerformanceDataChooserDataSet extends DataSetEventAdapter
{
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
