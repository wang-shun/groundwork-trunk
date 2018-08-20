package org.itgroundwork.foundation.pagebeans;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.bs.plugin.PluginService;

import com.groundwork.collage.CollageFactory;
import com.icesoft.faces.component.inputfile.FileInfo;
import com.icesoft.faces.component.inputfile.InputFile;

public class PluginBean {

	private List<SelectItem> platforms;

	private CollageFactory factory = null;

	private PluginService pluginService = null;

	private MetadataService metaService = null;

	private Plugin plugin = null;

	private Integer selectedPlatform = null;

	private String[] selectedDependencies = {"None"};

	private List<Plugin> pluginList = new ArrayList<Plugin>();

	private List pluginItems = new ArrayList<Plugin>();

	// file upload completed percent (Progress)
	private int fileProgress;

	// files associated with the current user
	// private final List fileList = Collections.synchronizedList(new
	// ArrayList());

	// latest file uploaded by client
	private InputFileData currentFile;

	// File sizes used to generate formatted label
	public static final long MEGABYTE_LENGTH_BYTES = 1048000l;
	public static final long KILOBYTE_LENGTH_BYTES = 1024l;

	private Log log = LogFactory.getLog(this.getClass());
	
	private static final char[] HEX_CHARS = {'0', '1', '2', '3',
        '4', '5', '6', '7',
        '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f',};
	
	private String uploadDirectory = null;


	public PluginBean() {
		factory = CollageFactory.getInstance();
		pluginService = factory.getPluginService();
		metaService = factory.getMetadataService();
		plugin = new Plugin();
		pluginItems.add(new SelectItem("None", "None"));
		populatePlugins();

	}

	private void populatePlugins() {
		pluginList = new ArrayList<Plugin>();
		pluginItems = new ArrayList<Plugin>();
		pluginItems.add(new SelectItem("None", "None"));
		Collection<com.groundwork.collage.model.Plugin> plugins = pluginService
				.getAllPlugins();
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		for (com.groundwork.collage.model.Plugin hbPlugin : plugins) {
			Plugin uiPlugin = new Plugin();
			uiPlugin.setPluginId(hbPlugin.getPluginId());
			uiPlugin.setName(hbPlugin.getName());
            uiPlugin.setUrl(hbPlugin.getExternalUrl(request));
			PluginPlatform uiPlatform = new PluginPlatform();
			uiPlatform.setPlatformId(hbPlugin.getPluginPlatform()
					.getPlatformId());
			uiPlatform.setName(hbPlugin.getPluginPlatform().getName());
			uiPlatform.setDescription(hbPlugin.getPluginPlatform()
					.getDescription());
			uiPlatform.setArch(hbPlugin.getPluginPlatform().getArch());
			uiPlugin.setPluginPlatform(uiPlatform);
			uiPlugin.setLastUpdateTimestamp(hbPlugin.getLastUpdateTimestamp());
			uiPlugin.setDependencies(hbPlugin.getDependencies());
			pluginList.add(uiPlugin);
			pluginItems.add(new SelectItem(uiPlugin.getName(), uiPlugin
					.getName() + "   (" + uiPlugin.getPluginPlatform().getDescription() + ")"));
		}
	}

	/**
	 * <p>
	 * This method is bound to the inputFile component and is executed multiple
	 * times during the file upload process. Every call allows the user to finds
	 * out what percentage of the file has been uploaded. This progress
	 * information can then be used with a progressBar component for user
	 * feedback on the file upload progress.
	 * </p>
	 * 
	 * @param event
	 *            holds a InputFile object in its source which can be probed for
	 *            the file upload percentage complete.
	 */
	public void fileUploadProgress(EventObject event) {
		InputFile ifile = (InputFile) event.getSource();
		fileProgress = ifile.getFileInfo().getPercent();
	}

	/**
	 * <p>
	 * Action event method which is triggered when a user clicks on the upload
	 * file button. Uploaded files are added to a list so that user have the
	 * option to delete them programatically. Any errors that occurs during the
	 * file uploaded are added the messages output.
	 * </p>
	 * 
	 * @param event
	 *            jsf action event.
	 */
	public void uploadFile(ActionEvent event) {
		InputFile inputFile = (InputFile) event.getSource();
		com.groundwork.collage.model.PluginPlatform platform = metaService
		.getPlatformById(selectedPlatform);
		String platformName = platform.getName();
		int arch = platform.getArch();
		FileInfo fileInfo = inputFile.getFileInfo();
		if (fileInfo.getStatus() == FileInfo.SAVED) {
			// reference our newly updated file for display purposes and
			// added it to our history file list.
			currentFile = new InputFileData(fileInfo);
			String md5sum = null;
			try {
				md5sum = this.performChecksum(fileInfo.getFile());
				String pluginName = currentFile.getFile().getName();
				HttpServletRequest request = (HttpServletRequest) FacesContext
						.getCurrentInstance().getExternalContext().getRequest();
				String pluginUrl = request.getScheme() + "://" + request.getServerName()
						+ "/plugin_download/" + platformName.toLowerCase() + "-" + arch + "/"  + pluginName;
			
				pluginService.createPlugin(pluginName, pluginUrl, platform,
						stringArrToString(selectedDependencies, ","), md5sum,
						null);
				plugin = new Plugin();
				FacesMessage message = new FacesMessage();
				message.setDetail("Plugin Uploaded Successfully");
				message.setSummary("Success");
				message.setSeverity(FacesMessage.SEVERITY_INFO);
				FacesContext.getCurrentInstance()
						.addMessage("success", message);
				
			} catch (IOException fne) {
				log.error(fne.getMessage());
			}
			catch (NoSuchAlgorithmException nsae) {
				log.error(nsae.getMessage());
			} // end try/catch
		}
	}

