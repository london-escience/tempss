/*
 * JavaScript functions for the tempss template manager sample
 * application distributed along with the tempss web service.
 */

/**
 * Variable to check whether a template has been edited.
 */
window.templateEdited = false;

/**
 * The name of the libhpc parameter tree plugin.
 */
window.treePluginName = 'plugin_LibhpcParameterTree';

/**
 * The HTML root element of the tree.
 */
window.treeRoot = null;
window.$templateContainer = $('#template-container');

/**
 * This function is called via a setTimeout call when the selected
 * template is changed.
 */
function templateChanged(selectedValue, selectedText) {
    log("Template has changed");

    // Destroy plugin
    if ($templateContainer.data(treePluginName) !== undefined) {
        removeChangeHandlers();
        $templateContainer.data(treePluginName).destroy();
    }
    // Display the new template
    displayTemplate(selectedValue, selectedText);
    // Update the profile list to match the selected template
    updateProfileList(selectedValue);
    window.templateEdited = false
    window.profileLoaded = false
}

/**
 * Used by the TemPSS web interface to update its list of
 * available templates.
 *
 * @return promise which is resolved when list is returned.
 */
function updateTemplateList() {
    $('#template-loading').show();

    return getTemplateMetadata(null, null)
        .then(
            // Success callback function:
            function(data) {
                // Remove current content excluding the placeholder
                $('#template-select option:gt(0)').remove();
                var templateSelect = $('#template-select');
                var components = data.components;
                components.sort(function(a, b) {
                    if (a.name.toLowerCase() < b.name.toLowerCase()) {
                        return -1;
                    }
                    if (a.name.toLowerCase() > b.name.toLowerCase()) {
                        return 1;
                    }
                });
                for (var i = 0; i < components.length; i++) {
                    var item = components[i];
                    templateSelect.append("<option value=\"" + item.id + "\">" + item.id + " - " + item.name + "</option>");
                }
                $("#template-loading").hide(0);
            },
            // Error callback function:
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
	
	var ajaxCall = $.ajax({
        method:   'get',
        url:      url,
        dataType: 'json',
        success:  successCallback,
        error: errorCallback,
	});
	
	return ajaxCall;
}

/**
 * Display the tree for the template with the specified ID in the
 * template panel.
 */
function displayTemplate(templateID, templateText) {
    log("About to display tree for template with ID <" + templateID + "> and text <" + templateText + ">");

    if (templateID == "NONE") {
        disableProfileButtons(true);
        disableGenerateInputButton(true);
        hideTreeExpandCollapseButtons(true);
        $templateContainer.html("<h6 class=\"infotext\">No template selected. Please select a template from the drop-down list above.</h6>");
        return;
    }

    $("#template-tree-loading").show();

    var templateHTMLCall = $.ajax({
        method:   'get',
        url:      '/tempss/api/template/id/' + templateID,
        headers: { 
            Accept : "application/json",
        }
    });
    
    var dfd = $.Deferred();
    templateHTMLCall.then(
        // success callback function
        function(data) {
            log('Got HTML tree from server');
            // Data that comes back is the raw HTML to place into the page
            $templateContainer.html(data.TreeHtml);
            // Instantiate the tree plugin on the tree
            $templateContainer.LibhpcParameterTree();

            treeRoot = $('#template-container ul[role="tree"]');

            // Enable the profile buttons for saving/clearing template content
            // and show the expand/collapse buttons
            if(data.authenticated) {
            	disableProfileButtons(false);
            }
            else {
            	disableProfileButtons(true);
            }
            hideTreeExpandCollapseButtons(false);
            // Add click/change handlers
            attachChangeHandlers();
            setEditingProfileName("");

            $("#template-tree-loading").hide(0);
            dfd.resolve();
        },
        // Error callback function
        function(data) {
            log('Error getting HTML tree: ' + JSON.stringify(data));
            $("#template-tree-loading").hide(0);
            dfd.reject();
        }
    );
    return dfd.promise();
}

/**
 * Update the contents of the list of profiles.
 * If a template is selected, display the relevant profiles
 * or a message saying none are available. If no template
 * is selected then display a message to select template.
 */
function updateProfileList(templateID) {

    // If the placeholder has been selected
    if(templateID == "NONE") {
        $('#profiles').html('<h6 class="infotext">Profiles for the currently selected template will appear here.</h6>');
        return;
    }

    // Now do a database lookup for profile names for this template.
    $("#profiles-loading").show();
    var profileListCall = $.ajax({
        method:   'get',
        url:      '/tempss/api/profile/' + templateID + '/names',
        dataType: 'json'
    });
    profileListCall.then(
        // Success callback:
        function (data) {
           log('Profile name data received from server: ' + data.profile_names);
           if(data.profile_names.length > 0) {
                   var htmlString = "";
                   for(var i = 0; i < data.profile_names.length; i++) {
                           var profileVisibilityIcon = "";
                           if(data.profile_names[i].public == true) {
                                   profileVisibilityIcon += '<span class="profile-type glyphicon glyphicon-globe text-success no-pointer" data-toggle="tooltip" data-placement="left" title="Public profile"></span>';
                           }
                           else {
                                   profileVisibilityIcon += '<span class="profile-type glyphicon glyphicon-lock text-danger no-pointer" data-toggle="tooltip" data-placement="left" title="Private profile"></span>';
                           }
                           htmlString += '<div class="profile-item">' + 
                               profileVisibilityIcon +
                               '<a class="profile-link" href="#"' +  
                                   'data-pid="'+ data.profile_names[i].name + '">' + data.profile_names[i].name +
                                   '</a><div style="float: right;">';
                                   if(data.profile_names[i].owner) {
                                           htmlString += '<span class="glyphicon glyphicon-remove-sign delete-profile" aria-hidden="true" data-toggle="tooltip" data-placement="top" title="Delete profile"></span>';
                                   }
                                   else {
                                           htmlString += '<span></span>';
                                   }
                                   
                                   htmlString += '<span class="glyphicon glyphicon-floppy-save load-profile" aria-hidden="true" data-toggle="tooltip" data-placement="top" title="Load profile into template"></span>\n';
                                   
                                   if(data.profile_names[i].owner) {
                                           var newStateStr = (data.profile_names[i].public == true) ? "private" : "public";
                                           var stateStr = (data.profile_names[i].public == true) ? "public" : "private";
                                           htmlString += '<span class="dropdown">' +
                                                   '<a class="dropdown-toggle dropdown-link" id="dropdown-' + i + '" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true"><span class="caret"></span></a>' +
                                                   '<ul class="dropdown-menu" aria-labelledby="dropdown-' + i + '">' +
                                                   '<li><a class="change-state" data-state="' + stateStr + '" href="#">Make profile ' + newStateStr + '</a></li>' +
                                                   '</ul></span>';
                                   }
                                   
                                   htmlString += '</div></div>';
                   }
                   $('#profiles').html(htmlString);
                   $('.profile-item span[data-toggle="tooltip"]').tooltip();
           }
           else {
                   // If no profiles are available
                   $('#profiles').html('<h6 class="infotext">There are no profiles registered for the "' + templateID + '" template.</h6>');
           }
           $("#profiles-loading").hide(0);
        },
        // Error callback
        function(data) {
            $("#profiles-loading").hide(0);
            $('#profiles').html('<h6 class="infotext">Unable to get profiles for the "' + templateID + '" template.</h6>');
        }
    );
}

/**
 * Disable the buttons used for saving a profile or
 * clearing profile content. These should only be enabled
 * when a template is selected.
 *
 * @param disable disable or enable buttons
 */
function disableProfileButtons(disable) {
    if (disable) {
        $('#clear-profile-btn').prop('disabled', true);
        $('#save-as-profile-btn').prop('disabled', true);
    } else {
        $('#clear-profile-btn').removeProp('disabled');
        $('#save-as-profile-btn').removeProp('disabled');
    }
}

// Hide the buttons used for expanding or collapsing a
// template tree shown in the profile editor.
function hideTreeExpandCollapseButtons(hide) {
    if (hide) {
        $('#tree-expand').hide();
        $('#tree-collapse').hide();
    } else {
        $('#tree-expand').show();
        $('#tree-collapse').show();
    }
}

// Disable/enable the button used for application
// input when there is not a valid profile loaded
function disableGenerateInputButton(disable) {
    if (disable) {
        $('#generate-input-file-btn').prop('disabled', true);
    } else {
        $('#generate-input-file-btn').removeProp('disabled');
    }
}

// Given a profile name entered by the user into a modal
// pop up, save the profile, relating to the specified
// template.
function saveProfile(templateId, profileName) {
    log('Request to save profile <' + profileName + '> for template <' + templateId + '>.');
    var profileData = $templateContainer.data(treePluginName).getXmlProfile();
    var profilePublic = $('#profile-public').prop('checked');
	var csrfToken = $('input[name="_csrf"]').val();
	var profileObject = {profile:profileData, profilePublic:profilePublic};
    $("#profile-saving").show();
    // Clear any existing error text
    $('#profile-save-errors').html("");
       
	var saveProfileCall = $.ajax({
        method:   'POST',
        url:      '/tempss/api/profile/' + templateId + '/' + profileName,
        dataType: 'json',
        contentType: 'application/json',
        beforeSend: function(jqxhr, settings) {
        	jqxhr.setRequestHeader('X-CSRF-TOKEN', csrfToken);
        },
        data:     JSON.stringify(profileObject)
	});

    saveProfileCall.then(
	    // Success function
	    function(data) {
	        // Check if save succeeded
	        if (data.status == 'OK') {
	            // Close the modal and update the profile list since
	            //save completed successfully.
	            $('#save-profile-modal').modal('hide');
	            updateProfileList(templateId);
	        } else {
	            $('#profile-save-errors').html("<h6>An unknown error has occurred while trying to save the profile.</h6>");
	        }
	        $("#profile-saving").hide();
	    },
	    // Error function
	    function(data) {
	        var result = $.parseJSON(data.responseText);
	        if (result.status == 'ERROR') {
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
	                case 'PERMISSION_DENIED':
	                    errorText = result.error;
	                    break;
	                default:
	                    errorText = "An unknown error has occurred.";
	            }
	            $('#profile-save-errors').html("<h6>Unable to save profile: " + errorText + "</h6>");
	        } else {
	            $('#profile-save-errors').html("<h6>An unknown error has occurred while trying to save the profile.</h6>");
	        }
	        $("#profile-saving").hide();
	    }
	);
}

