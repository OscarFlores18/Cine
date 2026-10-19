# Build: compila el jar con Maven (imagen pesada, se descarta al final).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Copiamos el pom primero y bajamos las dependencias aparte del codigo: si
# despues solo cambia el .java, Docker reusa esta capa y no vuelve a bajar
# medio Maven Central de nuevo.
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime: solo el JRE (no Maven, no el codigo fuente), imagen final chica.
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
