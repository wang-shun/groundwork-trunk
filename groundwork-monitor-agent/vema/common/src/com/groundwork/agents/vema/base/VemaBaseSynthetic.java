package com.groundwork.agents.vema.base;

import org.apache.log4j.Logger;

public class VemaBaseSynthetic
{
	private String  synHandle;
	private String  synLookup1;
	private double  synFactor1;
	private String  synLookup2;
	private boolean synFromTop;
	private boolean synToPercent;

    private static  Logger log = Logger.getLogger(VemaBaseMetric.class);

	public VemaBaseSynthetic(
        String  handle, 
        String  lookup1, 
        double  factor1, 
        String  lookup2, 
        boolean fromTop, 
        boolean toPercent )  
	{
		synHandle    = handle;
		synLookup1   = lookup1;
		synFactor1   = factor1;
		synLookup2   = lookup2;
		synFromTop   = fromTop;
		synToPercent = toPercent;
	}

    public int compute( String v1, String v2 )
    {
        double value1, value2;
        double x;

        try                  { value1 = Double.parseDouble( v1 ); }
        catch( Exception e ) { value1 = 0; }

        try                  { value2 = Double.parseDouble( v2 ); }
        catch( Exception e ) { value2 = 0; }

        x = ( value1 * synFactor1 );
        x = ( value2 == 0 ) ? 0 : x / value2;
        x = synFromTop   ? 1.0 - x : x;

        if( synToPercent )
        {
            x *= 100;
            x = max(0.0, min(100.0, x ));
            x = (double)((int)(x+0.49));
        }
        return (int)x;
    }

    private static final double max( double a, double b ) { return a > b ? a : b; }
    private static final double min( double a, double b ) { return a < b ? a : b; }

    public String  getHandle()  { return synHandle;    }
    public String  getLookup1() { return synLookup1;   }
    public String  getLookup2() { return synLookup2;   }
    public boolean isPercent()  { return synToPercent; }
    public boolean isFromTop()  { return synFromTop;   }
}
