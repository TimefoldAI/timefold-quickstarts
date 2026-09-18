# Quarkus Console

This is the console-dashboard sibling of the
[`school-timetabling`](../../use-cases/school-timetabling) quickstart: same domain, same
constraints, same `timefold-solver-quarkus` wiring — but watched live in the terminal via
[`timefold-solver-console`](../../../timefold01-solver/tools/console), instead of a REST API and
web UI.

## Run it

```bash
mvn package
java -jar target/quarkus-app/quarkus-run.jar
```

To also write the solved timetable to a JSON file, pass its path as the only argument:

```bash
java -jar target/quarkus-app/quarkus-run.jar solution.json
```

Do not run this with `mvn quarkus:dev`: dev mode's own raw-mode stdin hotkey handler conflicts
with the dashboard's, and `SolverConsole` refuses to start under it.

While solving, press `q` to stop early and still see the best solution found so far.
