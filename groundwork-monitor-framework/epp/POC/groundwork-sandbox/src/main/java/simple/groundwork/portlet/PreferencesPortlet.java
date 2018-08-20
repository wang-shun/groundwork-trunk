package simple.groundwork.portlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * This portlet is used to show how portlet placement can dictate how portlet preferences react
 * 
 * @author tbeauvais
 *
 */
public class PreferencesPortlet extends GenericPortlet {

	private static final String NEW_VALUE = "new.value";
	private static final String TEST_VALUE = "test.value";

	@Override
	protected void doView(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {

		final PrintWriter writer = response.getWriter();
		
		// Below is preferences specific
		final PortletPreferences portletPreferences = request.getPreferences();

		writer.write("Potlet Preference: "
				+ portletPreferences.getValue(TEST_VALUE, "(test.value not set)"));

		writer.write("<br>Potlet Preference: new value"
				+ portletPreferences.getValue(NEW_VALUE, "(new.value not set)"));

		final PortletURL u = response.createActionURL();
		writer.println("<br><A href=" + u + ">Trigger an action to change values.");
	}

	@Override
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		// Update the values for the PortletPrefernces
		final PortletPreferences portletPreferences = request.getPreferences();
		
		portletPreferences.setValue(NEW_VALUE, "'New Value: " + System.currentTimeMillis() + "'");
		portletPreferences.setValue(TEST_VALUE, "'Test Value: " + System.currentTimeMillis() + "'" );
		
		portletPreferences.store();
	}
	
}
