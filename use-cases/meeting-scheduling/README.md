# Meeting Scheduling (Java, Quarkus, Maven)

Assign a room and a start to each meeting to produce a better schedule.

The input describes *when* meetings may be held with a single time configuration: the office hours of
each day, plus the granularity in minutes those hours are divided into. A meeting states its
`durationInMinutes` and, once solved, its `startDateTime`. The solver internally discretizes those
office hours into `TimeGrain`s, but that stays a solver domain concept and never reaches the API.

![Meeting Scheduling Screenshot](./meeting-scheduling-screenshot.png)

## Constraints

| Name                                                 | Level  | Description                                                                                           |
|------------------------------------------------------|--------|-------------------------------------------------------------------------------------------------------|
| Room conflict                                        | Hard   | Two meetings must not be held in the same room at the same time.                                       |
| Don't go in overtime                                 | Hard   | A meeting must finish within the office hours of its day.                                              |
| Required attendance conflict                         | Hard   | A required attendee must not be expected in two meetings at the same time.                             |
| Required room capacity                               | Hard   | A meeting's room must seat every attendee of that meeting.                                             |
| Start and end on same day                            | Hard   | A meeting must start and end on the same day.                                                          |
| Required and preferred attendance conflict           | Medium | A person required in one meeting should not have to skip another meeting they would prefer to attend.  |
| Preferred attendance conflict                        | Medium | A person should not have to pick between two meetings they would both prefer to attend.                |
| Do all meetings as soon as possible                  | Soft   | A meeting should be held as early in the scheduling horizon as possible.                               |
| One TimeGrain break between two consecutive meetings | Soft   | Two consecutive meetings should be separated by at least one free time slot.                           |
| Overlapping meetings                                 | Soft   | Two meetings should not run in parallel.                                                               |
| Assign larger rooms first                            | Soft   | A meeting should be held in the largest room available, so smaller rooms stay free.                    |
| Room stability                                       | Soft   | An attendee's consecutive meetings should be held in the same room, so they do not have to move.       |

- [Run the application](#run-the-application)
- [Run the packaged application](#run-the-packaged-application)
- [Run the application in a container](#run-the-application-in-a-container)
- [Run it native](#run-it-native)

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
   cd timefold-quickstarts/use-cases/meeting-scheduling
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
   docker run -p 8080:8080 --rm $USER/meeting-scheduling:0.0.1
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
