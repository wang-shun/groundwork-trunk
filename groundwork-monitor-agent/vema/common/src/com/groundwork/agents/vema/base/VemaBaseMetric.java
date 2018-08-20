package com.groundwork.agents.vema.base;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.groundwork.agents.vema.api.VemaConstants;

public class VemaBaseMetric 
{
    private static final String sPoweredDown       = "UNSCHEDULED DOWN";      // essentially
    private static final String sUnknown           = "UNKNOWN";               // constants...
    private static final String sCritical          = "UNSCHEDULED CRITICAL";  // which need to be 
    private static final String sWarning           = "WARNING";               // set to make GWOS
    private static final String sPending           = "PENDING";
    private static final String sOK                = "OK";

	private static final Date nullDate             = new Date(); // was ( 0L );

    private Boolean       callerUsesManualClearing = false;
    private String        querySpec                = null;

    private String        currValue                = null;
    private String        lastValue                = null;
    private String        currState                = null;
    private String        lastState                = sPending;  // rather important
    private String        currStateExtra           = null;
    
    private boolean       useLessThanLogic         = false;
    private long          thresholdWarning         = 0;
    private long          thresholdCritical        = 0;

    private boolean       monitorFlag              = false;
    private boolean       graphFlag                = false;
    private boolean       traceFlag                = false;   // for diagnostic output

    private long          creatMillisecTimestamp   = (new Date()).getTime();
    private long          valueMillisecTimestamp   = creatMillisecTimestamp;

    private static Logger log = Logger.getLogger(VemaBaseMetric.class);

    public VemaBaseMetric(String query, long warning, long critical, boolean isGraphed, boolean isMonitored )
    {
        this.querySpec         = query;
        this.thresholdWarning  = warning;
        this.thresholdCritical = critical;
        this.graphFlag         = isGraphed;
        this.monitorFlag       = isMonitored;
        
        useLessThanLogic       = (warning > critical);
    }

    public void setThresholds( long warning, long critical )
    {
        this.thresholdWarning  = warning;
        this.thresholdCritical = critical;

        useLessThanLogic       = (warning > critical);
    }
    
    public void setIsGraphed( boolean isGraphed )
    {
    	this.graphFlag         = isGraphed;
    }
    
    public void setIsMonitored( boolean isMonitored )
    {
    	this.monitorFlag       = isMonitored;
    }
    
    public void setTrace()
    {
    	this.traceFlag         = true;
    }

    public boolean isCritical()
    {
        if( currValue == null )
            return false;

        if( thresholdCritical == 0 && thresholdWarning == 0 )
            return false;

        long compvalue = 0;
        try
        {
        	if( currValue.contains("%") )
        		 compvalue = Long.parseLong( currValue.substring(0, currValue.indexOf("%") ) );
        	else compvalue = Long.parseLong( currValue );
        } 
        catch( Exception e )
        {
            compvalue = 0;
        }

        return 	useLessThanLogic 
        		? compvalue <= thresholdCritical
        		: compvalue >= thresholdCritical;  // notice NOT '>'
    }

    public boolean isWarning()
    {
        if( currValue == null )
            return false;

        if( thresholdCritical == 0 && thresholdWarning == 0 )
            return false;

        long compvalue = 0;
        try
        {
        	if( currValue.contains("%") )
        		 compvalue = Long.parseLong( currValue.substring(0, currValue.indexOf("%") ) );
        	else compvalue = Long.parseLong( currValue );
        } 
        catch( Exception e )
        {
            compvalue = 0;
        }

        return 	useLessThanLogic 
        		? compvalue <= thresholdWarning
        		: compvalue >= thresholdWarning;  // notice NOT '>'
    }
    
    public boolean isGraphed()   { return this.graphFlag;   }
    public boolean isMonitored() { return this.monitorFlag; }
    public boolean isTraced()    { return this.traceFlag;     }

    public boolean isDefunct()
    {
        return( currValue == null && lastValue == null );
    }

