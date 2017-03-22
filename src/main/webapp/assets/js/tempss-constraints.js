window.constraints = {
	
	setup: function(data, $nameNode, $treeRoot) {
		log("Request to setup constraints for template...");
		if(!data.constraints) {
			log("There is no constraints information in the provided data object.");
			return;
		}
		// Add a comment to the root node with a link to display the constraint
		// information
    	var $constraintHtml = $('<div class="constraint-header">' + 
    		'<i class="glyphicon glyphicon-link"></i> This template ' +
    		'has constraints set. Click <a href="#" ' + 
    		'class="constraint-info-link">here</a> for details.</div>');
    	$constraintHtml.insertAfter($nameNode);
    	
    	// Build a string for each element that has constraints listing the 
    	// other element(s) it is linked to. This will be displayed as a 
    	// tooltip alongside the link icon
    	var constraintMessages = {}
    	for(var key in data.constraintInfo) {
    		var mappings = data.constraintInfo[key];
    		// Split and get the last item from the variable fq name
    		var node = key.split(".").pop();
    		var msg = node + " has a constraint relationship with ";
    		for(var i = 0; i < mappings.length; i++) {
    			msg += mappings[i].split(".").pop();
    			if(i == mappings.length-1) msg+= ".";
    			else if(i == mappings.length-2) msg+= " and ";
    			else msg += ", ";
    		}
    		constraintMessages[key] = msg;
    	}
    	
    	// Add a constraint icon to each template node involved in a 
    	// constraint relationship. The constraint FQ name doesn't include the 
    	// top-level name - we search down from the top level.
    	var $rootLi = $treeRoot.children("li.parent_li");
    	for(var key in data.constraintInfo) {
    		//log("Handling key <" + key + ">...");
    		var pathItems = key.split(".");
    		// Search for the element that needs the constraint icon adding...
    		var $li = $rootLi;
    		for(var i = 0; i < pathItems.length; i++) {
    			$li = $li.find('> ul > li[data-fqname="' + pathItems[i] + '"]');
    		}
    		var $link = $('<i class="glyphicon glyphicon-link link-icon"' +
    				' title="' + constraintMessages[key] + 
    				'" data-toggle="tooltip" data-placement="right"></i>');
    		var $firstUl = $li.children('ul:first');
    		if($firstUl.length == 0) {
    			$li.append($link);
    		}
    		else {
    			$link.insertBefore($firstUl);
    		}
    		$li.addClass('constraint');
    	}
	},

	// Display a modal showing constraint information for the current template.
	showConstraintInfo: function(e) {
		var $target = $(e.currentTarget);
		var templateName = $target.parent().parent().children('span[data-fqname]').data('fqname');
		var templateId = $('input[name="componentname"]').val();
		log("Constraint info requested for solver template <" + templateName + "> with ID <" + templateId + ">...");
		e.preventDefault();
	
		// Get the constraint info and display in a BootstrapDialog
		BootstrapDialog.show({
			title: "Constraint information",
	        message: function(dialog) { 
	        	var $message = $('<div></div>');
	        	
	        	// Try to load the constraint data and show an error if loading fails
	        	$message.load('/tempss/api/constraints/' + templateId, 
	        			function( response, status, xhr ) {
	        				if (status=="error") {
	        					$message.html('<div class="alert alert-danger"><b>Unable to access constraint information.</b> An error has occurred accessing the constraint data from TemPSS for this template.</div>');
	        				}
	      		});
	        	return $message
	        }
	    });
	},
	
	updateConstraints: function(templateName, templateId, $triggerElement) {
		log("Constraints update triggered for template <" + templateName 
				+ "> with ID <" + templateId + "> and trigger element <" 
				+ $triggerElement.data('fqname') + ">");
		// Find all the constraint items and prepare a form request to 
		// submit them to the server.
		// Create form data object to post the params to the server
	    var formDict = {};
		$('.constraint').each(function(index, el) {
			// constraint elements are li.parent_li nodes
			// data-fqname attribute only gives us the local name so we need
			// to search up the tree to build the correct fq name.
			var name = "";
			var $element = $(el);
			while($element.attr("data-fqname") && $element.data('fqname') != templateName) {
				log("Processing name: " + $element.data('fqname'));
				if(name == "") name = $element.data('fqname'); 
				else name = $element.data('fqname') + "." + name;
				$element = $element.parent().closest('li.parent_li');
				if($element.length == 0) break;
				
			}
			var value = "";
			if($(el).children('select.choice').length > 0) {
				log("Preparing constraints - we have a select node...");
				var $option = $(el).children('select.choice').find('option:selected');
				value = $option.val();
				if(value == "Select from list") value = "NONE";
			}
			else if($(el).children('span.toggle_button').length > 0) {
				log("Preparing constraints - we have an on/off node...");
				var $iEl = $(el).find('> span.toggle_button > i.toggle_button');
				// FIXME: For now, we only want to set the actual value of the
				// on/off item when it has been set by a constraint result.
				if(!$iEl.hasClass("set_by_constraint")) value = "NONE";
				else if($iEl.hasClass("enable_button")) value = "Off";
				else value = "On";
			}
			log("Name: " + name + "    Value: " + value);
			formDict[name] = value;
		});
		
		var csrfToken = $('input[name="_csrf"]').val();
		
		// Now we need to post the constraintParams to the server
		var solveRequest = $.ajax({
			beforeSend: function(jqxhr, settings) {
	        	jqxhr.setRequestHeader('X-CSRF-TOKEN', csrfToken);
	        },
			method: 'POST',
			url: '/tempss/api/constraints/' + templateId + '/solver',
			data: formDict
		});
		
		solveRequest.done(function(data) {
			if(data.hasOwnProperty("result") && 
			   data.hasOwnProperty("solutions") && 
			   data["result"] == "OK") {
				
				log("solve request completed successfully." + JSON.stringify(data));
				// Iterate through solutions and update the values
				for(var i = 0; i < data.solutions.length; i++) {
					var solution = data.solutions[i];
					log("Processing constraint variable: " + solution['variable']);
					var name = solution['variable'];
					var nameParts = name.split(".");
					var $targetEl = window.treeRoot.find('li.parent_li[data-fqname="' + nameParts[0] + '"]');
					for(var j = 1; j < nameParts.length; j++) {
						$targetEl = $targetEl.find('li.parent_li[data-fqname="' + nameParts[j] + '"]')
					}
					if(!$targetEl.length) {
						log("ERROR, couldn't find tree node for variable <" + name + ">");
						continue;
					}
					// See if we have a select element or on/off
					if($targetEl.children('select.choice').length) {
						var $selectEl = $targetEl.children('select.choice');
						var selectHTML = '<option value="Select from list">Select from list</option>';
						for(var j = 0; j < solution['values'].length; j++) {
							selectHTML += '<option value="' + solution['values'][j] + 
							'">' + solution['values'][j] + '</option>';
						}
						$selectEl.html(selectHTML);
					}
				}
			}
			else {
				log("solve request failed: " + JSON.stringify(data));
			}
		}).fail(function(data) {
			log("solve request returned error: " + JSON.stringify(data));
		});
	}

}