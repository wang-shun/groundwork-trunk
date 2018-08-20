package com.groundworkopensource.portal.identity.extendedui;


public class GroundworkContainerExtensionException extends Exception {
	
	public GroundworkContainerExtensionException(String message) {
        super(message);
    }
	
	public GroundworkContainerExtensionException(String message, Exception exc) {
        super(message,exc);
    }
	

}
