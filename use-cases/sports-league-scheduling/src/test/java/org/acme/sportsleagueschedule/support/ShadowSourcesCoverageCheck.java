package org.acme.sportsleagueschedule.support;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class ShadowSourcesCoverageCheck {

    private static final Pattern SHADOW_SOURCES_PATTERN = Pattern.compile(
            "@\\s*(?:ai\\.timefold\\.solver\\.core\\.api\\.domain\\.variable\\.)?ShadowSources\\b");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^\\s*package\\s+([a-zA-Z0-9_.]+)\\s*;");
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    private ShadowSourcesCoverageCheck() {
    }

    public static void main(String[] args) {
        try {
            run(parseArguments(args));
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    static void run(Arguments arguments) {
        var shadowMethods = findShadowSourcesMethods(arguments.sourceRoot());
        if (shadowMethods.isEmpty()) {
            System.out.println("No methods annotated with @ShadowSources found; skipping dedicated coverage check.");
            return;
        }

        if (!Files.exists(arguments.jacocoReport())) {
            throw new IllegalStateException("JaCoCo report not found: " + arguments.jacocoReport());
        }

        var jacocoMethods = loadJacocoMethods(arguments.jacocoReport());
        var failures = new ArrayList<String>();
        for (var shadowMethod : shadowMethods) {
            var candidates = jacocoMethods.getOrDefault(new MethodKey(shadowMethod.className(), shadowMethod.methodName()),
                    List.of());
            var match = findBestMethodMatch(candidates, shadowMethod.line());
            if (match == null) {
                failures.add("%s#%s (source line %d): missing method entry in JaCoCo report".formatted(
                        shadowMethod.className(), shadowMethod.methodName(), shadowMethod.line()));
                continue;
            }
            if (match.lineMissed() != 0 || match.instructionMissed() != 0) {
                failures.add("%s#%s (source line %d): line missed=%d, instruction missed=%d".formatted(
                        shadowMethod.className(), shadowMethod.methodName(), shadowMethod.line(),
                        match.lineMissed(), match.instructionMissed()));
            }
        }

        if (!failures.isEmpty()) {
            var message = new StringBuilder(
                    "ShadowSources coverage check failed. The following methods are not 100% covered:\n");
            for (var failure : failures) {
                message.append(" - ").append(failure).append('\n');
            }
            throw new IllegalStateException(message.toString().stripTrailing());
        }

        System.out.println("ShadowSources coverage check passed: %d method(s) have 100%% line and instruction coverage."
                .formatted(shadowMethods.size()));
    }

    static Arguments parseArguments(String[] args) {
        Path sourceRoot = null;
        Path jacocoReport = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--source-root" -> sourceRoot = requirePathValue(args, ++i, "--source-root");
                case "--jacoco-report" -> jacocoReport = requirePathValue(args, ++i, "--jacoco-report");
                default -> throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }
        if (sourceRoot == null || jacocoReport == null) {
            throw new IllegalArgumentException("Expected --source-root <path> and --jacoco-report <path>.");
        }
        return new Arguments(sourceRoot, jacocoReport);
    }

    private static Path requirePathValue(String[] args, int index, String optionName) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Missing value for " + optionName);
        }
        return Path.of(args[index]);
    }

    static List<ShadowMethod> findShadowSourcesMethods(Path sourceRoot) {
        try (Stream<Path> pathStream = Files.walk(sourceRoot)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .flatMap(path -> findShadowSourcesMethodsInFile(path).stream())
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to scan source root " + sourceRoot, e);
        }
    }

    private static List<ShadowMethod> findShadowSourcesMethodsInFile(Path filePath) {
        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read source file " + filePath, e);
        }

        var packageName = "";
        for (var line : lines) {
            var matcher = PACKAGE_PATTERN.matcher(line);
            if (matcher.matches()) {
                packageName = matcher.group(1);
                break;
            }
        }

        var className = filePath.getFileName().toString().replaceFirst("\\.java$", "");
        var fullyQualifiedClassName = packageName.isEmpty() ? className : packageName + "." + className;

        var methods = new ArrayList<ShadowMethod>();
        var pendingShadow = false;
        var signatureLines = new ArrayList<String>();
        var signatureStartLine = 0;

        for (int index = 0; index < lines.size(); index++) {
            var line = lines.get(index);
            var lineNumber = index + 1;
            if (SHADOW_SOURCES_PATTERN.matcher(line).find()) {
                pendingShadow = true;
                signatureLines.clear();
                signatureStartLine = 0;
                continue;
            }
            if (!pendingShadow) {
                continue;
            }

            var stripped = line.trim();
            if (stripped.isEmpty()) {
                continue;
            }
            if (stripped.startsWith("@")) {
                continue;
            }
            if (signatureStartLine == 0) {
                signatureStartLine = lineNumber;
            }
            signatureLines.add(stripped);
            if (!stripped.contains("{") && !stripped.endsWith(";")) {
                continue;
            }

            var signature = String.join(" ", signatureLines);
            var methodName = extractMethodName(signature);
            pendingShadow = false;
            signatureLines.clear();
            signatureStartLine = 0;
            if (methodName == null || methodName.equals(className)) {
                continue;
            }
            methods.add(new ShadowMethod(fullyQualifiedClassName, methodName, signatureStartLine));
        }

        return methods;
    }

    static String extractMethodName(String signature) {
        var argumentStart = signature.indexOf('(');
        if (argumentStart < 0) {
            return null;
        }
        var beforeArguments = signature.substring(0, argumentStart);
        String lastToken = null;
        Matcher matcher = IDENTIFIER_PATTERN.matcher(beforeArguments);
        while (matcher.find()) {
            lastToken = matcher.group();
        }
        return lastToken;
    }

    static Map<MethodKey, List<CoverageEntry>> loadJacocoMethods(Path reportFile) {
        Document document;
        try {
            var documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            var documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
            document = documentBuilder.parse(reportFile.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read JaCoCo report " + reportFile, e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IllegalStateException("Failed to parse JaCoCo report " + reportFile, e);
        }

        var methodsByKey = new HashMap<MethodKey, List<CoverageEntry>>();
        var packageNodes = document.getDocumentElement().getElementsByTagName("package");
        for (int packageIndex = 0; packageIndex < packageNodes.getLength(); packageIndex++) {
            var packageElement = (Element) packageNodes.item(packageIndex);
            var packageName = packageElement.getAttribute("name").replace('/', '.');
            var childNodes = packageElement.getChildNodes();
            for (int childIndex = 0; childIndex < childNodes.getLength(); childIndex++) {
                var childNode = childNodes.item(childIndex);
                if (childNode.getNodeType() != Node.ELEMENT_NODE || !"class".equals(childNode.getNodeName())) {
                    continue;
                }
                var classElement = (Element) childNode;
                var classSimpleName = classElement.getAttribute("name").replace('/', '.');
                var className = packageName.isEmpty() || classSimpleName.startsWith(packageName + ".")
                        ? classSimpleName
                        : packageName + "." + classSimpleName;
                var methodNodes = classElement.getElementsByTagName("method");
                for (int methodIndex = 0; methodIndex < methodNodes.getLength(); methodIndex++) {
                    var methodElement = (Element) methodNodes.item(methodIndex);
                    var line = parseInt(methodElement.getAttribute("line"));
                    var lineCounter = findCounter(methodElement, "LINE");
                    var instructionCounter = findCounter(methodElement, "INSTRUCTION");
                    var entry = new CoverageEntry(line, parseInt(lineCounter.getAttribute("missed")),
                            parseInt(lineCounter.getAttribute("covered")),
                            parseInt(instructionCounter.getAttribute("missed")),
                            parseInt(instructionCounter.getAttribute("covered")));
                    methodsByKey.computeIfAbsent(new MethodKey(className, methodElement.getAttribute("name")),
                            ignored -> new ArrayList<>()).add(entry);
                }
            }
        }
        return methodsByKey;
    }

    private static Element findCounter(Element methodElement, String type) {
        NodeList childNodes = methodElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            var childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE || !"counter".equals(childNode.getNodeName())) {
                continue;
            }
            var counterElement = (Element) childNode;
            if (type.equals(counterElement.getAttribute("type"))) {
                return counterElement;
            }
        }
        return emptyCounter(methodElement.getOwnerDocument(), type);
    }

    private static Element emptyCounter(Document document, String type) {
        var counter = document.createElement("counter");
        counter.setAttribute("type", type);
        counter.setAttribute("missed", "0");
        counter.setAttribute("covered", "0");
        return counter;
    }

    private static int parseInt(String value) {
        return value == null || value.isEmpty() ? 0 : Integer.parseInt(value);
    }

    static CoverageEntry findBestMethodMatch(List<CoverageEntry> candidates, int expectedLine) {
        CoverageEntry bestCandidate = null;
        int bestDistance = Integer.MAX_VALUE;
        for (var candidate : candidates) {
            var distance = Math.abs(candidate.line() - expectedLine);
            if (distance < bestDistance) {
                bestCandidate = candidate;
                bestDistance = distance;
            }
        }
        return bestCandidate;
    }

    static final class Arguments {

        private final Path sourceRoot;
        private final Path jacocoReport;

        Arguments(Path sourceRoot, Path jacocoReport) {
            this.sourceRoot = sourceRoot;
            this.jacocoReport = jacocoReport;
        }

        Path sourceRoot() {
            return sourceRoot;
        }

        Path jacocoReport() {
            return jacocoReport;
        }
    }

    static final class ShadowMethod {

        private final String className;
        private final String methodName;
        private final int line;

        ShadowMethod(String className, String methodName, int line) {
            this.className = className;
            this.methodName = methodName;
            this.line = line;
        }

        String className() {
            return className;
        }

        String methodName() {
            return methodName;
        }

        int line() {
            return line;
        }
    }

    static final class MethodKey {

        private final String className;
        private final String methodName;

        MethodKey(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof MethodKey other)) {
                return false;
            }
            return className.equals(other.className) && methodName.equals(other.methodName);
        }

        @Override
        public int hashCode() {
            return 31 * className.hashCode() + methodName.hashCode();
        }
    }

    static final class CoverageEntry {

        private final int line;
        private final int lineMissed;
        private final int lineCovered;
        private final int instructionMissed;
        private final int instructionCovered;

        CoverageEntry(int line, int lineMissed, int lineCovered, int instructionMissed, int instructionCovered) {
            this.line = line;
            this.lineMissed = lineMissed;
            this.lineCovered = lineCovered;
            this.instructionMissed = instructionMissed;
            this.instructionCovered = instructionCovered;
        }

        int line() {
            return line;
        }

        int lineMissed() {
            return lineMissed;
        }

        int lineCovered() {
            return lineCovered;
        }

        int instructionMissed() {
            return instructionMissed;
        }

        int instructionCovered() {
            return instructionCovered;
        }
    }
}