// Before loading the profile, we need to clear any existing loaded  
// profile from the tree and check that the user is happy with this.
// Use the same approach as when loading a new tree - check that the current
// tree is unmodified or display a warning before loading the new profile.
function loadProfilePreCheck(templateId, profileId) {
    log("Checking if we can load profile <" + profileId + "> for template <" 
    		+ templateId + "> or whether we need to display a confirmation");
    
    if(window.profileLoaded) {
    	var modal = $('#confirm-load-profile-modal');
    	modal.data('templateId', templateId);
    	modal.data('profileId', profileId);
    	modal.modal('show');
    }
    else {
    	loadProfileClearCheck(templateId, profileId);
    }
}
    
// Load the specified profile into the currently displayed template.
function loadProfileClearCheck(templateId, profileId) {
    log("Load profile clear check for profile <" + profileId 
    		+ "> and template <" + templateId + ">");
   
    $("#template-profile-loading").show();
    
    // If we have a previously loaded profile, need to refresh template first.
    if(window.profileLoaded) {
    	var promise = clearProfileContentInTemplate();
    	promise.done(function() {
    		loadProfile(templateId, profileId);
    	}).fail(function() {
    		$('#profile-err-modal-text').html("Error refreshing template to load profile.");
    		$("#template-profile-loading").hide();
    	});
    	
    }
    else {
    	loadProfile(templateId, profileId);
    }
}

