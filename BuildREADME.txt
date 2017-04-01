This file is a readme file for the build and deployment process of the API.

Please follow the following instructions (in order) to build and deploy the API:-

1. Install Gradle on the machine on which the API is supposed to deploy -> refer to https://docs.gradle.org/current/userguide/installation.html
2. After gradle has been installed, clone the github repository which contains the API. The directory apiv3 directory should have a build.gradle and a gradle.properties file
3. Change the gradle.properties file to set up the environment for the deployment. (Read the gradle.properties file)

4. To start with the deployment process, please run the command "gradle -q compile"
5. After running that command, you need to wait and monitor the server Tomcat-webapps directory for the war file to get extracted into a folder
6. After the war file is extracted and you see a folder with the same name as the war file, you need to run the command "gradle -q deploy" to finish the deployment process
