/*
 * Legacy functions used by component.jsp
 * Factored out of libhpc-parameter-tree.js
 */

/**
 * Hide everything except the tree so tree can be printed etc.
 */
function hideForPrinting() {
    $('#xmlsubmission').hide();
    $('#libraryload').hide();
    $('h1').hide();
}

/**
 * Hide inputs to the tree so tree diagram can be printed.
 */
function hideInputs() {
    $('input').hide();
    $('select').hide();
}

/**
 * Gets the HTML parameter tree for a selected component.
 *
 * This is deprecated in favour of the REST api.
 */
function submitComponentRequest() {
    // Remove any error messages
    $('#errormessages').html("");

    // Create form data object to post the xml and files to the server
    var formData = new FormData();

    formData.append('serveraction', 'componentselector');
    formData.append('componentselector', $("select[name = 'componentselector']").val());

    // Load the html tree representing the component interface from the server.
    $.ajax({
        url: '/tempss-service/process',
        data: formData,
        processData: false,
        contentType: false,
        type: 'POST',
        dataType: 'json', // By specifying this, $.ajax automatically parses the json
        success: function (data) {
            var componentName = data.ComponentName;
            $("h1").text(componentName);

            var html = data.TreeHtml;
            var $treeContainer = $('#htmlcontent');
            $treeContainer.html(html);
            var treePluginName = 'plugin_LibhpcParameterTree';

            $treeContainer.LibhpcParameterTree();
            $treeContainer.data(treePluginName).setLeavesWithoutInputsToValid();
            $treeContainer.data(treePluginName).collapseTree();
            $treeContainer.data(treePluginName).attachExpandClickHandlers();
            $treeContainer.data(treePluginName).disableOptionalBranches();
            $treeContainer.data(treePluginName).attachRepeatClickHandlers();
            $treeContainer.data(treePluginName).enableTooltips();

            $('#xmlsubmission').show();
            $('#libraryload').show();
        },
        error: function (request, status, error) {
            $('#errormessages').html("There was an error selecting the component.");
        }
    });
}

/**
 * Get the XML representation of the tree and display.
 */
function generateParameterXML() {
    var xmlString = $('#htmlcontent').data('plugin_LibhpcParameterTree').getXMLProfile();
    $('#xml-content').html('<div style="padding: 10px 5px">Generated XML:</div><div><textarea class="form-control" rows="40" style="width: 70%;">' + xmlString + '</textarea></div>');
}

/**
 * Submit XML tree to server for transforming into input file.
 *
 * This function is DEPRECATED - it uses the old HTTP POST-based
 * service interface. Please use processJobProfile instead.
 */
function submitXML() {
    $('#xml-results').html("Submitting xml ...");

    var xmlString = $('#htmlcontent').data('plugin_LibhpcParameterTree').getXMLProfile();

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
        url: '/tempss-service/process',
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
                        if (element === true) {
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

function loadlibrary() {
    var parameterXml = $("input[name='libraryxml']").data("fileContents");
    loadlibrary(parameterXml, '#htmlcontent');
}
