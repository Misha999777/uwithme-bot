FROM eclipse-temurin:21-jre

RUN apt-get update
RUN apt-get install gettext-base

COPY ./target/UWMBot.jar /usr/app/UWMBot.jar

COPY ./image/application.properties.template /usr/app/application.properties.template
COPY ./image/substitute.sh /opt/uwm/substitute.sh

WORKDIR /usr/app

CMD /opt/uwm/substitute.sh && java -jar /usr/app/UWMBot.jar