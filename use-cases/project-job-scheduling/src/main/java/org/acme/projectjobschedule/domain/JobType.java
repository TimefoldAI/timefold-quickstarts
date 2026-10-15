package org.acme.projectjobschedule.domain;

/**
 * A {@link Project} is bracketed by two zero-duration jobs, so that every real job has at least one
 * predecessor and at least one successor: {@link #SOURCE} starts it and {@link #SINK} ends it.
 * Those two are never planned - their allocation is pinned - which makes the sink's end date the
 * project's completion date.
 */
public enum JobType {
    SOURCE,
    STANDARD,
    SINK;
}