    public String commentOnValue()
    {
    	String x = null;
    	
    	if(      isDefunct()  ) x = "DEFUNCT:  Value (null)";

    	else if( isCritical() ) x = "CRITICAL: Value (" + currValue + ") " 
                                  + (useLessThanLogic ? "<" : ">" ) 
                                  + " threshold (" + thresholdCritical + ")";

    	else if( isWarning()  ) x = "WARNING:  Value (" + currValue + ") " 
                                  + (useLessThanLogic ? "<" : ">" ) 
                                  + " threshold (" + thresholdWarning + ")";

    	else                    x = "NOMINAL:  Value (" + currValue + ")";
    	
    	return x;
    }
    
    private void printTrace( String header, VemaBaseMetric vbm )
    {
    	StringBuilder s = new StringBuilder();
    	
    	s.append( String.format( "metric %s:\n", header ));
    	s.append( formatSelf( vbm ) );
    	
    	log.info( s.toString() );
    }

    public String formatSelf()
    {
    	return formatSelf( this );
    }
    
    public String formatSelf( VemaBaseMetric o )
    {
    	StringBuffer s = new StringBuffer( 1000 );
    	
    	s.append( String.format( "%-40s: '%s'\n", "querySpec",         o.querySpec ));
    	s.append( String.format( "%-40s: '%s'\n", "currValue",         o.currValue ));
    	s.append( String.format( "%-40s: '%s'\n", "lastValue",         o.lastValue ));
    	s.append( String.format( "%-40s: '%s'\n", "currState",         o.currState ));
    	s.append( String.format( "%-40s: '%s'\n", "useLessThanLogic",  o.useLessThanLogic ? "true":"false"));
    	s.append( String.format( "%-40s: '%d'\n", "thresholdWarning",  o.thresholdWarning ));
    	s.append( String.format( "%-40s: '%d'\n", "thresholdCritical", o.thresholdCritical ));
    	s.append( String.format( "%-40s: '%s'\n", "graphFlag",         o.graphFlag ? "true":"false" ));
    	s.append( String.format( "%-40s: '%s'\n", "monitorFlag",       o.monitorFlag ? "true":"false" ));
    	s.append( String.format( "%-40s: '%s'\n", "traceFlag",         o.traceFlag ? "true":"false" ));
    	s.append( String.format( "%-40s: '%s'\n", "callerUsesManualClearing", o.callerUsesManualClearing ? "true":"false"));
    	
    	return s.toString();
    }
    
    public void mergeInNew( VemaBaseMetric update )
    {
        if( this.traceFlag || update.traceFlag )
        {
    		printTrace( "0: update  object: ", update );
    		printTrace( "1: base    object: ", this );
        }
    	
    	if( this.querySpec == null || update.querySpec != null )
            this.querySpec = update.querySpec;

        this.useLessThanLogic         = update.useLessThanLogic;
        this.thresholdWarning         = update.thresholdWarning;
        this.thresholdCritical        = update.thresholdCritical;
        this.graphFlag                = update.graphFlag;
        this.monitorFlag              = update.monitorFlag;
        this.traceFlag                = update.traceFlag || this.traceFlag;  //latching
        this.callerUsesManualClearing = update.callerUsesManualClearing;

        setValue( update.currValue );

        if( this.traceFlag || update.traceFlag )
    		printTrace( "2: updated object: ", this );
    }

    /**
     * by setting ALL values to "null" to start with (before assigning real values), 
     * and THEN "adjusting current values" as they are being parsed, the array of 
     * all metrics will retain as 'null' value any that are NOT adjusted.  This is
     * an excellent result for figuring out later which members to eliminate as 
     * no longer existing. 
     */
    public  void adjustCurrValue(String value) // very tricky (but really good) use
    {
    	currValue = value;
    }

    public  void setPoweredDown() // must be public
    {
        currState = sPoweredDown;
        currStateExtra = "";
    }

