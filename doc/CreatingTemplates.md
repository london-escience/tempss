# Creating templates

###### Note: This document relates to the _original XML Schema approach_ for creating templates in TemPSS. A new, simpler method using pure XML is now available. Details are provided in [BuildingTemplatesXML.md](BuildingTemplatesXML.md).

Creating a new template is a three-step process:

1. First, an XML schema which defines the properties of the parameter tree is
   created. It should include all necessary parameter choices for a given
   application. Schemas can be found in the `src/main/resources/Schema`
   directory.
2. Then, an XSL transformation is written that converts the XML schema, combined
   with the user's choices, into the desired format. Transforms can be found in
   the `src/main/resources/Transform` directory.
3. Finally, the template needs to be registered by creating a property file and
   placing it in the `src/main/resources/Template` directory.

In this file we document each of these stages by constructing a simple example.

## Creating a schema

TODO

## Creating a transformation

TODO

## Registering the template

Once the template schema and transformation files have been constructed, a
simple properties file must be created so that the new template is detected by
TemPSS. The format of these files is straightforward:

```
component.id=simple-example
simple-example.name=My Simple Example
simple-example.schema=SimpleExample.xsd
simple-example.transform=SimpleExample.xsl
```

Each file contains:

- `component.id`: a simple text string which uniquely identifies the template.
- `id.name`: a more descriptive name for the template.
- `id.schema`: the name of the file containing the schema.
- `id.transform`: the name of the file containing the transform.

Place this in a file called `simple-example.properties` and place it in the
`src/main/resource/Template` directory. Once the service is restarted, the new
template will become available.

Note that multiple component IDs can be registered in the same `.properties`
file; see `gromacs.properties` for an example of how this is done.
