# Automated Monolith to Microservice Migration Tool

An automated framework for analyzing Java Spring Boot monolithic applications and extracting them into independent microservices using static code analysis and dependency graph inference.

This project performs source code scanning, detects Spring-managed components, analyzes controller–repository–entity relationships, infers service boundaries, and generates standalone Spring Boot microservice skeletons automatically.

---

# Objective

Modern enterprise applications often begin as monoliths, making development simpler during initial stages. As systems scale, monolithic architectures become difficult to maintain, deploy, and scale independently.

This project aims to automate the migration process from a monolithic Spring Boot application into logically separated microservices by:

- Performing static code analysis
- Detecting Spring components automatically
- Building dependency graphs
- Inferring service boundaries
- Generating independent Spring Boot microservices

The demo monolithic application used for experimentation:

Spring PetClinic  
https://github.com/spring-projects/spring-petclinic

---

# Features

## Source Code Analysis

- Scans only `src/main/java`
- Parses Java source files using AST analysis
- Detects:
  - `@Controller`
  - `@RestController`
  - `@Service`
  - `@Repository`
  - Spring Data repositories

---

## Dependency Extraction

Supports:

- Field-based dependency injection
- Constructor-based dependency injection

Builds:

- Controller → Repository relationships
- Repository → Entity relationships
- Controller → Repository → Entity dependency chains

---

## Entity Resolution Engine

- Extracts JPA entity classes automatically
- Resolves repository generic types
- Maps:

```text
Entity Name → Physical File Path
```

This allows accurate file-level extraction during generation.

---

## Boundary Detection

The framework groups related components into service clusters.

Example inferred services:

- Vet Service
- Visit Service
- Owner Service
- Welcome Service

Boundary detection is based on:

- Shared entities
- Repository usage
- Controller dependencies

---

## Microservice Generation

Automatically generates independent Spring Boot services containing:

- Controllers
- Entities
- Repositories
- Configuration files
- Maven structure

Generated files include:

- `pom.xml`
- `application.yml`
- package declarations
- source code structure

---

# Project Architecture

```text
                +----------------------+
                | Spring Boot Monolith |
                +----------+-----------+
                           |
                           v
               +-----------------------+
               | Source Code Scanner   |
               | (AST Java Parser)     |
               +-----------+-----------+
                           |
                           v
               +-----------------------+
               | Component Detection   |
               | Controllers           |
               | Repositories          |
               | Entities              |
               +-----------+-----------+
                           |
                           v
               +-----------------------+
               | Dependency Graph      |
               | Controller -> Repo    |
               | Repo -> Entity        |
               +-----------+-----------+
                           |
                           v
               +-----------------------+
               | Boundary Inference    |
               | Service Clustering    |
               +-----------+-----------+
                           |
                           v
               +-----------------------+
               | Microservice          |
               | Code Generator        |
               +-----------+-----------+
                           |
                           v
              +------------------------+
              | Generated Services     |
              | Vet / Visit / Owner    |
              +------------------------+
```

---

# Repository Structure

```text
project-root/
│
├── analyzer/
│   ├── ControllerAnalyzer.java
│   ├── RepositoryAnalyzer.java
│   ├── EntityAnalyzer.java
│   └── DependencyGraphBuilder.java
│
├── boundary/
│   ├── BoundaryDetector.java
│   └── ServiceCluster.java
│
├── generator/
│   ├── MicroserviceGenerator.java
│   ├── PomGenerator.java
│   └── ConfigGenerator.java
│
├── model/
│   ├── ControllerInfo.java
│   ├── RepositoryInfo.java
│   ├── EntityInfo.java
│   └── DependencyNode.java
│
├── util/
│   ├── FileScanner.java
│   ├── ASTUtils.java
│   └── PackageUtils.java
│
└── Main.java
```

---

# Workflow

## Step 1 — Source Scanning

The tool recursively scans:

```text
src/main/java
```

and collects all Java source files.

---

## Step 2 — AST Parsing

Each Java file is parsed using AST analysis to extract:

- Controllers
- Repositories
- Entities
- Dependencies

---

## Step 3 — Dependency Graph Construction

Relationships are built:

```text
Controller -> Repository -> Entity
```

Example:

```text
VetController
    -> VetRepository
        -> Vet Entity
```

---

## Step 4 — Service Boundary Inference

The framework groups related modules into clusters.

Example:

```text
VetController
VetRepository
VetEntity
```

becomes:

```text
VetService
```

---

## Step 5 — Microservice Generation

Independent Spring Boot services are generated automatically with:

- Maven structure
- Configuration
- Extracted source files
- Updated package declarations

---

# Example Output Structure

```text
generated-services/
│
├── vet-service/
│   ├── src/main/java/
│   ├── pom.xml
│   └── application.yml
│
├── visit-service/
│   ├── src/main/java/
│   ├── pom.xml
│   └── application.yml
│
└── owner-service/
    ├── src/main/java/
    ├── pom.xml
    └── application.yml
```

---

# Technologies Used

- Java
- Spring Boot
- Spring Data JPA
- JavaParser / AST Parsing
- Maven

---

# Key Contributions

- Automated controller and repository detection
- Dependency graph generation
- Repository generic type resolution
- Entity file path mapping
- Service boundary inference
- Automatic Spring Boot microservice generation
- Package rewriting and source extraction

---

# Current Limitations

- Designed primarily for Spring Boot monoliths
- Static analysis only
- No runtime traffic analysis
- Limited handling of complex cyclic dependencies
- Security and API gateway generation not included yet

---

# Future Enhancements

- Kubernetes deployment generation
- Docker and Docker Compose integration
- API Gateway generation
- Service discovery support
- Database-per-service generation
- Runtime dependency analysis
- Distributed tracing support

---

# Running the Project

## Clone Repository

```bash
git clone <repository-url>
cd <repository-name>
```

---

## Build Project

```bash
mvn clean install
```

---

## Run Analyzer

```bash
java Main
```

---

# Sample Execution Flow

```text
Input:
Spring PetClinic Monolith

↓

Static Code Analysis

↓

Dependency Graph Extraction

↓

Boundary Detection

↓

Generated Microservices:
- Vet Service
- Visit Service
- Owner Service
```

---

# Research Relevance

This project demonstrates an automated approach toward:

- Monolith decomposition
- Static dependency analysis
- Service boundary detection
- Automated software modernization
- Microservice migration research

It can serve as a foundation for further research in:

- AI-assisted decomposition
- Dynamic dependency analysis
- Cloud-native modernization
- Automated refactoring systems

---

# License

This project is intended for academic and research purposes.
