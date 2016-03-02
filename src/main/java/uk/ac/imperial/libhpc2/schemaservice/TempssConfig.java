package uk.ac.imperial.libhpc2.schemaservice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This singleton class represents an instance of a TemPSS confguration loaded 
 * from a tempss.conf config file that is placed in either /etc or ~/.libhpc
 * A tempss.conf file placed in ~/.libhpc takes precedence over one placed in
 * /etc.
 * @author jhc02
 *
 */
public class TempssConfig {

	private static TempssConfig _instance = null;
	
	// A list of patterns for templates to ignore
	private List<String> _ignorePatterns = new ArrayList<String>();
	
	private static final Logger sLog = 
			LoggerFactory.getLogger(TempssConfig.class.getName());
	
	public static TempssConfig getInstance() {
		if(_instance == null) {
			_instance = new TempssConfig();
		}
		return _instance;
	}
	
	/**
	 * The constructor looks for and loads the 
	 */
	protected TempssConfig() {
		// See if we have a ~/.libhpc/tempss.conf config file.
		File userHome = new File(System.getProperty("user.home"));
		File libhpcDir = new File(userHome, ".libhpc");
		File configFileObj = new File(libhpcDir, "tempss.conf");
		if(configFileObj.exists()) {
			sLog.debug("Found a tempss configuration file at " +
					"~/.libhpc/tempss.conf");
		}
		else {
			// Try /etc/tempss.conf
			configFileObj = new File("/etc/tempss.conf");
			if(configFileObj.exists()) {
				sLog.debug("Found a tempss configuration file at " +
					"/etc/tempss.conf");
			}
			else {
				sLog.debug("No tempss configuration file found...");
				return;
			}
		}
		// Now read the ini-style configuration file
		// Now read the ini-style configuration file
		try {
			Wini configFile = new Wini(configFileObj);
			// If we have a template ignore section, process it
			if(configFile.keySet().contains("template-ignore")) {
				for(String ignorePattern : configFile.get("template-ignore").keySet()) {
					sLog.debug("Got template ignore pattern: " + ignorePattern);
					_ignorePatterns.add(ignorePattern);
				}
			}
		} catch (InvalidFileFormatException e) {
			sLog.error("The format of the tempss.conf ini configuration file" +
					"is invalid: {}", e.getMessage());
		} catch (IOException e) {
			sLog.error("Error reading the tempss.conf ini configuration " +
					"file: {}", e.getMessage());		
		}
    }
	
	public List<String> getIgnorePatterns() {
		return _ignorePatterns;
	}
}