///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21
//DEPS com.microsoft.playwright:playwright:1.55.0
//DEPS info.picocli:picocli:4.7.7

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Command(name = "run-all-quarkus",
        mixinStandardHelpOptions = true,
        description = "For each subfolder: mvn quarkus:dev → open with Playwright → click Solve → screenshot → stop.")
class App implements Runnable {

    @Option(names = {"-d","--java-folder"}, required = true,
            description = "Folder containing your Quarkus app directories (each must be a Maven project).")
    Path javaFolder;

    @Option(names = {"-u","--url"}, defaultValue = "http://localhost:8080",
            description = "Base URL to open in the browser (default: ${DEFAULT-VALUE}).")
    String baseUrl;

    @Option(names = {"-s","--solve-selector"}, defaultValue = "button:has-text(\"Solve\")",
            description = "Playwright selector for the Solve button (default: ${DEFAULT-VALUE}).")
    String solveSelector;

    @Option(names = {"--startup-timeout-seconds"}, defaultValue = "120",
            description = "How long to wait for Quarkus dev to start (default: ${DEFAULT-VALUE}).")
    long startupTimeoutSeconds;

    @Option(names = {"--solve-timeout-seconds"}, defaultValue = "120",
            description = "How long to wait for the solve to complete (default: ${DEFAULT-VALUE}).")
    long solveTimeoutSeconds;

    @Option(names = {"--screenshot-dir"},
            description = "Directory to write PNG screenshots. If omitted, screenshots are saved next to pom.xml.")
    Path screenshotDir;

    @Option(names = {"--enterprise"}, defaultValue = "false",
            description = "Run Quarkus dev with the enterprise edition flag (-Denterprise) (default: ${DEFAULT-VALUE}).")
    Boolean enterprise;

    @Option(names = {"--headless"}, defaultValue = "true",
            description = "Run browser headless (default: ${DEFAULT-VALUE}).")
    Boolean headless;

    @Option(names = {"--record-video"}, defaultValue = "false",
            description = "Enable video recording for visual validation (default: ${DEFAULT-VALUE}).")
    Boolean recordVideo;

    @Option(names = {"--video-dir"},
            description = "Directory to save video recordings (default: same as screenshot-dir or project dir).")
    Path videoDir;

    private static final Pattern STARTED_PATTERN = Pattern.compile(
            "(?i)(listening on: http://|installed features|dev services|dev mode starting in|quarkus .* started in .*\\.)");

    public static void main(String[] args) {
        int exit = new CommandLine(new App()).execute(args);
        System.exit(exit);
    }

    @Override public void run() {
        requireDir(javaFolder);

        if(Objects.nonNull(screenshotDir)) {
            try {
                Files.createDirectories(screenshotDir);
            } catch (IOException e) {
                throw new RuntimeException("Cannot create screenshot dir: " + screenshotDir, e);
            }
        }

        if(recordVideo && Objects.nonNull(videoDir)) {
            try {
                Files.createDirectories(videoDir);
            } catch (IOException e) {
                throw new RuntimeException("Cannot create video dir: " + videoDir, e);
            }
        }

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(headless));

            var contextOptions = new Browser.NewContextOptions().setViewportSize(1500, 750);

            // Configure video recording if enabled
            if (recordVideo) {
                Path videoDirToUse = videoDir != null ? videoDir :
                                     screenshotDir != null ? screenshotDir : javaFolder;
                contextOptions.setRecordVideoDir(videoDirToUse)
                              .setRecordVideoSize(1500, 750);
                System.out.println("Video recording enabled. Videos will be saved to: " + videoDirToUse.toAbsolutePath());
            }

            BrowserContext ctx = browser.newContext(contextOptions);

