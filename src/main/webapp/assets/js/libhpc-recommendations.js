// These are just used by the IDE (Visual Studio) to enable auto-completion.
// So use the full, not min, version in order to et full param names.

/// <reference path="jquery-1.11.0.js" />
/// <reference path="jquery.xpath-0.2.5.js" />
/// <reference path="bootstrap-tree.js" />



// variable is the variable to which the recommendation applies. It must haver a full path. Eg
// "CardiacElectrophysiology.Physics.ProblemSpecification.Expansion.PolynomialOrder"
function recommendations(variable) {

    // It is probably not efficient to build the xml to get hold of variables, but it is OK for now.
    var xmlString = getXMLString();

    switch (variable) {

        case "CardiacElectrophysiology.NumericalAlgorithm.GlobalOptimizationParameters.BackwardTransform":
            {
                var polynomialOrder = getVar(variable);

            }
            break;





    }





}
