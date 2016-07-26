<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="TemPSS Sample Profile Manager">
    <meta name="author" content="TemPSS Project Team">
    <!-- <link rel="icon" href="../../favicon.ico">  -->

	<title>TemPSS :: Profile Manager :: Registration</title>
	
	    <!-- Bootstrap core CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
    <!-- <link rel="stylesheet" href="../temp/css/bootstrap.min.css">  -->

    <!-- Custom styles for this template -->
    <link href="../assets/css/tempss.css" rel="stylesheet">
    <link rel="stylesheet" href="../assets/css/tree-style.css" media="all" />

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
  </head>

  <body>

    <nav class="navbar navbar-inverse navbar-fixed-top">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="#">TemPSS Template Manager</a>
        </div>
        <div id="navbar" class="collapse navbar-collapse">
          <ul class="nav navbar-nav">
            <li class="active"><a href="/tempss/profiles/">Home</a></li>
          </ul>
          <span class="navbar-brand" style="float: right; color: #d5d5d5; font-size: 16px;">TemPSS - Templates and Profiles for Scientific Software</span>
        </div><!--/.nav-collapse -->
      </div>
    </nav>

    <div class="container">

	<div class="row">
		<div class="col-md-3"></div>
		<div class="col-md-7">
			<div class="well">
			  <h3 style="padding-bottom: 20px;">New JSP Registration Page</h3>
			  <h3 style="padding-bottom: 20px;">TemPSS User Registration</h3>
			  <h4 style="padding-bottom: 20px;">Create an account</h4>
				<form class="form-horizontal" id="registration-form">
				  <div class="form-group">
				    <label for="input-username" class="col-sm-3 control-label">Username</label>
				    <div class="col-sm-8">
				      <input type="text" name="username" class="form-control" id="input-username" placeholder="Select a username">
				    </div>
				  </div>
				  <div class="form-group">
				    <label for="input-email" class="col-sm-3 control-label">Email</label>
				    <div class="col-sm-8">
				      <input type="email" name="email" class="form-control" id="input-email" placeholder="">
				    </div>
				  </div>
				  <hr/>
				  <div class="form-group">
				    <label for="input-firstname" class="col-sm-3 control-label">Firstname</label>
				    <div class="col-sm-8">
				      <input type="text" name="firstname" class="form-control" id="input-firstname" placeholder="">
				    </div>
				  </div>
				  <div class="form-group">
				    <label for="input-lastname" class="col-sm-3 control-label">Surname</label>
				    <div class="col-sm-8">
				      <input type="text" name="lastname" class="form-control" id="input-lastname" placeholder="">
				    </div>
				  </div>
				  <hr/>
				  <div class="form-group">
				    <label for="input-password1" class="col-sm-3 control-label">Password</label>
				    <div class="col-sm-8">
				      <input type="password" name="password" class="form-control" id="input-password1" placeholder="">
				    </div>
				  </div>
				  <div class="form-group">
				    <label for="input-password2" class="col-sm-3 control-label">Confirm Password</label>
				    <div class="col-sm-8">
				      <input type="password" name="password2" class="form-control" id="input-password2" placeholder="">
				    </div>
				  </div>
				  <input type="hidden" name="{{ _csrf.parameterName }}" 
				         value="{{ _csrf.token }}" />
				  <div class="form-group text-right">
				    <div class="col-sm-3"></div>
				    <div class="col-sm-8">
				      <button type="submit" id="register-user" class="btn btn-success">Register</button>
				    </div>
				  </div>
			    </form>
			</div>
		</div>
		<div class="col-md-2"></div>
		{% for error in bindingResult.errors %}
		  Field: {{ error.field }}
		  <br/>
		  Error: {{ error.defaultMessage }}
		  <br/>
		{% endfor %} 
	</div>

    </div><!-- /.container -->

    <!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
    <!-- <script src="../temp/js/jquery-1.11.0.min.js"></script> -->
    <!-- Latest compiled and minified JavaScript -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js"></script>
    <!-- <script src="../temp/js/bootstrap.min.js"></script> -->
    <!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
    <!-- <script src="../../assets/js/ie10-viewport-bug-workaround.js"></script>  -->
    
    <!-- Local script file for the template manager app -->
    <script src="../assets/js/tempss-manager.js"></script>
	<script type="text/javascript">
	$(document).ready( function() {
		log("Document ready...");
		
		$('#registration-form').submit(function(e) {
			submitRegistrationForm(e);
		});
	});
	
	function submitRegistrationForm(e) {
		log('Request to submit registration form...');
		e.preventDefault();
		var formData = $('#registration-form').serialize();
		$.ajax({
			url: '/tempss/profiles/register',
			method: 'POST',
			data: formData
		}).done(function() {
			// Success function
			log("Form submission successful...");
		}).fail(function() {
			// Error function
			log("Form submission error...");
		})
	}
	
	</script>
  </body>
</html>