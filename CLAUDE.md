# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.8 subscription server application for the CS4445 course project. The application uses:
- **Java 25** as the target JVM version
- **Maven** for build management
- **PostgreSQL** as the database (via Docker Compose)
- **Spring Data JPA** for database access
- **Spring Security** for authentication/authorization
- **Lombok** for reducing boilerplate code

**Important**: The package name is `com.CS445.CS4445_Sub_Server` (note the underscore, not hyphen) due to Java package naming restrictions.

## Build and Run Commands

### Building the Application
```bash
# Build using Maven wrapper (preferred)
./mvnw clean install

# Build without running tests
./mvnw clean install -DskipTests

# Compile only
./mvnw compile
```

### Running the Application
```bash
# Run with Spring Boot Maven plugin (automatically starts Docker Compose PostgreSQL)
./mvnw spring-boot:run

# The application will automatically start the PostgreSQL container defined in compose.yaml
# Database credentials: myuser/secret, database: mydatabase, port: 5432
```

### Testing
```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=Cs4445SubServerApplicationTests

# Run tests with coverage
./mvnw verify
```

### Docker Compose
```bash
# The Spring Boot Docker Compose support automatically manages the PostgreSQL container
# Manual control if needed:
docker compose up -d
docker compose down
```

## Project Structure

```
src/
├── main/
│   ├── java/com/CS445/CS4445_Sub_Server/
│   │   └── Cs4445SubServerApplication.java  # Main application entry point
│   └── resources/
│       └── application.properties            # Spring configuration
└── test/
    └── java/com/CS445/CS4445_Sub_Server/
        └── Cs4445SubServerApplicationTests.java
```

## Architecture

### Application Entry Point
- Main class: `Cs4445SubServerApplication` (note: class name uses `Cs4445` but package uses `CS4445_Sub_Server`)
- Standard Spring Boot application with `@SpringBootApplication` annotation

### Database Configuration
- PostgreSQL database is automatically managed via Spring Boot Docker Compose integration
- Connection details defined in `compose.yaml`
- Database connectivity available through Spring Data JPA repositories

### Security
- Spring Security is configured but currently in default state
- Security configuration will need to be implemented based on project requirements

## Development Notes

### Lombok Usage
- Lombok is configured with annotation processing in Maven
- The Lombok dependency is excluded from the final JAR
- Use standard Lombok annotations (@Data, @Builder, @Slf4j, etc.) for entity classes

### Maven Wrapper
- Always use `./mvnw` instead of `mvn` to ensure consistent Maven version
- Maven wrapper is included in the project (mvnw and mvnw.cmd)

### Package Naming
- Main package: `com.CS445.CS4445_Sub_Server`
- When creating new packages/classes, maintain this package structure
- The hyphen in the artifact name was converted to underscore for Java compatibility
