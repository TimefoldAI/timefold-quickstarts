///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21+

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Verifies that a model's copy of visualizations/shared/ is still byte-identical to the
 * source files it was copied from, and that its generated index.html still matches the
 * structure of visualizations/shared/index.template.html.
 *
 * visualizations/sync.sh physically duplicates visualizations/shared/*.js and *.css into
 * each consuming quickstart's META-INF/resources/shared/ folder, and renders index.html
 * from index.template.html by substituting its {{PLACEHOLDER}}s (see sync.sh for why there's
 * no build-time include mechanism). Nothing stops someone from hand-editing a quickstart's
 * copy, or its generated index.html, directly instead of the shared source and re-running
 * sync.sh, which would silently fork that quickstart's UI behavior from the rest. This runs
 * three independent checks against each model: checkSharedFiles(), checkIndexHtml() and
 * checkVisualizeJs().
 *
 * Usage: jbang CheckSharedUiSync.java <model-dir> [<model-dir> ...]
 * Run from the repository root. A model dir without a src/main/resources/META-INF/resources/shared
 * folder is skipped, since not every model consumes the shared UI template.
 */
public final class CheckSharedUiSync {

    private static final Path RELATIVE_SHARED_PATH = Path.of("src/main/resources/META-INF/resources/shared");

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Usage: jbang CheckSharedUiSync.java <model-dir> [<model-dir> ...]");
            System.exit(2);
        }

        var repoRoot = Path.of("").toAbsolutePath();
        var sharedSourceDir = repoRoot.resolve("visualizations/shared");
        if (!Files.isDirectory(sharedSourceDir)) {
            System.err.println("ERROR: " + sharedSourceDir + " not found; run this from the repository root.");
            System.exit(2);
        }

        var violations = new ArrayList<String>();
        var checkedCount = 0;
        for (String arg : args) {
            var targetDir = Path.of(arg).toAbsolutePath().normalize().resolve(RELATIVE_SHARED_PATH);
            if (!Files.isDirectory(targetDir)) {
                continue; // this model doesn't consume the shared UI template
            }
            checkedCount++;
            checkSharedFiles(repoRoot, sharedSourceDir, targetDir, violations);
            checkIndexHtml(repoRoot, sharedSourceDir, targetDir.getParent(), violations);
            checkVisualizeJs(repoRoot, targetDir.getParent(), violations);
        }

        if (violations.isEmpty()) {
            System.out.printf("OK: %d of %d model(s) consume the shared UI template and are identical to %s.%n",
                    checkedCount, args.length, repoRoot.relativize(sharedSourceDir));
            return;
        }
        System.err.printf("%n%d shared UI sync violation(s) found:%n%n", violations.size());
        violations.forEach(violation -> System.err.println("  " + violation));
        System.err.println("\nShared UI files must not be hand-edited in a quickstart; edit "
                + "visualizations/shared/ instead, then run visualizations/sync.sh and commit the result.");
        System.exit(1);
    }

    /*
     * Check 1: every *.js/*.css file sync.sh copies out of visualizations/shared/ into a
     * model's shared/ folder must still be byte-identical to its source, with no leftover
     * files from a stale copy either.
     */
    private static void checkSharedFiles(Path repoRoot, Path sharedSourceDir, Path targetDir, List<String> violations)
            throws IOException {
        var relativeTargetDir = repoRoot.relativize(targetDir);
        var relativeSharedDir = repoRoot.relativize(sharedSourceDir);
        var sourceFiles = listSyncedFiles(sharedSourceDir);
        var sourceFileNames = sourceFiles.stream().map(path -> path.getFileName().toString()).toList();

        for (Path sourceFile : sourceFiles) {
            var fileName = sourceFile.getFileName().toString();
            var targetFile = targetDir.resolve(fileName);
            if (!Files.exists(targetFile)) {
                violations.add("%s is missing %s (present in %s)".formatted(relativeTargetDir, fileName,
                        relativeSharedDir));
                continue;
            }
            if (!Arrays.equals(Files.readAllBytes(sourceFile), Files.readAllBytes(targetFile))) {
                violations.add("%s differs from %s".formatted(relativeTargetDir.resolve(fileName),
                        relativeSharedDir.resolve(fileName)));
            }
        }

        List<Path> targetFiles;
        try (var stream = Files.list(targetDir)) {
            targetFiles = stream.sorted().toList();
        }
        for (Path targetFile : targetFiles) {
            var fileName = targetFile.getFileName().toString();
            if (!sourceFileNames.contains(fileName)) {
                violations.add("%s contains %s, which has no counterpart in %s".formatted(relativeTargetDir,
                        fileName, relativeSharedDir));
            }
        }
    }

    // sync.sh only ever copies *.js and *.css out of visualizations/shared/ verbatim;
    // index.template.html is rendered into index.html, not copied, so it's excluded here.
    private static List<Path> listSyncedFiles(Path sharedSourceDir) throws IOException {
        try (var stream = Files.list(sharedSourceDir)) {
            return stream
                    .filter(path -> {
                        var name = path.getFileName().toString();
                        return name.endsWith(".js") || name.endsWith(".css");
                    })
                    .sorted()
                    .toList();
        }
    }

    /*
     * Check 2: a model's generated index.html must match the structure of
     * visualizations/shared/index.template.html. Mirrors sync.sh's render_index_html by
     * turning the template into a regex: strip the leading "this is a template" comment, then
     * replace every {{PLACEHOLDER}} with a non-greedy wildcard and everything else with
     * literal text, so only the placeholder slots may legitimately differ.
     */
    private static void checkIndexHtml(Path repoRoot, Path sharedSourceDir, Path resourcesDir,
            List<String> violations) throws IOException {
        var templateFile = sharedSourceDir.resolve("index.template.html");
        var relativeTemplateFile = repoRoot.relativize(templateFile);
        var indexFile = resourcesDir.resolve("index.html");
        var relativeIndexFile = repoRoot.relativize(indexFile);

        if (!Files.exists(indexFile)) {
            violations.add("%s is missing (expected to be rendered from %s)".formatted(relativeIndexFile,
                    relativeTemplateFile));
            return;
        }
        var templatePattern = buildTemplatePattern(Files.readString(templateFile));
        if (!templatePattern.matcher(Files.readString(indexFile)).matches()) {
            violations.add("%s does not match the structure of %s (only {{PLACEHOLDER}} slots may differ)"
                    .formatted(relativeIndexFile, relativeTemplateFile));
        }
    }

    /*
     * Check 3: a model's resources folder must contain a visualize.js that calls
     * setVisualizationSlot() to fill index.template.html's #visualization slot with its own
     * markup (see quickstart-page.js), and constructs a QuickstartPage - the shared controller
     * that wires up the header/solve controls and drives rendering into that markup. A
     * quickstart with a shared/ folder but no such visualize.js would leave the slot empty
     * and the whole page dead.
     */
    private static void checkVisualizeJs(Path repoRoot, Path resourcesDir, List<String> violations)
            throws IOException {
        var visualizeJsFile = resourcesDir.resolve("visualize.js");
        var relativeVisualizeJsFile = repoRoot.relativize(visualizeJsFile);
        if (!Files.exists(visualizeJsFile)) {
            violations.add("%s is missing".formatted(relativeVisualizeJsFile));
            return;
        }
        var visualizeJsContent = Files.readString(visualizeJsFile);
        if (!Pattern.compile("\\bsetVisualizationSlot\\s*\\(").matcher(visualizeJsContent).find()) {
            violations.add("%s never calls setVisualizationSlot()".formatted(relativeVisualizeJsFile));
        }
        if (!Pattern.compile("\\bnew\\s+QuickstartPage\\s*\\(").matcher(visualizeJsContent).find()) {
            violations.add("%s never constructs a QuickstartPage".formatted(relativeVisualizeJsFile));
        }
    }

    private static Pattern buildTemplatePattern(String templateContent) {
        var placeholderPattern = Pattern.compile("\\{\\{[A-Z_]+}}");
        var commentEnd = templateContent.indexOf("-->");
        var afterComment = commentEnd >= 0 ? templateContent.substring(commentEnd + 3) : templateContent;
        if (afterComment.startsWith("\n")) {
            afterComment = afterComment.substring(1);
        }
        var matcher = placeholderPattern.matcher(afterComment);
        var regex = new StringBuilder();
        var lastEnd = 0;
        while (matcher.find()) {
            regex.append(Pattern.quote(afterComment.substring(lastEnd, matcher.start())));
            regex.append(".*?");
            lastEnd = matcher.end();
        }
        regex.append(Pattern.quote(afterComment.substring(lastEnd)));
        return Pattern.compile(regex.toString(), Pattern.DOTALL);
    }

    private CheckSharedUiSync() {
    }
}
