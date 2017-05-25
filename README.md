# TemPSS - Templates and Profiles for Scientific Software

### A template and profile manager and editor for handling application inputs for HPC software

TemPSS is a web service for managing application parameter templates and
profiles which are used for collaboratively generating input files for
scientific HPC applications.

The service can be used as a standalone tool that offers a simplified
user-focused environment for preparing application input files or it may be
incorporated into more advanced tools for running scientific HPC applications
via a web interface.

The tool forms an element of the
[libhpc framework](http://www.imperial.ac.uk/lesc/projects/libhpc) that is being
developed to provide a simplified environment for specifying and running HPC
jobs on clusters and clouds.

**Templates** are structures that represent all the parameters that can be used
to configure a particular piece of software or a component of a piece of
software. These parameters are grouped and documented in a manner that is
designed to make them easy for users to understand. Note that the parameters
represented in a template may not map directly to individual parameters in an
application's input file - they may be structured differently to aid users in
defining their application configuration.

**Profiles** are instantiations of templates. A profile consists of values for
some or all of a given template's parameters. The corresponding template
contains validation data for each of its parameters. A profile that has valid
values provided for all the required parameters in a template is classed as a
valid profile and can be used to generate an input for for a job.

#### Building and Running the Service

TemPSS is a Java Web Service. The service can be built using
[Apache Maven](http://maven.apache.org) and requires Java 1.7 (Java 7) or above. To build, clone
the repository, and run `mvn package` in the project base directory.

If the build completes successfully, output files will be generated in the
`target` directory.

The service is contained within `target/tempss.war`.

The service can be deployed in [Apache Tomcat](http://tomcat.apache.org/). Copy
the `tempss.war` file to your `${CATALINA_HOME}/webapps/` directory to deploy
the service. If your Tomcat server is configured to run on port 8080, the
service will be accessible at `http://localhost:8080/tempss`.

Whilst it is strongly recommended to package the service as a .war archive for production use, the service can be also run locally by simply invoking the command `mvn jetty:run-war` in the project base directory. The
service again will be accessible at `http://localhost:8080/tempss`.

The interface at `http://localhost:8080/tempss/component.jsp` provides an example of a simple interface to display template trees. The interface available at `http://localhost:8080/tempss` is a more advance profile manager tool and is the recommended interface for general use of TemPSS.

---

__NOTE:__ The database file `profiles.db` used by a running TemPSS instance is, by default, stored within the deployed `tempss.war` file.
This simplifies the process of deploying TemPSS but each time you redeploy, the database will 
be overwritten by the base version in `src/main/resources`. This means that any stored profiles will be lost. To avoid this, you should place your database file in a location on your system that is outside the Tomcat server tree. Edit `src/main/webapp/WEB-INF/spring/applicationContext.xml` and within the `<bean id="dataSource"...` section, edit the `<property name="url"...` element and set the value to the location of your `profiles.db` file. For example, if the file is placed in `/home/myuser/tempss` you would set the value to `jdbc:sqlite:/home/myuser/tempss/profiles.db`.  

---

###### Using Docker
A Dockerfile is provided to support building a [docker](https://www.docker.com) image that can be used to start a container running the TemPSS service. To build the image, clone the repository and change into the base repository directory, `tempss`, where the `Dockerfile` file is located. From here you can use docker's command line tool to build the image:

`sudo docker build --rm=true -t <my tag> .`

You should replace `<my tag>` with a tag that will be used to identify the image within your local docker image store.

Assuming the image builds successfully, `sudo docker images` should show the new image listed.

You can now start a container based on this image. Network ports for the running services will be mapped to ports on the host system and these mappings are configured in the run command. `${HOST_IP}` should be the IP of the interface on the host server that will receive requests to forward to the container. Currently the image is configured to run SSH on port 22 and Apache Tomcat on port 8080. 

_To be able to SSH to the container, you will need to uncomment the line in the Dockerfile that creates an authorized\_keys file and paste your public key into this line. The container is run as follows:_

```
sudo docker run -t -d -p ${HOST_IP}:8080:8080 \
   -p ${HOST_IP}:8022:22 --name="tempss" <my tag>
```

You can adjust the first port number in the `-p` switch values to change the port on your host system that will listen for requests and forward them to the docker container. You can SSH to the container from your host machine by specifying the port on localhost that you have selected as port to forward to SSH on the container, e.g.

`ssh -i ~/.ssh/<private key file> -p 8022 root@${HOST_IP}`

#### Configuration

A configuration file `tempss.conf` can be used to provide static configuration information to the TemPSS service. This file is read once at startup. The file is an [INI-style](https://en.wikipedia.org/wiki/INI_file) configuration file. The file can be placed in `/etc/` or in the home directory of the user running the TemPSS service in the `.libhpc` directory, e.g. `/home/myuser/.libhpc/tempss.conf`. A configuration file placed in `~/.libhpc/tempss.conf` takes precedence over a `tempss.conf` file placed in `/etc`.

###### tempss.conf section: [template-ignore]
The configuration file currently supports only one section `[template-ignore]` which is used for hiding the display of certain templates. In cases where an administrator does not want all the default TemPSS templates to appear as options in the user interface, template IDs can be added to the template-ignore section of the configuration file. Each entry should appear on a separate line and can be a full template ID, or a partial ID followed by a wildcard `*` character. Note that, at present, the wildcard character can only appear at the end of a string. For example, to hide all templates that have an id beginning with `bio-`, enter a line into the `[template-ignore]` section of `tempss.conf` containing `bio-*`.

#### User Accounts

TemPSS now has support for user accounts. Registered users can save their profiles as private profiles that are only visible to them. They can also create new public profiles that are visible to all users. Unregistered users or users who are not logged in cannot save profiles but they are able to load existing public profiles and use/extend them. When an unregistered user completes a profile, they are able to convert this to an application input file which they can then download.

###### Standard Authentication

By default the user accounts implementation stores account information in the local database with secure password storage. Accounts that users create are local to the TemPSS system.

###### LDAP Authentication

We also provide support for LDAP authentication. LDAP authentication can be configured by editing the [src/main/webapp/WEB-INF/spring/security.xml](../blob/master/src/main/webapp/WEB-INF/spring/security.xml) file.

To enable LDAP authentication, uncomment the `<authentication-provider ref="ldapAuthenticationProvider"/>` tag in the `<authentication-manager>` element towards the end of the file. Then add your local LDAP configuration into the `ldapAuthenticationProvider` bean. The bean that is enabled by default is for authentication against an Active Directory LDAP infrastructure. To use this you need to replace the placeholder values for each of the three `constructor-arg` elements with your LDAP domain (e.g. EXAMPLE.COM), server URL, and base domain (e.g. dc=example,dc=com). 

If you wish to use a standard LDAP server, an example is given for a standard LDAP authentication provider bean configuration. Comment out the `bean` element for the `ldapAuthenticationProvider` bean that is configured to use the Active Directory provider and replace this with the `bean` element configured to use the standard LDAP provider, changing the properties to fit your local LDAP configuration.

#### Additional Documentation

Additional documentation is available in the `doc` directory covering various aspects of using and working with TemPSS:

The [API Documentation](doc/API.md) describes the TemPSS REST API for programmatically acessing the service.

The [Client-side JavaScript Library](doc/ClientSideLibrary.md) documentation describes the JavaScript API available for programmatically interacting with TemPSS from a client-side web interface.

The [Creating XML Templates](doc/BuildingTemplatesXML.md) documentation describes the process of defining templates using TemPSS' new XML-based template definition language. This offers a simplified approach compared with the original XML Schema-based template definition language which is described in [Creating Templates](doc/CreatingTemplates.md).

[Defining Constraints](doc/DefiningConstraints.md) describes how to specify constraints between parameters in a TemPSS template. [Working with constraints](doc/WorkingWithConstraints.md) describes how to work with constraints that have been defined on a template, from an end-user perspective.


#### Development Team

The TemPSS team includes members from Department of Computing, Imperial College
London and EPCC, University of Edinburgh. Current and former developers and
contributors to the TemPSS project include:

 * Peter Austing
 * Chris Cantwell
 * Jeremy Cohen
 * David Moxey
 * Jeremy Nowell

#### License

This tool is licensed under the BSD New (3-Clause) license. See the `LICENSE`
file in the source tree for full details.

#### Acknowledgements

TemPSS and the template/profile methodology have been developed as part of the
Engineering and Physical Sciences Research Council (EPSRC)-funded libhpc stage I
(EP/I030239/1) and II (EP/K038788/1) projects which are a collaboration between
Imperial College London and The University of Edinburgh.
