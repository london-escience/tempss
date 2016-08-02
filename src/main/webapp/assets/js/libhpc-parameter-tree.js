/*
  Sometimes some of the data loaded is an xml file.
  We may want to extract data from the xml file and display it in the tree.
  This function allows us to do so.
  It is called when any input file changes. Use case is when geometry file is
  loaded in Nektar++, want to display the boundary conditions.
  Markup is provided in the schema file and added to the html to keep track
  of what we want to display. The markup uses XPath.

  Rather than call a third party XPath xml parser, I am converting the XPath
  into the form understood by jquery. This is not ideal, and we may replace it later.
*/
function extractEntriesFromFile(event, path) {

    // Need to use the javascript file reader to read in the xml. This allows us to set an onload handler
    // so we only try to read the xml once it's actually loaded.
    var selectedFile = event.target.files[0];
    var reader = new FileReader();
    reader.onload = function (event) {
        var fileXml = event.target.result;

        var xmlDoc = $.parseXML(fileXml);
        var $xml = $(xmlDoc); // The $ of $xml just reminds us it is a jquery object

        var selector = "span[refersToFileTreePath = '" + path + "']";
        // Select all nodes in the tree that need some data out of the xml file that has just been uploaded.
        // These are just base nodes - the actual data may be needed in a descendent. This is to allow us to refer
        // to collections in the xml file (we need to know at what level the data structure may be repeated).
        $(selector).each(
            function (index, item) {
                // Get hold of the XPath to the chunk of data in the xml file we're interested in.
                var baseXPath = $(item).attr("refersToFileDataXPath");
                // remove white space
                baseXPath = baseXPath.replace(" ", "");
                // replace [@attribute='X'] with [attribute='X']
                baseXPath = baseXPath.replace("[@", "[");
                var jqueryStyleBaseXPath = baseXPath.split("/").join(" > ");
                // Count the number of instances of the
                var nInstancesOfXmlChunk = $xml.find(jqueryStyleBaseXPath).length;
                // Remove copies of the appropriate xml chunk that were added last time an xml file was selected
                $("[AddedToMatchXmlFile]").remove();
                // Copy the html for the appropriate chunk to match the number of instances in the xml
                var i;
                for (i = 0; i < nInstancesOfXmlChunk - 1; i++) {
                    var test = $(item).parent().parent().clone(true /* true indicates click handlers will be cloned too  */);
                    $(item).parent().parent().parent().append(test.attr('AddedToMatchXmlFile', 'true').attr('CollectionIndex', i + 1));
                }
                // Mark the block we copied with index 0.
                $(item).parent().parent().attr('CollectionIndex', 0);


                // For each node, find all descendents specifically refering to some data in the xml file (or the node itself).
                // This will give us an xpath using the baseXPath as a base.
                $(item).parent().parent().parent().find("[locationInFileXPath]").each(
                    function (index2, item2) {
                        var collectionIndex = $(item2).closest("[CollectionIndex]").attr('CollectionIndex');

                        var locationInFileXPath = $(item2).attr("locationInFileXPath");
                        locationInFileXPath = locationInFileXPath.replace(" ", "");
                        locationInFileXPath = locationInFileXPath.replace("[@", "[");
                        // This is a hack. In my particular example (Nektar++ boundary conditions)
                        // we get an xpath *[@...]. In jquery I want simply [...]
                        // However, more generally, * could appear in other places and so this could go wrong.
                        locationInFileXPath = locationInFileXPath.replace("*", "");

                        if (locationInFileXPath.substr(0, 1) != "@") {
                            locationInFileXPath = "/" + locationInFileXPath;
                        }

                        var xPath = baseXPath + ":eq(" + collectionIndex + ") " + locationInFileXPath;


                        var jqueryStyleXPathAndAttribute = xPath.split("/").join(" > ").split("@");
                        var jqueryStyleXPath = jqueryStyleXPathAndAttribute[0];
                        var attribute = jqueryStyleXPath.length > 1 ? jqueryStyleXPathAndAttribute[1] : "";

                        var dataFromXml = "";
                        // Another XPath hack:
                        if (jqueryStyleXPath.substr(jqueryStyleXPath.length - 8, jqueryStyleXPath.length) == "> name()") {
                            jqueryStyleXPath = jqueryStyleXPath.substr(0, jqueryStyleXPath.length - 8);
                            // Now extract the data from the xml file using the xpath (and attribute)
                            var $test2 = $xml.find(jqueryStyleXPath);
                            dataFromXml = $test2.prop("tagName");
                        }
                        else {
                            // Now extract the data from the xml file using the xpath (and attribute)
                            var $test2 = $xml.find(jqueryStyleXPath);
                            dataFromXml = jqueryStyleXPath.length > 1 ? $test2.attr(attribute) : $test2.text();
                        }


                        // Finally, set the data in the right place in the html tree.
                        // We allow 'input' text boxes or 'select' dropdown boxes
                        if (typeof dataFromXml === "undefined") {
                            dataFromXml = "NotProvided";
                        }
                        else if( (typeof attribute === "undefined") && (dataFromXml != 'D') && (dataFromXml != 'N')) {
                        	dataFromXml = "NotProvided";
                        }
                        $(item2).siblings("input, select").first().val(dataFromXml).change();


                    });

            });

    };
    reader.readAsText(selectedFile);

}

