# A Dockerfile to create a container running the TemPro temple and profile service
# available from https://www.github.com/london-escience/tempro.git

# Build on the base ubuntu 14.04 image
FROM ubuntu:14.04

MAINTAINER Jeremy Cohen <jeremy.cohen@imperial.ac.uk>

# Expose the ports that services run on
EXPOSE 8080
EXPOSE 8443
EXPOSE 22

# Update package lists and install JDK, Tomcat, Maven, git, and an SSH server
RUN apt-get update && apt-get install -y openjdk-7-jdk tomcat6 git-core maven2 supervisor openssh-server

# Create and configure the tempro user
RUN adduser --disabled-password --gecos "" tempro
RUN usermod -a -G tomcat6 tempro

# Set up SSH key
RUN mkdir -p /root/.ssh/ 
# Add your SSH key here if you want to be able to SSH to the container
#RUN echo "" >> /root/.ssh/authorized_keys

# Switch to tempro user and build and deploy the temproservice
USER tempro
WORKDIR /home/tempro

RUN ["pwd"]

# Can clone the latest code directly from git but for now, adding local copy.
# RUN git clone https://www.github.com/london-escience/tempro.git
# OR create directory manually and copy in local code as done below
RUN ["mkdir", "tempro"]
ADD . /home/tempro/tempro

WORKDIR /home/tempro/tempro
RUN ["pwd"]
RUN ["mvn", "package"]
RUN ["cp", "-p", "target/temproservice.war", "/var/lib/tomcat6/webapps/"]

USER root

RUN echo '; supervisor configuration file for tempro\n\n[supervisord]\nnodaemon=true\n\n[program:sshd]\ncommand=/etc/init.d/ssh start\n\n[program:tomcat6]\ncommand=/etc/init.d/tomcat6 start\nautorestart=false\n'  >> /etc/supervisor/conf.d/tempro.conf

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/supervisord.conf"]
