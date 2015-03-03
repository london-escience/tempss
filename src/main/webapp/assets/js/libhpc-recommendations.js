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