function loadProfile(templateId, profileId) {
	log("Load profile <" + profileId + "> for template <" + templateId + ">");
	$("#template-profile-loading").show();

	var savedProfileCall = $.ajax({
        method:   'GET',
        url:      '/tempss/api/profile/' + templateId + '/' + profileId,
        dataType: 'json'
	});
    
    savedProfileCall.then(
            // Success function
            function(data) {
                // Check if save succeeded
                if (data.status == 'OK') {
                	// Set the profile loaded flag to true
                	window.profileLoaded = true;
                    // Extract the profile data and load it into
                    // the template
                    var profileXml = data.profile;
                    $templateContainer.data(treePluginName).loadXmlProfile(profileXml);
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
                    if ($('ul[role=tree]').hasClass('valid')) {
                        $('ul[role=tree]').trigger('nodeValid', $('ul[role=tree]'));
                    }
                } else {
                    //$('#profile-delete-errors').html("<h6>An unknown error has occurred while trying to delete the profile.</h6>");
                }
                setEditingProfileName(profileId);
                $("#template-profile-loading").hide();
            },
            // Error function
            function(data) {
                var result = $.parseJSON(data.responseText);
                if (result.status == 'ERROR') {
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
                } else {
                    //$('#profile-delete-errors').html("<h6>An unknown error has occurred while trying to delete the profile.</h6>");
                }
                $("#template-profile-loading").hide();
            });
}

