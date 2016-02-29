/**
 * JavaScript functions for the tempss template manager sample
 * application distributed along with the tempss web service. 
 */


// This function is called via a setTimeout call when the selected
// template is changed.
function templateChanged(selectedValue, selectedText) {
	displayTemplate(selectedValue, selectedText);
	updateProfileList(selectedValue);
}

/**
 * Used by the TemPSS web interface to update its list of
 * available templates. To access raw template metadata from 
 * the TemPSS service, see getTemplateMetadata 
 */
function updateTemplateList() {
	$('#template-loading').show();
	
	getTemplateMetadata(null, null, 
			function(data) {
				// Remove current content excluding the placeholder
		    	$('#template-select option:gt(0)').remove();
		    	var templateSelect = $('#template-select');
		    	
		    	// Set up a sort function and sort the list of components
		    	// based on ID
		    	var components = data.components;
		    	components.sort(function(item1, item2) {
		    		return item1.name.localeCompare(item2.name);
		    	});
		    	components.sort();
		    	for(var i = 0; i < components.length; i++) {
		    		var item = components[i];
		    		templateSelect.append("<option value=\"" + item.id + "\">" + item.name + " (id: " + item.id + ")</option>");
		    	}
		        $("#template-loading").hide(0);
			},
			function(data) {
				$("#template-loading").hide(0);
			}
	);
}

/**
 * Get the template metadata from the TemPSS service. Success
 * and failure callbacks should be provided to handle the 
 * returned data or any error. The callbacks accept a data
 * parameter that will contain a JSON object containing either
 * the metadata, also in JSON format, or error info.
 * 
 * @param host The host of a remote tempss instance or null to use the current local instance
 * @param port The port of a remote tempss instance or null to use the current local instance
 * @param successCallback A function that accepts a data parameter to receive the service's response
 * @param errorCallback A function that accepts a data parameter to receive an error response
 */
function getTemplateMetadata(host, port, successCallback, errorCallback) {
	var url = '';
	if(host != null) {
		url = 'htp://' + host;
		if(port != null) {
			url += ':' + port;
		}
	}
	url += '/tempss/api/template';
	
	$.ajax({
        method:   'get',
        url:      url,
        dataType: 'json',
        success:  successCallback,
        error: errorCallback,
	});
}

// Display the tree for the template with the specified ID in the
// template panel.
function displayTemplate(templateId, templateText) {
	log("About to display tree for template with ID <" + templateId + "> and text <" + templateText + ">");
	
	if(templateId == "NONE") {
		disableProfileButtons(true);
		hideTreeExpandCollapseButtons(true);
		$("#template-container").html("<h6 class=\"infotext\">No template selected. Please select a template from the drop-down list above.</h6>");
		return;
	} 
	
	$("#template-tree-loading").show();
	$.ajax({
        method:   'get',
        url:      '/tempss/api/template/id/' + templateId,
        success:  function(data){
        	// Data that comes back is the raw HTML to place into the page
        	$("#template-container").html(data)
        	// Add the javascript handlers to this HTML to enable
        	// clicking of the nodes, etc.
        	attachClickHandlers(); // Currently this function resides in bootstrap-tree.js.
        	attachChangeHandlers(); // Part of tempss manager js
        	collapseTree();
        	setLeavesWithoutInputsToValid();

            // Add an HTML5 data tag to the tree's root UL node
            // to say which remote tree this schema is for. When
            // future clicks of the edit button are made, we can use
            // this to check if we need to load a new schema
            var treeRoot = $('#template-container ul[role="tree"]');
            /*
            if(treeRoot) {
                    treeRoot.data('schema',$('#id_profile').find(':selected').data('name'));
            }
            */

            // Enable the profile buttons for saving/clearing template content
            // and show the expand/collapse buttons
            disableProfileButtons(false);
            hideTreeExpandCollapseButtons(false);
        	
            setEditingProfileName("");
            $("#template-tree-loading").hide(0);
        },
        error: function() {
            $("#template-tree-loading").hide(0);
        }
    });
}

