package org.acme.schooltimetabling;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class JarWithDependenciesIT {

    // The property is set by maven-failsafe-plugin.
    private static final Path PATH_TO_JAR_WITH_DEPENDENCIES =
            Paths.get("target", System.getProperty("artifactName") + ".jar");

    private static final Path PATH_TO_JAVA_EXECUTABLE_UNIX =
            Paths.get(System.getenv("JAVA_HOME"), "bin", "java");
    private static final Path PATH_TO_JAVA_EXECUTABLE_WINDOWS =
            Paths.get(System.getenv("JAVA_HOME"), "bin", "java.exe");

    private Path pathToJavaExecutable;

    @BeforeEach
    void setJavaPath() {
        if (PATH_TO_JAVA_EXECUTABLE_WINDOWS.toFile().exists()) {
            pathToJavaExecutable = PATH_TO_JAVA_EXECUTABLE_WINDOWS;
        } else if (PATH_TO_JAVA_EXECUTABLE_UNIX.toFile().exists()) {
            pathToJavaExecutable = PATH_TO_JAVA_EXECUTABLE_UNIX;
        } else {
            Assertions.fail("Neither a Windows Java binary (" + PATH_TO_JAVA_EXECUTABLE_WINDOWS + ")"
                    + " nor a Unix Java binary (" + PATH_TO_JAVA_EXECUTABLE_UNIX + ") was found."
                    + System.lineSeparator() + "Maybe set JAVA_HOME?");
        }
    }

    @Test
    void runJarWithDependencies() throws IOException {
        Assumptions.assumeThat(PATH_TO_JAR_WITH_DEPENDENCIES)
                .as("Executable JAR not found. Maybe ensure maven-assembly-plugin was run beforehand?")
                .exists();

        Process process = new ProcessBuilder()
                .command(pathToJavaExecutable.toString(),
                        "-jar",
                        PATH_TO_JAR_WITH_DEPENDENCIES.toString())
                .inheritIO()
                .start();
        try {
            process.waitFor(1, TimeUnit.MINUTES);
            if (process.isAlive()) {
                Assertions.fail("Executable JAR timed out.");
            }
            Assertions.assertThat(process.exitValue())
                    .as("Executable JAR exited abnormally.")
                    .isZero();
        } catch (InterruptedException e) {
            Assertions.fail("Waiting for executable JAR to finish was interrupted.", e);
        } finally {
            process.destroyForcibly();
        }
    }

}
