# TemPSS REST API

This document outlines the REST API for the TemPSS service, which can be
accessed through the `/api` base URL from your installation. When running
locally through a development server, API functions are available from the
address

  http://localhost:8080/tempss/api

## Template operations

### Get all template metadata

Get a list of all template metadata in text or JSON format.

```
GET /api/template
```

##### Client accept header:

- `application/json` returns a JSON object with the key "components" containing
  an array of template metadata objects, one for each template.
- `text/plain` for text format. The response contains one template metadata
  record per line. Each record is a set of 4 comma-separated values enclosed in
  square brackets. The order of the values is as follows, meanings of the value
  identifiers are as described for the JSON version of the call above:
  ```
  [id, name, schema, transform]
  ```

##### Template metadata objects have the following keys:

- `id`: A string identifier for this template
- `name`: A more descriptive name string for the template
- `schema`: String containing the filename of the schema file that defines the
  template
- `transform`: String containing the filename of the XSLT transform to transform
  profiles based on this template into application input files

##### Errors:

- `500` (Internal Server Error): if an unexpected error occurs when generating
  the JSON (result of a JSONException).

##### Test using `curl`:

```bash
curl -i -H "Accept: application/json" -X GET http://localhost:8080/tempss/api/template
```

##### Returns:

```json
{
  "components": [
    {
      "schema":"NektarCardiacElectrophysiology.xsd",
      "transform":"LibhpcNektarToTrueNektar.xsl",
      "name":"Nektar++ Cardiac Electrophysiology Solver",
      "id":"nektar-cardiac-electrophysiology"
    },
    {
      "schema":"NektarCompressible.xsd",
      "transform":"LibhpcNektarToTrueNektar.xsl",
      "name":"Nektar++ Compressible Flow Solver",
      "id":"compressibleflowsolver"
    },
    {
      "schema": "IncompressibleNavierStokes.xsd",
      "transform": "LibhpcNektarToTrueNektar.xsl",
      "name": "Nektar++ Incompressible Navier-Stokes Solver",
      "id": "incompressiblenavierstokes"
    }
  ]
}
```

### Get all template IDs

Returns a list of all template IDs in text or JSON format.

```
GET /api/template/ids
```

##### Client accept header:

- `application/json` returns a JSON array of strings, each string representing a
  template ID.
- `text/plain` returns a plaintext response with one template ID per line.

##### Errors:

- `500` (Internal Server Error): if an unexpected error occurs when generating
  the JSON (result of a JSONException).

##### Test using `curl`:

```bash
curl -i -H "Accept: application/json" -X GET http://localhost:8080/tempss/api/template/ids
```

##### Returns:

```json
[
  "nektar-cardiac-electrophysiology",
  "compressibleflowsolver",
  "incompressiblenavierstokes"
]
```

### Get all template names

Returns a list of all template names in text or JSON format. Note that these are
the verbose/descriptive names for templates, not the IDs.

```
GET /api/template/names
```

##### Client accept header:

- `application/json` returns a JSON array of strings, each string representing a
  template name.
- `text/plain` returns a plaintext response with one template name per line.

##### Errors:

- `500` (Internal Server Error): if an unexpected error occurs when generating
  the JSON (result of a JSONException).

##### Test using `curl`:

```bash
curl -i -H "Accept: application/json" -X GET http://localhost:8080/tempss/api/template/names
```

##### Returns:

```json
[
  "Nektar++ Cardiac Electrophysiology Solver",
  "Nektar++ Compressible Flow Solver",
  "Nektar++ Incompressible Navier-Stokes Solver"
]
```

### Get rendered template HTML

Get the HTML application parameter tree for the template with a given
`templateId`.

```
GET /api/template/id/:templateId
```

##### Parameters:

- `templateId`: template ID for the required template.

##### Client accept header:

- `text/html`: returns a block of HTML representing the interactive tree. Note
  that the output of this call is a fragment of HTML that is intended to be
  included within a complete page. Certain JavaScript and CSS assets must be
  loaded in the target page for this data to be correctly displayed.

##### Errors:

