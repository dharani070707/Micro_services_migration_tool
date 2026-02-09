I have used Pet Clinic Project as demo - github link - https://github.com/spring-projects/spring-petclinic

Steps completed
Implemented source code scanning restricted to src/main/java and parsed Java files using AST analysis.
Automatically detected Spring MVC controllers (@Controller, @RestController) from the monolith.
Extracted controller dependencies by supporting both field-based and constructor-based dependency injection.
Identified Spring-managed components, including Spring Data JPA repositories (interfaces extending Repository, CrudRepository, etc.).
Built a controller → repository dependency graph after filtering non-domain dependencies.
Extracted domain entities by analyzing Spring Data repository generic types and ignoring ID types.
Successfully derived controller → repository → entity relationships as the foundation for automated microservice boundary detection.
