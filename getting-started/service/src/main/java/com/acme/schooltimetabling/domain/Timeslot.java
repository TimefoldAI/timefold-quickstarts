package com.acme.schooltimetabling.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record Timeslot(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
}
