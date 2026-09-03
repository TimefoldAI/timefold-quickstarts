///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21+

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Temporarily injects the timefold-maven-plugin deploy configuration into a quickstart's
 * pom.xml, runs the deploy, then restores the original pom.xml (even if the build fails or
 * the script is interrupted). Keeps deploy credentials/config out of the committed pom.xml.
 *
 * Usage:
 *   jbang ./.github/scripts/DeployToPlatform.java --url <platformUrl> --key <key> --tenants <t1,t2,...> [options]
 *
 * Required:
 *   --url                          Timefold platform URL, e.g. https://sandbox.timefold.dev
 *   --key                          Model key, e.g. maintenance-scheduling-template
 *   --tenants                      Comma-separated tenant id(s)
 *
 * Optional:
 *   --module <path>                Path to the module containing pom.xml (default: current directory)
 *   --overwrite <bool>              Value for <overwrite> (default: true)
 *   --handle-subscription <bool>    Value for <handleSubscription> (default: true)
 *
 * Example:
 *   jbang ./github/scripts/DeployToPlatform.java --module use-cases/maintenance-scheduling \
 *     --url https://sandbox.timefold.dev \
 *     --key maintenance-scheduling-template \
 *     --tenants ae226da1-5aea-4aba-93fc-8f911f37aa23
 */
public final class DeployToPlatform {

    // Matches a top-level <build> (2-space indent) only, not one nested in a <profile>.
    private static final Pattern TOP_LEVEL_BUILD =
            Pattern.compile("\\n {2}<build>\\n.*?\\n {2}</build>\\n", Pattern.DOTALL);
    private static final Pattern PLUGINS_OPEN = Pattern.compile("\\n {4}<plugins>\\n");
    private static final Pattern PARENT_BLOCK = Pattern.compile("<parent>(.*?)</parent>", Pattern.DOTALL);
    private static final Pattern VERSION_TAG = Pattern.compile("<version>\\s*(.*?)\\s*</version>");
    private static final Pattern MAJOR_MINOR_PATCH = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");

