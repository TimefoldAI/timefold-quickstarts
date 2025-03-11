# Vehicle Routing with time windows and capacity planning (Java, Quarkus, Maven)

Find the most efficient routes for a fleet of vehicles.

![Vehicle Routing Screenshot](./vehicle-routing-screenshot.png)

- [Run the application](#run-the-application)
- [Run the application with Timefold Solver Enterprise Edition](#run-the-application-with-timefold-solver-enterprise-edition)
- [Run the packaged application](#run-the-packaged-application)
- [Run the application in a container](#run-the-application-in-a-container)
- [Run it native](#run-it-native)

> [!TIP]  
> <img src="https://docs.timefold.ai/_/img/models/field-service-routing.svg" align="right" width="50px" /> [Check out our off-the-shelf model for Field Service Routing](https://app.timefold.ai/models/field-service-routing/v1). This model goes beyond basic Vehicle Routing and supports additional constraints such as priorities, skills, fairness and more.

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
   $ cd timefold-quickstarts/java/vehicle-routing
   ```

2. Start the application with Maven:
   ```sh
   $ mvn quarkus:dev
   ```

3. Visit [http://localhost:8080](http://localhost:8080) in your browser.

4. Click on the **Solve** button.

Then try _live coding_:

- Make some changes in the source code.
- Refresh your browser (F5). 
- Notice that those changes are immediately visible.

## Run the application with Timefold Solver Enterprise Edition

For high-scalability use cases, switch to [Timefold Solver Enterprise Edition](https://docs.timefold.ai/timefold-solver/latest/enterprise-edition/enterprise-edition), our commercial offering.  
[Contact Timefold](https://timefold.ai/contact) to obtain the credentials required to access our private Enterprise Maven repository.

1. Create `.m2/settings.xml` in your home directory with the following content:
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

1. Compile it with Maven:
   ```sh
   $ mvn package
   ```

2. Run it:
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
   The container image name

2. Run a container:
   ```sh
   $ docker run -p 8080:8080 --rm $USER/vehicle-routing:1.0-SNAPSHOT
   ```

## Run it native

To increase startup performance for serverless deployments, build the application as a native executable:

1. [Install GraalVM and gu install the native-image tool](https://quarkus.io/guides/building-native-image#configuring-graalvm).

2. Compile it natively.  
   This takes a few minutes:
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
