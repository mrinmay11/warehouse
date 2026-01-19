# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
I would refactor the `WarehouseRepository` to not expose `DbWarehouse` in its public interface methods if strict architectural boundaries are desired. Currently, it implements `WarehouseStore` which uses Domain objects, but it also extends `PanacheRepository<DbWarehouse>`. This is pragmatic for Quarkus/Panache but leaks leakage details if not careful.
I would also standardize the `LegacyStoreManagerGateway` to use an interface, allowing for easier mocking and switching of implementations (e.g., File, REST, Queue).
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
**Contract-First (OpenAPI)**:
Pros: Clear contract before coding, parallel development (FE/BE), auto-generated docs and clients.
Cons: Initial setup overhead, tooling complexity.

**Code-First**:
Pros: Faster to start, single source of truth (code).
Cons: API docs can drift from reality if not generated from code, harder to communicate changes beforehand.

**Choice**: I prefer Contract-First for public or inter-team APIs to ensure stability and clear communication. For internal, small microservices, Code-First with generated Swagger is often sufficient.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
1. **Unit Tests**: High priority. Cover business logic, edge cases, and validations (like the Warehouse replacement logic). Fast to run.
2. **Integration Tests**: Medium priority. Verify Database interactions and API contracts (like `WarehouseEndpointIT`). Essential for ensuring the "wiring" works.
3. **E2E/Manual**: Lower priority for this scope, but critical for final user flows.

**Strategy**:
- CI/CD pipeline enforcing minimum coverage (e.g., JaCoCo 80%).
- Mutation testing to ensure test quality.
- "Shift-left": Write tests as part of the feature development (TDD preferred).
```