# Vehicle Routing with time windows and capacity planning (Java, Quarkus, Maven)

Find the most efficient routes for a fleet of vehicles.

Each vehicle leaves its own home location at a set time, drives a route of visits, and returns home.
A vehicle carries a limited amount of demand over its whole route, and every visit only accepts a
vehicle inside its own time window: a vehicle that arrives early waits, and a vehicle that arrives
too late cannot finish servicing in time. A visit that fits on no route is left unassigned rather
than forced onto one.

The assignment *is* the route: a vehicle carries the ordered list of visit ids it services
(`visitIds`), so "unassigned" simply means the visit appears in no vehicle's list.

![Vehicle Routing Screenshot](./vehicle-routing-screenshot.png)

## Constraints

| Name                                | Level  | Description                                                                        |
|-------------------------------------|--------|------------------------------------------------------------------------------------|
| Vehicle capacity                    | Hard   | The total demand of all visits assigned to a vehicle must not exceed its capacity. |
| Service finished after max end time | Hard   | A visit must be serviced before its maximum end time.                              |
| Maximize visits assigned            | Medium | As many visits as possible should be assigned to a vehicle.                        |
| Minimize travel time                | Soft   | Minimize the total travel time of all vehicles.                                    |

- [Map service](#map-service)
- [Run the application](#run-the-application)
- [Run the packaged application](#run-the-packaged-application)
- [Run the application in a container](#run-the-application-in-a-container)
- [Run it native](#run-it-native)

> [!TIP]
> <img src="https://docs.timefold.ai/_/img/models/field-service-routing.svg" align="right" width="50px" /> [Check out our off-the-shelf model for Field Service Routing](https://app.timefold.ai/models/field-service-routing/v1). This model goes beyond basic Vehicle Routing and supports additional constraints such as priorities, skills, fairness and more.

## Map service

Driving time between locations is not computed by this quickstart's own code: it comes from the
Timefold Platform's **map service**.

Every location in the model - a vehicle's home location (`Vehicle.getHomeLocation()`) and a visit's
location (`Visit.getLocation()`) - is an `ai.timefold.solver.service.maps.api.model.Location`, and
both `Vehicle` and `Visit` implement `LocationAware` to expose it. Calling
`location.getDrivingTimeTo(otherLocation)` returns the driving time between the two, once the map
service has built a travel time matrix that covers them; this is what
`Vehicle.getTotalDrivingTimeSeconds()`, `Visit.getDrivingTimeSecondsFromPreviousStandstill()` and the
nearby-selection `LocationDistanceMeter` all call.

Building that matrix is the map service's responsibility, not this quickstart's: `VehicleRoutePlan`
implements `LocationsAwareSolverModel<HardMediumSoftScore>` so the platform can do it automatically
before every solve:

- `getLocations()` returns every location the matrix needs to cover - every vehicle's home location
  plus every visit's location.
- `getLocationSetName()` returns empty, so each solve builds its own one-off matrix rather than
  reusing a named, pre-built one.
- `setLocationsNotInMap()` / `getLocationsNotInMap()` let the map service report back any locations
  it could not resolve into the matrix, so the model retains that information instead of silently
  dropping it.

Two properties in `application.properties` control how the matrix gets built:

```properties
timefold.platform.map-service.use-remote=false
timefold.platform.map-service.enable-fallback=true
```

- `use-remote` switches between the platform's remote map service (real road-network driving times)
  and a local computation.
- `enable-fallback` allows falling back to the local computation when the remote one is disabled or
  unavailable.

`ConstraintVerifier`-based unit tests (`VehicleRoutePlanConstraintProviderTest`) build entities
directly, bypassing the model-conversion pipeline the map service hooks into, so `TestHelper` builds
the matrix itself for test data using the library's own test-support classes:
`HaversineTravelTimeAndDistanceMatrixProvider` (the same great-circle fallback calculation the
platform uses locally) and `TestDistanceCalculator.initDistanceMaps(...)`, applied to
`VehicleRoutePlan.getLocations()` - the same locations the real map service would be given.

## Prerequisites

1. Install Java and Maven, for example with [Sdkman](https://sdkman.io):

   ```sh
   sdk install java
   sdk install maven
   ```

## Run the application

1. Git clone the timefold-quickstarts repo and navigate to this directory:

   ```sh
   git clone https://github.com/TimefoldAI/timefold-quickstarts.git
   ...
   cd timefold-quickstarts/use-cases/vehicle-routing
   ```

2. (Optional) If you want to run a licensed edition (Plus / Enterprise), set up your license key first. See the [Timefold license tool](https://licenses.timefold.ai/) for instructions.

3. Start the application with Maven:

   1. Community Edition

      ```sh
      mvn quarkus:dev
      ```

   2. Plus / Enterprise Edition: The profile sets up the correct Maven artifacts to run the licensed version. See the `pom.xml` for the implementation details.

      ```sh
      mvn quarkus:dev -Denterprise
      ```

4. Visit [http://localhost:8080](http://localhost:8080) in your browser.

5. Click on the **Solve** button.

Then try _live coding_:

- Make some changes in the source code.
- Refresh your browser (F5).

Notice that those changes are immediately in effect.

## Run the packaged application

When you're done iterating in `quarkus:dev` mode, package the application to run as a conventional jar file.

1. Build it with Maven:

   ```sh
   mvn package
   ```

2. Run the Maven output:

   ```sh
   java -jar ./target/quarkus-app/quarkus-run.jar
   ```

   > **Note**
   > To run it on port 8081 instead, add `-Dquarkus.http.port=8081`.

3. Visit [http://localhost:8080](http://localhost:8080) in your browser.

4. Click on the **Solve** button.

## Run the application in a container

1. Build a container image:

   ```sh
   mvn package -Dcontainer
   ```

2. Run a container:

   ```sh
   docker run -p 8080:8080 --rm $USER/model-vehicle-routing-v1:0.0.1
   ```

## Run it native

To increase startup performance for serverless deployments, build the application as a native executable:

1. [Install GraalVM and gu install the native-image tool](https://quarkus.io/guides/building-native-image#configuring-graalvm).

2. Compile it natively. This takes a few minutes:

   ```sh
   mvn package -Dnative
   ```

3. Run the native executable:

   ```sh
   ./target/*-runner
   ```

4. Visit [http://localhost:8080](http://localhost:8080) in your browser.

5. Click on the **Solve** button.

## More information

Visit [timefold.ai](https://timefold.ai).
