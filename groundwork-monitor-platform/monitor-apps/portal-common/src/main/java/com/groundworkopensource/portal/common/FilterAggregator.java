
package com.groundworkopensource.portal.common;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.groundworkopensource.common.utils.FilterException;
import com.groundworkopensource.common.utils.FilterUtils;
import com.groundworkopensource.common.utils.Filters;
import com.groundworkopensource.common.utils.HostFilter;
import com.groundworkopensource.common.utils.ServiceFilter;

/**
 * The class aggregates the list of host and service filters from the XML
 * defining it.
 * 
 * @author mridu_narang
 * 
 */
public final class FilterAggregator {

    /**
     * Logger
     */
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     *Static reference for enabling singleton pattern
     */
    private static FilterAggregator instance;

    /**
     * Constant defining the default path to pick up the portal-filter.xml file
     */
    private static final String PORTAL_FILTER_DEFAULT_PATH = CommonConstants.GROUNDWORK_DEFAULT_CONFIG_PATH
            + CommonConstants.FILTER_XML_NAME;

    /**
     * Error boolean to set if error occurred
     */
    private boolean initError = false;

    /**
     * Fall-back XML path in war
     */
    private String xmlPath = null;

    /**
     * Fall-back XSD path in war
     */
    private String xsdPath = null;

    /**
     * Constant defining Filter xml file name
     */
    private static final String FILTERS_CLASS_NAME = "com.groundworkopensource.common.utils.Filters";

    /**
     * Constant defining schema language
     */
    private static final String SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

    /**
     * List of all filters
     */
    private Filters allFilters = null;

    /**
     * Hashed List of all host filters mapped according to their identifier key
     * value
     */
    private HashMap<String, HostFilter> allHostFilters = new HashMap<String, HostFilter>();

    /**
     * Hashed List of all service filters mapped according to their identifier
     * key value
     */
    private HashMap<String, ServiceFilter> allServiceFilters = new HashMap<String, ServiceFilter>();

    /**
     * Default Private Constructor
     */

    private FilterAggregator() {
        // Private constructor makes class non sub-classable
    }

    /**
     * Method retrieves Instance of Filter Aggregator. Make sure initialization
     * method [init(String xmlPathName, String xsdPathName)] is called before
     * using any other methods.
     * 
     * @return instance
     * 
     */
    public static synchronized FilterAggregator getInstance() {

        if (instance == null) {
            instance = new FilterAggregator();
        }
        return instance;
    }

    /**
     * Method to initialize FilterAggregator with filters populated from the XML
     * 
     * @param xmlPathName
     * @param xsdPathName
     */
    public void init(String xmlPathName, String xsdPathName) {
        this.xmlPath = xmlPathName;
        this.xsdPath = xsdPathName;
        loadFilters();
    }

    /**
     * Validates XML defining filters either user-defined or default
     * 
     */
    private void loadFilters() {

        // Field indicating if default XML is valid or not
        boolean defaultValidated = false;

        try {
            // First validate the XML against the schema
            validate(PORTAL_FILTER_DEFAULT_PATH);

            // Pass validated XML to JOX for conversion of XML to bean objects
            xmlToBeans(PORTAL_FILTER_DEFAULT_PATH);
            defaultValidated = true;

        } catch (SAXException ex) {
            this.logger.debug("loadFilters(): XML defined at "
                    + PORTAL_FILTER_DEFAULT_PATH + " is invalid - "
                    + ex.getMessage());
            defaultValidated = false;

        } catch (IOException ex) {
            this.logger.debug("loadFilters(): Error reading "
                    + PORTAL_FILTER_DEFAULT_PATH + " " + ex.getMessage());
            defaultValidated = false;
        }

        if (!defaultValidated) {
            this.logger
                    .debug("loadFilters(): Reading default filter XML from war bundle ");

            try {
                validate(this.xmlPath);
                xmlToBeans(this.xmlPath);
            } catch (SAXException e) {
                this.logger
                        .error("loadFilters(): Default XML in bundle is invalid - "
                                + e.getMessage());
                setInitError(true);

            } catch (IOException e) {
                this.logger
                        .error("loadFilters(): Cannot read default XML in bundle - "
                                + e.getMessage());
                setInitError(true);
            }
        }
    }

