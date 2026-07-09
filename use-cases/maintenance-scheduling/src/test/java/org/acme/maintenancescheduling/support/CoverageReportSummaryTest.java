package org.acme.maintenancescheduling.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class CoverageReportSummaryTest {

    @Test
    void summarizeReportsInstructionBranchAndConstraintProviderLineCoverage() throws IOException {
        Path reportPath = Files.createTempFile("jacoco-report", ".csv");
        Files.writeString(reportPath,
                """
                        GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,BRANCH_MISSED,BRANCH_COVERED,LINE_MISSED,LINE_COVERED,COMPLEXITY_MISSED,COMPLEXITY_COVERED,METHOD_MISSED,METHOD_COVERED
                        facilitylocation,org.acme.maintenancescheduling.solver,VehicleRoutingConstraintProvider,1,3,2,2,3,5,0,0,0,0
                        facilitylocation,org.acme.maintenancescheduling.service,AnotherClass,0,0,1,0,0,0,0,0,0,0
                        """,
                StandardCharsets.UTF_8);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            CoverageReportSummary.summarize(reportPath);
        } finally {
            System.setOut(originalOut);
            Files.deleteIfExists(reportPath);
        }

        String summary = output.toString(StandardCharsets.UTF_8);
        assertThat(summary).contains("[JaCoCo] Instruction coverage: 3/4 (");
        assertThat(summary).contains("[JaCoCo] Branch coverage: 2/5 (");
        assertThat(summary).contains("[JaCoCo] VehicleRoutingConstraintProvider line coverage: 5/8 (");
    }
}
