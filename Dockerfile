FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM quay.io/wildfly/wildfly:35.0.1.Final
USER root
ADD https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.13/postgresql-42.7.13.jar /opt/jboss/wildfly/postgresql-42.7.13.jar
COPY docker/configure-postgres.cli /tmp/configure-postgres.cli
COPY docker/ready.sh /opt/jboss/wildfly/ready.sh
RUN chown jboss:jboss /opt/jboss/wildfly/postgresql-42.7.13.jar /tmp/configure-postgres.cli /opt/jboss/wildfly/ready.sh \
    && chmod +x /opt/jboss/wildfly/ready.sh
USER jboss
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/configure-postgres.cli \
    && rm -f /opt/jboss/wildfly/postgresql-42.7.13.jar /tmp/configure-postgres.cli
COPY --from=build --chown=jboss:jboss /src/target/SmartFood-1.0-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/SmartFood-1.0-SNAPSHOT.war
ENV POSTGRES_HOST=db \
    POSTGRES_PORT=5432 \
    POSTGRES_DB=smartfood \
    POSTGRES_USER=smartfood \
    POSTGRES_PASSWORD=smartfood
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=10 CMD ["/opt/jboss/wildfly/ready.sh"]
