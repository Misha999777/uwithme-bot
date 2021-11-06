# UniBot Service

[![Build Status](https://github.com/Misha999777/UniBot-Service_University-With-Me/workflows/Main/badge.svg)](https://github.com/Misha999777/UniBot-Service/actions?query=workflow%3A%22Main%22)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

Service that runs UniBot. Uses [Spring Boot](http://projects.spring.io/spring-boot/).

UniBot is a system for everyone to acess data from the [University With Me](https://uwithme.education) via Telegram Bot.

## Requirements

For building and running the application you need:

- [JDK 11](https://openjdk.java.net/projects/jdk/11/)
- [Maven 3](https://maven.apache.org)

## Running the application locally

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the `tk.tcomad.unibot.Main` class from your IDE.

Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

```shell
mvn spring-boot:run
```

## Copyright

Released under the Apache License 2.0. See the [LICENSE](https://github.com/tCoMaD/UniBot-Service/blob/master/LICENSE) file.