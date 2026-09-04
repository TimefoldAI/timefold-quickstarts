///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21+
//DEPS info.picocli:picocli:4.7.7

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Temporarily injects the timefold-maven-plugin deploy configuration into a quickstart's
 * pom.xml, runs the deploy, then restores the original pom.xml (even if the build fails or
 * the script is interrupted). Keeps deploy credentials/config out of the committed pom.xml.
 *
 * Example:
 *   jbang .github/scripts/DeployToPlatform.java --module use-cases/maintenance-scheduling \
 *     --url https://sandbox.timefold.dev \
 *     --key maintenance-scheduling-template \
 *     --tenants ae226da1-5aea-4aba-93fc-8f911f37aa23
 */
@Command(name = "DeployToPlatform",
        mixinStandardHelpOptions = true,
        description = "Deploys a quickstart to the Timefold Platform by temporarily injecting the "
                + "timefold-maven-plugin deploy configuration into its pom.xml.")
public final class DeployToPlatform implements Callable<Integer> {

    private static final String NL = System.lineSeparator();

    // Matches a top-level <build> (2-space indent) only, not one nested in a <profile>.
    // \R matches whatever line break the pom.xml actually uses.
    private static final Pattern TOP_LEVEL_BUILD =
            Pattern.compile("\\R {2}<build>\\R.*?\\R {2}</build>\\R", Pattern.DOTALL);
    private static final Pattern PLUGINS_OPEN = Pattern.compile("\\R {4}<plugins>\\R");
    private static final Pattern PARENT_BLOCK = Pattern.compile("<parent>(.*?)</parent>", Pattern.DOTALL);
    private static final Pattern VERSION_TAG = Pattern.compile("<version>\\s*(.*?)\\s*</version>");
    private static final Pattern MAJOR_MINOR_PATCH = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");

    @Option(names = {"-m", "--module"}, defaultValue = ".",
            description = "Path to the module containing pom.xml (default: ${DEFAULT-VALUE}).")
    Path module;

    @Option(names = {"-u", "--url"}, required = true,
            description = "Timefold platform URL, e.g. https://sandbox.timefold.dev")
    String platformUrl;

    @Option(names = {"-k", "--key"}, required = true,
            description = "Model key, e.g. maintenance-scheduling-template")
    String key;

    @Option(names = {"-t", "--tenants"}, required = true, split = ",", paramLabel = "<tenantId>",
            description = "Comma-separated tenant id(s).")
    List<String> tenants;

    @Option(names = {"--overwrite"}, defaultValue = "true", arity = "0..1", fallbackValue = "true",
            paramLabel = "<bool>", description = "Value for <overwrite> (default: ${DEFAULT-VALUE}).")
    boolean overwrite;

    @Option(names = {"--handle-subscription"}, defaultValue = "true", arity = "0..1", fallbackValue = "true",
            paramLabel = "<bool>", description = "Value for <handleSubscription> (default: ${DEFAULT-VALUE}).")
    boolean handleSubscription;

    public static void main(String[] args) {
        System.exit(new CommandLine(new DeployToPlatform()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        List<String> tenantList = tenants.stream()
                .map(String::strip)
                .filter(tenant -> !tenant.isBlank())
                .toList();
        if (tenantList.isEmpty()) {
            System.err.println("No tenants given");
            return 1;
        }

        Path pom = module.resolve("pom.xml");
        if (!Files.isRegularFile(pom)) {
            System.err.println("No pom.xml found at " + pom);
            return 1;
        }

        try {
            validateParentVersion(pom);
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            return 1;
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

        injectDeployPlugin(pom, tenantList);

        System.out.println("Deploying '" + module + "' to '" + platformUrl + "' (key=" + key
                + ", tenants=" + String.join(",", tenantList) + ")...");
        Process process = new ProcessBuilder("mvn", "clean", "package", "-Denterprise=true", "timefold:deploy")
                .directory(module.toFile())
                .inheritIO()
                .start();
        return process.waitFor();
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

    private void injectDeployPlugin(Path pom, List<String> tenantList) throws IOException {
        String tenantXml = tenantList.stream()
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
                .formatted(platformUrl, key, tenantXml, overwrite, handleSubscription)
                .replace("\n", NL);

        String content = Files.readString(pom);
        Matcher buildMatcher = TOP_LEVEL_BUILD.matcher(content);
        String newContent;
        if (buildMatcher.find()) {
            String block = buildMatcher.group();
            Matcher pluginsMatcher = PLUGINS_OPEN.matcher(block);
            if (!pluginsMatcher.find()) {
                throw new IllegalStateException("Found a top-level <build> without <plugins>; cannot insert automatically");
            }
            String newBlock = block.substring(0, pluginsMatcher.end()) + pluginXml + NL
                    + block.substring(pluginsMatcher.end());
            newContent = content.substring(0, buildMatcher.start()) + newBlock + content.substring(buildMatcher.end());
        } else {
            String newBlock = """
                      <build>
                        <plugins>
                            %s
                        </plugins>
                      </build>
                    """
                    .replace("\n", NL)
                    .formatted(pluginXml);
            newContent = content.replaceFirst(Pattern.quote("</project>"), Matcher.quoteReplacement(newBlock) + "</project>");
        }
        Files.writeString(pom, newContent);
    }
}
