# University With Me Bot

As a component of the broader `University With Me` project, this service provides a way for everyone to access data from the platform via a Telegram Bot.

## Requirements

To develop the application, you will need:

- [JDK](https://openjdk.java.net/projects/jdk/21/)
- [Maven](https://maven.apache.org/)
- [Docker](https://www.docker.com/)

## Running the application locally

To run this application, follow these steps:

1. Download the [Docker files](https://github.com/HappyMary16/uwithme-docker-files).
2. Start them using:

```shell
docker compose up -d
```

3. Download the [University With Me Server](https://github.com/HappyMary16/uwithme-server).
4. Start it using:

```shell
mvn spring-boot:run
```

5. Download the [University With Me Client](https://github.com/HappyMary16/uwithme-client).
6. Install the client dependencies using:

```shell
npm install
```

7. Start the client using:

```shell
npm start
```

8. Start the `bot` using:

```shell
mvn spring-boot:run
```

Then you can [Log in to Telegram Testing Environment](https://core.telegram.org/bots/webapps#testing-web-apps) and speak to [@UniversityWithMe_bot](https://t.me/UniversityWithMe_bot)

## Copyright

Released under the GNU General Public License v2.0.
See the [LICENSE](https://github.com/Misha999777/U-With-Me-Bot/blob/master/LICENSE) file.
