FROM openjdk:8
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
ENV SBT_VERSION 1.3.6
RUN \
  apt-get update && \
  apt-get install curl && \
  curl -L -o sbt-$SBT_VERSION.deb http://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get install sbt && \
  sbt sbtVersion
EXPOSE 8080
CMD ["sbt", "run"]