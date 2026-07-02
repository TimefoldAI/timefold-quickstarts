<p align="center">
  <a href="https://solver.timefold.ai">
    <img src="/timefold-solver-logo.png" width="400px"  alt="Timefold Solver" />
  </a>
</p>

[![GitHub Discussions](https://img.shields.io/github/discussions/TimefoldAI/timefold-solver?style=for-the-badge&logo=github)](https://github.com/TimefoldAI/timefold-solver/discussions)

This repository contains quickstarts for [Timefold Solver](https://github.com/TimefoldAI/timefold-solver), an AI constraint solver for Java and Kotlin. 
It shows different use cases and basic implementations to get you started on your optimization journey.

## Overview

| Use Case                                                                | Notable Solver Concepts                                  |
|-------------------------------------------------------------------------|----------------------------------------------------------|
| 🚚 <a href="#-vehicle-routing">Vehicle Routing</a>                      | Chained Through Time, Shadow Variables                   |
| 🧑 <a href="#-employee-scheduling">Employee Scheduling</a>              | Load Balancing                                           |
| 🛠️ <a href="#-maintenance-scheduling">Maintenance Scheduling</a>       | TimeGrain, Shadow Variable, Variable Listener            |
| 📦 <a href="#-food-packaging">Food Packaging</a>                        | Mixed Model, Shadow Variables, Pinning                   |
| 🛒 <a href="#-order-picking">Order Picking</a>                          | Chained Planning Variable, Shadow Variables              |
| 🏫 <a href="#-school-timetabling">School Timetabling</a>                | Timeslot                                                 |
| 🏭 <a href="#-facility-location-problem">Facility Location Problem</a>  | Shadow Variable                                          |
| 🎤 <a href="#-conference-scheduling">Conference Scheduling</a>          | Timeslot, Justifications                                 |
| 🛏️ <a href="#-bed-allocation-scheduling">Bed Allocation Scheduling</a> | Allows Unassigned                                        |
| 🛫 <a href="#-flight-crew-scheduling">Flight Crew Scheduling</a>        |                                                          |
| 👥 <a href="#-meeting-scheduling">Meeting Scheduling</a>                | TimeGrain                                                |
| ✅ <a href="#-task-assigning">Task Assigning</a>                         | Bendable Score, Chained Through Time, Allows Unassigned  |
| 📆 <a href="#-project-job-scheduling">Project Job Scheduling</a>        | Shadow Variables, Variable Listener, Strenght Comparator |
| 🏆 <a href="#-sports-league-scheduling">Sports League Scheduling</a>    | Consecutive Sequences                                    |
| 🏅 <a href="#-tournament-scheduling">Tournament Scheduling</a>          | Pinning, Load Balancing                                  |

> [!NOTE]
> The implementations in this repository serve as a starting point and/or inspiration when creating your own application.
> Timefold Solver is a library and does not include a UI. To illustrate these use cases a rudimentary UI is included in these quickstarts.

## Getting started implementations

Our website contains several getting started guides. All of them build the <a href="#-school-timetabling">School Timetabling</a> use case.
You can find the resulting projects for each of the guides in the `getting started` folder.

- [Service](getting-started/service) (Java, Maven, Quarkus)
- [Library: Core](getting-started/hello-world) (Java, Maven)
- [Library: Quarkus Integration](getting-started/quarkus-integration) (Java, Maven, Quarkus)
- [Library: Spring Boot Integration](getting-started/spring-boot-integration/README.md) (Java, Maven, Spring)

## Use cases

### 🚚 Vehicle Routing

Find the most efficient routes for vehicles to reach visits, considering vehicle capacity and time windows when visits are available. Sometimes also called "CVRPTW".

![Vehicle Routing Screenshot](quickstarts/vehicle-routing/vehicle-routing-screenshot.png)

- [View constraints](quickstarts/vehicle-routing/README.md#constraints)
- [Run quarkus-vehicle-routing](quickstarts/vehicle-routing/README.md) (Java, Maven, Quarkus)

> [!TIP]
>  <img src="https://docs.timefold.ai/_/img/models/field-service-routing.svg" align="right" width="50px" /> [Check out our off-the-shelf model for Field Service Routing](https://app.timefold.ai/models/field-service-routing). This model goes beyond basic Vehicle Routing and supports additional constraints such as priorities, skills, fairness and more.

---

### 🧑 Employee Scheduling

Schedule shifts to employees, accounting for employee availability and shift skill requirements.

![Employee Scheduling Screenshot](quickstarts/employee-scheduling/employee-scheduling-screenshot.png)

- [View constraints](quickstarts/employee-scheduling/README.md#constraints)
- [Run quarkus-employee-scheduling](quickstarts/employee-scheduling/README.md) (Java, Maven, Quarkus)

> [!TIP]
>  <img src="https://docs.timefold.ai/_/img/models/employee-shift-scheduling.svg" align="right" width="50px" /> [Check out our off-the-shelf model for Employee Shift Scheduling](https://app.timefold.ai/models/employee-scheduling). This model supports many additional constraints such as skills, pairing employees, fairness and more.

---

### 🛠 Maintenance Scheduling

Schedule maintenance jobs to crews over time to reduce both premature and overdue maintenance.

![Maintenance Scheduling Screenshot](quickstarts/maintenance-scheduling/maintenance-scheduling-screenshot.png)

- [View constraints](quickstarts/maintenance-scheduling/README.md#constraints)
- [Run quarkus-maintenance-scheduling](quickstarts/maintenance-scheduling/README.md) (Java, Maven, Quarkus)

---

### 📦 Food Packaging

Schedule food packaging orders to manufacturing lines to minimize downtime and fulfill all orders on time.

![Food Packaging Screenshot](quickstarts/food-packaging/food-packaging-screenshot.png)

- [View constraints](quickstarts/food-packaging/README.md#constraints)
- [Run quarkus-food-packaging](quickstarts/food-packaging/README.md) (Java, Maven, Quarkus)

---

### 🛒 Order Picking

Generate an optimal picking plan for completing a set of orders.

![Order Picking Screenshot](quickstarts/order-picking/order-picking-screenshot.png)

- [View constraints](quickstarts/order-picking/README.md#constraints)
- [Run quarkus-order-picking](quickstarts/order-picking/README.md) (Java, Maven, Quarkus)

---

### 🏫 School Timetabling

Assign lessons to timeslots and rooms to produce a better schedule for teachers and students.

![School Timetabling Screenshot](quickstarts/school-timetabling/school-timetabling-screenshot.png)

- [View constraints](quickstarts/school-timetabling/README.md#constraints)
- [Run quarkus-school-timetabling](quickstarts/school-timetabling/README.md) (Java, Maven or Gradle, Quarkus)
- [Run spring-boot-integration](quickstarts/getting-started/spring-boot-integration/README.md) (Java, Maven or Gradle, Spring Boot)
- [Run kotlin-quarkus-school-timetabling](quickstarts/school-timetabling-kotlin/README.md) (Kotlin, Maven, Quarkus)

Without a UI:

- [Service / REST API](quickstarts/getting-started/service) (Java, Maven, Quarkus)
- [Library / Console Application](quickstarts/getting-started/hello-world/README.md) (Java, Maven or Gradle)

---

### 🏭 Facility Location Problem

Pick the best geographical locations for new stores, distribution centers, COVID test centers, or telecom masts.

![Facility Location Screenshot](quickstarts/facility-location/facility-location-screenshot.png)

- [View constraints](quickstarts/facility-location/README.md#constraints)
- [Run quarkus-facility-location](quickstarts/facility-location/README.md) (Java, Maven, Quarkus)

---

### 🎤 Conference Scheduling

Assign conference talks to timeslots and rooms to produce a better schedule for speakers.

![Conference Scheduling Screenshot](quickstarts/conference-scheduling/conference-scheduling-screenshot.png)

- [View constraints](quickstarts/conference-scheduling/README.md#constraints)
- [Run quarkus-conference-scheduling](quickstarts/conference-scheduling/README.md) (Java, Maven, Quarkus)

---

### 🛏️ Bed Allocation Scheduling

Assign beds to patient stays to produce a better schedule for hospitals.

![Bed Scheduling Screenshot](quickstarts/bed-allocation/bed-allocation-screenshot.png)

- [View constraints](quickstarts/bed-allocation/README.md#constraints)
- [Run quarkus-bed-allocation-scheduling](quickstarts/bed-allocation/README.md) (Java, Maven, Quarkus)

---

### 🛫 Flight Crew Scheduling

Assign crew to flights to produce a better schedule for flight assignments.

![Flight Crew Scheduling Screenshot](quickstarts/flight-crew-scheduling/flight-crew-scheduling-screenshot.png)

- [View constraints](quickstarts/flight-crew-scheduling/README.md#constraints)
- [Run quarkus-flight-crew-scheduling](quickstarts/flight-crew-scheduling/README.md) (Java, Maven, Quarkus)

---

### 👥 Meeting Scheduling

Assign timeslots and rooms for meetings to produce a better schedule.

![Meeting Scheduling Screenshot](quickstarts/meeting-scheduling/meeting-scheduling-screenshot.png)

- [View constraints](quickstarts/meeting-scheduling/README.md#constraints)
- [Run quarkus-meeting-scheduling](quickstarts/meeting-scheduling/README.md) (Java, Maven, Quarkus)

---

### ✅ Task Assigning

Assign employees to tasks to produce a better plan for task assignments.

![Task Assigning Screenshot](quickstarts/task-assigning/task-assigning-screenshot.png)

- [View constraints](quickstarts/task-assigning/README.md#constraints)
- [Run quarkus-task-assigning](quickstarts/task-assigning/README.md) (Java, Maven, Quarkus)

---

### 📆 Project Job Scheduling

Assign jobs for execution to produce a better schedule for project job allocations.

![Project Job Scheduling Screenshot](quickstarts/project-job-scheduling/project-job-scheduling-screenshot.png)

- [View constraints](quickstarts/project-job-scheduling/README.md#constraints)
- [Run quarkus-project-job-scheduling](quickstarts/project-job-scheduling/README.md) (Java, Maven, Quarkus)

---

### 🏆 Sports League Scheduling

Assign rounds to matches to produce a better schedule for league matches.

![Sports League Scheduling Screenshot](quickstarts/sports-league-scheduling/sports-league-scheduling-screenshot.png)

- [View constraints](quickstarts/sports-league-scheduling/README.md#constraints)
- [Run quarkus-sports-league-scheduling](quickstarts/sports-league-scheduling/README.md) (Java, Maven, Quarkus)

---

### 🏅 Tournament Scheduling

Tournament Scheduling service assigning teams to tournament matches.

![Tournament Scheduling Screenshot](quickstarts/tournament-scheduling/tournament-scheduling-screenshot.png)

- [View constraints](quickstarts/tournament-scheduling/README.md#constraints)
- [Run quarkus-tournament-scheduling](quickstarts/tournament-scheduling/README.md) (Java, Maven, Quarkus)

---

## Legal notice

Timefold Quickstarts was [forked](https://timefold.ai/blog/2023/optaplanner-fork/) on 20 April 2023 from OptaPlanner Quickstarts, which was entirely Apache-2.0 licensed (a permissive license).

Timefold Quickstarts is a derivative work of OptaPlanner Quickstarts, which includes copyrights of the original creator, Red Hat Inc., affiliates, and contributors, that were all entirely licensed under the Apache-2.0 license. 
Every source file has been modified.
