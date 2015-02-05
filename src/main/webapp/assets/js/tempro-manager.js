/**
 * JavaScript functions for the tempro template manager sample
 * application distributed along with the tempro web service. 
 */


// This function is called via a setTimeout call when the selected
// template is changed.
function templateChanged(selectedValue, selectedText) {
	displayTemplate(selectedValue, selectedText);
	updateProfileList(selectedValue);	
}

function updateTemplateList() {
	$('#template-loading').show();
	
	$.ajax({
        method:   'get',
        url:      '/temproservice/api/template',
        dataType: 'json',
        success:  function(data){
        	// Remove current content excluding the placeholder
        	$('#template-select option:gt(0)').remove();
        	var templateSelect = $('#template-select');
        	for(var i = 0; i < data.components.length; i++) {
        		var item = data.components[i];
        		templateSelect.append("<option value=\"" + item.id + "\">" + item.id + " - " + item.name + "</option>");
        	}
            $("#template-loading").hide(0);
        },
        error: function() {
            $("#template-loading").hide(0);
        }
    });
}

// Display the tree for the template with the specified ID in the
// template panel.
function displayTemplate(templateId, templateText) {
	log("About to display tree for template with ID <" + templateId + "> and text <" + templateText + ">");
	
	if(templateId == "NONE") {
		disableProfileButtons(true);
		$("#template-container").html("<h6>No template selected. Please select a template from the drop-down list above.</h6>");
		return;
	} 
	
	$("#template-tree-loading").show();
	$.ajax({
        method:   'get',
        url:      '/temproservice/api/template/id/' + templateId,
        success:  function(data){
        	// Data that comes back is the raw HTML to place into the page
        	$("#template-container").html(data)
        	// Add the javascript handlers to this HTML to enable
        	// clicking of the nodes, etc.
        	attachClickHandlers(); // Currently this function resides in bootstrap-tree.js.
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
            disableProfileButtons(false);
        	
            $("#template-tree-loading").hide(0);
        },
        error: function() {
            $("#template-tree-loading").hide(0);
        }
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
	
	// Now do a database lookup for profiles for this template.
	
	// If no profiles are available
	$('#profiles').html('<h6 class="infotext">There are no profiles registered for the "' + templateId + '" template.</h6>');
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

// Given a profile name entered by the user into a modal
// pop up, save the profile, relating to the specified
// template.
function saveProfile(templateId, profileName) {
	
	
}

// Utility function for displaying log messages
function log(message) {
	if(console && console.log) {
		console.log(message);
	}
}