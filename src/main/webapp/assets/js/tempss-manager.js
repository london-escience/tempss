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
            var $templateNameNode = treeRoot.find("> li.parent_li > span[data-fqname]");
            var templateName = $templateNameNode.text();
            
            // Enable the profile buttons for saving/clearing template content
            // and show the expand/collapse buttons
            if(data.authenticated) {
            	disableProfileButtons(false);
            }
            else {
            	disableProfileButtons(true);
            }
            
            // If this template has constraints, and the constraint functions
            // are available, add a constraint icon to the root node with a 
            // click button to get constraint details
            if(window.hasOwnProperty("constraints")) {
            	constraints.setup(data, $templateNameNode, treeRoot);
            }
            else {
            	log('No constraint library configured, ignoring constraints.');
            }
            
            hideTreeExpandCollapseButtons(false);
            // Add click/change handlers
            attachChangeHandlers();
            setEditingProfileName("");
            
            // Attach handlers for BoundaryCondition/BoundaryRegion processing
            attachBoundaryConditionHandlers();

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

function attachBoundaryConditionHandlers() {
    // Add an additional nodeValid/nodeInvalid handler to BoundaryCondition 
    // nodes that triggers an update of the boundary region options when a 
    // boundary condition becomes valid or invalid.
    // Since we don't have a way of directly selecting BoundaryCondition ul 
    // elements (we can only grab the li descendent of a BoundaryCondition
    // element and there is no CSS parent selector), we add a class to identify 
    // these nodes.
    // TEST: Attaching handlers to input nodes instead of main bc node.
    $('li.parent_li[data-fqname="BoundaryConditionName"]').parent(
    	).addClass('boundary-condition').on('nodeValid', function(e) {
    		updateBoundaryRegions(e, true);
    	}).on('nodeInvalid', function(e) {
    		updateBoundaryRegions(e, false);
    	});
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

/**
 * When an AJAX login is made, we need to:
 * 1) update dropdown menu to place the logged in user's name in the menu bar
 * If we're on the template display:
 * 2) Enable the save profile button so that a current profile can be saved.
 * 3) If we're on the template page and a template is selected, reload profiles
 * 
 */
function handleAjaxLogin(e, modalSource) {
	e.preventDefault();
	if(modalSource) {
		$('#signin-form-errors').html('&nbsp;');
	}
	
	log('AJAX login requested...');

	var logoutFormPart1 = 
		'<form id="logout-form" method="POST" action="/tempss/logout" style="display:none;">' +
		'  <input type="hidden" name="_csrf" value="{{ csrf_token }}"/>' +
		'</form>' +
		'<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button"' + 
		'   aria-haspopup="true" aria-expanded="false">';
	var logoutFormPart2 =
		'<span class="caret"></span>' +
		'</a>' +
		'<ul class="dropdown-menu dropdown-menu-right">' +
		'  <li role="separator" class="divider"></li>' +
		'  <li><a id="sign-out" href="#">Sign out</a></li>' +
		'</ul>';
	var saveBtn = '<span id="save-btn-wrapper">' +
                  '<button class="btn btn-default" id="save-as-profile-btn">' +
                  '<i class="glyphicon glyphicon-floppy-disk"></i>' +
                  ' Save profile</button>' +
                  '</span>';
	
	
	var data;
	if(modalSource) {
		data = $('#login-form-modal').serialize();
	}
	else {
		data = $('#login-form').serialize();
	}
	$.ajax({
	    'type': 'POST',
	    'url': '/tempss/login',
	    'data': data
	}).done(function(data, textStatus, jqXHR) {
		if(('result' in data) && (data['result'] == 'OK')) {
			var firstname = data['firstname'];
			var lastname = data['lastname'];
			var csrf_token = jqXHR.getResponseHeader('X-CSRF-TOKEN'); //data['csrf'];
			log('Login successful for user <' + firstname + ' ' +
					lastname + '>...');
			// Now we update the user name display in the menu bar
			logoutFormPart1 = logoutFormPart1.replace("{{ csrf_token }}", csrf_token);
			var logoutForm = logoutFormPart1 + firstname + ' ' + lastname + logoutFormPart2;
			$('#login-form').parent().parent().html(logoutForm);
			$('#save-btn-wrapper').replaceWith(saveBtn);
			
			// If the profile list is present on this page, schedule a request
			// to update it
			if( $('#profile-list').length ) {
				// Get the template ID to pass to the profile update function
				var tSelect = $('#template-select').find(":selected");
				setTimeout(updateProfileList(tSelect.val()), 0);
			}
			// Close the signin drop down or modal depending on login source
			if(modalSource) {
				$(e.currentTarget).closest('.modal').modal('hide');
			}
			else {
				$('#navbar .dropdown-toggle').dropdown('toggle');	
			}
			
		}
	}).fail(function(jqXHR, textStatus, errorThrown) {
		log('Error logging user in....');
		if(!modalSource) {
			// Close the signin drop down
			$('#navbar .dropdown-toggle').dropdown('toggle');
			displayLoginForm(modalSource);
		}
		else {
			$('#signin-form-errors').html('An incorrect user name or password was entered. Login failed.');
		}
		/*
		BootstrapDialog.show({
            type: BootstrapDialog.TYPE_DANGER,
			title: 'Login failed',
            message: 'Invalid credentials entered, login failed. Please retry.',
            buttons: [{
                label: 'Close',
                action: function(dialog) {
                    dialog.close();
                }
            }]
        });
        */
	});
	
}

function displayLoginForm(modalSource) {
	var loginForm = 
		'<div class="well">' +
		'  <h3 style="padding-bottom: 20px; margin-top: 10px;">TemPSS Sign In</h3>' +
		'  <form id="login-form-modal" class="form-horizontal">' +
		'    <div id="signin-form-errors" class="text-danger">{{ signin_form_errors }}</div>' +
		'	 <div class="row">' +
		'	   <div class="col-sm-2"></div>' +
		'      <div id="signin-errors" class="text-danger col-sm-5"></div>' +
		'	   <div class="col-sm-2"></div>' +
		'    </div>' +
		'    <div class="form-group">' +
		'      <label for="username" class="col-sm-4 control-label">Username</label>' +
		'      <div class="col-sm-5">' +
		'        <input id="username" name="username" type="text" class="form-control" placeholder="" value="{{ username }}"/>' +
		'      </div>' + 
		'      <div class="col-sm-4">' +  
		'        <div id="signin-errors-username" class="form-error text-danger"/>' +
		'      </div>' +
		'    </div>' +
		'    <div class="form-group">' +
		'      <label for="password" class="col-sm-4 control-label">Password</label>' +
		'      <div class="col-sm-5">' +
		'        <input id="password" type="password" name="password" class="form-control" placeholder=""/>' +
		'      </div>' +
		'      <div class="col-sm-3">' + 
		'        <div id="signin-errors-password" class="form-error text-danger"/>' +
		'      </div>' +
		'    </div>' +
		'    <input type="hidden" name="_csrf" value="{{ csrf_token }}"/>' +
		'    <div class="form-group text-right">' +
		'      <div class="col-sm-4" style="padding-top: 15px;">' +
		'        <a href="register" target="_blank"><i class="glyphicon glyphicon-edit"></i> Create account</a>' +
		'      </div>' +
		'      <div class="col-sm-7">' +
		'        <button type="submit" class="btn btn-success">Sign In</button>' +
		'      </div>' +
		'    </div>' +
		'  </form>' +
		'</div>';
	
	var csrf_token = $('#login-form input[name="_csrf"]').val();
	loginForm = loginForm.replace("{{ csrf_token }}", csrf_token);
	var signinFormErrors = '&nbsp;';
	if(!modalSource) {
		signinFormErrors = 'An incorrect user name or password was entered. Login failed.';
	}
	loginForm = loginForm.replace("{{ signin_form_errors }}", signinFormErrors);
	
	// Get the username from the header login form and put it in the input box
	var username = '';
	var $usernameInput = $('#login-form #username');
	var $passwordInput = $('#login-form #password');
	if($usernameInput.length) {
		username = $usernameInput.val();
	}
	$usernameInput.val("");
	$passwordInput.val("");
	loginForm = loginForm.replace("{{ username }}", username);
	
	BootstrapDialog.show({
        type: BootstrapDialog.TYPE_DEFAULT,
		title: '',
        message: loginForm,
        onhidden: function(dialog) {
        	$('#signin-form-errors').html('&nbsp;');
        },
        buttons: [{
            label: 'Close',
            action: function(dialog) {
                dialog.close();
            }
        }]
    });
}

/**
 * This function updates BoundaryRegion nodes by finding all the boundary 
 * conditions that have a name set and then placing these names in the list of  
 * available boundary conditions for each BoundaryRegion.
 */
function updateBoundaryRegions(event, valid) {
	log("Update boundary regions - valid? " + valid);
	// Iterate over all the BoundaryCondition elements and find those that have
	// a name set. Compile a list of these names and then add them to each 
	// BoundaryRegion/BoundaryCondition node.
	var BCNames = [];
	$('li.parent_li[data-fqname="BoundaryConditionName"] input').each(function() {
		if(!$(this).parent().parent().parent().parent().hasClass('disabled')) {
			var value = $(this).val();
			if(value != null && value != "") {
				BCNames.push(value);
			}
		}
	});
	
	// Prepare a list of boundary condition names to place in the change handler
	// and the list of option elements to add to each boundary region select.
	log("BCNames: " + BCNames);
	var BCNameList = "[";
	var optionHtml = '<option value="Select from list">Select from list</option>';
	for(var i = 0; i < BCNames.length; i++) {
		optionHtml += '<option value="' + BCNames[i] + '">' + BCNames[i] + '</option>';
		BCNameList += '"' + BCNames[i] + '"';
		if(i < BCNames.length-1) {
			BCNameList += ", ";
		}
	}
	BCNameList += ']';

	$('li.parent_li[data-fqname="BoundaryRegion"]').each(function() {
		var $select = $(this).find('li.parent_li[data-fqname="BoundaryCondition"] select');
		// Before removing options from the existing list, get the current value
		// from the select. If this is present in the new array, then we 
		// re-select it after updating the available options. An alternative 
		// would be to only remove an option if it is not in the list.
		var previousValue = $select.find(":selected").val();
		$select.find('option').remove();
		$select.append($(optionHtml));
		if(BCNames.indexOf(previousValue) >= 0) {
			$select.val(previousValue);
		}
		$select.attr("onChange", "validateEntries($(this), 'xs:string', '{\"xs:enumeration\": " + BCNameList + "}');");
		$select.trigger('change');
	});
}

// Utility function for displaying log messages
function log(message) {
    if(console && console.log) {
        console.log(message);
    }
}