- `404` (not found) - if the specified `templateId` is not known
- `500` (Internal Server Error) - if an unexpected error occurs while converting
  the template schema to HTML. This results from a number of different
  exceptions that may occur in the code.

##### Test using `curl`:

```bash
curl -i -H "Accept: text/html" -X GET http://localhost:8080/tempss/api/template/id/incompressiblenavierstokes
```

##### Returns:

```html
<div id="schema-tree" xmlns:libhpc="http://www.libhpc.imperial.ac.uk/SchemaAnnotation">
  ...
</div>
```

## Profile operations

At present the service does not handle the storage and management of profiles.
It only handles the XSLT transform process to undertake conversion of profiles
to application input files. Profile management is intended to be handled
individually by clients for which there is some support in the client-side
JavaScript tools for this service.

### Convert XML profile into required format

Converts the provided XML profile into the required format for the target
application, as defined by the transform that is registered in the metadata for
the template represented by `templateId`. This operation returns JSON data that
provides the result of the conversion and URLs to a set of resulting files. The
client-side JavaScript library for the tempss service provides support for
generating profile XML content from a tree and for sending this to the server
for conversion.

```
POST /api/profile/[templateId]/convert
```

##### Parameters:

- `templateId`: the ID of the template to which this profile conversion call
  relates.

##### Client accept header:

- `multipart/form-data`: the multipart request must contain one or more file
  elements, the first being the XML content for the profile and any subsequent
  elements providing files that are specified using filename placeholders within
  the profile. These are identified by two tags:

  - `xmlupload`: the multipart file object representing the profile;
  - `xmlupload_file`: any number of additional files that are to be embedded
    into the profile.

  *Note to developers:* You may need to increase the maximum file upload size
  supported by your server if users are intending to upload large files as
  elements of their profiles.

##### The returned JSON objects have the following keys:

- `status`: a string containing:
  - `"OK"` if the call completed (the transform could potentially still have failed);
  - `"ERROR"` in the event of an error that didn't generate one of the other
    possible error responses.
- `FullXmlInputs`: a URL that represents the full XML input to the transform,
  including the full content of any additional input files integrated into the
  profile XML.
- `BasicXmlInputs`: a URL to the original profile XML that was provided,
  containing the names of any additional files as placeholders. The additional
  files have not been substituted into the XML as they are in `FullXmlInputs`.
- `TransformedXml`: a URL to the data resulting from the transform. This data
  will be the input data for the application to be run. It may be wrapped in an
  XML wrapper and need extracting before it can be directly passed as input to
  the application.
- `TransformFailed`: a string containing
  - `"true"` if the transform didn't complete successfully;
  - `"false"` if it was successful
- `TransformErrorMessages`: if `TransformFailed` is true, this may contain one
  or more error messages explaining the problem.

##### Errors:

- `404` (not found) - if the specified `templateId` is not known.
- `400` (bad request) - if required file data is not provided in the request.
- `500` (Internal Server Error) - in case of a range of unexpected error cases:
   - problems working with JSON objects;
   - unable to read the file data in the request into a string for processing;
   - problems undertaking transform of the profile to the application input format;
   - unable to get the base URL for the generated output files.

##### Test using `curl`:

To test manually using curl, assuming you wish to use the Incompressible
Navier-Stokes solver, and have a file `IncNSProfile.xml` containing your XML
profile which in turn references a geometry file named CylinderGeometry.xml:

```bash
curl -i -H "Accept: application/json" -F xmlupload=@IncNSProfile.xml -F xmlupload_file=@CylinderGeometry.xml -X POST http://localhost:8080/tempss/api/profile/incompressiblenavierstokes/convert
```

##### Returns:

```html
{
  "TransformedXml": "http://localhost:8080/tempss/temp/output_xml_9BBAAC8B8B82A483F816763444D7407A.xml",
  "BasicXmlInputs": "http://localhost:8080/tempss/temp/basic_input_xml_9BBAAC8B8B82A483F816763444D7407A.xml",
  "FullXmlInputs": "http://localhost:8080/tempss/temp/full_input_xml_9BBAAC8B8B82A483F816763444D7407A.xml",
  "TransformFailed": "false",
  "TransformErrorMessages": "",
  "status": "OK"
}
```