// Some leaves on the tree may have no inputs.
// This is when the leaf itself is shows the result of a choice.
// These leaves should be set to valid at the outset.
function setLeavesWithoutInputsToValid() {

    $('ul[leaf="true"]').each(function () {
        // Check if there are any inputs in the leaf.
        var isTextInput = ($(this).find('input').length > 0);
        var isDropDown = ($(this).find('select').length > 0);
        // If there are no inputs, then the leaf must be valid!
        if (!(isTextInput || isDropDown)) {
            $(this).closest("ul").attr('class', 'valid').trigger('nodeValid',$(this).closest("ul"));
        }
    });
}

function validateParentNodes(caller) {
    // NB closest() traverses up the tree (including element itself).
    if (caller.parent().closest('ul').length > 0) {
        var parent = caller.parent().closest('ul');
        // Node is valid if there are no invalid children
        var isValid = (parent.children('li').children('ul:not([class="valid"], [chosen="false"])').length == 0);
        if (isValid) {
            parent.closest("ul").attr('class', 'valid').trigger('nodeValid',parent.closest("ul"));
            // TODO: If we're dealing with the root node of the tree,
            // fire an event to say that the tree has been made valid.
        }
        else {
            parent.closest("ul").removeAttr('class', 'valid').trigger('nodeInvalid',parent.closest("ul"));
            // TODO: If we're dealing with the root node of the tree,
            // fire an event to say that the tree has been made invalid.
        }

        // TODO: add index to safeguard against infinite loop.
        validateParentNodes(parent);
    }

}

