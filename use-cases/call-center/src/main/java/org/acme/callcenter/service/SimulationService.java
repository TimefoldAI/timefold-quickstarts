package org.acme.callcenter.service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.acme.callcenter.data.DataGenerator;
import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;

@ApplicationScoped
public class SimulationService {

    private static final int MAX_DURATION_SECONDS = 60;
    private static final int MIN_DURATION_SECONDS = 10;
    private static final int MAX_FREQUENCY_PER_MINUTE = 60;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final SolverService solverService;
    private final DataGenerator dataGenerator;
    private final ConcurrentMap<Long, CallInProgress> callsInProgress = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    // Initial simulation values that are overridden by a client.
    private int durationSeconds = 30;
    private int frequencyPerMinute = 25;

    private ScheduledFuture<?> addNewCallScheduledFuture;

    @Inject
    public SimulationService(SolverService solverService, DataGenerator dataGenerator) {
        this.solverService = solverService;
        this.dataGenerator = dataGenerator;
    }

    private ScheduledFuture<?> scheduleCallEnd(Call call, long delay, TimeUnit timeUnit) {
        return scheduledExecutorService.schedule(() -> {
            callsInProgress.computeIfPresent(call.getId(), (callId, callInProgress) -> {
                solverService.removeCall(callId);
                return null;
            });
        }, delay, timeUnit);
    }

    public void restartSimulation(int frequencyPerMinute, int durationSeconds) {
        if (frequencyPerMinute < 0 || frequencyPerMinute > MAX_FREQUENCY_PER_MINUTE) {
            throw new IllegalArgumentException(
                    "FrequencyPerMinute (" + frequencyPerMinute + ") must be between 0 and " + MAX_FREQUENCY_PER_MINUTE + ".");
        }
        if (durationSeconds < MIN_DURATION_SECONDS || durationSeconds > MAX_DURATION_SECONDS) {
            throw new IllegalArgumentException(
                    "DurationSeconds (" + durationSeconds + ") must be between " + MIN_DURATION_SECONDS + " and "
                            + MAX_DURATION_SECONDS + ".");
        }

        if (running.get()) {
            stopSimulation();
            startSimulation(frequencyPerMinute, durationSeconds);
        }

        this.frequencyPerMinute = frequencyPerMinute;
        this.durationSeconds = durationSeconds;
    }

    public void startSimulation() {
        startSimulation(frequencyPerMinute, durationSeconds);
    }

    private void startSimulation(int frequency, int duration) {
        if (running.getAndSet(true)) {
            return; // The simulation has been already running.
        }

        if (frequency == 0) {
            return;
        }
        int delayInSeconds = 60 / frequency;

        addNewCallScheduledFuture = scheduledExecutorService.scheduleAtFixedRate(
                () -> solverService.addCall(dataGenerator.generateCall(duration)), 0, delayInSeconds, TimeUnit.SECONDS);
    }

    public void stopSimulation() {
        running.set(false);
        if (addNewCallScheduledFuture != null) {
            addNewCallScheduledFuture.cancel(true);
            addNewCallScheduledFuture = null;
        }
    }

    /**
     * Cancels the scheduled end of a call in progress and schedules a new end, postponed by a minute.
     */
    public void prolongCall(long callId) {
        callsInProgress.computeIfPresent(callId, (id, callInProgress) -> {
            callInProgress.scheduledCallEnd.cancel(true);
            Call call = callInProgress.call;
            Duration remaining = call.getDuration().minus(Duration.between(call.getPickUpTime(), LocalTime.now()));
            long nextCallEndSeconds = remaining.plusMinutes(1).getSeconds(); // Prolong the call by a minute.
            return new CallInProgress(call, scheduleCallEnd(call, nextCallEndSeconds, TimeUnit.SECONDS));
        });
    }

    /**
     * Not thread-safe. The method is called from the Solver thread.
     */
    public void onNewBestSolution(CallCenter newBestSolution) {
        newBestSolution.getCalls().forEach(call -> {
            if (call.getPreviousCallOrAgent() != null && call.getPreviousCallOrAgent() instanceof Agent) {
                callsInProgress.computeIfAbsent(call.getId(), callId -> {
                    // Schedule finishing a call by an agent.
                    ScheduledFuture<?> existingCallScheduledFuture =
                            scheduleCallEnd(call, call.getDuration().getSeconds(), TimeUnit.SECONDS);
                    // Pick-up time needs to be set, as it hasn't been propagated to this best solution yet.
                    call.setPickUpTime(LocalTime.now());
                    return new CallInProgress(call, existingCallScheduledFuture);
                });
            }
        });
    }

    private static class CallInProgress {
        private final Call call;
        private final ScheduledFuture<?> scheduledCallEnd;

        public CallInProgress(Call call, ScheduledFuture<?> scheduledCallEnd) {
            this.call = call;
            this.scheduledCallEnd = scheduledCallEnd;
        }
    }
}
