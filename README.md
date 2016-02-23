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
[Apache Maven](http://maven.apache.org) and requires Java 1.7+. To build, clone
the repository, and run `mvn package` in the project base directory.

If the build completes successfully, output files will be generated in the
`target` directory.

The service is contained within `target/tempss.war`.

The service can be deployed in [Apache Tomcat](http://tomcat.apache.org/). Copy
the `tempss.war` to your `${CATALINA_HOME}/webapps/` directory to deploy
the service. If your Tomcat server is configured to run on port 8080, the
service will be accessible at `http://localhost:8080/tempss`.

Whilst it is strongly recommended to package the service as a .war archive for production use, the service can be also run locally by simply invoking the command `mvn jetty:run-war` in the project base directory. The
service again will be accessible at `http://localhost:8080/tempss`.

The interface at `http://localhost:8080/tempss/component.jsp` is an example of a simple interface to display template trees. The interface available at `http://localhost:8080/tempss` is an example of a more advance profile manager tool.

###### Using Docker
A Dockerfile is provided to support building a [docker](https://www.docker.com) image that can be used to start a container running the TemPSS service. To build the image, clone the repository and change into the base repository directory, `tempss`, where the `Dockerfile` file is located. From here you can use docker's command line tool to build the image:

`sudo docker build --rm=true -t <my tag> .`

You should replace `<my tag>` with a tag that will be used to identify the image.

Assuming the image builds successfully, `sudo docker images` should show the new image listed.

You can now start a container based on this image. Network ports for the running services will be mapped to ports on the host system and these mappings are configured in the run command. `${HOST_IP}` should be the IP of the interface on the host server that will receive requests to forward to the container. Currently the image is configured to run SSH on port 22 and Apache Tomcat on port 8080. To be able to SSH to the container, you will need to uncomment the line in the Dockerfile that creates an authorized_keys file and paste your public key into this line. The container is run as follows:

`sudo docker run -t -d -p ${HOST_IP}:8080:8080 -p ${HOST_IP}:8022:22 --name="tempss" <my tag>`

You can adjust the first port number in the `-p` switch values to change the port on your host system that will listen for requests and forward them to the docker container. You can SSH to the container from your host machine by specifying the port on localhost that you have selected as port to forward to SSH on the container, e.g.

`ssh -i ~/.ssh/<private key file> -p 8022 root@localhost`

#### Documentation

[API Documentation](doc/API.md)

[Client-side JavaScript Library](doc/ClientSideLibrary.md)

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
