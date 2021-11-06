FROM ubuntu:20.04
RUN apt-get update
RUN apt-get install -y openjdk-11-jdk
COPY UniBot/UniBot.jar /usr/app/UniBot.jar
CMD java -jar -Dspring.profiles.active=prod /usr/app/UniBot.jar