            try (Stream<Path> children = Files.list(javaFolder)) {
                List<Path> dirs = children
                        .filter(Files::isDirectory)
                        .sorted()
                        .toList();

                for (Path dir : dirs) {
                    Path pom = dir.resolve("pom.xml");
                    if (!Files.exists(pom)) {
                        System.out.println("Skipping (no pom.xml): " + dir);
                        continue;
                    }

                    // Check if pom.xml mentions "quarkus" (dependency, plugin, etc.)
                    boolean isQuarkusProject = false;
                    try (Stream<String> lines = Files.lines(pom)) {
                        isQuarkusProject = lines.anyMatch(l -> l.toLowerCase().contains("quarkus"));
                    } catch (IOException e) {
                        System.err.println("Could not read pom.xml for " + dir + ": " + e.getMessage());
                    }

                    if (!isQuarkusProject) {
                        System.out.println("Skipping (no Quarkus reference found): " + dir);
                        continue;
                    }
                    System.out.println("\n=== " + dir.getFileName() + " ===");

                    ProcessWrapper quarkus = null;
                    Page page = null;
                    try {
                        quarkus = startQuarkusDev(dir);
                        waitForQuarkusStarted(quarkus);

                        page = ctx.newPage();
                        System.out.println("Opening: " + baseUrl);
                        page.navigate(baseUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

                        // Click the Solve button
                        System.out.println("Clicking Solve (" + solveSelector + ")");
                        page.locator(solveSelector).first().click(new Locator.ClickOptions().setTimeout(ms(15)));

                        waitForSolve(page);

                        // Screenshot
                        String fileSafe = dir.getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
                        Path shot = null;
                        if(screenshotDir != null) {
                            shot = screenshotDir.resolve(fileSafe + ".png");
                        } else {
                            // saving in original dir next to the pom.xml file.
                            shot = dir.resolve(fileSafe + "-screenshot.png");
                        }
                        page.screenshot(new Page.ScreenshotOptions()
                                .setFullPage(false)
                                .setPath(shot));
                        System.out.println("Saved screenshot: " + shot.toAbsolutePath());

                    } catch (Exception e) {
                        System.err.println("Error while processing " + dir.getFileName() + ": " + e.getMessage());
                        e.printStackTrace(System.err);
                    } finally {
                        if (page != null && !page.isClosed()) {
                            if (recordVideo) {
                                // Close page to finalize video recording
                                page.close();
                                // Get the video path and rename it to match project name
                                Path video = page.video().path();
                                if (video != null && Files.exists(video)) {
                                    String fileSafe = dir.getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
                                    Path targetVideo = video.getParent().resolve(fileSafe + ".webm");
                                    try {
                                        Files.move(video, targetVideo, StandardCopyOption.REPLACE_EXISTING);
                                        System.out.println("Saved video: " + targetVideo.toAbsolutePath());
                                    } catch (IOException e) {
                                        System.err.println("Failed to rename video: " + e.getMessage());
                                    }
                                }
                            } else {
                                page.close();
                            }
                        }
                        if (quarkus != null) stopQuarkus(quarkus);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void waitForSolve(Page page) {
        // Strategy 2: a 'Solve' text appears on the solve button again after solving
        try {
            Thread.sleep(5000); //wait 5 seconds so solver is definitely started.
            page.waitForSelector("text=/\\bSolve\\b/i",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(ms(solveTimeoutSeconds)));
        } catch (Exception e) {
            // If we still didn't get it, just continue; we’ll still take a screenshot of the current state.
            System.out.println("Solve might not have signaled completion explicitly; proceeding to screenshot.");
        }
    }

    private ProcessWrapper startQuarkusDev(Path dir) throws IOException {
        List<String> command = new java.util.ArrayList<>(List.of("mvn", "-q", "quarkus:dev"));
        if (enterprise) command.add("-Denterprise");
        ProcessBuilder pb = new ProcessBuilder()
                .directory(dir.toFile())
                .command(command);
        pb.environment().putIfAbsent("JAVA_TOOL_OPTIONS", "-Dfile.encoding=UTF-8");
        pb.redirectErrorStream(true);
        Process p = pb.start();
        return new ProcessWrapper(p, dir.getFileName().toString());
    }

    private void waitForQuarkusStarted(ProcessWrapper pw) throws Exception {
        System.out.println("Starting mvn quarkus:dev ...");
        var latch = new CountDownLatch(1);

        // Drain output asynchronously and look for startup tokens
        Thread t = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(pw.process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println("[" + pw.name + "] " + line);
                    if (STARTED_PATTERN.matcher(line).find() || line.toLowerCase().contains("listening on: http")) {
                        latch.countDown();
                    }
                }
            } catch (IOException ignored) {}
        }, "quarkus-out-" + pw.name);
        t.setDaemon(true);
        t.start();

        boolean ok = latch.await(startupTimeoutSeconds, TimeUnit.SECONDS);
        if (!ok) {
            throw new TimeoutException("Quarkus did not start within " + startupTimeoutSeconds + "s.");
        }
        System.out.println("Quarkus reported it is listening.");
    }

    private void stopQuarkus(ProcessWrapper pw) {
        try {
            // Quarkus dev accepts 'q' to quit
            OutputStream os = pw.process.getOutputStream();
            os.write('q');
            os.write('\n');
            os.flush();
        } catch (IOException ignored) {}
        try {
            if (!pw.process.waitFor(10, TimeUnit.SECONDS)) {
                pw.process.destroy();
                if (!pw.process.waitFor(5, TimeUnit.SECONDS)) {
                    pw.process.destroyForcibly();
                }
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return;
        }
        System.out.println("Stopped: " + pw.name);
    }

    private static long ms(long seconds) {
        return Duration.ofSeconds(seconds).toMillis();
    }

    private static void requireDir(Path dir) {
        Objects.requireNonNull(dir, "dir");
        if (!Files.isDirectory(dir)) {
            throw new CommandLine.ParameterException(new CommandLine(new App()),
                    "Not a directory: " + dir.toAbsolutePath());
        }
    }

    private record ProcessWrapper(Process process, String name) {}
}
