package com.groundwork.agents.vema.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sun.misc.BASE64Encoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.log4j.Logger;

import com.groundwork.agents.vema.exception.VEMAException;


public class RESTbox
{
    private static final String GET    = "GET";
    private static final String PUT    = "PUT";
    private static final String DELETE = "DELETE";
    private static final String POST   = "POST";
    
	private static org.apache.log4j.Logger log = Logger.getLogger(RESTbox.class);
	
	private String              restHost;           // (e.g. "eng-rhev-m-1.groundwork.groundworkopensource.com")
	private String              restLogin;          // (e.g. "admin")
	private String              restRealm;          // (e.g. "internal")
	private String              restPassword;       // (e.g. "#m3t30r1t3")
    private String              restPort;           // (e.g. "443" or "8443" for HTTPS)
    private String              restProtocol;       // (e.g. "https")
    private String              restBaseNode;       // (e.g. "/api" most often)
    private String              keystorePassword;   // (commonly "changeit") for Auth Keystore JDK install
    private String              keystoreCertsPath;  // (commonly /usr/java/latest/jre/lib/security/cacerts)

    private boolean             isConnected      = false;

	public RESTbox( String host, String login, String pass, 
                    String realm, String port, String protocol, String basenode, 
                    String certspath, String keystorepass ) throws VEMAException
	{
		log.debug( "In RESTbox instantiator code" );
		
		if( ! basenode.startsWith( "/" ))
			basenode = "/" + basenode;
		
		this.restHost          = host;
		this.restLogin         = login;
        this.restRealm         = realm;
		this.restPassword      = pass == null ? "" : pass;
        this.restPort          = port;
        this.restProtocol      = protocol;
        this.restBaseNode      = basenode;
        this.keystorePassword  = keystorepass;
        this.keystoreCertsPath = certspath;
	}

	public String getHost()          { return this.restHost;          } 
	public String getLogin()         { return this.restLogin;         } 
	public String getRealm()         { return this.restRealm;         } 
	public String getPassword()      { return this.restPassword;      } 
    public String getPort()          { return this.restPort;          } 
    public String getProtocol()      { return this.restProtocol;      } 
    public String getBaseNode()      { return this.restBaseNode;      } 
    public String gettorePassword()  { return this.keystorePassword;  } 
    public String gettoreCertsPath() { return this.keystoreCertsPath; } 
    /*
     * isConnectionOK() returns boolean after testing connectivity
     * and throws exceptions when the result is false as to why
     * connectivity is bunged.
     * 
     * This could become quite complicated - testing a connection
     * - doing authentication, fail-thru to a non-connected state
     */

