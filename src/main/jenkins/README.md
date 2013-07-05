# Hibernate Validator and Bean Validation Jenkins

This directory contains the job configuration of Hibernate Validator joc configured on [Cloudbees](https://hibernate-validator.ci.cloudbees.com/).

Jobs are exported via [Jenkins CLI](https://wiki.jenkins-ci.org/display/JENKINS/Jenkins+CLI) using:

    java -jar jenkins-cli.jar -s https://hibernate-validator.ci.cloudbees.com get-job job > job.xml

To import a job you can run:

    java -jar jenkins-cli.jar -s https://hibernate-validator.ci.cloudbees.com create-job job < job.xml

Authentication is via ssh (see [https://hibernate-validator.ci.cloudbees.com/cli](https://hibernate-validator.ci.cloudbees.com/cli)).