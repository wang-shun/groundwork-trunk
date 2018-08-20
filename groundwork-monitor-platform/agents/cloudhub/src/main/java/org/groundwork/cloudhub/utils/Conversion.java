package org.groundwork.cloudhub.utils;


public class Conversion
{
    public static String scaleValue2String( String value, Double downScale )
    {
        if( value          == null )  return "0";
        if( value.length() < 2     )  return "0";

        double v;

        try { v = Double.parseDouble( value ); } catch ( Exception e ) { v = 0.0; };

        long vi = (int)( v / downScale + 0.49 );

        return Long.toString( vi );
    }

    public static String byte2KB( String value ) { return scaleValue2String( value, 1024.0 ); }
    public static String byte2MB( String value ) { return scaleValue2String( value, 1024.0 * 1024.0 ); }
    public static String byte2GB( String value ) { return scaleValue2String( value, 1024.0 * 1024.0 * 1024.0 ); }
    public static String byte2TB( String value ) { return scaleValue2String( value, 1024.0 * 1024.0 * 1024.0 * 1024.0 ); }
    public static String byte2PB( String value ) { return scaleValue2String( value, 1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0 ); }
}
