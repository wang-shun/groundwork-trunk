/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.report.birt.data.oda.ws.ui.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDriver;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.design.DataSetDesign;
import org.eclipse.datatools.connectivity.oda.design.DataSetParameters;
import org.eclipse.datatools.connectivity.oda.design.DesignFactory;
import org.eclipse.datatools.connectivity.oda.design.ParameterDefinition;
import org.eclipse.datatools.connectivity.oda.design.ResultSetColumns;
import org.eclipse.datatools.connectivity.oda.design.ResultSetDefinition;
import org.eclipse.datatools.connectivity.oda.design.ui.designsession.DesignSessionUtil;
import org.eclipse.datatools.connectivity.oda.design.ui.wizards.DataSetWizardPage;
import org.eclipse.datatools.connectivity.oda.design.util.DesignUtil;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.groundwork.foundation.ws.model.impl.EntityTypeProperty;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.report.birt.data.oda.ws.impl.Connection;
import org.groundwork.report.birt.data.oda.ws.impl.EntityFilter;

/**
 * Auto-generated implementation of an ODA data set designer page
 * for an user to create or edit an ODA data set design instance.
 * This custom page provides a simple Query Text control for user input.  
 * It further extends the DTP design-time framework to update
 * an ODA data set design instance based on the query's derived meta-data.
 * <br>
 * A custom ODA designer is expected to change this exemplary implementation 
 * as appropriate. 
 */
public class CustomDataSetWizardPage extends DataSetWizardPage
{	
	private static final String COMMA = ",";
	private static final String SEMI_COLON = ";";
	
	private static String DEFAULT_MESSAGE = "Define the query for the data set";
    private static String MSG_WARNING_CLEAR_FILTER = 
    	"Changing entity type will clear your filters!";
    private static String MSG_ERROR_NO_ENTITY_TYPES = 
    	"No Entity Types available!  Please make sure data source end point is correct and the web service is running.";
    private static String MSG_ERROR_NO_APP_TYPES = 
    	"No Application Types available!  Please make sure data source end point is correct and the web service is running.";
    private static String MSG_ERROR_NO_ENTITY_PROPERTIES = 
    	"No Entity Properties available!  Please make sure entity type is selected, data source end point is correct and the web service is running.";
    
    private transient Combo m_cboEntity;
    private transient Combo m_cboAppType;
    
    // Cache of entity properties
    private transient EntityTypeProperty[] entityProperties = null;
    private transient String[] entityPropertyNames = null;

	private Table table;
	private TableViewer tableViewer;
	
	// Set the table column property names
	private final String PROPERTY_COLUMN 			= "Property";
	private final String OPERATOR_COLUMN 			= "Operator";
	private final String VALUE_COLUMN 				= "Value";
	private final String LOGICAL_OPERATOR_COLUMN 	= "LogicalOperator";
	private final String SELECT_COLUMN			    = "Select";

	// Set column names
	private String[] columnNames = new String[] {
			SELECT_COLUMN,
			PROPERTY_COLUMN, 
			OPERATOR_COLUMN,
			VALUE_COLUMN,
			LOGICAL_OPERATOR_COLUMN			
			};
	
	// Fonts
    protected static final FontData LABEL_FONT_DATA = new FontData("Arial", 8, SWT.BOLD);
    
	// Create a ExampleTaskList and assign it to an instance variable
	private EntityFilterListViewer filterList = new EntityFilterListViewer(); 

	private ExampleContentProvider contentProvider = new ExampleContentProvider();
	
	/**
     * Constructor
	 * @param pageName
	 */
	public CustomDataSetWizardPage( String pageName )
	{
        super( pageName );
        setTitle( pageName );
        setMessage( DEFAULT_MESSAGE );

        setPageComplete( false );        		
	}

