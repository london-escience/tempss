# Working with Constraints in TemPSS Templates

The "Defining Constraints for TemPSS Templates" document describes the process of specifying constraints for a TemPSS template. This document provides information on working with these constraints from a user perspective within the TemPSS web-based interface.

When a template is loaded in TemPSS, if it has constraints defined, these will be processed and a link symbol and text will be shown alongside the root node in the profile editor:

![ADR solver root node with constraints](img/ADRRootConstraints.png)

## Constraint Details

An overview of the constraints defined for a solver template can be accessed by clicking the details link alongside the template root node (as shown in the above figure). When the link is clicked a pop-up box appears showing the details:

![ADR solver variable domains](img/ADRVariableDomains.png)

The variables tab in the above figure shows all the variables for the current solver that have constraints defined. Selecting the constraints tab displays pairs of constrained variables along with an overview of their value mappings. The figure below shows part of a constraint mapping information table:

![ADR solver constraint mappings](img/ADRConstraintMappings.png)

## Making Variable Selections

You can identify variables in a TemPSS template tree that have constraints attached by the link symbol that is displayed alongside them, e.g.:

![ADR solver EquationType parameter](img/ADRConstraintSymbolEqType.png)
![ADR solver constraint mappings](img/ADRConstraintSymbolAdvType.png)

If you place the mouse pointer over the constraint symbol, an overlay will be displayed showing specifying the variables that the selected variable has constraint relationships with, e.g.:

![ADR solver constraint overlay example](img/ADRConstraintOverlay.png)

When selection of a variable value is made from a list of possible choices, the constraint solver is called with the values of all currently set variables that have constraints. The solver identifies the possible values of all these variables given the selection that has been made and the state of all the variables is sent bacl to TemPSS interface.

Taking the AdvectionDiffusionReaction solver as an example, when the template is first opened, the EquationType variable has its full domain of values available as possible options:

![ADR solver EquationType full domain](img/ConstraintsEqType1.png)

There is a constraint between `EquationType` and `AdvectionType`. If an equation type is being used that has no advection term, it is invalid to specify an advection type and this must be set to `NotProvided`.

To demonstrate how TemPSS handles this, we set the `AdvectionType` value to `NotProvided`. The solver is then called and identifies that equation type values that have an advection term should be removed. The state of the list of `EquationType` values after making this change is shown below:

![ADR solver EquationType full domain](img/ConstraintsEqType2.png)

Here we can see that all the `EquationType` options that had an advection term have been removed. When there is more than one choice available for a variable with constraints, the interface shows a drop-down list with "Select from list" as the default option. When there is only a single possible value, only that value is shown. This is demosntrated below where we have chosen the `Poisson` equation type and that becomes the only available option in the list.

![ADR solver EquationType full domain](img/ConstraintsEqType3.png)

At present it is not possible to revert a constrained selection due to the method of solver operation. However, the "Reset constraints" button shown alongside the root node for all templates with constraints will reset all values in a template that have constraints back to their default choices. This is will be resolved with the addition of an undo option in a subsequent version of the system.