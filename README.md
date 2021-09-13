# aws-serverless-framework-app

## About the application
It is a [Serverless Framework](https://www.serverless.com/) application that implements a CRUD API with Lambda handlers written in Java.

The architecture of the application is in the picture given below.

![image](https://miro.medium.com/max/1400/1*hkYSOC3c1xv9svTJOzgLsA.png)

Please read more about the application in my [Medium blog post](https://medium.com/@rostyslav.myronenko/the-serverless-framework-for-an-aws-serverless-java-application-42beba675283).

## Prerequisites
- Java 1.8+
- Apache Maven
- Free account registered at https://www.serverless.com/
- Serverless Framework's Open Source CLI is installed and configured as described at https://www.serverless.com/framework/docs/getting-started
- AWS Access Key for a user with admin access (to be used in Provider configuration)

## serverless.yml file
It is a descriptor of a Serverless Frameowrk application that provisions and configures resources and infrastrucrure in a Public Cloud (for current application it is AWS)
To get the detailed information, please refer to the official documentation at https://www.serverless.com/framework/docs/providers/aws/guide/serverless.yml

## Build and deployment

To build and deploy with Maven and the Serverless Framework CLI, please execute the sequence of the commands given below in the project root:
```
mvn clean package
sls login
sls deploy
```
