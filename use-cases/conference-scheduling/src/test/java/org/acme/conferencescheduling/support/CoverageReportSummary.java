package org.acme.conferencescheduling.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CoverageReportSummary {

    private static final int CLASS_INDEX = 2;
    private static final int INSTRUCTION_MISSED_INDEX = 3;
    private static final int INSTRUCTION_COVERED_INDEX = 4;
    private static final int BRANCH_MISSED_INDEX = 5;
    private static final int BRANCH_COVERED_INDEX = 6;
    private static final int LINE_MISSED_INDEX = 7;
    private static final int LINE_COVERED_INDEX = 8;
    private static final String CONSTRAINT_PROVIDER_SUFFIX = "ConstraintProvider";

    private CoverageReportSummary() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected a single JaCoCo CSV report path argument.");
        }
        summarize(Path.of(args[0]));
    }

    static void summarize(Path reportPath) {
        List<String> lines;
        try {
            lines = Files.readAllLines(reportPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read JaCoCo CSV report " + reportPath, e);
        }

        long instructionMissed = 0;
        long instructionCovered = 0;
        long branchMissed = 0;
        long branchCovered = 0;
        long constraintProviderLineMissed = 0;
        long constraintProviderLineCovered = 0;
        String constraintProviderName = null;

        for (int i = 1; i < lines.size(); i++) {
            var columns = lines.get(i).split(",", -1);
            if (columns.length <= LINE_COVERED_INDEX) {
                throw new IllegalStateException("Unexpected JaCoCo CSV row at line %d: %s".formatted(i + 1, lines.get(i)));
            }
            instructionMissed += parseLong(columns[INSTRUCTION_MISSED_INDEX]);
            instructionCovered += parseLong(columns[INSTRUCTION_COVERED_INDEX]);
            branchMissed += parseLong(columns[BRANCH_MISSED_INDEX]);
            branchCovered += parseLong(columns[BRANCH_COVERED_INDEX]);
            if (columns[CLASS_INDEX].endsWith(CONSTRAINT_PROVIDER_SUFFIX)) {
                constraintProviderName = columns[CLASS_INDEX];
                constraintProviderLineMissed = parseLong(columns[LINE_MISSED_INDEX]);
                constraintProviderLineCovered = parseLong(columns[LINE_COVERED_INDEX]);
            }
        }

        var instructionTotal = instructionMissed + instructionCovered;
        var instructionPercent = instructionTotal == 0 ? 0.0 : instructionCovered * 100.0 / instructionTotal;
        System.out.printf("[JaCoCo] Instruction coverage: %d/%d (%.1f%%)%n",
                instructionCovered, instructionTotal, instructionPercent);

        var branchTotal = branchMissed + branchCovered;
        var branchPercent = branchTotal == 0 ? 0.0 : branchCovered * 100.0 / branchTotal;
        System.out.printf("[JaCoCo] Branch coverage: %d/%d (%.1f%%)%n",
                branchCovered, branchTotal, branchPercent);

        var constraintProviderLineTotal = constraintProviderLineMissed + constraintProviderLineCovered;
        var constraintProviderLinePercent = constraintProviderLineTotal == 0
                ? 0.0
                : constraintProviderLineCovered * 100.0 / constraintProviderLineTotal;
        var constraintProviderLabel = constraintProviderName == null ? CONSTRAINT_PROVIDER_SUFFIX : constraintProviderName;
        System.out.printf("[JaCoCo] %s line coverage: %d/%d (%.1f%%)%n",
                constraintProviderLabel, constraintProviderLineCovered, constraintProviderLineTotal,
                constraintProviderLinePercent);
    }

    private static long parseLong(String value) {
        return value == null || value.isEmpty() ? 0L : Long.parseLong(value);
    }
}
