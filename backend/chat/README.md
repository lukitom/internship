# Backend chat-app

## Requirement

* [JDK 17.0.1](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
* [Docker](https://www.docker.com)
## Running application

### Java app

In terminal go inside project folder (here: /backend)

Type command:

###### On UNIX based system
`./mvnw spring-boot:run`

###### On Windows system
`mvnw.cmd spring-boot:run`

### Database
To create DB PostgresSQL:

This will be used to connect PGAdmin with database

`docker network create db-network`

Modify DB user, password and DB name below and type the same in application config file

`docker run -d --name pg_container -p 5432:5432 -e POSTGRES_USER=root -e POSTGRES_PASSWORD=root -e POSTGRES_DB=test_db --net db-network postgre`

For visual representation of data

`docker run -d --name pgadmin4_container -p 5050:80 -e PGADMIN_DEFAULT_EMAIL=admin@admin.com -e PGADMIN_DEFAULT_PASSWORD=root --net db-network dpage/pgadmin4`