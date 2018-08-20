package com.groundworkopensource.portal.licensing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;


/**
 * Manager for resources scoped to a client page request.
 * 
 * @since GWMON 6.0
 * @version $Revision$
 */
public class LicenseManager {
    private Logger log = Logger.getLogger(getClass());
    
    private EnterpriseLicenseValidator validator = null;
    
    private static LicenseManager manager = null;
   
    /**
     * Constructor (made private for Singleton/Factory pattern).
     */
    private LicenseManager() {
 
    }
    
    
    
    /**
     * Get a RequestResourceManager by request ID.
     * 
     * @param id - request ID
     * @param create - if true, create a new Manager if one doesn't already 
     * exist.
     */
    public static final synchronized LicenseManager getInstance( ) {
    	
        if (manager == null ) {
            manager = new LicenseManager();
        }
        
        return manager;
    }

  
    /**
     * Add a validator
     * 
     * @param resource
     */
    public void addValidator(EnterpriseLicenseValidator validator) {
          this.validator = validator;
    }
 
   
    
    /**
     * Return Validator
     */
    public EnterpriseLicenseValidator getLicenseValidator() {
    	return validator;
    }
    
 
}