function getTemplateHtml(host, port, templateId, templateText, successCallback, errorCallback) {
	var url = '';
	if(host != null) {
		url = 'htp://' + host;
		if(port != null) {
			url += ':' + port;
		}
	}
	url += '/tempss/api/template/id/' + templateId,
	
	$.ajax({
        method:   'get',
        url:      url,
        success:  successCallback,
        error: errorCallback
    });
}

// Update the contents of the list of profiles
// If a template is selected, display the relevant profiles
// or a message saying none are available. If no template
// is selected then display a message to select template.
function updateProfileList(templateId) {
	
	// If the placeholder has been selected
	if(templateId == "NONE") {
		$('#profiles').html('<h6 class="infotext">Profiles for the currently selected template will appear here.</h6>');
		return;
	}
	
	// Now do a database lookup for profile names for this template.
	$("#profiles-loading").show();
	$.ajax({
        method:   'get',
        url:      '/tempss/api/profile/' + templateId + '/names',
        dataType: 'json',
        success:  function(data){
        	log('Profile name data received from server: ' + data.profile_names);
        	if(data.profile_names.length > 0) {
	        	var htmlString = "";
	        	for(var i = 0; i < data.profile_names.length; i++) {
	        		htmlString += '<div class="profile-item"><a class="profile-link" href="#"' +  
	        			'data-pid="'+ data.profile_names[i] + '">' + data.profile_names[i] +
	        			'</a><div style="float: right;">' +
	        			'<span class="glyphicon glyphicon-remove-sign delete-profile" aria-hidden="true" data-toggle="tooltip" data-placement="top" title="Delete profile"></span>' +
	        			'<span class="glyphicon glyphicon-floppy-save load-profile" aria-hidden="true" data-toggle="tooltip" data-placement="top" title="Load profile into template"></span>' +
	        			'</div></div>\n';
	        	}
	        	$('#profiles').html(htmlString);
	        	$('.profile-item span[data-toggle="tooltip"]').tooltip();
        	}
        	else {
        		// If no profiles are available
        		$('#profiles').html('<h6 class="infotext">There are no profiles registered for the "' + templateId + '" template.</h6>');
        	}
            $("#profiles-loading").hide(0);
        },
        error: function() {
            $("#profiles-loading").hide(0);
            $('#profiles').html('<h6 class="infotext">Unable to get profiles for the "' + templateId + '" template.</h6>');
        }
    });
}

// Disable the buttons used for saving a profile or 
// clearing profile content. These should only be enabled
// when a template is selected.
function disableProfileButtons(disable) {
	if(disable) {
		$('#clear-profile-btn').prop('disabled', true);
		$('#save-as-profile-btn').prop('disabled', true);	
	}
	else {
		$('#clear-profile-btn').removeProp('disabled');
		$('#save-as-profile-btn').removeProp('disabled');
	}
	
}

// Hide the buttons used for expanding or collapsing a  
// template tree shown in the profile editor.
function hideTreeExpandCollapseButtons(hide) {
	if(hide) {
		$('#tree-expand').hide();
		$('#tree-collapse').hide();	
	}
	else {
		$('#tree-expand').show();
		$('#tree-collapse').show();
	}
	
}

// Disable/enable the button used for application 
// input when there is not a valid profile loaded
function disableGenerateInputButton(disable) {
	if(disable) {
		$('#generate-input-file-btn').prop('disabled', true);
	}
	else {
		$('#generate-input-file-btn').removeProp('disabled');
	}
	
}