	/**
     * Constructor
	 * @param pageName
	 * @param title
	 * @param titleImage
	 */
	public CustomDataSetWizardPage( String pageName, String title,
			ImageDescriptor titleImage )
	{
        super( pageName, title, titleImage );
        setMessage( DEFAULT_MESSAGE );
        setPageComplete( false );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.design.ui.wizards.DataSetWizardPage#createPageCustomControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPageCustomControl( Composite parent )
	{
        setControl( createPageControl( parent ) );
        initializeControl();
	}
    
    /**
     * Creates custom control for user-defined query text.
     */
    private Control createPageControl( Composite parent )
    {    	
    	// First query application types and entity types and make sure Foundation is available.
    	String[] appTypes = getApplicationTypes();
    	if (appTypes == null || appTypes.length == 0)
    	{
        	// Display a message box indicating there are no entity types defined or Foundation
        	// Web Service may be down
        	MessageBox msgBox = new MessageBox(parent.getShell(), SWT.OK);
        	msgBox.setMessage(MSG_ERROR_NO_APP_TYPES);
        	msgBox.open();    		
    		return null;
    	}
    	
    	String[] entityTypes = getEntityTypes();
    	if (entityTypes == null || entityTypes.length == 0)
    	{
        	// Display a message box indicating there are no entity types defined or Foundation
        	// Web Service may be down
        	MessageBox msgBox = new MessageBox(parent.getShell(), SWT.OK);
        	msgBox.setMessage(MSG_ERROR_NO_ENTITY_TYPES);
        	msgBox.open();    		
    		return null;
    	}
    	
    	// Set width and height of dialog
    	//parent.getShell().setSize(750, 475);    	
    	
    	Composite composite = new Composite(parent, SWT.NONE);        
    	GridLayout layout = new GridLayout(3, true);
        composite.setLayout(layout);
		
		// Create entity drop-down
		createEntityCombo(composite, appTypes, entityTypes);

		createFilterTable(composite);
						 
		// force a redraw
		composite.layout(true, true);
		composite.setRedraw(true);
        		
        return composite;
    }      
    
    private void createEntityCombo (Composite parent, String[] appTypes, String[] entityTypes)
    {
		Label cboLabel = new Label( parent, SWT.NONE);				
        cboLabel.setText( "&Application Type:" );
        cboLabel.setFont(new Font(parent.getDisplay(), LABEL_FONT_DATA));
                        
        // Application Type Combo Box
        GridData gridData = new GridData(SWT.BEGINNING);		
		gridData.horizontalSpan = 2;
		
        m_cboAppType = new Combo(parent, SWT.NONE);             
        m_cboAppType.setLayoutData(gridData);			
        
		// Populate Combo Box        
        if (appTypes != null)
        {
        	for (int i = 0; i < appTypes.length; i++)
        	{
        		m_cboAppType.add(appTypes[i]);
        	}
        	
        	m_cboAppType.select(0);
        }       
        
        // Create Entity Type Combo Box
		cboLabel = new Label( parent, SWT.NONE);				
        cboLabel.setText( "&Entity Type:" );
        cboLabel.setFont(new Font(parent.getDisplay(), LABEL_FONT_DATA));
                        
        // Entity Combo Box
        gridData = new GridData(SWT.BEGINNING);		
		gridData.horizontalSpan = 2;
		
        m_cboEntity = new Combo(parent, SWT.NONE);             
		m_cboEntity.setLayoutData(gridData);			
        
		// Populate Combo Box        
        if (entityTypes != null)
        {
        	for (int i = 0; i < entityTypes.length; i++)
        	{
        		m_cboEntity.add(entityTypes[i]);
        	}
        	
        	m_cboEntity.select(0);
        }
        
        m_cboEntity.addModifyListener( new ModifyListener( ) 
        {
            public void modifyText( ModifyEvent e )
            {
                onEntityChanged(e);
            }
        } );            	
    }    
    
    private void createFilterTable (Composite composite)
    {
    	// Add Table Label        
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);		
		gridData.horizontalSpan = 3;
		    	
		Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(gridData);
		
        gridData = new GridData();		
		gridData.horizontalSpan = 3;
		
		Label label = new Label(composite, SWT.NONE);				
		label.setText( "Query Filter:" );
		label.setFont(new Font(composite.getDisplay(), LABEL_FONT_DATA));
		//label.setLayoutData(gridData);
		
		// Add the buttons
		createButtons(composite);
		
		// Create the table 
		createTable(composite);
		
		// Create and setup the TableViewer
		createTableViewer();
		tableViewer.setContentProvider(contentProvider);
		tableViewer.setLabelProvider(new FilterLabelProvider());
		
		// The input for the table viewer is the instance of ExampleTaskList
		tableViewer.setInput(filterList);    	
    }
	/**
	 * Initializes the page control with the last edited data set design.
	 */
	private void initializeControl( )
	{
        /* 
         * To optionally restore the designer state of the previous design session, use
         *      getInitializationDesignerState(); 
         */

        // Restores the last saved data set design
        DataSetDesign dataSetDesign = getInitializationDesign();
        if( dataSetDesign == null )
            return; // nothing to initialize

        String queryText = dataSetDesign.getQueryText();
        if( queryText == null && queryText.length() == 0)
            return; // nothing to initialize

        // initialize controls
        // Parse query text
        String entityType = null;
        String appType = null;
        String appEntityString = null;
        String filterListString = null;
        int index = queryText.indexOf(SEMI_COLON);
        
        // No filter specified
        if (index < 0)
        {
        	appEntityString = queryText;
        }                
        else {              
        	
        	// Set the currently selected app type and entity type
        	appEntityString = queryText.substring(0, index);
        }
        
    	int commaIndex = appEntityString.indexOf(COMMA);
    	
    	// No app type defined, defaults to system
    	if (commaIndex < 0)
    	{
    		entityType = appEntityString;
    	}
    	else 
    	{
    		appType = appEntityString.substring(0, commaIndex);
    		entityType = appEntityString.substring(commaIndex + 1);
    	}
    		        	
    	if (index > 0 && ((index + 1) < queryText.length()))
    	{        		
    		filterListString = queryText.substring(index + 1);
    	}        	
       
        // Set currently selected app type and entity
        setSelectedEntityType(appType, entityType);        
        
        if (filterListString != null && filterListString.length() > 0)
        {
        	try {        
        		this.filterList.initData(this.getEntityProperties(true), filterListString);
        		
            	// Add Filters to UI table
            	updateTable(this.filterList);
        	}
        	catch (Exception e)
        	{
        		// TODO: Log error
        	}
        }
    	        
        // Validate Data
        validateData();
        
        setMessage( DEFAULT_MESSAGE );
	}
	