    public static void main(String[] args) throws Exception {
        Path module = Path.of(".");
        String platformUrl = null;
        String key = null;
        String tenants = null;
        String overwrite = "true";
        String handleSubscription = "true";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--module" -> module = Path.of(args[++i]);
                case "--url" -> platformUrl = args[++i];
                case "--key" -> key = args[++i];
                case "--tenants" -> tenants = args[++i];
                case "--overwrite" -> overwrite = args[++i];
                case "--handle-subscription" -> handleSubscription = args[++i];
                case "-h", "--help" -> usage(0);
                default -> {
                    System.err.println("Unknown argument: " + args[i]);
                    usage(1);
                }
            }
        }

        if (platformUrl == null || key == null || tenants == null) {
            usage(1);
        }

        List<String> tenantList = new ArrayList<>();
        for (String tenant : tenants.split(",")) {
            if (!tenant.isBlank()) {
                tenantList.add(tenant.strip());
            }
        }
        if (tenantList.isEmpty()) {
            System.err.println("No tenants given");
            System.exit(1);
        }

        Path pom = module.resolve("pom.xml");
        if (!Files.isRegularFile(pom)) {
            System.err.println("No pom.xml found at " + pom);
            System.exit(1);
        }

        try {
            validateParentVersion(pom);
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        Path backup = Files.createTempFile("deploy-to-platform-pom", ".xml");
        Files.copy(pom, backup, StandardCopyOption.REPLACE_EXISTING);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Files.copy(backup, pom, StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(backup);
            } catch (IOException e) {
                System.err.println("Failed to restore original pom.xml from " + backup + ": " + e.getMessage());
            }
        }));

        injectDeployPlugin(pom, platformUrl, key, tenantList, overwrite, handleSubscription);

        System.out.println("Deploying '" + module + "' to '" + platformUrl + "' (key=" + key
                + ", tenants=" + tenants + ")...");
        Process process = new ProcessBuilder("mvn", "clean", "package", "-Denterprise=true", "timefold:deploy")
                .directory(module.toFile())
                .inheritIO()
                .start();
        System.exit(process.waitFor());
    }

    /**
     * The deploy plugin's <version> is ${project.parent.version}, which only resolves
     * to a real artifact once the parent has been released. A pom still pointing at a SNAPSHOT
     * (or otherwise non-major.minor.patch) parent version would fail to resolve, so fail fast
     * with a clear message instead of leaving the pom modified for a doomed mvn run.
     */
    private static void validateParentVersion(Path pom) throws IOException {
        String content = Files.readString(pom);
        Matcher parentMatcher = PARENT_BLOCK.matcher(content);
        if (!parentMatcher.find()) {
            throw new IllegalStateException("pom.xml at " + pom + " has no <parent> block; cannot verify its version");
        }
        Matcher versionMatcher = VERSION_TAG.matcher(parentMatcher.group(1));
        if (!versionMatcher.find()) {
            throw new IllegalStateException("The <parent> in " + pom + " has no <version>; cannot verify its version");
        }
        String parentVersion = versionMatcher.group(1);
        if (!MAJOR_MINOR_PATCH.matcher(parentVersion).matches()) {
            throw new IllegalStateException("The <parent> version in " + pom + " is '" + parentVersion
                    + "', not a released major.minor.patch version (e.g. 2.6.0). Point the parent at a released "
                    + "version before deploying.");
        }
    }

    private static void injectDeployPlugin(Path pom, String platformUrl, String key, List<String> tenants,
            String overwrite, String handleSubscription) throws IOException {
        String tenantXml = tenants.stream()
                .map(tenant -> "            <tenant>" + tenant + "</tenant>")
                .reduce((a, b) -> a + "\n" + b)
                .orElseThrow();

        String pluginXml = """
                      <plugin>
                        <groupId>ai.timefold.solver</groupId>
                        <artifactId>timefold-maven-plugin</artifactId>
                        <version>${project.parent.version}</version>
                        <configuration>
                          <platformUrl>%s</platformUrl>
                          <key>%s</key>
                          <tenants>
                %s
                          </tenants>
                          <overwrite>%s</overwrite>
                          <handleSubscription>%s</handleSubscription>
                        </configuration>
                        <executions>
                          <execution>
                            <id>configure</id>
                            <phase>initialize</phase>
                            <goals><goal>configure</goal></goals>
                          </execution>
                        </executions>
                      </plugin>"""
                .formatted(platformUrl, key, tenantXml, overwrite, handleSubscription);

        String content = Files.readString(pom);
        Matcher buildMatcher = TOP_LEVEL_BUILD.matcher(content);
        String newContent;
        if (buildMatcher.find()) {
            String block = buildMatcher.group();
            Matcher pluginsMatcher = PLUGINS_OPEN.matcher(block);
            if (!pluginsMatcher.find()) {
                throw new IllegalStateException("Found a top-level <build> without <plugins>; cannot insert automatically");
            }
            String newBlock = block.substring(0, pluginsMatcher.end()) + pluginXml + "\n"
                    + block.substring(pluginsMatcher.end());
            newContent = content.substring(0, buildMatcher.start()) + newBlock + content.substring(buildMatcher.end());
        } else {
            String newBlock = "  <build>\n    <plugins>\n" + pluginXml + "\n    </plugins>\n  </build>\n";
            newContent = content.replaceFirst(Pattern.quote("</project>"), Matcher.quoteReplacement(newBlock) + "</project>");
        }
        Files.writeString(pom, newContent);
    }

    private static void usage(int exitCode) {
        System.err.println("""
                Usage: jbang scripts/DeployToPlatform.java --url <platformUrl> --key <key> --tenants <t1,t2,...> [options]

                Required:
                  --url                          Timefold platform URL, e.g. https://sandbox.timefold.dev
                  --key                          Model key, e.g. maintenance-scheduling-template
                  --tenants                      Comma-separated tenant id(s)

                Optional:
                  --module <path>                Path to the module containing pom.xml (default: current directory)
                  --overwrite <bool>              Value for <overwrite> (default: true)
                  --handle-subscription <bool>    Value for <handleSubscription> (default: true)

                Example:
                  jbang scripts/DeployToPlatform.java --module use-cases/maintenance-scheduling \\
                    --url https://app.timefold.ai \\
                    --key maintenance-scheduling-template \\
                    --tenants ae226da1-5aea-4aba-93fc-8f23411f37aa23""");
        System.exit(exitCode);
    }
}
