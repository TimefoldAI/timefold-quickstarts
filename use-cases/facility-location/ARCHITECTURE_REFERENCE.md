# Timefold Model SDK Architecture Reference

This file documents the exact, active type-signatures of the load-bearing interfaces of the Timefold Model SDK on the classpath. It is intended to guide optimization models and implementation agents so they can align correctly with the SDK's design.

---

## 1. Core Model Interfaces

### `ModelInput`

* **Package:** `ai.timefold.solver.service.definition.api.ModelInput`
* **Signature:** An interface representing input data. Does not accept generic parameters.

  ```java
  public interface ModelInput {
      // Marker interface for the input wire DTO
  }
  ```

### `ModelOutput`

* **Package:** `ai.timefold.solver.service.definition.api.ModelOutput`
* **Signature:** An interface representing output data.

  ```java
  public interface ModelOutput {
      // Marker interface for the output wire DTO
  }
  ```

### `ModelConfigOverrides`

* **Package:** `ai.timefold.solver.service.definition.api.ModelConfigOverrides`
* **Signature:** Interface for constraint weight overrides configured on the Platform.

  ```java
  public interface ModelConfigOverrides {
      // Marker interface for configuration weight overrides
  }
  ```

### `SolverModel`

* **Package:** `ai.timefold.solver.service.definition.api.SolverModel`
* **Signature:** Must be implemented by your `@PlanningSolution` class (e.g. `Schedule` / `Problem`).

  ```java
  public interface SolverModel {
      ai.timefold.solver.core.api.score.Score getScore();
      ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides getConstraintWeightOverrides();
  }
  ```

---

## 2. Core Service Interfaces

### `ModelValidator`

* **Package:** `ai.timefold.solver.service.definition.api.validation.ModelValidator`
* **Signature:** Interface used to validate incoming `ModelInput` before solving.

  ```java
  public interface ModelValidator {
      void validate(
          ai.timefold.solver.service.definition.api.validation.ValidationBuilder validationBuilder,
          ai.timefold.solver.service.definition.api.ModelInput modelInput,
          ai.timefold.solver.service.definition.api.domain.ModelConfig modelConfig
      );
  }
  ```

### `ModelConvertor`

* **Package:** `ai.timefold.solver.service.definition.api.ModelConvertor`
* **Signature:** Used to convert between wire format DTOs and the Timefold `@PlanningSolution` model.

  ```java
  public interface ModelConvertor {
      ai.timefold.solver.service.definition.api.SolverModel toSolverModel(
          ai.timefold.solver.service.definition.api.ModelInput modelInput,
          ai.timefold.solver.service.definition.api.domain.ModelConfig modelConfig,
          java.util.Optional<?> optional
      );

      ai.timefold.solver.service.definition.api.ModelOutput toModelOutput(
          ai.timefold.solver.service.definition.api.SolverModel solverModel
      );

      ai.timefold.solver.service.definition.api.ModelInput applyOutputToInput(
          ai.timefold.solver.service.definition.api.ModelInput modelInput,
          ai.timefold.solver.service.definition.api.ModelOutput modelOutput
      );
  }
  ```

### `ModelConfig`

* **Package:** `ai.timefold.solver.service.definition.api.domain.ModelConfig`
* **Signature:** Represents active runtime model configuration.

  ```java
  public class ModelConfig {
      public java.lang.Object overrides(); // Returns the active ModelConfigOverrides
      public static ai.timefold.solver.service.definition.api.domain.ModelConfig empty();
  }
  ```

---

## 3. Storage and Rest Interfaces

### `ModelRest`

* **Package:** `ai.timefold.solver.service.rest.api.ModelRest`
* **Signature:** REST resource interface for Platform endpoints.

  ```java
  public interface ModelRest {
      ai.timefold.solver.service.definition.internal.storage.AbstractStorageService storageService();
  }
  ```

---

## 4. Demo Data Generation

### `DemoDataGenerator`

* **Package:** `ai.timefold.solver.service.definition.api.data.DemoDataGenerator`
* **Signature:** Interface used to yield demo datasets selectable on the Platform.

  ```java
  public interface DemoDataGenerator {
      java.util.List<ai.timefold.solver.service.definition.api.data.DemoMetaData> demoMetaData();
      ai.timefold.solver.service.definition.api.data.DemoData generateDemoData(java.lang.String demoId);
      default java.util.List<ai.timefold.solver.service.definition.api.data.DemoData> generateDemoData();
  }
  ```
