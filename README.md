# Introduction
This is an alternative for Spring Cloud Config which has its own client and server. This project is for the server component of the config server.

The server is using database as the configuration source.

# Server
Server can be easily integrated with any Spring Boot project. Server provides following features at the moment.

* Configurations can be stored against a unique application ID
* Configurations are version controlled in the database with history
* Configurations are stored as a single JSON object in the database for integrity
* Provide RESTful APIs to Create, Update and Get configurations based on application ID
* Actuator endpoint to query the configuration version and last update timestamp on the client side

# Quick Start
To use the client side in your application, add following dependency on your pom.xml file.

.pom.xml
```
<dependency>
    <groupId>io.github.melangad.spring.config</groupId>
    <artifactId>spring-config-rest-server</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

The module use Spring Data JPA as the persistence layer and you could configure any relational database as the data source.

Add following for MySql database connectivity
```
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
```

Add following to your SpringBootApplication

```
@ComponentScan({ "io.github.melangad.spring.config.server" })
@EnableJpaRepositories("io.github.melangad.spring.config.server.repository")
@EntityScan({"io.github.melangad.spring.config.server.entity"})
```

## Configurations
Configure following properties for the database access
```
spring.jpa.hibernate.ddl-auto=update
spring.jpa.hibernate.datasource.url=jdbc:mysql://DB_HOST:DB_PORT/DATABASE_NAME
spring.jpa.hibernate.datasource.username=DB_USERNAME
spring.jpa.hibernate.datasource.password=DB_PASSWORD
```

## DDL
Main configuration table
```
CREATE TABLE `config` (
  `id` int(11) NOT NULL,
  `application` varchar(255) DEFAULT NULL,
  `config_version` int(11) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `config_value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`application`)
)
```

History table
```
CREATE TABLE `config_history` (
  `id` int(11) NOT NULL,
  `application` varchar(255) DEFAULT NULL,
  `config_version` varchar(255) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
)
```

## APIs
### Create New Application with configuration
#### API
```
POST /config/{APPLICATION_ID}
```
#### Sample Request Body
```
{
    "SOME-KEY1":{
        "value": "val1",
        "description": "desc1"
    },
    "SOME-KEY2":{
        "value": "val2",
        "description": "desc2"
    }
}
```
You could add multiple keys at the same time

### Get Configurations for an application
#### API
```
GET /config/{APPLICATION_ID}
```
### Update configuration on an exisiting application
#### API
```
PATCH /config/{APPLICATION_ID}
```
#### Sample Request Body
```
{
    "SOME-KEY1":{
        "value": "new vlaue",
        "description": "desc1"
    },
    "SOME-KEY3":{
        "value": "val2",
        "description": "desc2"
    }
}
```
At the same time you could add new configurations also

Note: Everytime when configuration update API invoke, it would increase the version even though there are no changes.

# TODO
* Add config push to clients on configuration update with pluggable adapters for custom providers
* Add security to Server APIs