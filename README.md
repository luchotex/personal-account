# personal-account
Application to handle personal account operations

To perform operations like 

You need to provide the pin in MD5 coding, so each operation you reach is needed authentication.

# Personal Account check

This micro service contains endpoints for administrate a personal Account operations 

### Prerequisites

 * Java 8
 * Maven 3.6

### Run with intellij

Go to Run/Debug view and add the following program argument with your own value, but is added suggested values

```
--DATABASE_CONNECTION=jdbc:h2:mem:g2db;DB_CLOSE_ON_EXIT=FALSE
--DATABASE_USERNAME=sa
--DATABASE_PASSWORD=password
--MAIL_HOST=smtp.gmail.com
--MAIL_USERNAME=g2testservices@gmail.com
--MAIL_PASSWORD=Testg2123@
--MAIL_PORT=587
--SERVICE_URL=http://192.168.0.101:8080
--CONFIRMATION_EXPIRATION_SECONDS=86400
--PIN_LENGTH=4
--PIN_EXPIRATION_SECONDS=180
--NUMBER_PIN_RETRIES=5
--ACCOUNT_LOCKING_SECONDS=86400
--LOCKING_REGISTRIES_SECONDS=30
```

### Compile service

#### Avoid tests and SonarQube Report
mvn clean install -DskipTests -Dsonar.skip=true


### Documentation

This project has swagger documentation you can go to HOST/swagger-ui.html#/ page


### SonarQube

#### In Local Env

Install: docker pull sonarqube

Run the container: docker run -d --name sonarqube -p 9000:9000 sonarqube

Run the analysis: mvn sonar:sonar (linux) | mvn sonar:sonar -Dsonar.host.url=http://$(boot2docker ip):9000  (With boot2dockerWith boot2docker)

Check the analysis on: http://localhost:9000

More info in: https://hub.docker.com/_/sonarqube/?tab=description

### Postman collection

[![Run in Postman](https://run.pstmn.io/button.svg)](https://www.getpostman.com/collections/326bcd576c02b30c93ac)
