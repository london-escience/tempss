(function($, document, window, log) {

	/**
	 * The frequency with which the keep alive call is made in seconds 
	 */
	var KEEP_ALIVE_FREQUENCY = 300;
	
	$(function() {
		log("Index page - document ready...")

		updateTemplateList();
		disableClearTemplateButton(true);
		disableSaveProfileButton(true);
		disableGenerateInputButton(true);

		// Variable to check whether a template has been edited
		window.templateEdited = false;
		// Variable to check whether a profile has been loaded - used to 
		// determine whether to refresh the template when loading a profile
		window.profileLoaded = false;
		
		$('#template-select').on('change', function(e) {
			var target = e.currentTarget;
			var selected = $(target).find(":selected");
			var selectedText = selected.text();
			var selectedValue = selected.val();

			// Before changing the template, if any modifications
			// to the base template have been made, confirm that
			// the user wants to change template. This change will
			// lose any unsaved changes.
			if(templateEdited) {
				$('#confirm-change-template-modal').modal('show');
			}
			else {
				// We need to store the previous value of #template-select so that we
				// can revert to the originally selected value if the user cancels a 
				// change request - since the confirmation is not being displayed
				// we can store the new value as previous.
				$('#template-select').data('prev', $('#template-select').val());
				setTimeout(function() {
					templateChanged(selectedValue, selectedText);
				}, 0);
			}
		});

		// If the template change confirmation was displayed, this event
		// will be fired if the user chooses to continue with the change.
		$('#change-template-btn-modal').on('click', function(e) {
			var selected = $('#template-select').find(":selected");
			var selectedText = selected.text();
			var selectedValue = selected.val();
			// Since the user has confirmed that they want to change the 
			// displayed template, we store the new value as previous.
			$('#template-select').data('prev', selectedValue);
			
			setTimeout(function() {
				templateChanged(selectedValue, selectedText);
			}, 0);
			templateEdited = false;
			$('#confirm-change-template-modal').modal('hide');
		});

		// If the user cancels the template change process, we revert the 
		// selected value in the box to the previously selected value
		$('#cancel-change-template-btn-modal').on('click', function() {
			var prevVal = $('#template-select').data('prev'); 
			$('#template-select').val(prevVal);
		});
		
		// When the "Save as profile" button is clicked below a
		// template that has had values entered into it, open
		// the save profile modal to prompt the user for a profile name
		$('#template-tree').on('click', '#save-as-profile-btn', function(e) {
			saveProfileBtnClicked();
		});

		// When the modal input box is changed, check if there is text
		// in it and if so, make sure the save button is enabled.
		$('body').on('keyup', '#profileName', function() {
			var saveBtn = $('#save-profile-btn-modal');

			if($('#profileName').val() == "") {
				saveBtn.prop('disabled', true);
			}
			else {
				saveBtn.prop('disabled', false);
			}
		});

		// When the save button on the modal is clicked to save the
		// profile using the provided name, initiate the save process
		$('#save-profile-btn-modal').on('click', function(e) {
			saveProfile($('input[name=componentname]').val(), $('#profileName').val());
		});

		// Add click event listeners for items in the profile list
		$('#profile-list').on('click', '.profile-link', function(e) {
			// Before loading the profile, check if the current template has
			// been modified. 
			// Load selected profile into the template
			var profileId = $(e.currentTarget).data('pid');
			var templateId = $('input[name=componentname]').val();
			loadProfilePreCheck(templateId, profileId);
			e.preventDefault();
		});

		// Add click listeners for the delete and load profile icons
		$('#profile-list').on('click', '.delete-profile', function(e) {
			var selectedProfileName = $(e.currentTarget).parent().parent().find('.profile-link').first().data('pid');
			$('#delete-confirm-text').html('<h5 class="text-danger">Are you sure you want to delete profile "'
					+ '<span id="delete-profile-name">' + selectedProfileName + '</span>"?');
			$('#profile-delete-errors').html("");
			$('#delete-profile-modal').modal('show');
		});

		$('#profile-list').on('click', '.load-profile', function(e) {
			var selectedProfileName = $(e.currentTarget).parent().parent().find('.profile-link').first().data('pid');
			var templateId = $('input[name=componentname]').val();
			log('About to load profile <' + selectedProfileName + '>.');
			loadProfile(templateId, selectedProfileName);
		});

		// Add click listeners for the change state links
		$('#profile-list').on('click', '.change-state', function(e) {
			e.preventDefault();
			var profilesObj = $(e.currentTarget).parents('.profile-item');
			var selectedProfileName = profilesObj.find('.profile-link').first().data('pid');
			var templateId = $('input[name=componentname]').val();
			log('About to display confirmation modal for profile state change <' + selectedProfileName + '>.');
			var currentState = $(e.currentTarget).data('state');
			var newState = (currentState == "private") ? "public" : "private";
			$('#change-state-text').html('<h5 class="text-info">Are you sure you want to change the state of profile '
					+ '<span id="profile-state-change-name">' + selectedProfileName + '</span> from ' 
					+ '<span id="profile-change-current" class="text-warning">' + currentState + '</span> ' 
					+ 'to <span id="profile-change-new" class="text-warning">' + newState + '</span>?"');
			$('#change-state-errors').html("");
			$('#change-profile-state-modal').modal('show');
		});
		
		// Delete profile when the confirm button is pressed on the confirmation modal
		$('#delete-profile-btn-modal').on('click', function() {
			var profileToDelete = $('#delete-profile-name').text();
			var templateId = $('input[name=componentname]').val();
			log('About to delete profile <' + profileToDelete + '>.');
			deleteProfile(templateId, profileToDelete);
		});
		
		// Change the state of a profile when confirmed via modal 'yes' button
		$('#change-state-btn-modal').on('click', function() {
			var profileName = $('#profile-state-change-name').text();
			var templateId = $('input[name=componentname]').val();
			var currentState = $('#profile-change-current').html();
			var newState = $('#profile-change-new').html();
			log('About to change the state of profile <' + profileName + '>.');
			changeProfileState(templateId, profileName, currentState, newState);
		});

		// Open a modal to confirm that the user wants to clear
		// the current profile data.
		$('#clear-profile-btn').on('click', function() {
			$('#confirm-clear-profile-modal').modal('show');
		});

		$('#clear-profile-btn-modal').on('click', function() {
			clearProfileContentInTemplate();
			$('#confirm-clear-profile-modal').modal('hide');
		});

		// Genrate the input data file for the current, valid profile.
		$('#generate-input-file-btn').on('click', function() {
			$('#process-profile-loading').show();
			tempssProcessProfile();
		});

		$('#tree-collapse').on('click', function() {
			collapseTree();
		});

		$('#tree-expand').on('click', function() {
			expandTree();
		});
		
		// Set up tooltips for collapse/expand tree buttons
		hideTreeExpandCollapseButtons(true);
		
		// Enable tooltips (for save button)
        $(function () {
            $('[data-toggle="tooltip"]').tooltip()
        });
		
        $('#load-profile-btn-modal').on('click', function(e) {
        	var modal = $('#confirm-load-profile-modal');
        	var templateId = modal.data('templateId');
        	var profileId = modal.data('profileId');
        	modal.modal('hide');
        	loadProfileClearCheck(templateId, profileId);   	
        });
        
        $('#load-profile-btn-modal').on('hide.bs.modal', function(e) {
        	var modal = $('#confirm-load-profile-modal');
        	modal.removeData('templateId');
        	modal.removeData('profileId');
        });
        
        $('#profile-error-modal').on('hide.bs.modal', function(e) {
        	$('#profile-err-modal-text').html("");
        });
        
        // Handle click of constraint info link
        $('body').on('click', '.constraint-info-link', function(e) {
        	constraints.showConstraintInfo(e);
        });
        
     	// Handle click of "Reset constraints" button
        $('body').on('click', '.reset-constraints-btn', function(e) {
        	constraints.resetConstraintsConfirmation(e);
        });
     	
		$('#constraint-undo').on('click', function() {
			if(window.hasOwnProperty('constraints'))
				constraints.undoConstraintChange();
		});

		$('#constraint-redo').on('click', function() {
			if(window.hasOwnProperty('constraints'))
				constraints.redoConstraintChange();
		});
        
        // Enable tooltips for dynamically added constraint link icons
        $('#template-container').tooltip({ selector: 'i.link-icon'});
        
        $('#save-profile-btn-new').on('click', function(e) {
        	saveProfileNewClicked(e);
        });
        
        $('#save-profile-btn-current').on('click', function(e) {
        	saveProfileCurrentClicked(e);
        });
        
        $('#add-template-init-btn').on('click', function(e) {
        	$('#add-template-modal').modal('show');
        });
        
        // Set up the keepalive call to run periodically to main an 
        // active session
        setInterval(function() {
        	var time = new Date();
			var keepalive = $.get('/tempss/api/keepalive');
			keepalive.done(function() {
				log("Keep alive request successful at: " + time.toLocaleString());
			}).fail(function() {
				log("Keep alive request FAILED at: " + time.toLocaleString());
			});
		}, 1000 * KEEP_ALIVE_FREQUENCY);
        	
	});
	
	function saveProfileNewClicked(e) {
		$('#save-existing-modal').modal('hide');
		$('#save-profile-modal').modal('show');
	}
	
	function saveProfileCurrentClicked(e) {
		var profileName = $('#editing-profile-name').text();
		$('#save-existing-error').text("");
		if(profileName == '') {
			$('#save-existing-error').text('Error saving profile - unable to '+
					'get current profile name.');
			return;
		}
		saveProfile($('input[name=componentname]').val(), profileName, true);
	}
	
	// Called when the save profile button below the template editor is clicked.
	function saveProfileBtnClicked() {
		$('#profileName').val("");
		$('#save-existing-error').text("");
		$('#save-profile-btn-modal').prop('disabled', true);
		if($('#template-container').data('saved')) {
			$('#save-existing-modal').modal('show');
		}
		else {
			$('#save-profile-modal').modal('show');
		}
	}

	// Given a profile name entered by the user into a modal
	// pop up, save the profile, relating to the specified
	// template.
	function saveProfile(templateId, profileName, overwrite) {
		var overwriteProfile = false;
		if(overwrite !== undefined) {
			overwriteProfile = overwrite;
		}
	    log('Request to save profile <' + profileName + '> for template <' + templateId + '>.');
	    var profileData = $templateContainer.data(treePluginName).getXmlProfile();
	    var profilePublic = $('#profile-public').prop('checked');
		var csrfToken = $('input[name="_csrf"]').val();
		var profileObject = {profile:profileData, profilePublic:profilePublic,
				profileOverwrite: overwriteProfile};
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
	        data: JSON.stringify(profileObject)
		});

	    saveProfileCall.then(
		    // Success function
		    function(data) {
		        // Check if save succeeded
		        if (data.status == 'OK') {
		            // Close the modal and update the profile list since
		            //save completed successfully.
		            $('#save-profile-modal').modal('hide');
		            $('#save-existing-modal').modal('hide');
		            $('#template-container').data('saved', true);
		            updateProfileList(templateId);
		            // Set the profile name in the editor panel header
		            $('#editing-profile-name').text(profileName);
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
	
}(window.jQuery, document, window, window.log));