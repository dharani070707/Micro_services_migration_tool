I have used Pet Clinic Project as demo - github link - https://github.com/spring-projects/spring-petclinic

Steps completed

Implemented source code scanning restricted to src/main/java and parsed Java files using AST analysis.

Automatically detected Spring MVC controllers (@Controller, @RestController) from the monolith.

Extracted controller dependencies by supporting both field-based and constructor-based dependency injection.

Identified Spring-managed components, including Spring Data JPA repositories (interfaces extending Repository, CrudRepository, etc.).

Built a controller → repository dependency graph after filtering non-domain dependencies.

Extracted domain entities by analyzing Spring Data repository generic types and ignoring ID types.

Successfully derived controller → repository → entity relationships as the foundation for automated microservice boundary detection.

Implemented a global entity scanning mechanism to build an entity name → file path mapping, enabling accurate resolution of repository generic types to physical source files.

Refactored the analysis layer to store absolute file paths for controllers and entities instead of class names, ensuring compatibility with the code generation phase.

Enhanced microservice boundary inference logic to operate on physical file references, resulting in correctly grouped service clusters (e.g., Vet, Visit, Welcome services).

Developed and integrated an automated MicroserviceGenerator that creates independent Spring Boot service skeletons, copies relevant controllers and entities, generates pom.xml and application.yml, and rewrites package declarations during extraction.