	private void updateTable (EntityFilterListViewer filterList)
	{		
		if (filterList == null || filterList.size() == 0)
			return;
		
		List<EntityFilter> filters = filterList.getFilters();
		if (filters == null)
			return;
		
		EntityFilter filter = null;
		Iterator<EntityFilter> it = filters.iterator();
		while (it.hasNext())
		{
			filter = it.next();
			
			if (filter != null)
				tableViewer.add(filter);
		}		
	}

    /**
     * Set the selected entity type in the combo box by value
     * @param itemValue
     */
    private void setSelectedEntityType (String appType, String entityType)
    {
    	if (m_cboAppType == null || m_cboEntity == null)
    		return;
    	
    	// Select App Type
    	if (appType == null || appType.length() == 0)
    	{
    		m_cboAppType.select(0); // Select first item
    	}
    	else 
    	{
        	int numItems = m_cboAppType.getItemCount();
        	for (int i = 0; i < numItems; i++)
        	{
        		String itemText = m_cboAppType.getItem(i);
        		if (appType.equalsIgnoreCase(itemText) == true)
        		{
        			m_cboAppType.select(i);    			
        			break;
        		}    		
        	}
        	
        	if (m_cboAppType.getSelectionIndex() == -1)
        		m_cboAppType.select(0);  // Select the first item    
    	}
    	
    	// Select Entity
    	if (entityType == null || entityType.length() == 0)
    	{
    		m_cboEntity.select(0); // Select first item
    	}    
    	else 
    	{    		    	
	    	int numItems = m_cboEntity.getItemCount();
	    	for (int i = 0; i < numItems; i++)
	    	{
	    		String itemText = m_cboEntity.getItem(i);
	    		if (entityType.equalsIgnoreCase(itemText) == true)
	    		{
	    			m_cboEntity.select(i);    			
	    			break;
	    		}    		
	    	}
	    	
	    	if (m_cboEntity.getSelectionIndex() == -1)
	    		m_cboEntity.select(0);  // Select the first item	    	
    	}
    	
		// Update cell editor combo box with the correct entity properties
    	if (tableViewer != null)
    	{
    		ComboBoxCellEditor cellEditor = (ComboBoxCellEditor)tableViewer.getCellEditors()[getColumnIndex(PROPERTY_COLUMN)];		
    		cellEditor.setItems(getEntityPropertyNames(true));
    	}
    }
    
