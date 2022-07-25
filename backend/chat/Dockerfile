FROM maven:3.8.3-openjdk-17
WORKDIR /chat
COPY . .
ENV POSTGRESQL_HOST=postgres
RUN mvn -DskipTests=true clean install
CMD mvn spring-boot:run