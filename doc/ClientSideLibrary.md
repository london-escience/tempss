# TemPSS Client-side Library

The TemPSS client-side library is a JavaScript library that provides third-party developers with access to common client-side template/profile management tasks. Some of the functions supported by the library can be accessed directly in the REST API and the JavaScript functions are provided as higher-level helper functions to simplify API access.

## API Functionality
The main tasks supported by the client-side API are:

* Loading XML profile data into a template
* Extract a profile from data entered into a template
* Profile validation
* Submitting a profile to the TemPSS service for conversion to job input data

## Using the API

### Load the library

To load the library, include the following in your HTML:

    <script type="text/javascript" src="/js/libhpc-parameter-tree.js"></script>

### Use the library

#### Extracting a profile

`String getProfileXML(String treeRoot)`

When a user has entered values into a template tree, the data entered can be extracted as an XML document that can subsequently be loaded back into a blank template of the same type. This API does not provide support for saving the profile to a database.

`treeRoot` is a string representing a jQuery selector for the root element of the template tree to get the profile for. This should be the root `ul` element of the template tree that has a `role` attribute with the value `tree`.

The function returns the profile XML document as a string.

#### Loading a profile

`loadProfile(String profileXML, String targetTemplate)`

Loads the provided XML profile document (provided as a string) into the specified template.

`profileXML` is an XML document, provided as a string, containing the profile to load.

`targetTemplate` is a jQuery selector for the root node of the target template into which the profile data should be loaded. This should bethe root `ul` element of the target template tree that has a `role` attribute with the value `tree`.

There is no return value from this function.

