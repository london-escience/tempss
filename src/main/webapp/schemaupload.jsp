<!DOCTYPE html>
<html lang="en">
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Job Configuration</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css">
    <link href="./assets/css/style.css" rel="stylesheet" media="all" />
  </head>
  <body>
    <div class="container">
      <div class="page-header">
        <h1>Libhpc Schema Upload</h1>
      </div>
      <div class="jumbotron">
        <form class="form-horizontal" method="POST" action="/tempss/process" enctype="multipart/form-data">
          <div class="form-group">
            <label class="col-md-3 control-label" for="schemaupload">
              Schema:
            </label>
            <div class="col-md-5">
              <input type="file" name="schemaupload" id="schemaupload"/>
            </div>
            <input type="hidden" name="serveraction" value="schemaupload"/>
            <div class="col-md-1">
              <button type="submit" class="btn btn-primary">Submit</button>
            </div>
          </div>
        </form>
      </div>
    </div>

    <!-- ==============================================
         JavaScript below!                                -->
    <!-- jQuery via CDN + local fallback, see h5bp.com -->
    <script src="//code.jquery.com/jquery-1.11.1.min.js"></script>
    <script>window.jQuery || document.write('<script src="./assets/js/jquery-1.11.1.min.js"><\/script>')</script>
    <!-- Bootstrap JS -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="./assets/js/bootstrap-fileinput.js"></script>
  </body>
</html>