// Function to validate entries in the tree
function validateEntries(caller, validationtype, restrictions_json) {

    try {
        // Start by assuming it's not valid, but remove any "invalid" class
    	// signifying a previous validation error since this will be re-added
    	// below if validation has still failed. Also remove validation data
    	// error strings.
    	// TODO: Check if we need to fire the nodeInvalid event here
    	caller.closest("ul").removeClass('invalid');
        caller.closest("ul").removeClass('valid').trigger('nodeInvalid',caller.closest("ul"));
        var ul = $(caller.closest("ul"));
        ul.find('.val-help').remove();
        

        switch (validationtype) {
            case "xs:double":
                if (!isNaN(caller.val())) {
                    // If it's a number
                    caller.closest("ul").attr('class', 'valid').trigger('nodeValid',caller.closest("ul"));
                }
                else {
                	// Add the invalid class to display a red X for this node
                	caller.closest("ul").attr('class', 'invalid').trigger('validationError',caller.closest("ul"));
                	caller.closest("ul").children().first().append(
                			'<i class="glyphicon glyphicon-question-sign val-help" ' +
                			'style="padding-left: 10px;" ' +
                			'title="A double value is required for this property." ' +
                			'data-toggle="tooltip" data-placement="right"></i>');
                	//caller.closest("ul").attr('data-val-error','A double value is required for this property.');
                	//caller.closest("ul").attr('title','A double value is required for this property.');
                	//alert(caller.closest("ul").data('val-error'));
                }
                break;
            case "xs:positiveInteger":
                // See http://stackoverflow.com/questions/16941386/validate-a-string-is-non-negative-whole-number-in-javascript
                var intRegex = /^\d+$/; // ^ start of string, \d digit, + any number of times, $ end of string
                if (intRegex.test(caller.val()) && caller.val() > 0) {
                    caller.closest("ul").attr('class', 'valid').trigger('nodeValid',caller.closest("ul"));
                }
                else {
                	// Add the invalid class to display a red X for this node
                	caller.closest("ul").attr('class', 'invalid').trigger('validationError',caller.closest("ul"));
                	caller.closest("ul").children().first().append(
                			'<i class="glyphicon glyphicon-question-sign val-help" ' +
                			'style="padding-left: 10px;" ' +
                			'title="A positive integer value is required for this property." ' +
                			'data-toggle="tooltip" data-placement="right"></i>');
                }

                break;
            case "xs:boolean":
                if (caller.val() === "true" || caller.val() === "false") {
                    caller.closest("ul").attr('class', 'valid').trigger('nodeValid',caller.closest("ul"));
                }
                else {
                	// Add the invalid class to display a red X for this node
                	caller.closest("ul").attr('class', 'invalid').trigger('validationError',caller.closest("ul"));
                	caller.closest("ul").children().first().append(
                			'<i class="glyphicon glyphicon-question-sign val-help" ' +
                			'style="padding-left: 10px;" ' +
                			'title="A boolean value ("true" or "false") is required for this property. ' +
                			'data-toggle="tooltip" data-placement="right"></i>');
                }
                break;
            case "xs:file":
                // Any string filename will do for now, but extension will be checked below.
                if (caller.val().length > 0) {
                    caller.closest("ul").attr('class', 'valid').trigger('nodeValid',caller.closest("ul"));
                }
                break;
            case "xs:string":
                // Any string will do for now.
                if (caller.val().length > 0) {
                    caller.closest("ul").attr('class', 'valid').trigger('nodeValid',caller.closest("ul"));
                }
                break;
        }

        // Parse the restrictions (json) and check each one
        if (!(restrictions_json === undefined)) {
            $.each($.parseJSON(restrictions_json), function (item, value) {
                switch (item) {
                    case "xs:minExclusive":
                        if (caller.val() <= value) {
                            caller.closest("ul").removeAttr('class', 'valid').trigger('nodeInvalid',caller.closest("ul"));
                        }
                        break;
                    case "xs:maxExclusive":
                        if (caller.val() >= value) {
                            caller.closest("ul").removeAttr('class', 'valid').trigger('nodeInvalid',caller.closest("ul"));
                        }
                        break;
                    case "xs:minInclusive":
                        if (caller.val() < value) {
                            caller.closest("ul").removeAttr('class', 'valid').trigger('nodeInvalid',caller.closest("ul"));
                        }
                        break;
                    case "xs:maxInclusive":
                        if (caller.val() > value) {
                            caller.closest("ul").removeAttr('class', 'valid').trigger('nodeInvalid',caller.closest("ul"));
                        }
                        break;
                    case "xs:enumeration":
                        var isStringEnumerationFound = false;
                        for (var index = 0; index < value.length; ++index) {
                            if (caller.val() === value[index]) {
                                isStringEnumerationFound = true;
                                break; //loop
                            }
                        }
                        if (!(isStringEnumerationFound)) {
                            caller.closest("ul").removeAttr('class', 'valid').trigger('nodeInvalid',caller.closest("ul"));
                        }
                        break;
                    case "xs:filetype":
                        var extension = caller.val().match(/[^.]+$/)[0].toLowerCase();
                        var extensionFound = false;
                        for (var index = 0; index < value.length; ++index) {
                            var desiredExtension = value[index].toLowerCase();
                            if (extension === desiredExtension) {
                                extensionFound = true;
                                break; // loop
                            }
                        }
                        if (!(extensionFound)) {
                            // File is not valid after all
                            caller.closest("ul").removeAttr('class', 'valid').trigger('nodeInvalid',caller.closest("ul"));
                        }
                        break;
                }
            });
        }

        // Assume an empty input is never valid.
        if (caller.val() == "") {
            caller.closest("ul").removeAttr('class', 'valid').trigger('nodeInvalid',caller.closest("ul"));
        }
    }
    catch (exception) {
        // If there was an exception, set to invalid
        caller.closest("ul").removeAttr('class', 'valid').trigger('nodeInvalid',caller.closest("ul"));
    }
    validateParentNodes(caller.parent().closest('ul'));
}


