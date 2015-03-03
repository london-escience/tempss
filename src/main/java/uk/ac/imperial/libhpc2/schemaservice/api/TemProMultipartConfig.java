package uk.ac.imperial.libhpc2.schemaservice.api;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/")
public class TemProMultipartConfig extends ResourceConfig {

	public TemProMultipartConfig() {
		super(MultiPartFeature.class);
	}
}
