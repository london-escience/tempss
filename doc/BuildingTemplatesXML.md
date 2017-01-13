# Building TemPSS Templates

###### Note: This document relates to the _new XML model_ for creating templates in TemPSS, not the original XML Schema model for creating templates.

## Overview

TemPSS templates are tree-style structures that represent all the possible parameters for a Nektar++ solver, structured according to how they are semantically related.

A TemPSS template is specified by a developer or domain expert user in XML format and is then processed and displayed to users as an HTML tree within the TemPSS web application. Templates were originally specified using an XML Schema format but this was considered to be overly complex and verbose so a new XML format is available. This is the format described in this document. The legacy XML Schema format is still used behind the scenes and old schema-based template definitions will still work correctly within TemPSS.

## Template XML Format Reference

##### Template structure

The XML template definition format has no specific structural requirements beyond that of being a valid XML document. The template definition is a free-form XML structure that can accept tags of any name defining the names of nodes in a template tree. There are, however, some pre-defined tags that may be used to specify key information such as documentation and constraints. These are detailed in the sections below.

##### Parameter naming

We follow the convention of using "[upper camel case](https://en.wikipedia.org/wiki/Camel_case)" (camel case with an intial capital letter, e.g. MyNodeName) for template parameter names and advise the use of this approach when designing templates. 

##### XML declaration

Always begin you template definition with the XML v1.0 declaration:

```<?xml version="1.0" encoding="UTF-8"?>```

##### Root tag

The root tag should be based on the solver name and will be rendered as the base template tree node from which all other nodes inherit. For example, in the case of the Nektar++ Cardiac Electrophysiology solver, the root node may be named:

`<CardiacElectrophysiology>`

##### Node types

In addition to the root node, there are three different types of nodes that may be rendered in a template tree:

1. _Group node_: A group node is rendered in green and is a parent node for one or more _group_, _choice_ or _input_ nodes. A group node can be clicked to display or hide its descendents.
2. _Choice node_: A choice node is rendered in orange and includes a drop down select box containing a set of value choices for the parameter in question. Selecting one of these choices will open a new branch containing additional nodes to be completed.
3. _Input node_: An input node is rendered in blue and may contain a standard input box for keyboard input or a dropdown list containing a set of valid choices for the parameter.

###### Group nodes

A group node can be specified simply by adding an XML tag into the XML template definition. By then adding child nodes to this tag, you are denoting this as a group node. All child nodes will be displayed when the group node is clicked. So, for example, to specify a group node representing _Physics_ properties of a problem that consist of _Model_ and _CellModel_ details, you might add something similar to the following to your XML:

```
<Physics>
    <Model ... > ... </Model>
    <CellModel ... > ... </CellModel>
</Physics>
```

This would be rendered as shown:

![Example of a rendered group node](./img/group-node.png "Example of a renedered group node")

_Note: You can optionally add the `nodeType` attribute with a value of `group` to a group node to aid readability of your template definition._
 
###### Choice nodes

A choice node is denoted by adding the attribute `nodeType` with a value of `choice`. If a node is set as a choice node, instead of being a clickable tree node that opens up to display all the child nodes when clicked, the node is rendered with a dropdown selection box displaying the names of all the child elements. When a selection is made, the node opens up to reveal the selected branch and its nodes.

A choice node may be specified as follows:

```
<MatrixInversion nodeType="choice"> ... </MatrixInversion>
```

This node would be rendered like this:

![Example choice node](./img/choice-node.png "Example choice node")

Choices within a choice node list are formed from its top level descendents, e.g.:

```
<MatrixInversion nodeType="choice">
  <Direct>
    ...
  </Direct>
  <Iterative>
    ...
  </Iterative>
</MatrixInversion>
```
The above structure, with the `Direct` option selected would render like this:

![Example choice node with value selected](./img/choice-node-selected.png "Example choice node with value selected")

###### Input nodes

Input nodes can take the form of either a dropdown list containing possible options for a value, or a text input box where a value can be entered by the user. The `inputType` attribute should be set to either `choice` or `text` to determine the type of input node. Validation information can be added to `text` input nodes so that the user interface can validate the value entered by a user and provide feedback on whether the user has entered a correct value or not. More information on validation is provided in the _[Additional elements](#add-elements)_ section. 

To specify a text input node, you would write something similar to the following:

```
<SubSteps inputType="text"></SubSteps>
```

This would be rendered in the template tree as a text input node:

![Example template text input node](./img/input-node-text-empty.png "Example template text input node")

To specify a choice input node, you would write XML similar to the following:

```
<Projection inputType="choice">
  <libhpc:item>ContinuousGalerkin</libhpc:item>
  <libhpc:item>DiscontinuousGalerkin</libhpc:item>
</Projection>
```

This will result in the node being rendered as shown:

![Example template choice input node](./img/input-node-choice.png "Example template choice input node")

When the select box is clicked, the options will be shown for the user to make a selection:

![Example template choice input node showing dropdown list](./img/input-node-choice-list.png "Example template choice input node showing dropdown list")

#### Additional elements

In addition to the flexible, free-form nature of the element structure for specifying template parameter nodes, there are some pre-defined elements that can be used to define additional template properties. These elements include the ability to specify _parameter documentation_, _validation details_ and _parameter constraints_.

These additional elements belong to the TemPSS _Template Annotations_ schema. This can be imported as the _tempss_ namespace by adding the following attributes to the root node of your XML template definition:

```
xmlns:tempss="http://www.libhpc.imperial.ac.uk/tempss/TemplateAnnotations"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://www.libhpc.imperial.ac.uk/tempss/TemplateAnnotations TemPSSTemplateAnnotations.xsd"
```

The different elements defined in the TemPSS Template Annotation schema and their functionality will now be described.

###### Documentation

Details of how to add documentation to nodes.

###### Validation and Units

Details of how to add validation information and details of parameter value units.

###### Constraints

Details of how to specify constraints on different parameters. The constraint information will need to explain how template developers only add constraint information to source constraints in a tree and this is then processed on the client side to highlight target nodes and verify/process constraints.

## Template conversion

Information about how template conversion is carried out.