    private void setCurrState() // should always be PRIVATE, internally used.
    {
        if( currState != null && !callerUsesManualClearing )
            lastState = currState;

        String state;
        String x;

        if     ( currValue == null )     { state = sUnknown;  x = "No Value"; }
        else if( isCritical() )          { state = sCritical; x = ""; }
        else if( isWarning() )           { state = sWarning;  x = ""; }
        else if( lastState == sPending ) { state = sOK;       x = ""; }
        else if( lastState == null     ) { state = sOK;       x = ""; }
        else                             { state = sOK;       x = ""; }

        currState      = state;
        currStateExtra = x + " (" + querySpec + ")";
    }
    
    /**
     * So... this is kind of dual-purpose.  Used ONCE on a VemaBaseMetric object
     * and it also sets the callerUsesManualClearing flag, which SUPPRESSES the
     * normal 'remember last state every time a new set of values is update' 
     * functionality above.  Once used, it therefore MUST be used over and
     * over again. 
     * 
     * Now... you might ask, but... wouldn't it be better to have a 
     * setter method for this, to allow it to be explicitly set?  
     * 
     * Perhaps.  But, in use, either the upper level calling logic expects
     * the retention of the 'last state' to be an automatic thing (i.e. its
     * not interested in specifically clearing the state), or it IS expecting
     * to be able to clear the state itself.  Either way... the first use of
     * clearStateChange() then latches it into the 'must be manually cleared'
     * mode for the rest of the life of the object.
     * 
     * FINALLY - this 'magic' functionality rests with each object independent
     * of the others.  Again... because perhaps there are a class of objects
     * where manual-clearing of state changes is vitally important, and where
     * all the rest don't need such husbandry. 
     * 
     * If you feel like changing the callerUsesManualClearing to a 'static'
     * variable then ALL objects will inherit the same flag.  Could be useful
     * in its own right.
     */
    public void clearStateChange()
    {
    	if( currState != null )
    		lastState = currState;
    	
    	callerUsesManualClearing = true;
    }

    /**
     * LOGIC TABLE
     * __________________________________________________
     *   currState  lastState      RESULT
     * --------------------------------------------------
     *     null       null         false
     *     null        *           true
     *      A          A           false
     *      A          B           true
     *      A         null         true
     * @return
     */
    public boolean isStateChange()
    {
        if(      currState != null ) // and...
            if(  lastState != null ) return( currState != lastState );
            else                     return( true );
        else if( lastState != null ) return( true );
        else                         return( false );
    }
    
    public boolean isValueChange()
    {
    	if(      currValue != null ) // and...
    		if(  lastValue != null ) return( lastValue != currValue );
    		else                     return( true );
    	else if( lastValue != null ) return( true );
    	else                         return( false );
    }

    public void setValue(String value)
    {
        if( currValue != null )      // possible subtle BUG / TODO
        	lastValue = currValue;
        
        currValue = value;

        valueMillisecTimestamp = (new Date()).getTime();  // throw away object

        setCurrState();              // very important: CHAINED computation
    }

    public String getDateValue()                  
    { 
        SimpleDateFormat sdf = new SimpleDateFormat( VemaConstants.gwosDateFormat );

        if( valueMillisecTimestamp != 0 )
             return sdf.format( new Date(valueMillisecTimestamp) ).toString();
        else return sdf.format( nullDate ).toString();
    }

    public String getDateCreated()                  
    { 
        SimpleDateFormat sdf = new SimpleDateFormat( VemaConstants.gwosDateFormat );

        if( creatMillisecTimestamp != 0 )
             return sdf.format( new Date(creatMillisecTimestamp) ).toString();
        else return sdf.format( nullDate ).toString();
    }

    public String getCurrState()         { return this.currState; }
    public String getCurrStateExtra()    { return this.currStateExtra; }
    public String getCurrValue()         { return this.currValue; }
    public String getLastState()         { return this.lastState; }
    public String getLastValue()         { return this.lastValue; }
    public String getQuerySpec()         { return this.querySpec; }
    public long   getThresholdWarning()  { return this.thresholdWarning; }
    public long   getThresholdCritical() { return this.thresholdCritical; }
}