    /**
     * 
     * Validates XML with given name
     * 
     * @param xmlName
     * @throws SAXException
     * @throws IOException
     */
    private void validate(String xmlName) throws SAXException, IOException {

        /*
         * Factory for W3C XML Schema language - Lookup implementation of
         * SchemaFactory supporting the specified schema language
         */
        SchemaFactory factory = SchemaFactory.newInstance(SCHEMA_LANGUAGE);

        if (this.xsdPath != null) {
            // Compile the schema
            // XML Schema bundled with war is used to validate the filter XML

            Schema schema = factory.newSchema(new File(this.xsdPath));

            // Get validator from schema
            Validator validator = schema.newValidator();

            // Parse document to be validated
            Source source = new StreamSource(xmlName);

            // Validate
            validator.validate(source);
            this.logger.debug("validate(): Filter XML is valid - well formed");
        } else {
            this.logger.debug("validate(): Cannot find XSD to validate");
            setInitError(true);
        }
    }

    /**
     * Method that converts XML defined filters to corresponding beans
     */
    private void xmlToBeans(String xmlName) {
        // Populate the beans
        try {
            this.allFilters = (Filters) FilterUtils.loadFilter(xmlName,
                    FILTERS_CLASS_NAME);
        } catch (FilterException e) {
            this.logger.error("xmlToBeans(): Problem loading filters from XML "
                    + e.getMessage());
            setInitError(true);
        }

        /*
         * Create hash-map of filters where name of filter forms its key for
         * identification
         */
        if (this.allFilters == null) {
            this.logger
                    .error("xmlToBeans(): Problem loading filters from XML - Null filters returned");
            setInitError(true);
            return;
        }
        for (HostFilter tempHostFilter : this.allFilters.getHostFilter()) {
            this.allHostFilters.put(tempHostFilter.getName(), tempHostFilter);
        }

        for (ServiceFilter tempServiceFilter : this.allFilters
                .getServiceFilter()) {
            this.allServiceFilters.put(tempServiceFilter.getName(),
                    tempServiceFilter);
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @throws CloneNotSupportedException
     * @return the <code>Object</code> on which clone() call was raised.
     * 
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Retrieves all filters
     * 
     * @return The list of aggregated host and service filters
     */
    public Filters getAllFilters() {
        return this.allFilters;
    }

    /**
     * Sets the allFilters.
     * 
     * @param allFilters
     *            the allFilters to set
     */
    public void setAllFilters(Filters allFilters) {
        this.allFilters = allFilters;
    }

    /**
     * Retrieves all host filters
     * 
     * @return The list of host filters
     */
    public HashMap<String, HostFilter> getAllHostFilters() {

        return this.allHostFilters;
    }

    /**
     * Sets the allHostFilters.
     * 
     * @param allHostFilters
     *            the allHostFilters to set
     */
    public void setAllHostFilters(HashMap<String, HostFilter> allHostFilters) {
        this.allHostFilters = allHostFilters;
    }

    /**
     * Retrieves all service filters
     * 
     * @return The list of service filters
     */
    public HashMap<String, ServiceFilter> getAllServiceFilters() {

        return this.allServiceFilters;
    }

    /**
     * Sets the allServiceFilters.
     * 
     * @param allServiceFilters
     *            the allServiceFilters to set
     */
    public void setAllServiceFilters(
            HashMap<String, ServiceFilter> allServiceFilters) {
        this.allServiceFilters = allServiceFilters;
    }

    /**
     * Get host filter based on host filter key
     * 
     * @param hostFilterKey
     * @return host filter for given key
     */
    public HostFilter getHostFilter(String hostFilterKey) {

        HostFilter hostFilter = this.allHostFilters.get(hostFilterKey);
        return hostFilter;
    }

    /**
     * Get service filter based on service filter key
     * 
     * @param serviceFilterkey
     * @return service filter for given key
     */
    public ServiceFilter getServiceFilter(String serviceFilterkey) {

        ServiceFilter serviceFilter = this.allServiceFilters
                .get(serviceFilterkey);
        return serviceFilter;
    }

    /**
     * Sets the initError.
     * 
     * @param initError
     *            the initError to set
     */
    public void setInitError(boolean initError) {
        this.initError = initError;
    }

    /**
     * Returns the initError.
     * 
     * @return the initError
     */
    public boolean isInitError() {
        return this.initError;
    }
}
