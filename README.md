[![Build Status](https://dev.azure.com/melanga0355/Spring%20Config%20Server/_apis/build/status/melangad.spring-config-rest-server?branchName=master)](https://dev.azure.com/melanga0355/Spring%20Config%20Server/_build/latest?definitionId=1&branchName=master)

# Introduction
This is an alternative for Spring Cloud Config which has its own client and server. This project is for the server component of the config server. You can find the client under [client project](https://github.com/melangad/spring-config-rest-client)

The server is using database as the configuration source.

# Server
Server can be easily integrated with any Spring Boot project. Server provides following features at the moment.

* Configurations can be stored against a unique label
* Configurations are version controlled in the database with history
* Configurations are stored as a single JSON object in the database for integrity
* Provide RESTful APIs to Create, Update and Get configurations based on label
* Actuator endpoint to query the configuration version and last update timestamp on the client side

# Quick Start
To use the client side in your application, add following dependency on your pom.xml file.

.pom.xml
```
<dependency>
    <groupId>io.github.melangad</groupId>
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
  `label` varchar(255) DEFAULT NULL,
  `config_version` int(11) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `config_value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`label`)
)
```

History table
```
CREATE TABLE `config_history` (
  `id` int(11) NOT NULL,
  `label` varchar(255) DEFAULT NULL,
  `config_version` varchar(255) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
)
```

## APIs
### Create New Label with configuration
#### API
```
POST /config/{LABEL}
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

### Get Configurations for an label
#### API
```
GET /config/{LABEL}
```
### Update configuration on an existing label
#### API
```
PATCH /config/{LABEL}
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

Note: Every time when configuration update API invoke, it would increase the version even though there are no changes.

### Replace configuration on an existing label
Existing configuration will be fully replaced with provided configurations. Versions will ne bumped up.
#### API
```
PUT /config/{LABEL}
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

### Client Feedback
Client application can provide feedback to the config server via this API. Using this API, client can inform the server with current client configuration version and last updated timestamp.

Although API is provided server does not perform any action with client provided feedback. You can create a bean implementing ClientFeedbackHandler interface to handle the feedback data as you want.

#### API
```
POST /config/feedback
```
#### Sample Request Body
```
{
    "label": "LABEL",
    "clientId": "CLIENT_ID",
    "clientVersion": 3
    "lastUpdateTime": "2020-04-19T13:49:35Z"
}
```

## Events
You can use events if you need to perform additional task on any configuration create, update and patch action. In order to listen to events, create a bean implementing ConfigEventHandler.

You could use the event to handle any post processing actions such as notifying clients about the updates so that client could refresh the configurations.

# TODO
* Add config push to clients on configuration update with pluggable adapters for custom providers
* Add security to Server APIs
