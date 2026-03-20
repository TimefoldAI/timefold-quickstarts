# School Timetabling (Java, Quarkus, Maven or Gradle)

Assign lessons to timeslots and rooms to produce a better schedule for teachers and students.

![School Timetabling Screenshot](./school-timetabling-screenshot.png)

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
- [Run the application with Timefold Solver Enterprise Edition](#run-the-application-with-timefold-solver-enterprise-edition)
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
   $ cd timefold-quickstarts/java/school-timetabling
   ```

2. Start the application with Maven:

   ```sh
   $ mvn quarkus:dev
   ```

   or with Gradle:

   ```sh
   $ gradle quarkusDev
   ```

3. Visit [http://localhost:8080](http://localhost:8080) in your browser.

4. Click on the **Solve** button.

Then try _live coding_:

- Make some changes in the source code.
- Refresh your browser (F5).

Notice that those changes are immediately in effect.

## Run the application with Timefold Solver Enterprise Edition

For high-scalability use cases, switch to [Timefold Solver Enterprise Edition](https://docs.timefold.ai/timefold-solver/latest/enterprise-edition/enterprise-edition), our commercial offering.
[Contact Timefold](https://timefold.ai/contact) to obtain the credentials required to access our private Enterprise Maven repository.

1. Configure the Enterprise Edition Maven repository:

   **Maven:** Create `.m2/settings.xml` in your home directory with the following content:

   ```xml
   <settings>
     ...
     <servers>
       <server>
         <!-- Replace "my_username" and "my_password" with credentials obtained from a Timefold representative. -->
         <id>timefold-solver-enterprise</id>
         <username>my_username</username>
         <password>my_password</password>
       </server>
     </servers>
     ...
   </settings>
   ```

   See [Settings Reference](https://maven.apache.org/settings.html) for more information on Maven settings.

   **Gradle:** Add the following in your `build.gradle`:

   ```groovy
   repositories {
     mavenCentral()
     maven {
       url "https://timefold.jfrog.io/artifactory/releases/"
       credentials { // Replace "my_username" and "my_password" with credentials obtained from a Timefold representative.
           username "my_username"
           password "my_password"
       }
       authentication {
           basic(BasicAuthentication)
       }
     }
   }
   ```

   See [Settings Reference](https://docs.gradle.org/current/dsl/org.gradle.api.artifacts.repositories.AuthenticationSupported.html#content) for more information on Gradle settings.

2. Start the application with Maven:

   ```sh
   $ mvn clean quarkus:dev -Denterprise
   ```

3. Visit [http://localhost:8080](http://localhost:8080) in your browser.

4. Click on the **Solve** button.

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

   or with Gradle:

   ```sh
   $ gradle clean build
   ```

2. Run the Maven output:

   ```sh
   $ java -jar ./target/quarkus-app/quarkus-run.jar
   ```

   or the Gradle output:

   ```sh
   $ java -jar ./build/quarkus-app/quarkus-run.jar
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
   $ docker run -p 8080:8080 --rm $USER/school-timetabling:1.0-SNAPSHOT
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
