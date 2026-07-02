# Meeting Scheduling (Java, Quarkus, Maven)

Assign timeslots and rooms for meetings to produce a better schedule.

![Meeting Scheduling Screenshot](./meeting-scheduling-screenshot.png)

## Constraints

| Name                                       | Level  | Description                                                                 |
|--------------------------------------------|--------|-----------------------------------------------------------------------------|
| Room conflict                              | Hard   | Two meetings cannot be held in the same room at the same time.              |
| Avoid overtime                             | Hard   | Meetings should not be scheduled outside working hours.                     |
| Required attendance conflict               | Hard   | A required attendee cannot be in two meetings at the same time.             |
| Required room capacity                     | Hard   | The room must be large enough for all required attendees.                   |
| Start and end on same day                  | Hard   | A meeting must start and end on the same day.                               |
| Required and preferred attendance conflict | Medium | Avoid scheduling a required and preferred attendee in conflicting meetings. |
| Preferred attendance conflict              | Medium | Avoid scheduling a preferred attendee in two meetings at the same time.     |
| Do meetings as soon as possible            | Soft   | Schedule meetings as early as possible in the day.                          |
| One break between consecutive meetings     | Soft   | There should be a break between consecutive meetings for an attendee.       |
| Overlapping meetings                       | Soft   | Minimize overlapping meetings for attendees.                                |
| Assign larger rooms first                  | Soft   | Assign the largest available room to each meeting.                          |
| Room stability                             | Soft   | An attendee's consecutive meetings should be in the same room.              |

- [Run the application](#run-the-application)
- [Run the packaged application](#run-the-packaged-application)
- [Run the application in a container](#run-the-application-in-a-container)
- [Run it native](#run-it-native)

## Prerequisites

1. Install Java and Maven, for example with [Sdkman](https://sdkman.io):

   ```sh
   $ sdk install java
   $ sdk install maven
   ```

## Run the application

1. Git clone the timefold-quickstarts repo and navigate to this directory:

   ```sh
   $ git clone https://github.com/TimefoldAI/timefold-quickstarts.git
   ...
   $ cd timefold-quickstarts/java/meeting-scheduling
   ```

2. (Optional) If you want to run a licensed edition (Plus / Enterprise), set up your license key first. See the [Timefold license tool](https://licenses.timefold.ai/) for instructions.

3. Start the application with Maven:

   1. Community Edition
   
      ```sh
      $ mvn quarkus:dev
      ```
   
   2. Plus / Enterprise Edition: The profile sets up the correct Maven artifacts to run the licensed version. See the `pom.xml` for the implementation details.

      ```sh
      $ mvn quarkus:dev -Denterprise
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
   $ mvn package
   ```

2. Run the Maven output:

   ```sh
   $ java -jar ./target/quarkus-app/quarkus-run.jar
   ```

   > **Note**
   > To run it on port 8081 instead, add `-Dquarkus.http.port=8081`.

3. Visit [http://localhost:8080](http://localhost:8080) in your browser.

4. Click on the **Solve** button.

## Run the application in a container

1. Build a container image:

   ```sh
   $ mvn package -Dcontainer
   ```

2. Run a container:

   ```sh
   $ docker run -p 8080:8080 --rm $USER/meeting-scheduling:1.0-SNAPSHOT
   ```

## Run it native

To increase startup performance for serverless deployments, build the application as a native executable:

1. [Install GraalVM and gu install the native-image tool](https://quarkus.io/guides/building-native-image#configuring-graalvm).

2. Compile it natively. This takes a few minutes:

   ```sh
   $ mvn package -Dnative
   ```

3. Run the native executable:

   ```sh
   $ ./target/*-runner
   ```

4. Visit [http://localhost:8080](http://localhost:8080) in your browser.

5. Click on the **Solve** button.

## More information

Visit [timefold.ai](https://timefold.ai).