	/**
	 * Helper to checksum
	 * 
	 * @param file
	 * @return
	 */
	private String performChecksum(File file) throws NoSuchAlgorithmException,
			FileNotFoundException {
		String md5 = null;
		MessageDigest digest = MessageDigest.getInstance("MD5");
		InputStream is = new FileInputStream(file);
		byte[] buffer = new byte[8192];
		int read = 0;
		try {
			while ((read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}
			byte[] md5sum = digest.digest();
			md5 = PluginBean.asHex(md5sum);

		} catch (IOException e) {
			throw new RuntimeException("Unable to process file for MD5", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				throw new RuntimeException(
						"Unable to close input stream for MD5 calculation", e);
			}
		}
		return md5;
	}

	/**
	 * Helper method to convert array to string
	 * 
	 * @param a
	 * @param separator
	 * @return
	 */
	private String stringArrToString(String[] a, String separator) {
		StringBuffer result = new StringBuffer();
		if (a.length > 0) {
			result.append(a[0]);
			for (int i = 1; i < a.length; i++) {
				result.append(separator);
				result.append(a[i]);
			}
		}
		return result.toString();
	}

	public List<SelectItem> getPlatforms() {
		if (platforms == null) {
			platforms = new ArrayList<SelectItem>();
			Collection<com.groundwork.collage.model.PluginPlatform> platformCol = pluginService
					.getPlatforms();
			Collection<PluginPlatform> uiPlatformObj = new ArrayList<PluginPlatform>();
			int count=0;
			for (com.groundwork.collage.model.PluginPlatform platform : platformCol) {
				PluginPlatform uiPlatform = new PluginPlatform();
				Integer platId = platform.getPlatformId();
				uiPlatform.setPlatformId(platId);
				String platformName = platform.getName();
				Integer arch = platform.getArch();
				uiPlatform.setName(platformName);
				uiPlatform.setDescription(platform.getDescription());
				uiPlatform.setArch(arch);
				uiPlatformObj.add(uiPlatform);
				if (count==0) {
					this.selectedPlatform = platId;
					this.uploadDirectory = "/usr/local/groundwork/apache2/htdocs/agents/plugin_download/" +platformName.toLowerCase() + "-" + arch ;
				}
				if (platform.getDescription() != null
						&& platform.getDescription().length() > 0)
					platforms.add(new SelectItem(uiPlatform.getPlatformId(),
							uiPlatform.getDescription()));
				count++;
			}
		}
		return platforms;
	}

	public String deletePlugin() {
		for (Plugin plugin : pluginList) {
			if (plugin.isSelected()) {
				int pluginID = plugin.getPluginId();
				pluginService.deletePlugin(pluginID);
			}
		}
		populatePlugins();
		return "pluginList";
	}

	public String editPlugin() {
		return "pluginList";
	}

	public List<Plugin> getPluginList() {
		return pluginList;
	}

	public void setPluginList(List<Plugin> pluginList) {
		this.pluginList = pluginList;
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public void setPlugin(Plugin plugin) {
		this.plugin = plugin;
	}

	public InputFileData getCurrentFile() {
		return currentFile;
	}

	public int getFileProgress() {
		return fileProgress;
	}

	/**
	 * Value change listen called when the new platform is selected..
	 * 
	 * @param event
	 *            jsf value change event
	 */
	public void platformChanged(ValueChangeEvent event) {
		this.selectedPlatform = (Integer) event.getNewValue();
		com.groundwork.collage.model.PluginPlatform platform = metaService
		.getPlatformById(selectedPlatform);
		String platformName = platform.getName();
		int arch = platform.getArch();
		if (platformName != null && arch >= 32)
		this.uploadDirectory ="/usr/local/groundwork/apache2/htdocs/agents/plugin_download/" +platformName.toLowerCase() + "-" + arch ;
	}

	public Integer getSelectedPlatform() {
		return selectedPlatform;
	}

	public void setSelectedPlatform(Integer selectedPlatform) {
		this.selectedPlatform = selectedPlatform;
	}

	public List getPluginItems() {
		return pluginItems;
	}

	public void setPluginItems(List pluginItems) {
		this.pluginItems = pluginItems;
	}

	public String[] getSelectedDependencies() {
		return selectedDependencies;
	}

	public void setSelectedDependencies(String[] selectedDependencies) {
		this.selectedDependencies = selectedDependencies;
	}
	
	 /**
     * Turns array of bytes into string representing each byte as 
     * unsigned hex number. 
     * 
     * @param hash Array of bytes to convert to hex-string
     * @return Generated hex string
     */
    public static String asHex (byte hash[]) {
        char buf[] = new char[hash.length * 2];
        for (int i = 0, x = 0; i < hash.length; i++) {
            buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf]; 
            buf[x++] = HEX_CHARS[hash[i] & 0xf]; 
        }
        return new String(buf);
    }

	public String getUploadDirectory() {
		return uploadDirectory;
	}

	public void setUploadDirectory(String uploadDirectory) {
		this.uploadDirectory = uploadDirectory;
	}


}
