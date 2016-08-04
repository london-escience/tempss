<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

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
    <!-- <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">  -->
    <link rel="stylesheet" href="../assets/css/bootstrap.min.css">

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
		<div class="col-md-6" style="margin-top: 200px;">
			<div class="alert alert-success">
				<b style="font-size:24px;">Registration successful</b>
				<br/><br/>
				<p style="font-size:16px;">You have been successfully 
				registered with TemPSS.</p>
				<br/>
				<p style="font-size:16px;">Thanks for signing up. You can now 
				create and store your own public and private profiles.</p>
			</div>
			
			<div><h5>
				<a href="/tempss/profiles">Return to profile manager</a>
			</h5></div>
		</div>
		<div class="col-md-3"></div>
	</div>
    </div><!-- /.container -->

    <!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <!-- <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>  -->
    <script src="../assets/js/jquery-1.11.0.min.js"></script>
    <!-- Latest compiled and minified JavaScript -->
    <!-- <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js"></script>  -->
    <script src="../assets/js/bootstrap.min.js"></script>
    <!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
    <!-- <script src="../../assets/js/ie10-viewport-bug-workaround.js"></script>  -->
    
    <!-- Local script file for the template manager app -->
    <script src="../assets/js/tempss-manager.js"></script>
	<script type="text/javascript">
	$(document).ready( function() {
		log("Document ready...");
		
		$('#registration-form').submit(function(e) {
			//submitRegistrationForm(e);
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