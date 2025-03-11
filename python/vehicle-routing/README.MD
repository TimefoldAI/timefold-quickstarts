# Vehicle Routing (Python)

Find the most efficient routes for a fleet of vehicles.

![Vehicle Routing Screenshot](./vehicle-routing-screenshot.png)

- [Prerequisites](#prerequisites)
- [Run the application](#run-the-application)
- [Test the application](#test-the-application)

> [!TIP]
> <img src="https://docs.timefold.ai/_/img/models/field-service-routing.svg" align="right" width="50px" /> [Check out our off-the-shelf model for Field Service Routing](https://app.timefold.ai/models/field-service-routing/v1). This model goes beyond basic Vehicle Routing and supports additional constraints such as priorities, skills, fairness and more.

## Prerequisites

1. Install [Python 3.10+](https://www.python.org/downloads/).

2. Install JDK 17+, for example with [Sdkman](https://sdkman.io):
    ```sh
    $ sdk install java

## Run the application

1. Git clone the timefold-quickstarts repo and navigate to this directory:
   ```sh
   $ git clone https://github.com/TimefoldAI/timefold-quickstarts.git
   ...
   $ cd timefold-quickstarts/python/vehicle-routing
   ```

2. Create a virtual environment:
   ```sh
   $ python -m venv .venv
   ```

3. Activate the virtual environment:
   ```sh
   $ . .venv/bin/activate
   ```

4. Install the application:
   ```sh
   $ pip install -e .
   ```

5. Run the application:
   ```sh
   $ run-app
   ```

6. Visit [http://localhost:8080](http://localhost:8080) in your browser.

7. Click on the **Solve** button.

## Test the application

1. Run tests:
   ```sh
   $ pytest
   ```

## More information

Visit [timefold.ai](https://timefold.ai).