// Delete the specified profile and then update the profile list
function deleteProfile(templateId, profileId) {
    log("Request to delete profile <" + profileId + "> for template <" + templateId + ">");
    $("#profile-deleting").show();
    // Clear any existing error text
    $('#profile-delete-errors').html("");
	
	var csrfToken = $('input[name="_csrf"]').val();
	var deleteProfileCall = $.ajax({
        method:   'DELETE',
        url:      '/tempss/api/profile/' + templateId + '/' + profileId,
        beforeSend: function(jqxhr, settings) {
        	jqxhr.setRequestHeader('X-CSRF-TOKEN', csrfToken);
        },
        dataType: 'json'
	});
    
    deleteProfileCall.then(
        // Success function
        function(data) {
            // Check if delete succeeded
            if (data.status == 'OK') {
                // Close the modal and update the profile list since
                // delete completed successfully.
                $('#delete-profile-modal').modal('hide');
                $('#delete-confirm-text').html("");
                updateProfileList(templateId);
            } else {
                $('#profile-delete-errors').html("<h6>An unknown error has occurred while trying to delete the profile.</h6>");
            }
            $("#profile-deleting").hide();
        },
        // Error function
        function(data) {
            var result = $.parseJSON(data.responseText);
            if (result.status == 'ERROR') {
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
            } else {
                $('#profile-delete-errors').html("<h6>An unknown error has occurred while trying to delete the profile.</h6>");
            }
            $("#profile-deleting").hide();
        }
    );
}

function changeProfileState(templateId, profileName, currentState, newState) {
    log('Request to change the state of profile <' + profileName + '> for '+
                    'template <' + templateId + '> from state <' + currentState +
                    '> to <' + newState + '>.');

    $("#state-change-loading").show();

    var csrfToken = $('input[name="_csrf"]').val();
    $.ajax({
    method:   'POST',
    url:      '/tempss/api/profile/' + templateId + '/' + profileName + '/' + newState,
    dataType: 'json',
    contentType: 'application/json',
    beforeSend: function(jqxhr, settings) {
            jqxhr.setRequestHeader('X-CSRF-TOKEN', csrfToken);
    },
    success:  function(data) {
        // Check if profile state change succeeded
        if(data.status == 'OK') {
                $('#change-profile-state-modal').modal('hide');
                updateProfileList(templateId);
        }
        else {
                $("#change-state-errors").html("An unknown error has occured when updating profile status.");
        }
        $("#state-change-loading").hide();
    }, // end success callback
    error: function(data) {
        if(data.status == 'ERROR') {
                var errorText = "";
                switch(result.code) {
                case 'INVALID_TEMPLATE':
                        errorText = "An invalid template identifier has been specified.";
                        break;
                case 'PROFILE_DOES_NOT_EXIST':
                        errorText = "The specified profile does not exist.";
                        break;
                case 'UPDATED FAILED':
                        errorText = "The profile status update has failed.";
                        break;
                case 'RESPONSE_DATA':
                        errorText = "Unable to prepare JSON response data.";
                        break;
                default:
                        errorText = "An unknown error has occurred.";
                }
                $("#change-state-errors").html(errorText);
                $("#state-change-loading").hide();
        }
    } // end error callback
    }); // end $.ajax
}


// Clears any profile content entered into the template and
// returns it to the original blank template.
function clearProfileContentInTemplate() {
    // For now, we re-load the blank template from the service rather
    // than clearing values on the client side.
    var templateId = $('input[name=componentname]').val();

    // Destroy plugin
    if ($templateContainer.data(treePluginName) !== undefined) {
        removeChangeHandlers();
        $templateContainer.data(treePluginName).destroy();
    }

    var promise = displayTemplate(templateId, 'REFRESH');
    return promise;
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
    treeRoot.on('click', function(e) {
        if (window.templateEdited === false) {
            window.templateEdited = true;
        }
    });

    treeRoot.on('nodeValid', function() {
        disableGenerateInputButton(false);
    });

    treeRoot.on('nodeInvalid', function() {
        disableGenerateInputButton(true);
    });
}

function removeChangeHandlers() {
    treeRoot.off('click');
    treeRoot.off('nodeValid');
    treeRoot.off('nodeInvalid');
}

function setEditingProfileName(profileName) {
    if(profileName == "NONE") {
        profileName = "";
    }
    $('#editing-profile-name').text(profileName);
}

function collapseTree() {
    $templateContainer.data(treePluginName).collapseTree();
}

function expandTree() {
    $templateContainer.data(treePluginName).expandTree();
}

// Utility function for displaying log messages
function log(message) {
    if(console && console.log) {
        console.log(message);
    }
}
