/*
 * JS API for interacting with tempss REST service.
 *
 * Requires jQuery for ajax calls.
 */

/** Base location of tempss service. */
var tempssBase = 'tempss-service'

/**
 * Get data from a REST server.
 *
 * @param host The host of a remote TemPSS instance or null to use the current local instance.
 * @param port The port of a remote TemPSS instance or null to use the current local instance.
 * @param localURL The local URL to use.
 * @param dataType Type of data expected from server (xml, json, script or html).
 * @param successCallback A function that accepts a data parameter to receive the service's response.
 * @param errorCallback A function that accepts a data parameter to receive an error response.
 */
function getDataFromRest(host, port, localURL, dataType, successCallback, errorCallback) {
    var url = '';
    if (host !== null) {
        url = 'http://' + host;
        if (port !== null) {
            url += ':' + port;
        }
    }

    url += localURL;

    return $.ajax({
        method: 'GET',
        url: url,
        dataType: dataType,
        success: successCallback,
        error: errorCallback,
    });
}

/**
 * Post data to a REST server.
 *
 * @param host The host of a remote TemPSS instance or null to use the current local instance.
 * @param port The port of a remote TemPSS instance or null to use the current local instance.
 * @param localURL The local URL to use.
 * @param dataType Type of data expected from server (xml, json, script or html).
 * @param contentType Content type of data being sent. Must be false for FormData with file.
 * @param data The data to post.
 * @param successCallback A function that accepts a data parameter to receive the service's response.
 * @param errorCallback A function that accepts a data parameter to receive an error response.
 */
function postDataToRest(host, port, localURL, dataType, contentType, data, successCallback, errorCallback) {
    var url = '';
    if (host !== null) {
        url = 'http://' + host;
        if (port !== null) {
            url += ':' + port;
        }
    }

    url += localURL;

    return $.ajax({
        method:   'POST',
        url:      url,
        dataType: dataType,
        contentType: contentType,
        processData: false,
        data:     data,
        success:  successCallback,
        error: errorCallback,
    });
}

/**
 * Send a DELETE to a REST server.
 *
 * @param host The host of a remote TemPSS instance or null to use the current local instance.
 * @param port The port of a remote TemPSS instance or null to use the current local instance.
 * @param localURL The local URL to use.
 * @param dataType Type of data expected from server (xml, json, script or html).
 * @param successCallback A function that accepts a data parameter to receive the service's response.
 * @param errorCallback A function that accepts a data parameter to receive an error response.
 */
function sendDeleteToRest(host, port, localURL, dataType, successCallback, errorCallback) {
    var url = '';
    if (host !== null) {
        url = 'http://' + host;
        if (port !== null) {
            url += ':' + port;
        }
    }

    url += localURL;

    return $.ajax({
        method: 'DELETE',
        url: url,
        dataType: dataType,
        success: successCallback,
        error: errorCallback,
    });
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

    var url = '/' + tempssBase + '/api/template';

    return getDataFromRest(host, port, url, 'json', successCallback, errorCallback);
}

/**
 * Get the HTML representation of a template from a server.
 *
 * @param host The host of a remote tempss instance or null to use the current local instance.
 * @param port The port of a remote tempss instance or null to use the current local instance.
 * @param templateId The template ID to retrieve from the server.
 * @param successCallback A function that accepts a data parameter to receive the service's response.
 * @param errorCallback A function that accepts a data parameter to receive an error response.
 */
function getTemplateHTML(host, port, templateId, successCallback, errorCallback) {

    var url = '/' + tempssBase + '/api/template/id/' + templateId;

    return getDataFromRest(host, port, url, 'html', successCallback, errorCallback);

}

/**
 * Get the profile list from the server.
 *
 * @param host The host of a remote tempss instance or null to use the current local instance.
 * @param port The port of a remote tempss instance or null to use the current local instance.
 * @param templateId ID of template to get profiles for.
 * @param successCallback A function that accepts a data parameter to receive the service's response.
 * @param errorCallback A function that accepts a data parameter to receive an error response.
 */
function getProfileList(host, port, templateId, successCallback, errorCallback) {
    var url = '/' + tempssBase + '/api/profile/' + templateId + '/names';

    return getDataFromRest(host, port, url, 'json', successCallback, errorCallback);
}

/**
 * Save a profile the server.
 *
 * @param host The host of a remote tempss instance or null to use the current local instance.
 * @param port The port of a remote tempss instance or null to use the current local instance.
 * @param templateId ID of template to save profile for.
 * @param profileName Name of profile
 * @param profileData Profile data to save.
 * @param successCallback A function that accepts a data parameter to receive the service's response.
 * @param errorCallback A function that accepts a data parameter to receive an error response.
 */
function postProfileData(host, port, templateId, profileName, profileData,
        successCallback, errorCallback) {
    var url = '/' + tempssBase + '/api/profile/' + templateId + '/' + profileName;

    return postDataToRest(host, port, url, 'json', 'application/json', profileData, successCallback, errorCallback);
}

/**
 * Get a saved profile from the server.
 *
 * @param host The host of a remote tempss instance or null to use the current local instance.
 * @param port The port of a remote tempss instance or null to use the current local instance.
 * @param templateId The template ID to retrieve from the server.
 * @param profileId The profile ID to retrieve from the server.
 * @param successCallback A function that accepts a data parameter to receive the service's response.
 * @param errorCallback A function that accepts a data parameter to receive an error response.
 */
function getSavedProfile(host, port, templateId, profileId, successCallback, errorCallback) {

    var url = '/' + tempssBase + '/api/profile/' + templateId + '/' + profileId;

    return getDataFromRest(host, port, url, 'json', false, successCallback, errorCallback);

}

/**
 * Delete a saved profile from the server.
 *
 * @param host The host of a remote tempss instance or null to use the current local instance.
 * @param port The port of a remote tempss instance or null to use the current local instance.
 * @param templateId The template ID to delete from the server.
 * @param profileId The profile ID to delete from the server.
 * @param successCallback A function that accepts a data parameter to receive the service's response.
 * @param errorCallback A function that accepts a data parameter to receive an error response.
 */
function deleteSavedProfile(host, port, templateId, profileId, successCallback, errorCallback) {

    var url = '/' + tempssBase + '/api/profile/' + templateId + '/' + profileId;

    return sendDeleteToRest(host, port, url, 'html', successCallback, errorCallback);

}

function processProfile(host, port, templateId, profileXml, uploadFile, successCallback, errorCallback) {

    var url = '/' + tempssBase + '/api/profile/' + templateId + '/convert';

    var formData = new FormData();
    if (uploadFile !== null) {
        formData.append('xmlupload_file', uploadFile);
    }
    formData.append('xmlupload', profileXml);

    return postDataToRest(host, port, url, 'json', formData, successCallback, errorCallback);
}