function selectChoiceItem(event) {
    var target = event.target;
    // selectedOptions is not well supported:
    // http://stackoverflow.com/questions/13753201/selectedoptions0-textcontent-not-working-in-ie-or-firefox
    //var selected = event.target.selectedOptions[0].value;
    var selected = event.target.options[target.selectedIndex].value;
    var path = event.target.attributes.getNamedItem("choice-path").value;
    var full_path = path + "." + selected;

    $("[path='" + full_path + "']").show('fast');
    $("[path='" + full_path + "']").attr('chosen', 'true'); // Keep a record this choice was made to aid xml generation
    $("[path='" + full_path + "']").children().show('fast');
    $("[path='" + full_path + "']").children().children().show('fast');
    $("[path='" + full_path + "']").children().children().children().show('fast');
    $("[path='" + full_path + "']").siblings("ul").hide();
    $("[path='" + full_path + "']").siblings("ul").attr('chosen', 'false');

    validateParentNodes($("[path='" + full_path + "']"));
}




function loadChildXML(obj, path, $xml) {
    // For each child node, look in the xml.

    $(obj).children("ul").children("li").each(function (i, child) {
        var thisName = $.trim($(child).children("span").text());
        var thisPath = path + " > " + thisName;


        var isLeaf = false;

        if (!$(child).children("ul").length) {
            isLeaf = true;
        }

        //console.log(thisPath);

        // If it's an input box, get the text
        if ($(child).children("input").length) {
            var xmlEntry = $.trim($xml.find(thisPath).text());
            //console.log(xmlEntry);
            // Set the entry and call the onchange function
            $(child).children("input").val(xmlEntry).change();
        }
        // If it's a dropdown select box at a leaf, get the selection
        if (isLeaf && $(child).children("select").length) {
            var xmlEntry = $.trim($xml.find(thisPath).text());
            //console.log(xmlEntry);
            $(child).children("select").val(xmlEntry).change();
        }
        // If it's a choice via dropdown select but not a leaf
        if (!isLeaf && $(child).children("select").length) {
            // Here is a selection (choice). There should be only one child node, the result of the choice
            // Question: do more general xsds break this? We won't worry for now.
            var xmlEntry = $xml.find(thisPath).children()[0];
            if (typeof xmlEntry === "undefined") {
                // do nothing
            }
            else {
                var choiceVal = xmlEntry.nodeName;
                //console.log(choiceVal);
                $(child).children("select").val(choiceVal).change();
            }
        }

        // If it's not a leaf, call recursively
        if (!isLeaf) {
            loadChildXML(child, thisPath, $xml);
        }
    });
}

// Added this for API clarity, retained original
// loadlibrary function to retain compatibility
// with legacy applications
function loadLibhpcProfile(profileXML, targetTemplate) {
	loadlibrary(profileXML, targetTemplate);
}

function loadlibrary() {
    var parameterXml = $("input[name='libraryxml']").data("fileContents");
    loadlibrary(parameterXml, "[role='tree']");
}

function loadlibrary(parameterXml, targetTemplate) {

    var xmlDoc = $.parseXML(parameterXml);
    var $xml = $(xmlDoc);

    var root = $(targetTemplate).children("li");
    var thisName = $.trim(root.children("span").text());
    loadChildXML(root, thisName, $xml);
}

var indentation = "    ";
function generateChildXML(obj, depthString, useFileContent) {
    var xmlString = "";
    // For each child node, fill in the xml.

    $(obj).children("ul:not([chosen='false'])").children("li").each(function (i, child) {
        var thisName = $.trim($(child).children("span").text());
        var isLeaf = false;
        var inputValue = "";
        if (!$(child).children("ul").length) {
            isLeaf = true;
        }
        // If it's an input box, get the text
        if ($(child).children("input").length) {
            inputValue = $.trim($(child).children("input").val());
        }
        // If it's a dropdown select box, get the selection
        if (isLeaf && $(child).children("select").length) {
            inputValue = $(child).children("select").val();
        }
        // If it's an uploaded file, get the file contents or filename as appropriate
        if (isLeaf && $(child).children("span").children("input[type='file']").length) {
            if (useFileContent) {
                inputValue = $(child).children("span").children("input[type='file']").data("fileContents");
            }
            else {
                var fakeFilePath = $(child).children("span").children("input[type='file']").val();
                // Use reg exp to remove the fake file path (everything before the file name)
                var fileName = fakeFilePath.replace(/^.*\\/, "");
                inputValue = fileName;
            }
        }



        xmlString += depthString + "<" + thisName + ">";
        xmlString += inputValue;
        if (!isLeaf) {
            xmlString += "\n" + generateChildXML(child, depthString + indentation, useFileContent) + depthString;
        }
        xmlString += "</" + thisName + ">\n";
    });
    return xmlString;
}

