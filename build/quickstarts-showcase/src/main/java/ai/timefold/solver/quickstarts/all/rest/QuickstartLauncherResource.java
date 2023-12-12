package ai.timefold.solver.quickstarts.all.rest;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PathParam;

import ai.timefold.solver.quickstarts.all.domain.QuickstartMeta;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@jakarta.ws.rs.Path("quickstart")
public class QuickstartLauncherResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuickstartLauncherResource.class);

    @ConfigProperty(name = "startup-open-browser", defaultValue = "false")
    boolean startupOpenBrowser;
    @ConfigProperty(name = "quarkus.http.port")
    int httpPort;

    private List<QuickstartMeta> quickstartMetaList;
    private boolean development;
    private int nextPort = 8081;
    private Path baseDirectory;

    private Map<Integer, Process> portToProcessMap;

    public void setup(@Observes StartupEvent startupEvent) {
        quickstartMetaList = Arrays.asList(
                new QuickstartMeta("school-timetabling"),
                new QuickstartMeta("facility-location"),
                new QuickstartMeta("maintenance-scheduling"),
                new QuickstartMeta("vaccination-scheduling"),
                new QuickstartMeta("call-center"),
                new QuickstartMeta("vehicle-routing-capacity"),
                new QuickstartMeta("vehicle-routing-time-windows"),
                new QuickstartMeta("order-picking"),
                new QuickstartMeta("employee-scheduling"),
                new QuickstartMeta("food-packaging"));
        Path workingDirectory = Paths.get("").toAbsolutePath();
        if (Files.exists(workingDirectory.resolve("target"))) {
            baseDirectory = workingDirectory.getParent().getParent();
            development = true;
        } else {
            baseDirectory = workingDirectory.resolve(Paths.get("quickstarts", "binaries"));
            development = false;
        }
        portToProcessMap = new HashMap<>(quickstartMetaList.size());
        baseDirectory = baseDirectory.toAbsolutePath();

        if (startupOpenBrowser) {
            openInBrowser(httpPort);
        }
    }

    public void shutdown(@Observes ShutdownEvent shutdownEvent) {
        for (QuickstartMeta quickstartMeta : quickstartMetaList) {
            for (int port : quickstartMeta.getPorts()) {
                portToProcessMap.remove(port).destroy();
            }
            quickstartMeta.getPorts().clear();
        }
    }

    @GET
    public List<QuickstartMeta> getQuickstartMetaList() {
        return quickstartMetaList;
    }

    @jakarta.ws.rs.Path("{quickstartId}/launch")
    @POST
    public void launchQuickstart(@PathParam("quickstartId") String quickstartId) {
        QuickstartMeta quickstartMeta = quickstartMetaList.stream()
                .filter(quickstartMeta_ -> quickstartMeta_.getId().equals(quickstartId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The quickstartId (" + quickstartId + ") doesn't exist."));
        LOGGER.info("Starting quickstart ({})...", quickstartId);

        ProcessBuilder processBuilder;
        int port = this.nextPort;
        String portArg = "-Dquarkus.http.port=" + port;
        // CORS allows the JS to detect when the server has started
        String corsArg = "-Dquarkus.http.cors=true";
        this.nextPort++;
        if (development) {
            String mvnCommand;
            if (System.getProperty("os.name").startsWith("Windows")) {
                mvnCommand = baseDirectory.resolve(Paths.get("build", "mvnw.cmd")).toString();
            } else {
                mvnCommand = baseDirectory.resolve(Paths.get("build", "mvnw")).toString();
            }
            processBuilder = new ProcessBuilder(mvnCommand, "-f", "../use-cases/" + quickstartId, "quarkus:dev", portArg,
                    corsArg, "-Ddebug=false");
            processBuilder.directory(baseDirectory.resolve("build").toFile());
        } else {
            processBuilder = new ProcessBuilder("java", portArg, corsArg, "-jar",
                    getQuickstartRunnerJar(quickstartId).getAbsolutePath());
            processBuilder.directory(baseDirectory.resolve(Paths.get("use-cases", quickstartId)).toFile());
        }
        processBuilder.inheritIO();
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new IllegalStateException("Failed starting the subprocess for quickstart (" + quickstartId + ").\n"
                    + (development ? "Maybe check if \"build/mvnw\" and \"build\\mvnw.cmd\" exist."
                            : "Maybe install Java and check if \"java --version\" works."),
                    e);
        }
        portToProcessMap.put(port, process);
        quickstartMeta.getPorts().add(port);
    }

    private File getQuickstartRunnerJar(String quickstartId) {
        File quickstartRunnerJar = baseDirectory.resolve(Paths.get(
                "use-cases", quickstartId,
                "quarkus-app", "quarkus-run.jar")).toFile();
        if (!quickstartRunnerJar.exists()) {
            throw new IllegalStateException(
                    "The quickstart (" + quickstartId + ") runner JAR file does not exist ("
                            + quickstartRunnerJar.getAbsolutePath() + ").");
        }
        return quickstartRunnerJar;
    }

    @jakarta.ws.rs.Path("{quickstartId}/stop/{port}")
    @DELETE
    public void stopQuickstart(@PathParam("quickstartId") String quickstartId, @PathParam("port") int port) {
        QuickstartMeta quickstartMeta = quickstartMetaList.stream()
                .filter(quickstartMeta_ -> quickstartMeta_.getPorts().contains((Object) port))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The process on port (" + port
                        + ") was already destroyed or never existed."));
        if (!quickstartMeta.getId().equals(quickstartId)) {
            throw new IllegalArgumentException("The quickstartId (" + quickstartId
                    + ") does not match the quickstart (" + quickstartMeta.getId() + ") on port (" + port + ").");
        }
        quickstartMeta.getPorts().remove((Object) port);
        Process process = portToProcessMap.remove(port);
        process.destroy();
    }

    private void openInBrowser(int port) {
        String url = "http://localhost:" + port;
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop == null || !desktop.isSupported(Desktop.Action.BROWSE)) {
            LOGGER.warn("There is no default browser to show the URL (" + url + ").");
            return;
        }
        try {
            desktop.browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Failed opening the default browser to show the URL (" + url + ").", e);
        }
    }

}
