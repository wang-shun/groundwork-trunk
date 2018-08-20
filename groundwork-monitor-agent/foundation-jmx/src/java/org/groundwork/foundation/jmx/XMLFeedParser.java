package org.groundwork.foundation.jmx;

/**
 * 
 * @author Vong Tran (vong.tran@gmail.com)
 *
 */

public class XMLFeedParser 
{
	// String Constants
	private static final String LESS_THAN = "<";
	private static final String GREATER_THAN = ">";
	private static final String END_TAG = "/>";
	private static final String BEG_END_TAG = "</";
	private static final String SPACE = " ";
	
    /**
     * Internal variables to keep state 
     */
    private StringBuilder feederInput = new StringBuilder(ConfigurationManager.BLOCK_READ_SIZE);
    private StringBuilder message = new StringBuilder(256);
    private String endTag = null;
    
    //State information
    private boolean isMsgAvailable = false;
    private boolean isXMLStructure = false;
     
    
    public XMLFeedParser() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    /**
     * Keeps an internal buffer of data provided through input argument and extracts XML messages.
     * Method can be called with null argument just for parsing the next XML message. This is usually the case
     * if a large buffer containing several xml messages was provided.
     * @param input String that needs to be parsed out
     */
    public void processData(String input)
    {
        // Attach to current internal buffer
        if (input != null)
            this.feederInput.append(input);
        
        // If message was not read don't parse the message
        if (this.isMsgAvailable == true)
            return;
        
        // Do the parsing
        
        // Check if we are in the middle of a struct parsing
        if (this.isXMLStructure == false)
        {
	        int indexClose = this.feederInput.indexOf(END_TAG);
	        int indexOpen = this.feederInput.indexOf(LESS_THAN, this.feederInput.indexOf(LESS_THAN)+1);
	        
	        //Incomplete message -- wait for more input
	        if (indexClose == -1 && indexOpen == -1)
	            return;
	        
	        // Message with just attributes
	        if ( ((indexClose != -1) && (indexClose < indexOpen)) || (indexOpen == -1 && indexClose != -1) ) {
	            this.message.append(this.feederInput.substring(0, indexClose+2));
	            this.feederInput.delete(0, indexClose+2);
	            this.isMsgAvailable = true;
	        }else {
	            // XML struct with multiple elements
	            this.isXMLStructure = true;
	            
	            // Build the end tag
	            this.endTag = BEG_END_TAG;
	            int startBracket = this.feederInput.indexOf(LESS_THAN)+1;
	            this.endTag += this.feederInput.substring(startBracket, this.feederInput.indexOf(SPACE, startBracket));
	            this.endTag += GREATER_THAN;	            
	        }
        }
        
        if (this.isXMLStructure == true)
        {
            // Search the end tag
            int indexEndTag = this.feederInput.indexOf(this.endTag);
            
            if (indexEndTag != -1)
            {
                //Got messge
                this.message.append(this.feederInput.substring(0, indexEndTag+this.endTag.length()));
	            this.feederInput.delete(0, indexEndTag+this.endTag.length());
	            this.isMsgAvailable = true;
	            this.isXMLStructure = false;
            }   
        }
    }
    
    /**
     * Check if a complete message is available
     * @return true if message is ready to be read false if no message is available
     */
    public boolean isMessageAvailable() 
    {
        return this.isMsgAvailable;
    }
    
    /**
     * Check if the Input was parsed
     * @return true if nothing remains in input buffer false if ther is still data available
     */
    public boolean isAllParsed() {
        if (this.feederInput.length() > 0)
            return false;
        else
            return true;
    }
    
    public String getMessage()
    {
        if ( this.isMsgAvailable == false)
            return null;
        
        // save message
        String finalMsg = this.message.toString();
        
        // Reset
        this.isMsgAvailable = false;
        this.isXMLStructure = false;
        
        // Clear message
        this.message.setLength(0);
        
        return finalMsg; 
    }

}
