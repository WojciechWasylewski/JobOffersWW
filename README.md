<h1 align="center">Job Offers Application
</h1>

# Job Offers Web Application

This project is a web application created in Spring Boot, allowing users to search for and add new job offers. The architecture of the project is designed using the Facade Architecture pattern.

## Features
- **Search Job Offers:** Users can search for job offers within the application.
- **Add New Job Offers:** Users can add new job offers to the system.

## Deployment
The application is deployed on AWS, and you can access the [Swagger UI](http://ec2-3-79-99-187.eu-central-1.compute.amazonaws.com:8000/swagger-ui/index.html#/github-rest-controller/getAllRepositories) to interact with its endpoints.
## Diagram
<img src="diagram.png">
<br>
<br>

|       ENDPOINT        | METHOD  |         REQUEST          |       RESPONSE       |                    FUNCTION                     |
|:---------------------:|:-------:|:------------------------:|:--------------------:|:-----------------------------------------------:|
|        /offers        |  GET    |            -             |    JSON (offers)     |                returns all offers               |
|        /offers        |  POST   |    JSON BODY (offer)     |      JSON (uuid)     |                creates new offer                |
|     /offers/{uuid}    |  GET    |   PATH VARIABLE (uuid)   |     JSON (offer)     |          returns offer with given uuid          |
|         /token        |  POST   |  JSON BODY (credentials) |   JSON (JWT token)   | returns token after successfully authorization  |
|       /register       |  POST   |     JSON BODY (user)     |      JSON (id)       |                 creates new user                |

## Tech Stack
Code: <br>
![Static Badge](https://img.shields.io/badge/java_17-orange?style=for-the-badge&logo=openjdk&logoColor=white)
![Static Badge](https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-%234ea94b.svg?style=for-the-badge&logo=mongodb&logoColor=white)
<br>
Tests: <br>
![image](https://img.shields.io/badge/Junit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![image](https://img.shields.io/badge/Mockito-78A641?style=for-the-badge)
![image](https://img.shields.io/badge/Testcontainers-9B489A?style=for-the-badge)
![image](https://img.shields.io/badge/WireMock-ac4642?style=for-the-badge) 
<br>
Other: <br>
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![Swagger](https://img.shields.io/badge/-Swagger-%23Clojure?style=for-the-badge&logo=swagger&logoColor=white)
![image](https://img.shields.io/badge/maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

## Solved Problems
Throughout the development of this project, I encountered a variety of challenges. Here are a few of them.
<ul>
    <li>Code organization with independent modules - Facade architecture</li>
    <li>Implementing facades was a key aspect of the project.</li>
    <li>I ensured that my code was encapsulated, allowing me to hide implementation details and making it easier to use individual components.</li>
    <li>I implemented authorization using JWT tokens and security mechanisms provided by Spring Security, ensuring the security of my application.</li>
    <li>Integration tests using TestContainers</li>
    <li>Applying SOLID principles</li>
    <li>I deployed my application on AWS, utilizing EC2 for hosting and ECR for container storage.</li>
    <li>Effective use of GitHub was crucial for organized and efficient project development. I focused on proper branch usage, pull request management, and maintaining a clear commit history.</li>
</ul>
