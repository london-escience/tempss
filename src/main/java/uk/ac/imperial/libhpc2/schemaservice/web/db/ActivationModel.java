package uk.ac.imperial.libhpc2.schemaservice.web.db;

import javax.validation.constraints.AssertTrue;

/**
 * A simple data model class (not stored to DB) that represents the activation
 * checkbox on an activaton form. This simplifies the process of displaying 
 * errors when the box is not checked and makes it more straightforward to 
 * extend the activation form later if required.
 * 
 * @author jhc02
 */
public class ActivationModel {
	
	@AssertTrue
	private boolean activated;

	public ActivationModel() {}
	
	public ActivationModel(boolean pActivated) {
		this.activated = pActivated;
	}
	
	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean pActivated) {
		this.activated = pActivated;
	}
	
	
}