    public boolean isConnectionOK()
    {
        if( this.isConnected == false )
        {
            if( this.restHost          != null
            &&  this.restLogin         != null
            &&  this.restRealm         != null
            &&  this.restPassword      != null
            &&  this.restPort          != null
            &&  this.restProtocol      != null
            &&  this.restBaseNode      != null
            &&  this.keystorePassword  != null
            &&  this.keystoreCertsPath != null )
            {
                this.isConnected = true; // which the following might negate
                getXML( "" );
            }
            else
            {
            	this.isConnected = false; // redundant, but "safe" - and clearer...
            	
            	log.info( "Couldn't connect: " +
            	( this.restHost          == null ? "(null host) " : "" ) +
            	( this.restLogin         == null ? "(null login/user name) " : "" ) + 
                ( this.restRealm         == null ? "(null realm) " : "" ) + 
                ( this.restPassword      == null ? "(null password) " : "" ) + 
                ( this.restPort          == null ? "(null port) " : "" ) + 
                ( this.restProtocol      == null ? "(null protocol) " : "" ) + 
                ( this.restBaseNode      == null ? "(null base REST node) " : "" ) + 
                ( this.keystorePassword  == null ? "(null keystore password) " : "" ) + 
                ( this.keystoreCertsPath == null ? "(null keystore path) " : "" ) 
                );
            }
        }
        return this.isConnected;
    }
   
//    public static void testRest(String arguments[])
//    {
//    	String  nextArg = null;
//    	String  thisArg = null;
//		boolean verbose = false;
//		String  user    = null;
//		String  pass    = null;
//		String  method  = null;
//		URL     url     = null;
//
//    	for( int i = 0; i < arguments.length; i++ )
//    	{
//    		thisArg = arguments[ i ];
//    		nextArg = ( i + 1 < arguments.length ) 
//    				? arguments[ i + 1 ]
//    				: null;
//    				
//    		// this logic (below) "picks off" arguments one at a time, in order.
//    		
//    		if( thisArg.equals( "-v" ) ) { verbose = !verbose; continue; }
//    		if( user   == null )         { user    = thisArg;  continue; }
//    		if( pass   == null )         { pass    = thisArg;  continue; }
//    		if( method == null )         { method  = thisArg;  continue; }
//    		if( url    == null )
//    		{
//    			try
//    			{
//    				url = new URL( thisArg );
//    			}
//    			catch( Exception e )
//    			{
//    				log.error( "URL('" + thisArg + "') exception: " + e );
//    			}
//    			continue;
//    		}
//    		
//    		// and this section ONLY happens when all arguments ABOVE are scanned.
//	        try
//	        {
//	            if (GET.equalsIgnoreCase(method))
//	            {
//	                if (nextArg != null) 
//	                	usage("too many args at '" + nextArg + "'");
//
//	                request(verbose, GET, url, user, pass, null);
//	            }
//	            else if (PUT.equalsIgnoreCase(method))
//	            {
//	                if (nextArg == null) 
//	                	usage("not enough args");
//
//	                String file = nextArg;
//	                request(verbose, PUT, url, user, pass, new FileInputStream(new File(file)));
//	            }
//	            else if (POST.equalsIgnoreCase(method))
//	            {
//	                if (nextArg == null) 
//	                	usage("not enough args");
//
//	                String file = nextArg;
//	                request(verbose, POST, url, user, pass, new FileInputStream(new File(file)));
//	            }
//	            else if (DELETE.equalsIgnoreCase(method))
//	            {
//	                if (nextArg != null) 
//	                	usage("too many args at '" + nextArg + "'");
//
//	                request(verbose, DELETE, url, user, pass, null);
//	            }
//	            else
//	            {
//	                usage("Should never get here. (Method = '" + method + "')");
//	            }
//	        }
//	        catch (Exception x)
//	        {
//	            log.error(x);
//	            System.exit(1);
//	        }
//    	}
//    }
   
//    private static void request(
//        boolean verbose, String method, URL url, String username, 
//        String password, InputStream body) throws IOException
//    {
//        // sigh.  openConnection() doesn't actually open the connection,
//        // just gives you a URLConnection.  connect() will open the connection.
//        if (verbose)
//            log.info("[issuing request: " + method + " " + url + "]");
//
//        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
//        connection.setRequestMethod(method);
//       
//        // write auth header
//        BASE64Encoder    encoder = new BASE64Encoder();
//        String encodedCredential = encoder.encode( (username + ":" + password).getBytes() );
//        connection.setRequestProperty("Authorization", "BASIC " + encodedCredential);
//       
//        // write body if we're doing POST or PUT
//        byte buffer[] = new byte[8192];
//        int  read = 0;
//        if (body != null) 
//        {
//            connection.setDoOutput(true);
//           
//            OutputStream output = connection.getOutputStream();
//            while ((read = body.read(buffer)) != -1)
//            {
//                output.write(buffer, 0, read);
//            }
//        }
//       
//        // do request
//        long time = System.currentTimeMillis();
//        connection.connect();
//       
//        InputStream responseBodyStream = connection.getInputStream();
//        StringBuffer responseBody = new StringBuffer();
//        while ((read = responseBodyStream.read(buffer)) != -1)
//        {
//            responseBody.append(new String(buffer, 0, read));
//        }
//        connection.disconnect();
//        time = System.currentTimeMillis() - time;
//       
//        // start printing output
//        if (verbose)
//            log.info("[read " + responseBody.length() + " chars in " + time + "ms]");
//       
//        // look at headers
//        // the 0th header has a null key, and the value is the response line ("HTTP/1.1 200 OK" or whatever)
//        if (verbose)
//        {
//            String header      = null;
//            String headerValue = null;
//            int    index       = 0;
//            
//            while ((headerValue = connection.getHeaderField(index)) != null)
//            {
//                header = connection.getHeaderFieldKey(index);
//               
//                log.info(
//                    ((header != null) ? header + ": " : "" ) 
//                    + headerValue);
//               
//                index++;
//            }
//            log.info("");
//        }
//       
//        // dump body
//        log.info(responseBody);
//        // System.out.flush();
//    }
//    