// Given a profile name entered by the user into a modal
// pop up, save the profile, relating to the specified
// template.
function saveProfile(templateId, profileName) {
	log('Request to save profile <' + profileName + '> for template <' + templateId + '>.');
	var profileData = getProfileXml($("ul[role='tree']"));
	var profileObject = {profile:profileData};
	$("#profile-saving").show();
	// Clear any existing error text
	$('#profile-save-errors').html("");
	$.ajax({
        method:   'POST',
        url:      '/tempss/api/profile/' + templateId + '/' + profileName,
        dataType: 'json',
        contentType: 'application/json',
        data:     JSON.stringify(profileObject),
        success:  function(data){
        	// Check if save succeeded
        	if(data.status == 'OK') {
        		// Close the modal and update the profile list since 
        		//save completed successfully.
        		$('#save-profile-modal').modal('hide');
        		updateProfileList(templateId);
        	}
        	else {
        		$('#profile-save-errors').html("<h6>An unknown error has occurred while trying to save the profile.</h6>");
        	}
        	$("#profile-saving").hide();
        },
        error: function(data) {
        	var result = $.parseJSON(data.responseText);
        	if(result.status == 'ERROR'){
        		// Some error occurred, show the error message in the modal
        		var errorText = "";
        		switch(result.code) {
        		case 'INVALID_TEMPLATE':
        			errorText = "An invalid template identifier has been specified.";
        			break;
        		case 'PROFILE_NAME_EXISTS':
        			errorText = "The specified profile name already exists.";
        			break;
        		case 'REQUEST_DATA':
        			errorText = "The JSON request data provided is invalid.";
        			break;
        		case 'RESPONSE_DATA':
        			errorText = "Unable to prepare JSON response data. Profile may have saved successfully";
        			break;
        		default:
        			errorText = "An unknown error has occurred.";
        		}
        		$('#profile-save-errors').html("<h6>Unable to save profile: " + errorText + "</h6>");
        	}
    		else {
        		$('#profile-save-errors').html("<h6>An unknown error has occurred while trying to save the profile.</h6>");
        	}
        	$("#profile-saving").hide();
        }
    });
}

// Load the specified profile into the currently displayed template.
function loadProfile(templateId, profileId) {
	log("Request to load profile <" + profileId + "> for template <" + templateId + ">");
	$("#template-profile-loading").show();
	$.ajax({
        method:   'GET',
        url:      '/tempss/api/profile/' + templateId + '/' + profileId,
        dataType: 'json',
        success:  function(data) {
        	// Check if save succeeded
        	if(data.status == 'OK') {
        		// Extract the profile data and load it into
        		// the template
        		var profileXml = data.profile;
        		loadLibhpcProfile(profileXml, 'ul[role=tree]');
        		// Now add valid/invalid listeners to the root node
        		// to enable the save button when the whole tree is valid
        		// and disable when it is invalidated.
        		$('ul[role=tree]').on('nodeValid', function() {
        			disableGenerateInputButton(false);
        		});
        		$('ul[role=tree]').on('nodeInvalid', function() {
        			disableGenerateInputButton(true);
        		});
        		// TODO: If the root node is already valid on load, we
        		// need to fire the nodeValid event now since it won't
        		// be triggered otherwise. Should we add listeners before
        		// loading profile or will this result in a performance
        		// issue? Since we're interested only in the root node
        		// this is considered ok for now...
        		if($('ul[role=tree]').hasClass('valid')) {
        			$('ul[role=tree]').trigger('nodeValid', $('ul[role=tree]'));
        		}
        	}
        	else {
        		//$('#profile-delete-errors').html("<h6>An unknown error has occurred while trying to delete the profile.</h6>");
        	}
        	setEditingProfileName(profileId);
        	$("#template-profile-loading").hide();
        	
        },
        error: function(data) {
        	var result = $.parseJSON(data.responseText);
        	if(result.status == 'ERROR') {
        		// An error occurred, show the error message in the modal
        		var errorText = "";
        		switch(result.code) {
        		case 'INVALID_TEMPLATE':
        			errorText = "An invalid template identifier has been specified.";
        			break;
        		case 'PROFILE_DOES_NOT_EXIST':
        			errorText = "The specified profile does not exist.";
        			break;
        		case 'RESPONSE_DATA':
        			errorText = "Profile load failed, unable to prepare JSON response data.";
        			break;
        		default:
        			errorText = "An unknown error has occurred while loading profile.";
        		}
        		//$('#profile-delete-errors').html("<h6>Unable to delete profile: " + errorText + "</h6>");
        	}
    		else {
        		//$('#profile-delete-errors').html("<h6>An unknown error has occurred while trying to delete the profile.</h6>");
        	}
        	$("#template-profile-loading").hide();
        }
	});
	
}