function getXMLString() {
    var xmlString = "";
    var root = $("[role='tree']").children("li");
    var thisName = $.trim(root.children("span").text());

    xmlString += "<" + thisName + ">\n";
    xmlString += generateChildXML(root, indentation, false);
    xmlString += "</" + thisName + ">\n";

    return xmlString;
}

function generateParameterXML() {

    var xmlString = getXMLString();
    $('#xml-content').html('<div style="padding: 10px 5px">Generated XML:</div><div><textarea class="form-control" rows="40" style="width: 70%;">' + xmlString + '</textarea></div>');

}

function submitComponentRequest() {
    $('#errormessages').html("");

    // Create form data object to post the xml and files to the server
    var formData = new FormData();

    formData.append('serveraction', 'componentselector');
    formData.append('componentselector',$("select[name = 'componentselector']").val());

    // Load the html tree representing the component interface from the server.
    $.ajax({
        url: '/tempss/process',
        data: formData,
        processData: false,
        contentType: false,
        type: 'POST',
        dataType: 'json', // By specifying this, $.ajax automatically parses the json
        success: function (data) {
            //var test = $.parseJSON(data); // create an object with the key of the array
            var componentName = data.ComponentName;
            $("h1").text(componentName);

            var html = data.TreeHtml;
            $('#htmlcontent').html(html);

            attachClickHandlers(); // Currently this function resides in bootstrap-tree.js.
            collapseTree();
            setLeavesWithoutInputsToValid();

            $('#xmlsubmission').show();
            $('#libraryload').show();
        },
        error: function (request, status, error) {
            console.log(request.responseText);
            $('#errormessages').html("There was an error selecting the component.");
        }
    });

}

// This function is DEPRECATED - it uses the old HTTP POST-based
// service interface. Please use processJobProfile instead.
function submitXML() {
    $('#xml-results').html("Submitting xml ...");

    var xmlString = "";
    var root = $("[role='tree']").children("li");
    var thisName = $.trim(root.children("span").text());

    xmlString += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    xmlString += "<" + thisName + ">\n";
    xmlString += generateChildXML(root, indentation, false); // useFileContent=false: Don't include xml from files, but upload them seperately, as large data will fail in POST
    xmlString += "</" + thisName + ">\n";

    // Create form data object to post the xml and files to the server
    var formData = new FormData();

    // Add the files to the form data
    $("input[type = 'file']").each(function (index, element) {
        formData.append('xmlupload_file', element.files[0]);  // Just assume one file provided for each thing for now.
    });

    // Add the xml string
    formData.append('xmlupload', xmlString);

    // Add a field to tell the server what component this is.
    // The component name has to match the name of the root node in the tree!
    var componentName = $("input[name = 'componentname']").val();
    formData.append('componentname', componentName);

    // Add a field that tells the server how to handle the data
    formData.append('serveraction', "xmlupload");


    $.ajax({
        url: '/tempss/process',
        data: formData,
        processData: false,
        contentType: false,
        type: 'POST',
        dataType: 'json',
        success: function (data) {
            $('#xml-results').html("<BR />"); // Clear the message
            $.each(data, function (index, element) {
                var outputHtml = "";
                switch (index) {
                    case "BasicXmlInputs":
                        outputHtml = "<a href=\"" + element + "\">EnteredParameters.xml</a><br />";
                        break;
                    case "FullXmlInputs":
                        outputHtml = "<a href=\"" + element + "\">FullInputData.xml</a><br />";
                        break;
                    case "TransformFailed":
                        if (element == true) {
                            outputHtml = "An error occurred transforming the full input data to the form required by the component.<br />";
                        }
                        break;
                    case "TransformErrorMessages":
                        outputHtml = element + "<br />";
                        break;
                    case "TransformedXml":
                        outputHtml = "<a href=\"" + element + "\">TransformedParameters.xml</a><br />";
                        break;
                    default:
                        break;
                }

                $('#xml-results').append(outputHtml);

            });

        }
    });
}

//Get the profile XML data from the specified element
//This function must be provided with the ul element
//with the role "tree" at the root of a template tree.
function getProfileXml(treeElement) {
	// Get the XML for the tree
	var indentation = "    ";
	var treeRoot = treeElement.children("li");
	var thisName = $.trim(treeRoot.children("span").text());

	xmlString = "<" + thisName + ">\n";
	xmlString += generateChildXML(treeRoot, indentation, false);
	xmlString += "</" + thisName + ">\n";

	return xmlString;
}

