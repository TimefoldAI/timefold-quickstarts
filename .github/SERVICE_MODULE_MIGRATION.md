# Migrating a quickstart to the Models Service SDK

A brief for whoever migrates the next `use-cases/*` quickstart from a hand-written Quarkus REST
resource to the Timefold **Service module** (Models Service SDK). Written right after
`food-packaging` was migrated; `bed-allocation` and `conference-scheduling` went first, and
`facility-location` followed (the first two-level `HardSoftScore` model, and the first one with a map
UI — see the `best-score-limit` and `leaflet` notes below).

## Read these first

| Source | What you actually get from it |
|---|---|
| [Service module overview](https://docs.timefold.ai/timefold-solver/latest/running-timefold-solver/service/overview) | The framing: the Service module is the opinionated way to expose a solver over REST, and it owns the endpoints (submit, poll, terminate, score analysis), constraint weight overrides, demo data and metrics. It does **not** list the concrete interface names. |
| [Service getting-started](https://docs.timefold.ai/timefold-solver/latest/quickstart/service/getting-started) | The minimal shape: a `timefold-solver-service-parent` pom, a solution extending `AbstractSimpleModel`, a REST interface extending `ModelRest`, and `POST /v1/<resource>` + `GET /v1/<resource>/{id}`. Also `getting-started/service` in this repo is that same example. |
| **`use-cases/bed-allocation` and `use-cases/conference-scheduling`** | The real template. Copy their structure, naming and comment style; these two are what the CI checks were written against. |
| `.github/scripts/ArchitectureCheck.java` | 37 ArchUnit rules that *are* the specification. Read the javadoc on each rule — they explain the "why", which the docs don't. |
| The solver's own `service/definition/src/main/java/ai/timefold/solver/service/definition/api/**` | Authoritative signatures for `ModelInput`, `ModelOutput`, `ModelConfigOverrides`, `SolverModel`, `ModelConvertor`, `ModelValidator`, `DemoDataGenerator`, `InputMetricsAware`/`OutputMetricsAware`, `ConstraintInfo`, `ConstraintGroupInfo`, `AbstractIssue`, `SolvingStatus`. Clone `TimefoldAI/timefold-solver` and grep — faster than guessing. |

Don't try to derive the API from the docs alone. Diff the two migrated quickstarts against their
pre-migration state (`git log --oneline -- use-cases/bed-allocation`, commits `fb6449e5` "update bed
allocation to use service module" and `3b4c0e79` "codify and align service based quickstarts") — the
second commit is where most conventions were codified into `ArchitectureCheck.java`.

## Target layout

Base package stays `org.acme.<usecase>`. Every non-test class must live in one of
`domain`, `dto`, `solver`, `rest`, `service`, `demo`, `enricher`, `support`, `integrationtest`.

```
domain/                    solver model: @PlanningSolution, entities, facts
domain/<Name>ConstraintProperties.java   constraint-name String constants (final class, private ctor)
domain/justification/<Name>Justification.java   exactly ONE file, all justifications nested
dto/input/                 <Name>Input (ModelInput), <Name>InputMetrics, <Name>ConfigOverrides, *DTO
dto/output/                <Name>Output (ModelOutput), <Name>OutputMetrics, *DTO
service/<Name>ModelConvertor.java        @ApplicationScoped ModelConvertor
service/<Name>Validator.java             @ApplicationScoped ModelValidator
service/validation/<Name>Issue.java      exactly ONE file, all issues nested, extends AbstractIssue
solver/<Name>ConstraintProvider.java
solver/<Name>ConstraintGroup.java        ConstraintGroupInfo constants
rest/<Name>Resource.java                 interface, @Path("/<plural>"), extends ModelRest — that's all
demo/DemoDataGenerator.java              implements the SDK interface (do NOT extend AbstractBasicDemoDataGenerator)
demo/DemoDataBuilder.java                builds the demo <Name>Input
```

Delete: the old REST resource(s), any `persistence`/`bootstrap`/`rest/DemoDataGenerator`, exception
mappers, `solverStatus` fields on the solution, and `app.js`.

## Step by step

### 1. pom.xml

Throw the whole file away and copy `use-cases/conference-scheduling/pom.xml` (~20 lines): parent
`ai.timefold.solver:timefold-solver-service-parent:999-SNAPSHOT`, `groupId ai.timefold.model`,
`version 0.0.1`, `revision`/`maven.compiler.release` properties. No dependencies, no plugins, no
profiles — the parent owns all of it (including formatting, which it will reformat your sources with).

### 2. application.properties

Copy conference-scheduling's and change the model block: `timefold.model.id`,
`timefold.model.name`, `timefold.model.features[0..n]` (one sentence per capability, user-facing),
contact, maturity level, thread counts, `timefold.model.termination.spent-limit=PT30S`.

**Pick the test `best-score-limit` to match the score levels your model actually uses.** The other
quickstarts use `%test…best-score-limit=0hard/*medium/*soft` with `%test…spent-limit=PT1H`, which
only works because medium is unused there. If a medium level carries real constraints, use
`0hard/0medium/*soft` and think about whether that score is reachable — `food-packaging` deliberately
keeps the production `PT30S` spent limit as a backstop instead of `PT1H`, because
`<Name>ResourceIT` only waits one minute for a terminal state.

**There are two `best-score-limit` keys, and the SDK sets both.**
`timefold-solver-service-defaults`' own `application.properties` (config_ordinal 230) ships
`%test.timefold.model.termination.best-score-limit=0hard/0medium/*soft` *and*
`%test.quarkus.timefold.solver.termination.best-score-limit=0hard/0medium/*soft`. The three migrated
quickstarts before `facility-location` all score with `HardMediumSoftScore`, so they only ever needed
to restate the `quarkus.timefold.solver.*` one. A two-level score (`HardSoftScore`) has to override
**both**, otherwise the `timefold.model.*` one still holds the three-level string and every run the
Service module starts dies with `The scoreString (0hard/0medium/*soft) for the scoreClass
(HardSoftScore) doesn't follow the correct pattern`.

### 3. Solver model (`domain`)

- Implement `SolverModel<Score>` plus `InputMetricsAware<…InputMetrics>` and
  `OutputMetricsAware<…OutputMetrics>`; expose `getScore()` and
  `getConstraintWeightOverrides()`/setter (default `ConstraintWeightOverrides.none()`).
- Drop all Jackson annotations from the domain — only the DTOs are serialized now.
- Any class with an identifier (`@PlanningId`, or a `domain` field literally named `id`/`name`) must
  override `equals`/`hashCode` on **exactly** that field. Records included (the compiler-generated
  ones don't count).
- No `java.util.Set` fields — use `SequencedSet`/`List`.
- Domain setters return `void`. Domain may not depend on `service`/`rest`/`demo`, and may only depend
  on `dto` for the two metrics types.
- The value type of every `@PlanningVariable`/`@PlanningListVariable` needs `@PlanningId` (enums exempt).
- Compute the metrics from the solution in a way that survives uninitialized shadow variables (e.g.
  read a list variable rather than its inverse relation).

### 4. DTOs

The `DtoRecordConstructorCondition` rule is the one that bites. Read it before writing DTOs.

- Records only (or interfaces/enums) at the top level of `dto`; no nested classes; every type ends in
  `DTO`/`Input`/`Output`/`Metrics`/`ConfigOverrides`/`ValidationIssue`/`Detail`.
- **Use `OffsetDateTime`, not `LocalDateTime`.** The Service module validates `date-time` fields
  against ISO-8601-with-offset and returns 400 for offset-less values. `LocalDate` is fine. It is
  simplest to use `OffsetDateTime` in the domain too, so the convertor needs no conversion.
- Describe constraints with `@Schema(required=…, minLength=…, minItems=…, minimum=…)`. **Never import
  `jakarta.validation`** — the Service module enforces the OpenAPI schema at the REST layer.
- An input record needs a *non-empty compact constructor* exactly when it has an optional
  (non-`required`) collection/map field or nullable nested DTO field to default to empty — and **no
  explicit constructor at all** otherwise. Output records: never an explicit constructor. `*Metrics`
  records are exempt and may use a compact constructor for validation.
- No `@JsonSetter`; no `new ArrayList<>` anywhere in a DTO source file.
- `dto.input` must not depend on `dto.output`. Referring to a `domain` constant from
  `@ConstraintReference(...)` is fine (String constants are inlined, so ArchUnit sees no dependency).
- `ConfigOverrides` needs a no-arg constructor delegating to all-null — the framework instantiates it
  to generate the default config profile.
- `withXxx(...)` methods must be used at least once (they exist for `applyOutputToInput`).

### 5. Convertor, validator, issues, justifications

- `ModelConvertor<Score, Input, ConfigOverrides, SolverModel, Output>`: `toSolverModel` (resolve every
  id through a `require(map, id, kind)` helper that throws with an actionable message),
  `toModelOutput`, `applyOutputToInput`. Apply `modelConfig.overrides()` weights only when non-null.
  Handle the `lastModelOutput` argument.
- `ModelValidator`: **only** domain-specific checks — duplicate ids, dangling references, model
  invariants. OpenAPI/schema compliance is already enforced before it runs. Keep the issue list
  bounded (e.g. report only the first gap per entity rather than every pair).
- One issue file, one justification file, all implementations nested, every implementation listed in
  the parent's `@Schema(oneOf = …)`. Justification records: no zero-arg constructors, never convert
  `null` strings to `""`. `@Schema` is allowed only in `dto`, `domain.justification` and issue classes.
- Constraints: `asConstraint(new ConstraintInfo(id, name, description, group))` — the bare
  `asConstraint(String)` overload is banned. Add `justifyWith(...)` for every constraint. Group icons
  are [Tabler icon](https://tabler.io/icons) names, `IconLikeThis`.

### 6. Demo data

`DemoDataGenerator` implements the SDK interface directly (the abstract base can't carry a
description), returns `DemoMetaData(id, shortDescription, longDescription, tags, …)` and a
`DemoData(metaData, new ModelRequest<>(configuration, problem))`. `DemoDataBuilder` is a plain final
class with a static `builder()`; seed any randomness so the dataset is reproducible. Drop
`@ConfigProperty`-driven dataset sizes — make them constants.

### 7. Tests

Mirror the file set of the two migrated quickstarts:

```
solver/<Name>ConstraintProviderTest      ConstraintVerifier, one test per constraint
solver/SolverManagerTest                 solves TestHelper.createProblem(), asserts feasibility + metrics
solver/<Name>EnvironmentTest             FULL_ASSERT, @EnabledIfSystemProperty(named="slowly")
service/<Name>ValidatorTest              one test per issue type + a mixed dataset
rest/<Name>OpenApiValidationTest         POSTs mutated demo data, expects 400 with the field name
demo/DemoDataBuilderTest                 shape of the demo dataset
integrationtest/<Name>ResourceIT         @QuarkusIntegrationTest, awaits a terminal SolvingStatus
support/TestHelper                       REQUIRED by an arch rule
```

- `support/TestHelper.java` must exist and is the **only** place allowed to call `domain`/`dto`
  constructors from test code. Give it DTO factories (`product(id)`, `line(id)`, `inputWithJobs(...)`,
  `createProblem()`) and `aXxx(...)` builders for solver-model objects.
- AssertJ only — `org.junit.jupiter.api.Assertions` is banned anywhere in the module.
- Add a test that the **demo dataset passes your own validator**; otherwise the service can ship demo
  data it would reject.
- `ConstraintVerifier.given(...)` does not recompute shadow variables, so TestHelper has to set them
  (line, previous/next element, derived times) the way the solver would.

### 8. UI — do not write the page yourself

The shell (`index.html`) and everything under `META-INF/resources/shared/` are **generated** by
`visualizations/sync.sh` from `visualizations/shared/index.template.html` + `shared/*.js|css`.
`.github/scripts/CheckSharedUiSync.java` fails CI if a quickstart's copy drifts. So:

1. Add the quickstart to `visualizations/sync.sh` in **four** places: `QUICKSTART_DIRS`,
   `quickstart_name()`, `quickstart_utm_content()` (`<dir>-java`), `quickstart_features()`.
   Existing features: `vis-timeline` (Gantt JS bundle, ships its own CSS), `leaflet` (map JS + CSS,
   added for `facility-location`), `color-picker`, `custom-css` (links a `style.css` you keep next to
   `index.html`). Add a new one to `feature_head()`/`feature_scripts()` rather than writing
   `<script>` tags into a page — and compute its `integrity` hash from the file you are actually
   pinning, never from memory.
2. Write **only** `META-INF/resources/visualize.js` (plus `style.css` if you enabled `custom-css`).
   It must call `setVisualizationSlot('<your markup>')` and construct one
   `new QuickstartPage({modelPath: '/v1/schedules', renderSchedule, renderInfo, mergeModelOutput})` —
   `CheckSharedUiSync` greps for both. `QuickstartPage` (in `shared/quickstart-page.js`) already owns
   solve/stop/score-analysis/demo-data-picker/error toasts; `mergeModelOutput(schedule, modelOutput)`
   overlays the solver's assignments onto the loaded `modelInput`, which is what `renderSchedule` draws.
3. Run `bash visualizations/sync.sh`, then `java .github/scripts/CheckSharedUiSync.java use-cases/*`
   for each service quickstart. Never hand-edit the generated `index.html`. If the shell itself needs
   to change, change the template and re-run sync for *all* quickstarts in one commit.

### 9. Finish up

- Root `README.md`: prefix the quickstart's "Notable Solver Concepts" cell with `Service Model`.
- Quickstart `README.md`: refresh the constraint table (name/level/description matching the
  `ConstraintInfo` descriptions), drop the `$ ` shell prompts, and check the `docker run` image tag
  against the new pom version.
- The screenshot still shows the old UI; regenerate it or say you didn't.

## Local verification loop

There is no Maven or `ai.timefold` artifact in a fresh sandbox, and the parent pom is `999-SNAPSHOT`,
so you have to build the solver first:

```sh
sudo apt-get install -y maven
git clone --depth 1 https://github.com/TimefoldAI/timefold-solver.git
cd timefold-solver && mvn -B -Dquickly -DskipTests clean install   # ~10 min
```

Then, per iteration:

```sh
cd use-cases/<usecase>
mvn -B clean test                      # always with `clean` — see gotchas
```

```sh
# Architecture rules. CI uses jbang; without it, put the two deps on the classpath by hand:
mvn -q dependency:get -Dartifact=com.tngtech.archunit:archunit:1.4.2
mvn -q dependency:get -Dartifact=org.slf4j:slf4j-nop:2.0.17
CP="$HOME/.m2/repository/com/tngtech/archunit/archunit/1.4.2/archunit-1.4.2.jar:$HOME/.m2/repository/org/slf4j/slf4j-api/2.0.17/slf4j-api-2.0.17.jar:$HOME/.m2/repository/org/slf4j/slf4j-nop/2.0.17/slf4j-nop-2.0.17.jar"
java -cp "$CP" .github/scripts/ArchitectureCheck.java use-cases/<usecase>   # needs test-compile first
java .github/scripts/CheckSharedUiSync.java use-cases/<usecase>
```

Then actually run it — the unit tests do not cover the wire format:

```sh
mvn -B package -DskipTests
java -Dquarkus.http.port=8099 -jar target/quarkus-app/quarkus-run.jar &
curl -s localhost:8099/v1/demo-data/BASIC -o demo.json
ID=$(curl -s -X POST -H 'Content-Type: application/json' --data-binary @demo.json \
       localhost:8099/v1/schedules | jq -r .id)
curl -s "localhost:8099/v1/schedules/$ID" | jq '.metadata.solverStatus, .metadata.score, .kpis'
```

Optional but it caught real mistakes: render `visualize.js` against that saved `demo.json` and the
solved response in jsdom (`npm i jsdom jquery @js-joda/core`), stubbing `vis.DataSet`/`vis.Timeline`
and `QuickstartPage`, with the DataSet stub throwing on duplicate item ids.

## Gotchas that cost time

- **Offset-less date-times are rejected** with `does not match the date-time pattern must be a valid
  ISO-8601 date and time with an offset` → use `OffsetDateTime`. This surfaces as a 400 on the
  "valid input is accepted" test, so read the response body, not just the status.
- **`mvn test` without `clean` fails** once `target/timefold/<model>_v1` exists, with
  `FileAlreadyExistsException` from the Service module's build step. Always `mvn clean test`.
- **`mvn clean verify` fails the same way** in a bind-mounted sandbox — and it fails identically on
  the already-migrated `conference-scheduling`, so it is environmental, not your change. Verify with
  `mvn clean test` and check `conference-scheduling` before blaming yourself. To still get the
  "actually run it" step below, copy the module to a path outside the bind mount (e.g. under `/tmp`)
  and `mvn -o clean package -DskipTests` there; the parent pom resolves from `~/.m2` either way.
- **A wrong test `best-score-limit` does not fail a unit test.** The Service module starts the run
  asynchronously, so a solver that cannot even be built only shows up as a stack trace in the surefire
  output and a `SOLVING_FAILED` run — `mvn clean test` still reports success. Grep the test log for
  `Solving with id (...) failed` before believing a green build.
- `mvn clean` deletes the generated, untracked `src/main/jib/timefold/diagnostic-tools.jar`. If you
  clean a quickstart you weren't asked to touch, restore it.
- A `forEachUniquePair` join on a nullable planning variable pairs up the nulls; filter them out.
- The `quickstart_*()` functions in `sync.sh` `exit 1` on an unknown dir — miss one of the four and
  sync fails loudly, which is the intended behaviour.