    /**
     * Obtains the user-defined query text of this data set from page control.
     * Query Text Format:
     * 	<app type>,<entity type>;<property>,<operator>,<value>,<logical operator>;<property 2>,<operator 2>,<value 2>,<logical operator 2>;...
     * @return query text
     */
    private String buildQueryText( )
    {
    	if (m_cboEntity == null)
    		return null;
    	
    	int selectedIndex = m_cboEntity.getSelectionIndex();
    	if (selectedIndex < 0)
    		return null;
    	
        StringBuilder sb = new StringBuilder(32);
        
        sb.append(m_cboAppType.getText());
        sb.append(COMMA);
        sb.append(m_cboEntity.getText());
        
        if (filterList != null && filterList.size() > 0)
        {
        	sb.append(SEMI_COLON);
        	sb.append(filterList.toString());
        }
        
        return sb.toString();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.design.ui.wizards.DataSetWizardPage#collectDataSetDesign(org.eclipse.datatools.connectivity.oda.design.DataSetDesign)
	 */
	protected DataSetDesign collectDataSetDesign( DataSetDesign design )
	{
        if( ! hasValidData() && (design.getQueryText() == null || design.getQueryText().length() == 0))
            return design;
        savePage( design );
        return design;
	}

    /*
     * (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.design.ui.wizards.DataSetWizardPage#collectResponseState()
     */
	protected void collectResponseState( )
	{
		super.collectResponseState( );
		/*
		 * To optionally assign a custom response state, for inclusion in the ODA
		 * design session response, use 
         *      setResponseSessionStatus( SessionStatus status );
         *      setResponseDesignerState( DesignerState customState );
		 */
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.design.ui.wizards.DataSetWizardPage#canLeave()
	 */
	protected boolean canLeave( )
	{
        return isPageComplete();
	}

	private void onEntityChanged (ModifyEvent e)
	{
		if (this.filterList.size() > 0)
		{
			MessageBox msgBox = new MessageBox(e.display.getActiveShell(), SWT.OK | SWT.CANCEL);
			msgBox.setMessage(MSG_WARNING_CLEAR_FILTER);
			if (msgBox.open() == SWT.CANCEL)
			{
				// TODO:  revert back to last selected on cancel
				return;
			}
						
			// Clear cache of entity properties b/c entity has changed and needs to be required.
			this.filterList.clear();			
		}
		
		// Update cell editor combo box with the correct entity properties
		ComboBoxCellEditor cellEditor = (ComboBoxCellEditor)tableViewer.getCellEditors()[getColumnIndex(PROPERTY_COLUMN)];		
		cellEditor.setItems(getEntityPropertyNames(true));		
	}
	
    /**
     * Validates the user-defined value in the page control exists
     * and not a blank text.
     * Set page message accordingly.
     */
	private void validateData( )
	{
		boolean isValid = true;
		String msg = null;
		
		// If the control has not been created then we have not changed anything
		// therefore the values are still valid
		if (m_cboEntity != null && m_cboEntity.getSelectionIndex() < 0)
		{
			isValid = false;
			msg = "You must select an entity type";
		}
			
        if( isValid )
            setMessage( DEFAULT_MESSAGE );
        else
            setMessage( msg, ERROR );

		setPageComplete( isValid );
	}

	/**
	 * Indicates whether the custom page has valid data to proceed 
     * with defining a data set.
	 */
	private boolean hasValidData( )
	{
        validateData( );
        
		return canLeave();
	}

	/**
     * Saves the user-defined value in this page, and updates the specified 
     * dataSetDesign with the latest design definition.
	 */
	private void savePage( DataSetDesign dataSetDesign )
	{
        // save user-defined query text
        String queryText = buildQueryText();
        if (queryText != null && queryText.length() > 0)
        	dataSetDesign.setQueryText( queryText );

        // obtain query's current runtime metadata, and maps it to the dataSetDesign
        IConnection customConn = null;
        try
        {
            // instantiate your custom ODA runtime driver class
            /* Note: You may need to manually update your ODA runtime extension's
             * plug-in manifest to export its package for visibility here.
             */
            IDriver customDriver = new org.groundwork.report.birt.data.oda.ws.impl.Driver();
            
            // obtain and open a live connection
            customConn = customDriver.getConnection( null );
            java.util.Properties connProps = 
                DesignUtil.convertDataSourceProperties( 
                        getInitializationDesign().getDataSourceDesign() );
            customConn.open( connProps );

            // update the data set design with the 
            // query's current runtime metadata
            updateDesign( dataSetDesign, customConn );
        }
        catch( OdaException e )
        {
            // not able to get current metadata, reset previous derived metadata
            dataSetDesign.setResultSets( null );
            dataSetDesign.setParameters( null );
            
            e.printStackTrace();
        }
        finally
        {
            closeConnection( customConn );
        }
	}

    /**
     * Updates the given dataSetDesign with the queryText and its derived metadata
     * obtained from the ODA runtime connection.
     */
    private void updateDesign( DataSetDesign dataSetDesign, IConnection conn)
        throws OdaException
    {
        IQuery query = conn.newQuery( null );
        query.prepare( dataSetDesign.getQueryText() );
        
        // TODO a runtime driver might require a query to first execute before
        // its metadata is available
//      query.setMaxRows( 1 );
//      query.executeQuery();
        
        try
        {
            IResultSetMetaData md = query.getMetaData();
            updateResultSetDesign( md, dataSetDesign );
        }
        catch( Exception e )
        {
            // no result set definition available, reset previous derived metadata
            dataSetDesign.setResultSets( null );
            e.printStackTrace();
        }
        
        // proceed to get parameter design definition
        try
        {
            IParameterMetaData paramMd = query.getParameterMetaData();
            updateParameterDesign( paramMd, dataSetDesign );
        }
        catch( Exception ex )
        {
            // no parameter definition available, reset previous derived metadata
            dataSetDesign.setParameters( null );
            ex.printStackTrace();
        }    
        
        /*
         * See DesignSessionUtil for more convenience methods
         * to define a data set design instance.  
         */     
    }

    /**
     * Updates the specified data set design's result set definition based on the
     * specified runtime metadata.
     * @param md    runtime result set metadata instance
     * @param dataSetDesign     data set design instance to update
     * @throws OdaException
     */
	private void updateResultSetDesign( IResultSetMetaData md,
            DataSetDesign dataSetDesign ) 
        throws OdaException
	{
        ResultSetColumns columns = DesignSessionUtil.toResultSetColumnsDesign( md );

        ResultSetDefinition resultSetDefn = DesignFactory.eINSTANCE
                .createResultSetDefinition();
        // resultSetDefn.setName( value );  // result set name
        resultSetDefn.setResultSetColumns( columns );

        // no exception in conversion; go ahead and assign to specified dataSetDesign
        dataSetDesign.setPrimaryResultSet( resultSetDefn );
        dataSetDesign.getResultSets().setDerivedMetaData( true );
	}

    /**
     * Updates the specified data set design's parameter definition based on the
     * specified runtime metadata.
     * @param paramMd   runtime parameter metadata instance
     * @param dataSetDesign     data set design instance to update
     * @throws OdaException
     */
    private void updateParameterDesign( IParameterMetaData paramMd,
            DataSetDesign dataSetDesign ) 
        throws OdaException
    {
        DataSetParameters paramDesign = 
            DesignSessionUtil.toDataSetParametersDesign( paramMd, 
                    DesignSessionUtil.toParameterModeDesign( IParameterMetaData.parameterModeIn ) );
        
        if (paramDesign == null)
        	return;
        
        // no exception in conversion; go ahead and assign to specified dataSetDesign
        paramDesign.setDerivedMetaData( true );
        dataSetDesign.setParameters( paramDesign );

        // TODO replace below with data source specific implementation;
        // hard-coded parameter's default value for demo purpose
        EList<ParameterDefinition> paramList =  (EList<ParameterDefinition>)paramDesign.getParameterDefinitions();
        
        if( paramList != null && paramList.size() > 0)
        {
        	ParameterDefinition paramDef = null;
        	Iterator<ParameterDefinition> it = paramList.iterator();
        	
        	while (it.hasNext())
        	{
        		paramDef = it.next();
            
        		if( paramDef != null )
        		{
        			paramDef.setDefaultScalarValue( "TODO:  Set Value Or Link To Parameter" );        		
        		}
        	}
        }
    }
    
    /**
     * Attempts to close given ODA connection.
     */
    private void closeConnection( IConnection conn )
    {
        try
        {
            if( conn != null && conn.isOpen() )
                conn.close();
        }
        catch ( OdaException e )
        {
            // ignore
            e.printStackTrace();
        }
    }

    private String[] getEntityTypes ()
    {
        Connection gwConnection = null;
        try
        {
            // instantiate your custom ODA runtime driver class
            /* Note: You may need to manually update your ODA runtime extension's
             * plug-in manifest to export its package for visibility here.
             */
            IDriver customDriver = new org.groundwork.report.birt.data.oda.ws.impl.Driver();
            
            // obtain and open a live connection
            gwConnection = (Connection)customDriver.getConnection( null );
            java.util.Properties connProps = 
                DesignUtil.convertDataSourceProperties( 
                        getInitializationDesign().getDataSourceDesign() );
            gwConnection.open( connProps );

            return gwConnection.getEntityTypes();
        }
        catch( OdaException e )
        {
            e.printStackTrace();
        }
        finally
        {
            closeConnection( gwConnection );
        }   
        
        return null;
    }
    
    private String[] getApplicationTypes ()
    {
        Connection gwConnection = null;
        
        try
        {
            // instantiate your custom ODA runtime driver class
            /* Note: You may need to manually update your ODA runtime extension's
             * plug-in manifest to export its package for visibility here.
             */
            IDriver customDriver = new org.groundwork.report.birt.data.oda.ws.impl.Driver();
            
            // obtain and open a live connection
            gwConnection = (Connection)customDriver.getConnection( null );
            java.util.Properties connProps = 
                DesignUtil.convertDataSourceProperties( 
                        getInitializationDesign().getDataSourceDesign() );
            gwConnection.open( connProps );

            return gwConnection.getApplicationTypes();
        }
        catch( OdaException e )
        {
            e.printStackTrace();
        }
        finally
        {
            closeConnection( gwConnection );
        }   
        
        return null;
    }    
    
    private EntityTypeProperty[] getEntityProperties (String appType, String entityType)
    {
        Connection gwConnection = null;
        try
        {
            // instantiate your custom ODA runtime driver class
            /* Note: You may need to manually update your ODA runtime extension's
             * plug-in manifest to export its package for visibility here.
             */
            IDriver customDriver = new org.groundwork.report.birt.data.oda.ws.impl.Driver();
            
            // obtain and open a live connection
            gwConnection = (Connection)customDriver.getConnection( null );
            java.util.Properties connProps = 
                DesignUtil.convertDataSourceProperties( 
                        getInitializationDesign().getDataSourceDesign() );
            gwConnection.open( connProps );

            return gwConnection.getEntityProperties(appType, entityType);
        }
        catch( OdaException e )
        {
            e.printStackTrace();
        }
        finally
        {
            closeConnection( gwConnection );
        }   
        
        return new EntityTypeProperty[0];
    }    
    
    // Returns entity properties for the currently select entity type
    private EntityTypeProperty[] getEntityProperties (boolean bRefresh)
    {
    	if (bRefresh == true || this.entityProperties == null)
    	{
    		if (m_cboEntity != null && m_cboEntity.getSelectionIndex() >= 0)
    		{
    			String appType = null;
    			String entityType = m_cboEntity.getItem(m_cboEntity.getSelectionIndex());
    			
    			this.entityProperties =	getEntityProperties(appType, entityType);
    		}
    		else {
    			this.entityProperties = new EntityTypeProperty[0];
    		}
    		    		
    		// Clear entity property names to insure it is updated
    		this.entityPropertyNames = null;
    	}
    	
   		return this.entityProperties;
    }     
    
    private String[] getEntityPropertyNames (boolean bRefresh)
    {
    	if (bRefresh == true || this.entityPropertyNames == null || this.entityProperties == null)
    	{
    		EntityTypeProperty[] entityTypeProps = getEntityProperties(bRefresh);

    		entityPropertyNames = new String[entityTypeProps.length];
        	for (int i = 0; i < entityTypeProps.length; i++)
        	{ 
        		entityPropertyNames[i] = entityTypeProps[i].getName();
        	}           	
    	}    	
    	 	   
    	return this.entityPropertyNames;
    }
    
    /*************************************************************************/
    /* TABLE VIEWER CODE */
    
	/**
	 * InnerClass that acts as a proxy for the FilterList 
	 * providing content for the Table. It implements the IFilterListViewer 
	 * interface since it must register changeListeners with the 
	 * FilterList 
	 */
	class ExampleContentProvider implements IStructuredContentProvider, IFilterListViewer 
	{
		public void inputChanged(Viewer v, Object oldInput, Object newInput) 
		{
			if (newInput != null)
				((EntityFilterListViewer) newInput).addChangeListener(this);
			if (oldInput != null)
				((EntityFilterListViewer) oldInput).removeChangeListener(this);
		}

		public void dispose() {
			filterList.removeChangeListener(this);
		}

		// Return the filters as an array of Objects
		public Object[] getElements(Object parent) {
			return filterList.getFilters().toArray();
		}

		public void addFilter(EntityFilter filter) {
			tableViewer.add(filter);
		}

		public void removeFilter(EntityFilter filter) {
			tableViewer.remove(filter);			
		}

		public void updateFilter(EntityFilter filter) {
			tableViewer.update(filter, null);	
		}
		
		public void clearFilters()
		{
			tableViewer.refresh();
		}
	}    

	/**
	 * Create the Table
	 */
	private void createTable(Composite parent) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | 
					SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

		table = new Table(parent, style);		
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 3;
		gridData.heightHint = 205;
		table.setLayoutData(gridData);		
					
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		// Empty column used to select row
		TableColumn column = new TableColumn(table, SWT.CENTER, 0);
		column.setText(" ");
		column.setWidth(15);
		
		// 1st column with image/checkboxes - NOTE: The SWT.CENTER has no effect!!
		column = new TableColumn(table, SWT.LEFT, 1);		
		column.setText("Property");
		column.setWidth(250);		
		
		// 2nd column with operator
		column = new TableColumn(table, SWT.LEFT, 2);
		column.setText("Operator");
		column.setWidth(100);

		// 3rd column with value
		column = new TableColumn(table, SWT.LEFT, 3);
		column.setText("Value");
		column.setWidth(100);

		// 4th column with default value
		column = new TableColumn(table, SWT.CENTER, 4);
		column.setText("Logical Operator");
		column.setWidth(100);							
	}

	/**
	 * Create the TableViewer 
	 */
	private void createTableViewer() {

		tableViewer = new TableViewer(table);
		tableViewer.setUseHashlookup(true);
		
		tableViewer.setColumnProperties(columnNames);

		// Create the cell editors
		CellEditor[] editors = new CellEditor[columnNames.length];
		
		editors[0] = new ComboBoxCellEditor(table, new String[0], SWT.READ_ONLY);
		
		// Column 1 : Property (Combo Box)
		editors[1] = new ComboBoxCellEditor(table, getEntityPropertyNames(true), SWT.READ_ONLY);

		// Column 2 : Operator (Combo Box) 
		editors[2] = new ComboBoxCellEditor(table, filterList.getOperators(), SWT.READ_ONLY);

		// Column 3 : Value (Free text)
		TextCellEditor textEditor = new TextCellEditor(table);
		((Text) textEditor.getControl()).setTextLimit(256);
		editors[3] = textEditor;

		// Column 4 : Logical Operator
		editors[4] = new ComboBoxCellEditor(table, filterList.getLogicalOperators(), SWT.READ_ONLY);				

		// Assign the cell editors to the viewer 
		tableViewer.setCellEditors(editors);		
		
		// Set the cell modifier for the viewer
		tableViewer.setCellModifier(new FilterCellModifier(this));
	}
	
	/**
	 * Add the "Add", "Delete" and "Close" buttons
	 * @param parent the parent composite
	 */
	private void createButtons(Composite parent) 
	{
		// Create and configure the "Add" button
		Button add = new Button(parent, SWT.PUSH | SWT.CENTER);
		add.setText("&Add");
		
		GridData gridData = new GridData (GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = 80;
		add.setLayoutData(gridData);
		add.addSelectionListener(new SelectionAdapter() {
       	
       		// Add a filter to the FilterList and refresh the view
			public void widgetSelected(SelectionEvent e) 
			{
				EntityTypeProperty[] entityProperties = getEntityProperties(false);
				
				if (entityProperties != null && entityProperties.length > 0)
				{	
					// Note:  We add a dummy value of zero so it is a valid filter
					filterList.addFilter(entityProperties[0], FilterOperator.EQ, "0", FilterOperator.AND);
				}
				else {
		        	MessageBox msgBox = new MessageBox(e.widget.getDisplay().getActiveShell(), SWT.OK);
		        	msgBox.setMessage(MSG_ERROR_NO_ENTITY_PROPERTIES);
		        	msgBox.open();    				    	
				}
			}
		});

		//	Create and configure the "Delete" button
		Button delete = new Button(parent, SWT.PUSH | SWT.CENTER);
		delete.setText("&Delete");
		gridData = new GridData (GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = 80; 
		delete.setLayoutData(gridData); 

		delete.addSelectionListener(new SelectionAdapter() {
       	
			//	Remove the selection and refresh the view
			public void widgetSelected(SelectionEvent e) {
				EntityFilter filter = (EntityFilter) ((IStructuredSelection) 
						tableViewer.getSelection()).getFirstElement();
				if (filter != null) 
				{
					filterList.removeFilter(filter);
				} 			
				else {
		        	MessageBox msgBox = new MessageBox(e.widget.getDisplay().getActiveShell(), SWT.OK);
		        	msgBox.setMessage("You must first select a filter to delete.");
		        	msgBox.open();  					
				}
			}
		});			
	}	
	
	/**
	 * Return the column names in a collection
	 * 
	 * @return List  containing column names
	 */
	public java.util.List<String> getColumnNames() 
	{
		return Arrays.asList(columnNames);
	}
	
	public int getColumnIndex (String columnName)
	{
		return getColumnNames().indexOf(columnName);
	}

	/**
	 * @return currently selected item
	 */
	public ISelection getSelection() {
		return tableViewer.getSelection();
	}

	/**
	 * Return the EntityFilterList
	 */
	public EntityFilterListViewer getFilterList() {
		return filterList;	
	}	
	
	/**
	 * Return the array of choices for a multiple choice cell
	 */
	public String[] getChoices(String property) 
	{
		if (PROPERTY_COLUMN.equals(property))
		{
			return getEntityPropertyNames(false);	
		} 
		else if (OPERATOR_COLUMN.equals(property))
		{
			return filterList.getOperators();
		}
		else if (LOGICAL_OPERATOR_COLUMN.equals(property))
		{
			return filterList.getLogicalOperators();
		}		
		else
			return new String[]{};
	}	
	
    public EntityTypeProperty getEntityProperty (String propName)
    {
    	if (propName == null || propName.length() == 0)
    		throw new IllegalArgumentException("Unable to lookup EntityTypeProperty - Invalid null / empty property name");
    
    	EntityTypeProperty[] props = getEntityProperties(false);
    	if (props == null || props.length == 0)
    		return null;
    	
    	EntityTypeProperty prop = null;
    	for (int i = 0; i < props.length; i++)
    	{
    		prop = props[i];
    		
    		if (propName.equalsIgnoreCase(props[i].getName()))
    			return prop;
    	}
    	
    	return null; // Not Found    	
    }
}