// Process the job profile data currently in the template tree
// identified by treeRootNode. Send this data to the TemPSS REST
// API and return the JSON response to the caller.
function processJobProfile(treeRootNode, templateId) {
	
    var profileXml = "";
    //var rootNode = $("[role='tree']").children("li");
    var templateName = $.trim(treeRootNode.children("span").text());
    var indentation = "    ";
    
    profileXml += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    profileXml += "<" + templateName + ">\n";
    profileXml += generateChildXML(treeRootNode, indentation, false); // useFileContent=false: Don't include xml from files, but upload them seperately, as large data will fail in POST
    profileXml += "</" + templateName + ">\n";

    // Create form data object to post the xml and files to the server
    var formData = new FormData();

    // Add the files to the form data
    $("input[type = 'file']").each(function (index, element) {
        formData.append('xmlupload_file', element.files[0]);  // Just assume one file provided for each thing for now.
    });

    // Add the xml string
    formData.append('xmlupload', profileXml);

    // Add a field to tell the server what component this is.
    // The component name has to match the name of the root node in the tree!
    // UPDATED: This is no longer required with the REST API
    // var componentName = $("input[name = 'componentname']").val();
    // formData.append('componentname', componentName);

    var csrfToken = $('input[name="_csrf"]').val();

    $.ajax({
        url: '/tempss/api/profile/' + templateId + '/convert',
        data: formData,
        processData: false,
        contentType: false,
        type: 'POST',
        dataType: 'json',
        beforeSend: function(jqxhr, settings) {
        	jqxhr.setRequestHeader('X-CSRF-TOKEN', csrfToken);
        },
        success: function (data) {
        	// If the request returns successfully we'll
        	// have some JSON data containing a group of URLs
        	// The TransformedXml parameter will contain a 
        	// link to the required data.
        	log('Location of transformed XML: ' + data.TransformedXml);
        	
        	var inputFileUrl = data.TransformedXml;
        	var fileId = inputFileUrl.substring(inputFileUrl.lastIndexOf('_') +1, inputFileUrl.length - 4);
        	log('Using fileId <' + fileId + '>');
        	
        	// Trigger a download request to get the transformed XML
        	// and prompt the user to save the file.
        	$.fileDownload('/tempss/api/profile/inputFile/' + fileId);
        	
        	// If a loading element is displayed, hide it
        	if($('#process-profile-loading').length > 0) {
        		$('#process-profile-loading').hide();
        	}
        },
        error: function(data) {
        	log('An error occured downloading the application input file for templateId ' + templateId);
        	if($('#process-profile-loading').length > 0) {
        		$('#process-profile-loading').hide();
        	}
        }
    });
}

function collapseTree() {
    // At startup, hide all nodes except the root.

    // Hide all li nodes
    // $("li").hide();
    //JC - modified to correct issue with menu not showing.
    $("#schema-tree li").hide();

    // Hide all ul nodes that are choices (i.e. have choice-id attribute).
    // This ensures when we click on a choice bubble which has been selected, nothing will show.
    // This is a trick. We are using the fact that the tree javascript only acts on li nodes.
    // $("ul[choice-id]").hide();
    $("#schema-tree ul[choice-id]").hide();

    // Show the root.
    $("[role='tree']").children().show();
    // Show the top level elements - JC
    $("#schema-tree ul[role='tree'] > li > ul").children().show();


    // Handle file uploads
    $("input[type='file']").change(function () {
        var fileInput = $(this)[0];
        var file = fileInput.files[0];
        var textType = /text.*/;

        if (file.type.match(textType)) {
            var reader = new FileReader();
            var currentElement = $(this);

            reader.onload = function (e) {
                var file_text = reader.result;
                //$("textarea.file-data").val(file_text);
                //currentElement.parent().siblings("textarea.file-data").val(file_text);
                currentElement.data("fileContents", file_text); // Set the file contents on the file upload object.
            };

            reader.readAsText(file);
        } else {
            var yyy = 0;
        }
    });
}

function expandTree() {
	$("#schema-tree li").show();
}

$(document).ready(function () {

    // Hide the contents of the xmlsubmission div on the libhpc component selctor page.
    $('#xmlsubmission').hide();
    $('#libraryload').hide();

    // Set up tooltips for validation help and tree expand/collapse buttons
    $('body').tooltip({
        selector: ".val-help, .tree-control-btn"
    });
});