	public String getXML( String node ) throws VEMAException
	{
        String xml = null;  // we can return NULL if didn't work.

		String username         = restLogin + "@" + restRealm;
		String password         = restPassword;
		String protocol         = restProtocol;
		String host             = restHost;
		String basenode         = restBaseNode;
		int    port             = Integer.parseInt( restPort );
        String keystoreLocation = keystoreCertsPath;
		String authorizationEncodingString = "Basic "
				+ new sun.misc.BASE64Encoder()
                    .encode( (username + ":" + password).getBytes() );
		URIBuilder uriBuilder = new URIBuilder();

		if( node == null )  // ensure node is a String.  
			node = "";

		if( !node.startsWith( "/" ) && node.length() > 0 )
			node = "/" + node;
		
		if( node.startsWith( restBaseNode ) )  // trim off base node, 
			node = node.substring( restBaseNode.length() );
		

		try
		{
			KeyStore clientKeyStore = KeyStore.getInstance( "JKS" );

			clientKeyStore.load( new FileInputStream( keystoreLocation ),
					keystorePassword.toCharArray() );

			// set up the socketfactory, to use our keystore for client
			// authentication.

			SSLSocketFactory socketFactory = new SSLSocketFactory(
					SSLSocketFactory.TLS,
					clientKeyStore,
					keystorePassword,
					null,
					null,
					null,
					(X509HostnameVerifier) SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );

			// create and configure scheme registry

			SchemeRegistry registry = new SchemeRegistry();
			registry.register( new Scheme( restProtocol, port, socketFactory ) );

			// create a client connection manager to use in creating httpclients

			BasicClientConnectionManager mgr = new BasicClientConnectionManager( registry );
			HttpClient httpClient = new DefaultHttpClient( mgr );

			// Build the URI
			uriBuilder.setScheme( protocol );
			uriBuilder.setHost( host );
			uriBuilder.setPort( port );
			uriBuilder.setPath( restBaseNode + node ); // and put it back!

			HttpGet httpget = new HttpGet( uriBuilder.build() );
			httpget.setHeader( "Authorization", authorizationEncodingString );

			HttpResponse response = httpClient.execute( httpget );
			
			this.isConnected =
					   ( response.getStatusLine().getStatusCode() >= 200 )
					&& ( response.getStatusLine().getStatusCode() <  300 );
			
			xml = httpEntityToString( response.getEntity() );  // must do, 'cuz once the stream is used up, its gone.
			log.debug( xml );
        }
		catch( Exception exc )
		{
			log.error( "\nstack trace: (exception)\n"
		            + "exc.getMessage          = '" + exc.getMessage() + "'\n"
		            + "exc.getLocalizedMessage = '" + exc.getLocalizedMessage() + "'\n"
		            + "exc.toString            = '" + exc.toString()   + "'\n"
		            + "---- key variables ----\n" 
		            + "node                    = '" + node             + "'\n"
		            + "basenode                = '" + basenode         + "'\n"
		            + "username                = '" + username         + "'\n"
		            + "password                = '" + password         + "'\n"
		            + "protocol                = '" + protocol         + "'\n"
		            + "hostname                = '" + host             + "'\n"
		            + "port                    = '" + port             + "'\n"
		            + "keystoreLocation        = '" + keystoreLocation + "'\n"
		            + "authorizationencoding   = '" + authorizationEncodingString + "'\n"
		            + "URI string              = '" + uriBuilder.toString() + "'\n"
		            + "---- stack ----\n" 
		            + exc.getStackTrace()[0] + "\n" 
		            + exc.getStackTrace()[1] + "\n" 
		            + exc.getStackTrace()[2] + "\n" 
		            + exc.getStackTrace()[3] + "\n" 
		            + exc.getStackTrace()[4] + "\n" 
		            + exc.getStackTrace()[5] + "\n" 
		            + exc.getStackTrace()[6] + "\n" 
		            + exc.getStackTrace()[7]
		            + "====\n" ); 

			exc.printStackTrace();
		}
        return xml;  // which can be (null)
	}
	
	private static String httpEntityToString( HttpEntity entity )
	{
		StringBuilder str = new StringBuilder();
		InputStream   is  = null;
		try
		{
			is = entity.getContent();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader( is ) );
			String line = null;
			while( (line = bufferedReader.readLine()) != null )
				str.append( line + "\n" );
		}
		catch( IOException e )
		{
			throw new RuntimeException( e );
		}
		finally
		{
			try
			{
				is.close();
			}
			catch( IOException e )
			{
				// tough luck...
				e.printStackTrace();
			}
		}
		return str.toString();
	}
	
	private String file2string( String file ) throws IOException 
    {
	    BufferedReader reader        = new BufferedReader( new FileReader (file) );
	    String         line          = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls            = System.getProperty("line.separator");

	    while( ( line = reader.readLine() ) != null ) 
        {
	        stringBuilder.append( line );
	        stringBuilder.append( ls );
	    }

	    return stringBuilder.toString();
	}
}
