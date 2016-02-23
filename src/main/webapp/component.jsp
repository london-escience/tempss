<!DOCTYPE html>
<html lang="en">
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Job Configuration</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap.min.css">
    <link href="./assets/css/style.css" rel="stylesheet" media="all" />
  </head>
  <body>
    <div class="container">
      <div class="page-header">
        <h1>Libhpc Component Selector</h1>
      </div>
      <div id="libraryload">
        <div class="well">
          <form class="form-horizontal">
            <div class="form-group">
              <label class="col-md-3 control-label" for="libraryxml">
                Load from library:
              </label>
              <div class="col-md-5">
                <input type="file" name="libraryxml" id="libraryxml" />
              </div>
              <div class="col-md-1">
                <button type="button" class="btn btn-primary" onclick="loadlibrary();">Load</button>
              </div>
            </div>
          </form>
        </div>
        <button type="button" class="btn btn-info" onclick="hideForPrinting();">Printable</button>
        <button type="button" class="btn btn-info" onclick="hideInputs();">Hide Inputs</button>
      </div>

      <div id="htmlcontent">
        <div class="jumbotron">
          <form class="form-horizontal">
            <div class="form-group">
              <label class="col-md-3 control-label" for="componentselector">Component:</label>
              <div class="col-md-5">
                <select name="componentselector" id="componentselector">
                  <option value="nektar-cardiac-electrophysiology">Nektar++ Cardiac Electrophysiology</option>
                  <option value="incompressiblenavierstokes">Nektar++ Incompressible Navier-Stokes</option>
                  <option value="compressibleflowsolver">Nektar++ Compressible Flow Solver</option>
                  <option value="gromacs-pdb2gmx">GROMACS pdb2gmx</option>
                  <option value="gromacs-editconf">GROMACS editconf</option>
                  <option value="gromacs-solvate">GROMACS solvate</option>
                  <option value="gromacs-grompp">GROMACS grompp</option>
                  <option value="gromacs-genion">GROMACS genion</option>
                  <option value="gromacs-mdrun">GROMACS mdrun</option>
                </select>
              </div>
              <input type="hidden" name="serveraction" value="componentselector">
              <div class="col-md-1">
                <button type="button" class="btn btn-primary" onclick="submitComponentRequest()">Submit</button>
              </div>
            </div>
          </form>
          <div id="errormessages"></div>
        </div><!-- jumbotron -->
      </div><!-- htmlcontent -->

      <div id="xmlsubmission">
        <div class="btn btn-info" onclick="generateParameterXML();">View XML</div>
        <div class="btn btn-info" onclick="submitXML();">Submit XML</div>
        <div id="xml-results"></div>
      </div>
      <div id="xml-content"></div>
    </div><!-- Container -->

    <!-- ==============================================
         JavaScript below!                                 -->
    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="//code.jquery.com/jquery-1.11.1.min.js"></script>
    <script type="text/javascript" src="./assets/js/bootstrap-tree.js"></script>
    <!-- Latest compiled and minified JavaScript -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.0.3/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="./assets/js/bootstrap-fileinput.js"></script>
    <script type="text/javascript" src="./assets/js/libhpc-parameter-tree.js"></script>
  </body>
</html>
