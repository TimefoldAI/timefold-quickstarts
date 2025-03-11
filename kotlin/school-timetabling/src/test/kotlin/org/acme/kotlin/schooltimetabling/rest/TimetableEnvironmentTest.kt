package org.acme.kotlin.schooltimetabling.rest

import ai.timefold.solver.core.api.solver.Solver
import ai.timefold.solver.core.api.solver.SolverFactory
import ai.timefold.solver.core.config.solver.EnvironmentMode
import ai.timefold.solver.core.config.solver.SolverConfig
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.acme.kotlin.schooltimetabling.domain.Timetable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.time.Duration

@QuarkusTest
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class TimetableEnvironmentTest {

    @Inject
    var solverConfig: SolverConfig? = null

    @Test
    fun solveFullAssert() {
        solve(EnvironmentMode.FULL_ASSERT)
    }

    @Test
    fun solveStepAssert() {
        solve(EnvironmentMode.STEP_ASSERT)
    }

    fun solve(environmentMode: EnvironmentMode) {
        // Load the problem
        val problem: Timetable = given()
            .`when`()["/demo-data/SMALL"]
            .then()
            .statusCode(200)
            .extract()
            .`as`(Timetable::class.java)

        // Update the environment
        val updatedConfig = solverConfig!!.copyConfig()
            .withTerminationSpentLimit(Duration.ofSeconds(30))
            .withEnvironmentMode(environmentMode)
        val solverFactory: SolverFactory<Timetable> = SolverFactory.create(updatedConfig)

        // Solve the problem
        val solver: Solver<Timetable> = solverFactory.buildSolver()
        val solution: Timetable = solver.solve(problem)
        assertThat(solution.score?.isFeasible).isTrue()
    }
}
