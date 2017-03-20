package uk.ac.imperial.libhpc2.schemaservice.web;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.spring4.extension.SpringExtension;

/**
 * Factory class for generating a PebbleEngine. Used in spring configuration to 
 * generate pebble engine bean. Based on the example pebble/spring configuration at:
 * https://github.com/PebbleTemplates/pebble-example-spring/blob/master/xml-config
 *
 */
public class PebbleEngineFactory {

	public static PebbleEngine getEngine(Loader<?> pLoader, SpringExtension pExtension) {
		return new PebbleEngine.Builder().loader(pLoader).extension(pExtension).build();
	}	

}
