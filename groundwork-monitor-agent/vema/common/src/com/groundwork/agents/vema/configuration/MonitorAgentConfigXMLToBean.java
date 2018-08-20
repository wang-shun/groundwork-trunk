package com.groundwork.agents.vema.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.log4j.Logger;
import org.itgroundwork.foundation.joxbeans.VemaMonitoring;

import com.groundwork.agents.vema.api.VemaConstants;
import com.groundwork.agents.vema.utils.SharedSecretProtector;
import com.wutka.jox.JOXBeanInputStream;

/**
 * This class get the Configuration Information from the Config files for GWOS
 * and Vema These methods returns Java Beans from the XML Configuration.
 * 
 * 
 * @author rvardhineedi
 * 
 */

public class MonitorAgentConfigXMLToBean
{
	private static org.apache.log4j.Logger	log	= Logger.getLogger( MonitorAgentConfigXMLToBean.class );

	/**
	 * Generates VEMAGwosConfiguration Bean from the XML configuration
	 * 
	 * @return VEMAGwosConfiguration object or null if no xml file is found
	 */

	public static VEMAGwosConfiguration gwosConfigXMLToBean( String filename )
	{
		log.debug( "Gwos Config XML to Bean" );
		String vemaFileName = VemaConstants.CONFIG_FILE_PATH 
				            + filename
				            + VemaConstants.CONFIG_FILE_EXTN;
		
		String origFileName = VemaConstants.CONFIG_FILE_PATH
				            + VemaConstants.VEMA_CONFIG_FILE
				            + VemaConstants.CONFIG_FILE_EXTN;
		
		VEMAGwosConfiguration config = null;
		FileInputStream in = null;

		log.debug( "\n"
		+ "ORIG Config File (name='" + origFileName + "')" + "\n"
		+ "READ Config File (name='" + vemaFileName + "')" );
		try
		{
			File orig = new File( origFileName );
			if(orig.exists())
			{
				orig.renameTo( new File( vemaFileName ));
			}
			
			in = new FileInputStream( vemaFileName );

			JOXBeanInputStream joxIn = new JOXBeanInputStream( in );
			config = (VEMAGwosConfiguration) joxIn
					.readObject( VEMAGwosConfiguration.class );

			// Just decrypt before using it
			String encryptedPassword = config.getVirtualEnvPassword();
			config.setVirtualEnvPassword( 
					SharedSecretProtector.decrypt( encryptedPassword ) );

			if (log.isDebugEnabled())
				log.debug( "CONFIG decodes to...\n" + config.formatSelf() );

			in.close();
		}
		catch( FileNotFoundException e )
		{
			log.error( "File Not Found " + e.getMessage() );
		}
		catch( IOException e )
		{
			log.error( "IO Exception " + e.getMessage() );
		}
		catch( GeneralSecurityException e )
		{
			log.error( "General Security Exception " + e.getMessage() );
		}
		catch( Exception e )
		{
			log.error( "Exception " + e.getMessage() );
		}

		return config;
	}

	/**
	 * Converts vema XML to bean
	 * 
	 * @return
	 */
	public static VemaMonitoring vemaXMLToBean( String filename )
	{
		String vemaFileName = VemaConstants.CONFIG_FILE_PATH + filename
				+ VemaConstants.CONFIG_FILE_EXTN;
		VemaMonitoring vemaBean = null;
		FileInputStream in = null;
		try
		{
			in = new FileInputStream( vemaFileName );
			JOXBeanInputStream joxIn = new JOXBeanInputStream( in );
			vemaBean = (VemaMonitoring) joxIn.readObject( VemaMonitoring.class );
		}
		catch( FileNotFoundException e )
		{
			log.error( e.getMessage() + "\n" + e.getStackTrace()[0].toString()
					+ "\n" + e.getStackTrace()[1].toString() + "\n"
					+ e.getStackTrace()[2].toString() + "\n"
					+ e.getStackTrace()[3].toString() + "\n"
					+ e.getStackTrace()[4].toString() + "\n"
					+ e.getStackTrace()[5].toString() + "\n"
					+ e.getStackTrace()[6].toString() + "\n"
					+ e.getStackTrace()[7].toString() );
		}
		catch( IOException e )
		{
			log.error( e.getMessage() + "\n" + e.getStackTrace()[0].toString()
					+ "\n" + e.getStackTrace()[1].toString() + "\n"
					+ e.getStackTrace()[2].toString() + "\n"
					+ e.getStackTrace()[3].toString() + "\n"
					+ e.getStackTrace()[4].toString() + "\n"
					+ e.getStackTrace()[5].toString() + "\n"
					+ e.getStackTrace()[6].toString() + "\n"
					+ e.getStackTrace()[7].toString() );
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch( IOException e )
				{
					log.error( e.getMessage() + "\n"
							+ e.getStackTrace()[0].toString() + "\n"
							+ e.getStackTrace()[1].toString() + "\n"
							+ e.getStackTrace()[2].toString() + "\n"
							+ e.getStackTrace()[3].toString() + "\n"
							+ e.getStackTrace()[4].toString() + "\n"
							+ e.getStackTrace()[5].toString() + "\n"
							+ e.getStackTrace()[6].toString() + "\n"
							+ e.getStackTrace()[7].toString() );
				} // end try/catch
			} // end if
		}
		return vemaBean;
	}

}
