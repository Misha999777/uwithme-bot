FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install gettext-base

WORKDIR /usr/app

COPY target/UWMBot.jar image/application.properties.template ./
COPY image/substitute.sh /opt/uwm/

CMD /opt/uwm/substitute.sh && java -jar /usr/app/UWMBot.jar
