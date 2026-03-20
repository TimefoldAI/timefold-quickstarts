# School Timetabling (Java, Maven or Gradle)

Assign lessons to timeslots and rooms to produce a better schedule for teachers and students.

## Constraints

| Name                          | Level | Description                                                                   |
|-------------------------------|-------|-------------------------------------------------------------------------------|
| Room conflict                 | Hard  | Two lessons cannot be scheduled in the same room at the same time.            |
| Teacher conflict              | Hard  | A teacher cannot teach two lessons at the same time.                          |
| Student group conflict        | Hard  | A student group cannot attend two lessons at the same time.                   |
| Teacher room stability        | Soft  | A teacher should teach all their lessons in the same room.                    |
| Teacher time efficiency       | Soft  | A teacher should have consecutive lessons to minimize gaps in their schedule. |
| Student group subject variety | Soft  | A student group should not have the same subject in consecutive timeslots.    |

- [Run the application](#run-the-application)

## Prerequisites

1. Install Java and Maven, for example with [Sdkman](https://sdkman.io):

   ```sh
   $ sdk install java
   $ sdk install maven
   ```

## Run the application

1. Git clone the timefold-quickstarts repo:

   ```sh
   $ git clone https://github.com/TimefoldAI/timefold-quickstarts.git
   ...
   $ cd timefold-quickstarts/java/hello-world
   ```

2. Start the application with Maven:

   ```sh
   $ mvn verify
   ...
   $ java -jar target/hello-world-run.jar
   ```

   or with Gradle:

   ```sh
   $ gradle run
   ```

Look for the planning solution in the console log.

## More information

Visit [timefold.ai](https://timefold.ai).