// Delete the specified profile and then update the profile list
function deleteProfile(templateId, profileId) {
	log("Request to delete profile <" + profileId + "> for template <" + templateId + ">");
	$("#profile-deleting").show();
	// Clear any existing error text
	$('#profile-delete-errors').html("");
	$.ajax({
        method:   'DELETE',
        url:      '/tempss/api/profile/' + templateId + '/' + profileId,
        dataType: 'json',
        success:  function(data) {
        	// Check if save succeeded
        	if(data.status == 'OK') {
        		// Close the modal and update the profile list since 
        		//save completed successfully.
        		$('#delete-profile-modal').modal('hide');
        		$('#delete-confirm-text').html("");
        		updateProfileList(templateId);
        	}
        	else {
        		$('#profile-delete-errors').html("<h6>An unknown error has occurred while trying to delete the profile.</h6>");
        	}
        	$("#profile-deleting").hide();
        },
        error: function(data) {
        	var result = $.parseJSON(data.responseText);
        	if(result.status == 'ERROR') {
        		// An error occurred, show the error message in the modal
        		var errorText = "";
        		switch(result.code) {
        		case 'INVALID_TEMPLATE':
        			errorText = "An invalid template identifier has been specified.";
        			break;
        		case 'PROFILE_DOES_NOT_EXIST':
        			errorText = "The specified profile does not exist.";
        			break;
        		case 'PROFILE_NOT_DELETED':
        			errorText = "The specified profile could not be deleted.";
        			break;
        		case 'MULTIPLE_PROFILES_DELETED':
        			errorText = "ERROR: Multiple profiles deleted.";
        			break;
        		case 'RESPONSE_DATA':
        			errorText = "Unable to prepare JSON response data. Profile may have been successfully deleted";
        			break;
        		default:
        			errorText = "An unknown error has occurred.";
        		}
        		$('#profile-delete-errors').html("<h6>Unable to delete profile: " + errorText + "</h6>");
        	}
    		else {
        		$('#profile-delete-errors').html("<h6>An unknown error has occurred while trying to delete the profile.</h6>");
        	}
        	$("#profile-deleting").hide();
        }
	});
	
}

// Clears any profile content entered into the template and
// returns it to the original blank template.
function clearProfileContentInTemplate() {
	// For now, we re-load the blank template from the service rather
	// than clearing values on the client side.
	var templateId = $('input[name=componentname]').val();
	displayTemplate(templateId, 'REFRESH');
}

// Process the job profile currently displayed in the template,
// running it through the profile XSLT that transforms the 
// profile into a job input file.
function tempssProcessProfile() {
	var treeRootNode = $("ul[role='tree']").children("li");
	var templateId = $('input[name=componentname]').val();
	processJobProfile(treeRootNode, templateId);
}

// Function to attach click handlers to all the clickable nodes
// in a template tree. This ensures that if a user has opened up
// nodes in a tree, they are prompted before browsing to another
// tree in case there are changes that will be lost.
// TODO: Modify this so that we can detect if something has actually
// been altered rather than just checking if a node has been expanded.
function attachChangeHandlers() {
	$('#template-container .parent_li > span').on('click', function(e) {
		if(templateEdited == false) {
			templateEdited = true;
		}
	});
}

function setEditingProfileName(profileName) {
	if(profileName == "NONE") {
		profileName = "";
	}
	$('#editing-profile-name').text(profileName);
}

// Utility function for displaying log messages
function log(message) {
	if(console && console.log) {
		console.log(message);
	}
}
