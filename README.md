# BrainTracker_API
This is the API for the BrainTracker APP.
Please read the following contents for the build and deployment process of the API.

## Getting Started
Please follow the following instructions (in order) to build and deploy the API:-

### Prerequisites
1. Install Gradle on the machine on which the API is supposed to deploy -> refer to https://docs.gradle.org/current/userguide/installation.html
2. After gradle has been installed, clone the github repository which contains the API: https://github.com/kgary/BrainTracker_API.git. 
The directory BrainTracker_API/ directory should have a build.gradle and a gradle.properties file
3. Change the gradle.properties file to set up the environment for the deployment. (Read the gradle.properties file)

### Installing
4. To start with the deployment process, please run the command "gradle buildv30".
5. After running that command, you should see a message 'BUILD SUCCESSFUL'.
6. A war file will be copied into directory which defined in gradle.properties.

