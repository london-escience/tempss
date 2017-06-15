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
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
    <!-- <link rel="stylesheet" href="../assets/css/bootstrap.min.css"> -->

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
		<div class="col-md-2"></div>
		<div class="col-md-8">
			<div class="well">
			  <h3 style="padding-bottom: 20px;">TemPSS User Registration</h3>
			  <p class="text-info">Registering for a TemPSS account will allow you  
			    to save your own profiles, either as private profiles that only you have 
			    access to, or as public profiles that are available to all registered and 
			    unregistered TemPSS users.</p>
			    
		      <p class="text-info">By saving your profiles, you can avoid the  
		         time consuming process of creating new profiles from scratch  
		         when you want to build a new input file.</p> 
			  <h4 style="padding-bottom: 20px;">Create an account</h4>
				<form:form commandName="tempssUser" class="form-horizontal" 
				           id="registration-form">
				           
				  <form:errors/>
				  
				  <div class="form-group">
				    <form:label path="username" for="username" cssClass="col-sm-3 control-label">Username</form:label>
				    <div class="col-sm-6">
				      <form:input path="username" class="form-control" placeholder="Select a username"/>
				    </div>
				    <div class="col-sm-3">  
				      <form:errors path="username" cssClass="form-error text-danger"/>
				    </div>
				  </div>
				  <div class="form-group">
				    <form:label path="email" for="email" cssClass="col-sm-3 control-label">Email</form:label>
				    <div class="col-sm-6">
				      <form:input path="email" type="email" class="form-control" placeholder=""/>
				    </div>
				    <div class="col-sm-3">  
				      <form:errors path="email" cssClass="form-error text-danger"/>
				    </div>
				  </div>
				  <hr/>
				  <div class="form-group">
				    <form:label path="firstname" for="firstname" cssClass="col-sm-3 control-label">Firstname</form:label>
				    <div class="col-sm-6">
				      <form:input path="firstname" class="form-control" placeholder=""/>
				    </div>
				    <div class="col-sm-3">  
				      <form:errors path="firstname" cssClass="form-error text-danger"/>
				    </div>
				  </div>
				  <div class="form-group">
				    <form:label path="lastname" for="lastname" cssClass="col-sm-3 control-label">Surname</form:label>
				    <div class="col-sm-6">
				      <form:input path="lastname" class="form-control" placeholder=""/>
				    </div>
				    <div class="col-sm-3">  
				      <form:errors path="lastname" cssClass="form-error text-danger"/>
				    </div>
				  </div>
				  <hr/>
				  <div class="form-group">
				    <form:label path="password" for="password" cssClass="col-sm-3 control-label">Password</form:label>
				    <div class="col-sm-6">
				      <input name="password" type="password" class="form-control" placeholder=""/>
				    </div>
				    <div class="col-sm-3">
				      <form:errors path="password" cssClass="form-error text-danger"/>
				    </div>
				  </div>
				  <div class="form-group">
				    <form:label path="password2" for="password2" cssClass="col-sm-3 control-label">Confirm Password</form:label>
				    <div class="col-sm-6">
				      <input name="password2" type="password" class="form-control" placeholder=""/>
				    </div>
				    <div class="col-sm-3">
				      <form:errors path="password2" value="" cssClass="form-error text-danger"/>
				    </div>
				  </div>
				  <sec:csrfInput />
				  <div class="form-group">
				    <div class="col-sm-3"></div>
				    <div class="col-sm-6">
				      <p class="text-info">By registering for a TemPSS  
				      account you confirm that you have read and accept our 
				      <a href="/tempss/profiles/privacy">terms</a> and 
				      <a href="/tempss/profiles/privacy">privacy policy</a>.
				    </div>
				    <div class="col-sm-3"></div>
				  </div>
				  <div class="form-group text-right">
				    <div class="col-sm-3"></div>
				    <div class="col-sm-8">
				      <button type="submit" id="register-user" class="btn btn-success">Register</button>
				    </div>
				  </div>
			    </form:form>
			</div>
			<div class="col-md-2"></div>
		</div>
	</div>

    </div><!-- /.container -->

    <!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
    <!-- <script src="../assets/js/jquery-1.11.0.min.js"></script> -->
    <!-- Latest compiled and minified JavaScript -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
    <!-- <script src="../assets/js/bootstrap.min.js"></script> -->
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