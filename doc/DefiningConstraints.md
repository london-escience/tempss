# Defining Constraints for TemPSS Templates

Parameters in TemPSS templates will often have constraints that cross branches of the template tree. Such constraints are challenging to represent and manage within a tree-based template structure. TemPSS' constraints functionality has been developed to address this and to help scientists to represent complex dependencies between values in their template trees. This, in turn, will support users of TemPSS in better understanding the configuration process for a code and also in developing their configuration profiles correctly and more efficiently. 

Constraints are defined using an XML-based language that is used to define the variables involved in a constraint relationship. Constraints are defined between pairs of variables and for each pair of variables, valid combinations of values are defined. 

*NOTE: For higher-order constraint relationships that involve more than two variables, you should break these down into two or more binary constraint relationshps.*

## Constraint Solving in TemPSS

Where groups of variables in a TemPSS template have constraints defining which combinations of their values are valid, it is necessary for a scientist to understand how to set these values correctly when setting up a profile. If the values are not correctly set, in some cases, the target code may fail to start running and the error will be obvious. However, there are cases where it may not be immediately obvious that values are invalid and a code may run but produce erroneous results, or only fail after a significant amount of computation has been carried out.

This document describes the specification of constraint relationships from a developer perspective, explaining how to prepare a constraint definition document for a template and how to link it to the template for use in TemPSS.

TemPSS uses a Constraint Satisfaction Problem solver to determine the groups of values that are valid for constrained variables. When a selection is made for any of these variables, the solver is run and identifies all the possible choices for remaining constrained variables. The user interface is then updated to make only the valid options available for selection.

You can find more detail about working with constraints from the user perspective in the "Working with Constraints in TemPSS Templates" document.

## The TemPSS Constraint Language

The TemPSS constraint language has been designed to be as straightforward as possible to work with. It has a simple structure for defining variables, their domains and the constraints between them.

At the root of a constraint definition document is the `<TempssConstraints>` element. This contains sub-elements for `<varliables>` and `<constraints>`. As such, the basic structure of a constraint definition document looks like this:

```xml
<?xml version="1.0" encoding="UTF-8"?> 
<TempssConstraints     
    xmlns="http://libhpc.doc.ic.ac.uk/tempss/constraints"  
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://libhpc.doc.ic.ac.uk/tempss/constraints TempssConstraints.xsd">

    <variables>
      <!-- Details of variables and their domains go here. -->
    </variables>
    
    <constraints>
       <!-- Details of constraints go here. -->
    </constraints>

</TempssConstraints>
```

### Defining Variables
Within the `variables` block, each variable is defined using a `<variable>` element. This provides details of the variable and its domain of possible values. The variable element is defined as follows:

__Element:__ `variable`<br/>
 
###### Attributes
 
 * ___path___: The dot-separated path of the target element within the TemPSS template tree, _excluding_ the root node. e.g. for an EquationType variable that appears under the ProblemSpecification branch of the Nektar++ AdvectionDiffusionReaction solver - ProblemSpecification.EquationType
 * ___name___: A name identifying the variable. This should be unique within this constraint document and will be used to identify variables within constraint mapping definitions.

###### Sub-elements

 * `domain`: A single domain element must be provided for each variable. The domain element contains zero or more `value` elements each defining a value that the variable can be assigned.

###### Example

Consider the variable `DiffusionAdvancement` for the Nektar++ `AdvectionDiffusionReaction` solver. It is located under the `NumericalAlgorithm` -> `TimeIntegration` -> `DiffusionAdvancement` tree branch and has three possible values `NotProvided`, `Implicit` or `Explicit`. We have decided to use the identifier `DiffusionAdvancement` to identify the variable in constraint definitions. _Note that in some cases there may be variables in different tree branches that have the same name. The ability to define a variable identifier is useful to avoid name conflicts while being able to specify a short name as an identifier when referring to the variable to make mapping definitions less verbose._

This variable definition would be specified using the following XML:

```xml
<variable 
      path="NumericalAlgorithm.TimeIntegration.DiffusionAdvancement" 
      name="DiffusionAdvancement">

    <domain>
        <value>NotProvided</value>
        <value>Implicit</value>
        <value>Explicit</value>
    </domain>
</variable>
```

### Defining Constraints
Within the `constraints` block, constraints are defined as a series of `<mapping>` elements. Each mapping element defines a constraint of the form:

"When variable _X_ has value _a_, variable _Y_ (the target variable in the constraint relationship) must have a value that is one of _p_, _q_, _r_, ...". 

The structure of a mapping element is now described.

__Element:__ `mapping`<br/>
 
###### Attributes

 * ___variable___: The first variable identifier (variable _X_ in the above mapping definition). This identifier must match the `name` attribute for one of variables defined in this constraint definition document.
 * ___varValue___: A value for the variable (value _a_ in the above mapping definition). This value must be present in the list of domain values for the variable in its definition in the `variables` section. 
 * ___targetVariable___: The second variable identifier (variable _Y_ in the above mapping defintiion). This identifier must match the `name` attribute for one of variables defined in this constraint definition document.

###### Sub-elements

 * `targetValue`: A targetValue element must be provided for each value that is compatible with the varValue value for the first variable.

###### Example

This example demonstrates how to define constraint mappings for a constraint between two variables `NumericalAlgorithm` -> `Projection` and `NumericalAlgorithm` -> `UpwindType`. This is a simple constraint relationship, with each variable domain containing only two values, as such it provides an ideal relationship to illustrate the approach of defining mappings. Variables with larger value domains and more complex relationships may require a larger number of mapping elements to encapsulate the possible combinations between each of their values. 

To help understand the constraint mappings the example below also shows the variable definitions for these two variables in the example below. This is not, however, a full XML constraint definition document since we ommit the root node and namespace definitions.

```xml
<variables>
  <variable path="NumericalAlgorithm.Projection" name="Projection">
    <domain>
      <value>ContinuousGalerkin</value>
      <value>DiscontinuousGalerkin</value>
    </domain>
  </variable>
      
  <variable path="NumericalAlgorithm.UpwindType" name="UpwindType">
    <domain>
      <value>Off</value>
      <value>Upwind</value>
    </domain>
  </variable>
</variables>

<constraints>
  <!-- Projection -> UpwindType constraints -->
  <mapping variable="Projection" varvalue="ContinuousGalerkin" targetVariable="UpwindType">
    <targetValue>Off</targetValue>
  </mapping>
  
  <mapping variable="Projection" varvalue="DiscontinuousGalerkin" targetVariable="UpwindType">
    <targetValue>Upwind</targetValue>
  </mapping>
</constraints>
```

Constraints between pairs of variables only need to be added in one direction. When the TemPSS constraints framework processes the mappings in the above definition, it creates an equivalent mapping between `UpwindType` and `Projection`.

## Adding Constraints to TemPSS

Once you have written a constraint definition XML file, you need to link this into your TemPSS template definition.

Your constraint definition XML file should be placed in the `src/main/resources/Constraints` directory.

In your template definition properties file that is stored in the `src/main/resources/Template` directory of the TemPSS project, you need to add an additional line specifying the constraints file. For example, for a template that has its `component.id` value set to `advectiondiffusion` and constraints stored in the file `src/main/resources/Constraints/ADRConstraints.xml` you would add the following line to the bottom of the template properties file:

```
advectiondiffusion.constraints=ADRConstraints.xml
```

This will cause TemPSS to load and process the constraints when it initialises the associated template.


For details on working with constraints from the user perspective see "Working with Constraints in TemPSS Templates"