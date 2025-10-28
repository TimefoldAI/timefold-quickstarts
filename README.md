<p align="center">
  <a href="https://solver.timefold.ai">
    <img src="/timefold-solver-logo.png" width="400px"  alt="Timefold Solver" />
  </a>
</p>

[![GitHub Discussions](https://img.shields.io/github/discussions/TimefoldAI/timefold-solver?style=for-the-badge&logo=github)](https://github.com/TimefoldAI/timefold-solver/discussions)

This repository contains quickstarts for [Timefold Solver](https://github.com/TimefoldAI/timefold-solver), an AI constraint solver for Java and Kotlin. 
It shows different use cases and basic implementations to get you started on your PlanningAI journey.

## Overview

| Use Case                                                                | Notable Solver Concepts                                  |
|-------------------------------------------------------------------------|----------------------------------------------------------|
| 🚚 <a href="#-vehicle-routing">Vehicle Routing</a>                      | Chained Through Time, Shadow Variables                   |
| 🧑‍💼 <a href="#-employee-scheduling">Employee Scheduling</a>           | Load Balancing                                           |
| 🛠️ <a href="#-maintenance-scheduling">Maintenance Scheduling</a>       | TimeGrain, Shadow Variable, Variable Listener            |
| 📦 <a href="#-food-packaging">Food Packaging</a>                        | Chained Through Time, Shadow Variables, Pinning          |
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

## Use cases

### 🚚 Vehicle Routing

Find the most efficient routes for vehicles to reach visits, considering vehicle capacity and time windows when visits are available. Sometimes also called "CVRPTW".

![Vehicle Routing Screenshot](java/vehicle-routing/vehicle-routing-screenshot.png)

- [Run quarkus-vehicle-routing](java/vehicle-routing/README.MD) (Java, Maven, Quarkus)

> [!TIP]
>  <img src="https://docs.timefold.ai/_/img/models/field-service-routing.svg" align="right" width="50px" /> [Check out our off-the-shelf model for Field Service Routing](https://app.timefold.ai/models/field-service-routing). This model goes beyond basic Vehicle Routing and supports additional constraints such as priorities, skills, fairness and more.

---

### 🧑‍💼 Employee Scheduling

Schedule shifts to employees, accounting for employee availability and shift skill requirements.

![Employee Scheduling Screenshot](java/employee-scheduling/employee-scheduling-screenshot.png)

- [Run quarkus-employee-scheduling](java/employee-scheduling/README.MD) (Java, Maven, Quarkus)

> [!TIP]
>  <img src="https://docs.timefold.ai/_/img/models/employee-shift-scheduling.svg" align="right" width="50px" /> [Check out our off-the-shelf model for Employee Shift Scheduling](https://app.timefold.ai/models/employee-scheduling). This model supports many additional constraints such as skills, pairing employees, fairness and more.

---

### 🛠️ Maintenance Scheduling

Schedule maintenance jobs to crews over time to reduce both premature and overdue maintenance.

![Maintenance Scheduling Screenshot](java/maintenance-scheduling/maintenance-scheduling-screenshot.png)

- [Run quarkus-maintenance-scheduling](java/maintenance-scheduling/README.adoc) (Java, Maven, Quarkus)

---

### 📦 Food Packaging

Schedule food packaging orders to manufacturing lines to minimize downtime and fulfill all orders on time.

![Food Packaging Screenshot](java/food-packaging/food-packaging-screenshot.png)

- [Run quarkus-food-packaging](java/food-packaging/README.adoc) (Java, Maven, Quarkus)

---

### 🛒 Order Picking

Generate an optimal picking plan for completing a set of orders.

![Order Picking Screenshot](java/order-picking/order-picking-screenshot.png)

- [Run quarkus-order-picking](java/order-picking/README.adoc) (Java, Maven, Quarkus)

---

### 🏫 School Timetabling

Assign lessons to timeslots and rooms to produce a better schedule for teachers and students.

![School Timetabling Screenshot](java/school-timetabling/school-timetabling-screenshot.png)

- [Run quarkus-school-timetabling](java/school-timetabling/README.adoc) (Java, Maven or Gradle, Quarkus)
- [Run spring-boot-school-timetabling](java/spring-boot-integration/README.adoc) (Java, Maven or Gradle, Spring Boot)
- [Run kotlin-quarkus-school-timetabling](kotlin/school-timetabling/README.adoc) (Kotlin, Maven, Quarkus)

Without a UI:

- [Run hello-world-school-timetabling](java/hello-world/README.adoc) (Java, Maven or Gradle)

---

### 🏭 Facility Location Problem

Pick the best geographical locations for new stores, distribution centers, COVID test centers, or telecom masts.

![Facility Location Screenshot](java/facility-location/facility-location-screenshot.png)

- [Run quarkus-facility-location](java/facility-location/README.adoc) (Java, Maven, Quarkus)

---

### 🎤 Conference Scheduling

Assign conference talks to timeslots and rooms to produce a better schedule for speakers.

![Conference Scheduling Screenshot](java/conference-scheduling/conference-scheduling-screenshot.png)

- [Run quarkus-conference-scheduling](java/conference-scheduling/README.adoc) (Java, Maven, Quarkus)

---

### 🛏️ Bed Allocation Scheduling

Assign beds to patient stays to produce a better schedule for hospitals.

![Bed Scheduling Screenshot](java/bed-allocation/bed-allocation-screenshot.png)

- [Run quarkus-bed-allocation-scheduling](java/bed-allocation/README.adoc) (Java, Maven, Quarkus)

---

### 🛫 Flight Crew Scheduling

Assign crew to flights to produce a better schedule for flight assignments.

![Flight Crew Scheduling Screenshot](java/flight-crew-scheduling/flight-crew-scheduling-screenshot.png)

- [Run quarkus-flight-crew-scheduling](java/flight-crew-scheduling/README.adoc) (Java, Maven, Quarkus)

---

### 👥 Meeting Scheduling

Assign timeslots and rooms for meetings to produce a better schedule.

![Meeting Scheduling Screenshot](java/meeting-scheduling/meeting-scheduling-screenshot.png)

- [Run quarkus-meeting-scheduling](java/meeting-scheduling/README.adoc) (Java, Maven, Quarkus)

---

### ✅ Task Assigning

Assign employees to tasks to produce a better plan for task assignments.

![Task Assigning Screenshot](java/task-assigning/task-assigning-screenshot.png)

- [Run quarkus-task-assigning](java/task-assigning/README.adoc) (Java, Maven, Quarkus)

---

### 📆 Project Job Scheduling

Assign jobs for execution to produce a better schedule for project job allocations.

![Project Job Scheduling Screenshot](java/project-job-scheduling/project-job-scheduling-screenshot.png)

- [Run quarkus-project-job-scheduling](java/project-job-scheduling/README.adoc) (Java, Maven, Quarkus)

---

### 🏆 Sports League Scheduling

Assign rounds to matches to produce a better schedule for league matches.

![Sports League Scheduling Screenshot](java/sports-league-scheduling/sports-league-scheduling-screenshot.png)

- [Run quarkus-sports-league-scheduling](java/sports-league-scheduling/README.adoc) (Java, Maven, Quarkus)

---

### 🏅 Tournament Scheduling

Tournament Scheduling service assigning teams to tournament matches.

![Tournament Scheduling Screenshot](java/tournament-scheduling/tournament-scheduling-screenshot.png)

- [Run quarkus-tournament-scheduling](java/tournament-scheduling/README.adoc) (Java, Maven, Quarkus)

---

## Legal notice

Timefold Quickstarts was [forked](https://timefold.ai/blog/2023/optaplanner-fork/) on 20 April 2023 from OptaPlanner Quickstarts, which was entirely Apache-2.0 licensed (a permissive license).

Timefold Quickstarts is a derivative work of OptaPlanner Quickstarts, which includes copyrights of the original creator, Red Hat Inc., affiliates, and contributors, that were all entirely licensed under the Apache-2.0 license. 
Every source file has been modified.
