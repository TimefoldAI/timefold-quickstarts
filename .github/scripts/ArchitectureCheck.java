///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.tngtech.archunit:archunit:1.4.2
//DEPS org.slf4j:slf4j-nop:2.0.17
//JAVA 21+

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * Enforces the layered project structure of Models Service SDK quickstarts.
 * This is the CI twin of the former in-module ArchUnit ProjectStructureTest:
 * the rules are identical, but they run against target/classes and
 * target/test-classes from a GitHub workflow (see architecture_check.yml)
 * instead of adding ArchUnit to every model's test classpath.
 *
 * Usage: jbang ArchitectureCheck.java <model-dir> [<model-dir> ...]
 * Each model must be compiled first (mvn test-compile).
 */
public final class ArchitectureCheck {

    private static final String MODEL_INPUT_METRICS = "ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics";
    private static final String MODEL_OUTPUT_METRICS = "ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics";
    private static final String ABSTRACT_ISSUE = "ai.timefold.solver.service.definition.api.validation.AbstractIssue";
    private static final String SCHEMA_ANNOTATION = "org.eclipse.microprofile.openapi.annotations.media.Schema";

    // Set per model before the rules run; the source-scanning conditions need them.
    private static Path moduleRoot;
    private static String basePackage;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: jbang ArchitectureCheck.java <model-dir> [<model-dir> ...]");
            System.exit(2);
        }
        var anyViolation = false;
        for (String arg : args) {
            anyViolation |= !checkModel(Path.of(arg).toAbsolutePath().normalize());
        }
        System.exit(anyViolation ? 1 : 0);
    }

    private static boolean checkModel(Path root) {
        moduleRoot = root;
        var classesDir = root.resolve("target/classes");
        var testClassesDir = root.resolve("target/test-classes");
        if (!Files.isDirectory(classesDir)) {
            System.err.printf("ERROR: %s has no target/classes — run 'mvn test-compile' first.%n", root);
            return false;
        }
        var importPaths = new ArrayList<Path>();
        importPaths.add(classesDir);
        if (Files.isDirectory(testClassesDir)) {
            importPaths.add(testClassesDir);
        }
        JavaClasses importedClasses = new ClassFileImporter().importPaths(importPaths);
        basePackage = commonPackagePrefix(importedClasses);
        System.out.printf("Checking %s (base package %s, %d classes)...%n",
                root.getFileName(), basePackage, importedClasses.size());

        var failures = new ArrayList<String>();
        for (ArchRule rule : rules()) {
            var result = rule.evaluate(importedClasses);
            if (result.hasViolation()) {
                failures.add("Rule violated: " + rule.getDescription() + "\n  "
                        + String.join("\n  ", result.getFailureReport().getDetails()));
            }
        }
        if (failures.isEmpty()) {
            System.out.printf("OK: %s passes all %d architecture rules.%n", root.getFileName(), rules().size());
            return true;
        }
        System.err.printf("%n%s: %d architecture rule(s) violated:%n%n", root.getFileName(), failures.size());
        failures.forEach(failure -> System.err.println(failure + "\n"));
        return false;
    }

    private static String commonPackagePrefix(JavaClasses importedClasses) {
        List<String> packages = importedClasses.stream()
                .filter(JavaClass::isTopLevelClass)
                .map(JavaClass::getPackageName)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
        if (packages.isEmpty()) {
            throw new IllegalStateException("No classes found to derive the base package from.");
        }
        var prefix = packages.getFirst().split("\\.");
        var length = prefix.length;
        for (String pkg : packages) {
            var segments = pkg.split("\\.");
            var i = 0;
            while (i < Math.min(length, segments.length) && segments[i].equals(prefix[i])) {
                i++;
            }
            length = i;
        }
        return String.join(".", List.of(prefix).subList(0, length));
    }

    private static List<ArchRule> rules() {
        return List.of(
                layerRule("domain", "dto", not(resideInAPackage(basePackage + ".dto.."))
                        .or(assignableTo(MODEL_INPUT_METRICS)).or(assignableTo(MODEL_OUTPUT_METRICS))),
                layerRule("dto", "domain", not(resideInAPackage(basePackage + ".domain.."))
                        .or(domainEnums())),
                layerRule("dto", "solver", not(resideInAPackage(basePackage + ".solver.."))),
                layerRule("dto", "service", not(resideInAPackage(basePackage + ".service.."))),
                layerRule("domain", "service", not(resideInAPackage(basePackage + ".service.."))),
                layerRule("domain", "rest", not(resideInAPackage(basePackage + ".rest.."))),
                layerRule("domain", "demo", not(resideInAPackage(basePackage + ".demo.."))),
                layerRule("solver", "rest", not(resideInAPackage(basePackage + ".rest.."))),
                solverMustNotDependOnDemo(),
                layerRule("demo", "rest", not(resideInAPackage(basePackage + ".rest.."))),
                repositoryMustNotContainPythonOrAwkScripts(),
                domainSettersMustReturnVoid(),
                justificationRecordsMustNotDefineZeroArgumentConstructors(),
                justificationRecordsMustNotReplaceNullStringsWithEmptyStrings(),
                dtoRecordsMustNotCreateMutableArrayLists(),
                testUtilityClassesMustResideInSupportPackage(),
                everyModuleMustHaveATestHelperInSupportPackage(),
                testClassesMustBuildDomainObjectsViaTestHelper(),
                everyModuleMustHaveASingleJustificationFile(),
                dtoTypesMustEndWithDto(),
                withMethodsMustBeUsed(),
                onlyDtoPackageMayUseSchemaAnnotation(),
                classesMustResideInValidSubpackages(),
                onlyInterfacesAndRecordsInDtoPackage(),
                nestedDtoClassesMustBeBuilders());
    }

    private static ArchRule layerRule(String from, String to,
                                      com.tngtech.archunit.base.DescribedPredicate<? super JavaClass> allowed) {
        return classes()
                .that(resideInAPackage(basePackage + "." + from + ".."))
                .should()
                .onlyDependOnClassesThat(allowed)
                .as("%s layer must not depend on %s layer".formatted(capitalize(from), capitalize(to)));
    }

    private static DescribedPredicate<JavaClass> domainEnums() {
        return resideInAPackage(basePackage + ".domain..").and(new DescribedPredicate<JavaClass>("are enums") {
            @Override
            public boolean test(JavaClass javaClass) {
                return javaClass.isEnum();
            }
        });
    }

    private static ArchRule solverMustNotDependOnDemo() {
        return classes()
                .that(resideInAPackage(basePackage + ".solver.."))
                .and().haveSimpleNameNotEndingWith("Test")
                .should()
                .onlyDependOnClassesThat(not(resideInAPackage(basePackage + ".demo..")))
                .as("Solver layer must not depend on Demo layer (tests excluded)");
    }

    private static ArchRule repositoryMustNotContainPythonOrAwkScripts() {
        return classes()
                .that().areTopLevelClasses()
                .should(notHaveFilesMatching("glob:**/*.py", "glob:**/*.awk"))
                .as("Repository must not contain Python or awk scripts");
    }

    private static ArchRule domainSettersMustReturnVoid() {
        return methods()
                .that().arePublic()
                .and().haveNameMatching("set[A-Z].*")
                .and().areDeclaredInClassesThat().resideInAPackage(basePackage + ".domain..")
                .should().haveRawReturnType(void.class)
                .as("Domain setters must follow the JavaBean contract and return void");
    }

    private static ArchRule justificationRecordsMustNotDefineZeroArgumentConstructors() {
        return classes()
                .that().areRecords()
                .and().resideInAPackage(basePackage + ".domain.justification..")
                .should(notDeclareZeroArgumentConstructors())
                .allowEmptyShould(true)
                .as("Constraint justification records must not declare zero-argument constructors");
    }

    private static ArchRule justificationRecordsMustNotReplaceNullStringsWithEmptyStrings() {
        return classes()
                .that().areRecords()
                .and().resideInAPackage(basePackage + ".domain.justification..")
                .should(new NoNullStringToEmptyStringCondition())
                .allowEmptyShould(true)
                .as("Constraint justification records must preserve null strings instead of converting them to empty strings");
    }

    private static ArchRule dtoRecordsMustNotCreateMutableArrayLists() {
        return classes()
                .that().areRecords()
                .and().resideInAPackage(basePackage + ".dto..")
                .should(new NoMutableArrayListsCondition())
                .as("DTO records must not create mutable ArrayList instances");
    }

    private static ArchRule testUtilityClassesMustResideInSupportPackage() {
        return classes()
                .should(new TestUtilitiesMustResideInSupportPackageCondition())
                .as("Test utility classes must reside in a dedicated support package");
    }

    private static ArchRule everyModuleMustHaveATestHelperInSupportPackage() {
        return classes()
                .should(new TestHelperExistsCondition())
                .as("Every module must provide a TestHelper class in a support package to build valid domain objects for tests");
    }

    private static ArchRule testClassesMustBuildDomainObjectsViaTestHelper() {
        return classes()
                .should(new DomainConstructionOutsideSupportCondition())
                .as("Test classes must build domain objects via the support package's TestHelper instead of calling domain constructors directly");
    }

    private static ArchRule everyModuleMustHaveASingleJustificationFile() {
        return classes()
                .should(new SingleJustificationFileCondition())
                .as("The domain.justification package must contain exactly one file, hosting every justification implementation as a nested type");
    }

    private static ArchRule dtoTypesMustEndWithDto() {
        return classes()
                .that().resideInAPackage(basePackage + ".dto..")
                .and().areTopLevelClasses()
                .should(new DtoTypeNamingConventionCondition())
                .as("Types in the DTO package must follow DTO naming conventions");
    }

    private static ArchRule withMethodsMustBeUsed() {
        return methods()
                .that().haveNameMatching("with[A-Z].*")
                .and().areDeclaredInClassesThat().resideInAPackage(basePackage + "..")
                .should(new UsedWithMethodCondition())
                .as("withXxx methods must be used at least once");
    }

    private static ArchRule onlyDtoPackageMayUseSchemaAnnotation() {
        return classes()
                .that(not(resideInAPackage(basePackage + ".dto..")))
                .and(not(resideInAPackage(basePackage + ".domain.justification..")))
                // Validation issues are part of the wire format, so they need OpenAPI descriptions too.
                .and(not(assignableTo(ABSTRACT_ISSUE)))
                .should(new NotAnnotatedWithCondition(SCHEMA_ANNOTATION))
                .as("Only DTO, domain.justification and validation issue classes may use @Schema annotation");
    }

    private static ArchRule classesMustResideInValidSubpackages() {
        return classes()
                .that().haveNameNotMatching(".*Test$")
                .and().haveNameNotMatching(".*Test\\$.*")
                .should()
                .resideInAnyPackage("..domain..", "..dto..", "..solver..", "..rest..", "..service..", "..demo..",
                        "..enricher..", "..support..", "..integrationtest..")
                .as("Classes must reside in a valid layer package");
    }

    private static ArchRule onlyInterfacesAndRecordsInDtoPackage() {
        return classes()
                .that().resideInAPackage("..dto..")
                .and().areTopLevelClasses()
                .should().beInterfaces()
                .orShould().beRecords()
                .orShould().beEnums()
                .as("The DTO package may only contain interfaces, records and enums");
    }

    private static ArchRule nestedDtoClassesMustBeBuilders() {
        return classes()
                .that().resideInAPackage("..dto..")
                .and().areNotTopLevelClasses()
                .should().haveSimpleName("Builder")
                .allowEmptyShould(true)
                .as("The only nested classes allowed in the DTO package are builders");
    }

    private static ArchCondition<JavaClass> notHaveFilesMatching(String... syntaxAndPatterns) {
        return new ArchCondition<>("not have forbidden script files in the repository") {
            // Walking the module tree once is enough; anchor the scan to a single class.
            private boolean scanned;

            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                if (scanned) {
                    return;
                }
                scanned = true;
                var pathMatchers = List.of(syntaxAndPatterns).stream()
                        .map(pattern -> FileSystems.getDefault().getPathMatcher(pattern))
                        .toList();
                try (var pathStream = Files.walk(moduleRoot)) {
                    pathStream
                            .filter(Files::isRegularFile)
                            .filter(path -> !path.startsWith(moduleRoot.resolve("target")))
                            .filter(path -> !path.startsWith(moduleRoot.resolve(".git")))
                            .map(moduleRoot::relativize)
                            .filter(path -> pathMatchers.stream().anyMatch(pathMatcher -> pathMatcher.matches(path)))
                            .sorted()
                            .forEach(path -> events.add(SimpleConditionEvent.violated(javaClass,
                                    "Forbidden script file found: " + path)));
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to scan module root " + moduleRoot, e);
                }
            }
        };
    }

    private static ArchCondition<JavaClass> notDeclareZeroArgumentConstructors() {
        return new ArchCondition<>("not declare zero-argument constructors") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                var hasZeroArgumentConstructor = javaClass.getConstructors().stream()
                        .anyMatch(constructor -> constructor.getParameters().isEmpty());
                if (hasZeroArgumentConstructor) {
                    events.add(SimpleConditionEvent.violated(javaClass,
                            "%s declares a zero-argument constructor".formatted(javaClass.getName())));
                }
            }
        };
    }

    private static final class NotAnnotatedWithCondition extends ArchCondition<JavaClass> {

        private final String annotationTypeName;

        private NotAnnotatedWithCondition(String annotationTypeName) {
            super("not be annotated with @" + annotationTypeName);
            this.annotationTypeName = annotationTypeName;
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            if (javaClass.isAnnotatedWith(annotationTypeName)) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s is annotated with @%s".formatted(javaClass.getName(), annotationTypeName)));
            }
        }
    }

    private static final class DtoTypeNamingConventionCondition extends ArchCondition<JavaClass> {

        private DtoTypeNamingConventionCondition() {
            super("follow DTO naming conventions");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            if (javaClass.getSimpleName().matches(".*(?:DTO|Input|Output|Metrics|ConfigOverrides|ValidationIssue|Detail)$")) {
                return;
            }
            events.add(SimpleConditionEvent.violated(javaClass,
                    "%s does not use an allowed DTO package suffix".formatted(javaClass.getName())));
        }
    }

    private static final class NoNullStringToEmptyStringCondition extends ArchCondition<JavaClass> {

        private static final Pattern NULL_TO_EMPTY_STRING_PATTERN =
                Pattern.compile("==\\s*null\\s*\\?\\s*\"\"\\s*:");

        private NoNullStringToEmptyStringCondition() {
            super("not replace null strings with empty strings");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            var sourceFile = resolveSourceFile(javaClass);
            if (sourceFile.isEmpty()) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "Cannot verify null-string handling for %s: source file not found".formatted(javaClass.getName())));
                return;
            }
            var source = String.join("\n", readLines(sourceFile.get()));
            if (NULL_TO_EMPTY_STRING_PATTERN.matcher(source).find()) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s converts null strings to empty strings in %s".formatted(javaClass.getName(), sourceFile.get())));
            }
        }
    }

    private static final class NoMutableArrayListsCondition extends ArchCondition<JavaClass> {

        private static final Pattern MUTABLE_ARRAY_LIST_PATTERN =
                Pattern.compile("\\bnew\\s+ArrayList\\s*(?:<[^>]*>)?\\s*\\(");

        private NoMutableArrayListsCondition() {
            super("not create mutable ArrayList instances");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            var sourceFile = resolveSourceFile(javaClass);
            if (sourceFile.isEmpty()) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "Cannot verify collection mutability for %s: source file not found".formatted(javaClass.getName())));
                return;
            }
            var source = String.join("\n", readLines(sourceFile.get()));
            if (MUTABLE_ARRAY_LIST_PATTERN.matcher(source).find()) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s creates mutable ArrayList instances in %s".formatted(javaClass.getName(), sourceFile.get())));
            }
        }
    }

    private static final class TestUtilitiesMustResideInSupportPackageCondition extends ArchCondition<JavaClass> {

        private TestUtilitiesMustResideInSupportPackageCondition() {
            super("reside in ..support.. when declared in src/test/java");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            var sourceFile = resolveSourceFile(javaClass);
            if (sourceFile.isEmpty() || !sourceFile.get().startsWith(moduleRoot.resolve("src/test/java"))) {
                return;
            }
            if (!javaClass.getSimpleName().matches(".*(?:Util|Summary|Check)$")) {
                return;
            }
            if (!javaClass.getPackageName().contains(".support")) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s is a test utility in %s and should be moved to a support package"
                                .formatted(javaClass.getName(), sourceFile.get())));
            }
        }
    }

    private static final class TestHelperExistsCondition extends ArchCondition<JavaClass> {

        // Walking the module tree once is enough; anchor the scan to a single class.
        private boolean scanned;

        private TestHelperExistsCondition() {
            super("provide a TestHelper class in a support package");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            if (scanned) {
                return;
            }
            scanned = true;
            var testSourceRoot = moduleRoot.resolve("src/test/java");
            var hasTestHelper = false;
            if (Files.isDirectory(testSourceRoot)) {
                var pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/support/TestHelper.java");
                try (var pathStream = Files.walk(testSourceRoot)) {
                    hasTestHelper = pathStream.anyMatch(pathMatcher::matches);
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to scan " + testSourceRoot, e);
                }
            }
            if (!hasTestHelper) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "No TestHelper class found under %s/**/support/TestHelper.java".formatted(testSourceRoot)));
            }
        }
    }

    private static final class SingleJustificationFileCondition extends ArchCondition<JavaClass> {

        // Walking the module tree once is enough; anchor the scan to a single class.
        private boolean scanned;

        private SingleJustificationFileCondition() {
            super("contain exactly one file in the domain.justification package");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            if (scanned) {
                return;
            }
            scanned = true;
            var justificationDir = moduleRoot.resolve("src/main/java")
                    .resolve(basePackage.replace('.', '/'))
                    .resolve("domain/justification");
            if (!Files.isDirectory(justificationDir)) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "No domain.justification package found under " + justificationDir));
                return;
            }
            List<Path> justificationFiles;
            try (var pathStream = Files.list(justificationDir)) {
                justificationFiles = pathStream.filter(path -> path.toString().endsWith(".java")).sorted().toList();
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to scan " + justificationDir, e);
            }
            if (justificationFiles.size() != 1) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "Expected exactly one file in %s, found %d: %s".formatted(justificationDir,
                                justificationFiles.size(),
                                justificationFiles.stream().map(path -> path.getFileName().toString())
                                        .collect(Collectors.joining(", ")))));
            }
        }
    }

    private static final class DomainConstructionOutsideSupportCondition extends ArchCondition<JavaClass> {

        private DomainConstructionOutsideSupportCondition() {
            super("build domain objects via the support package's TestHelper");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            var sourceFile = resolveSourceFile(javaClass);
            if (sourceFile.isEmpty() || !sourceFile.get().startsWith(moduleRoot.resolve("src/test/java"))) {
                return; // only test sources are constrained; production code may construct domain objects freely
            }
            if (javaClass.getPackageName().contains(".support")) {
                return; // support package helpers are allowed to construct domain objects
            }
            for (JavaConstructorCall call : javaClass.getConstructorCallsFromSelf()) {
                var targetOwner = call.getTargetOwner();
                if (resideInAPackage(basePackage + ".domain..").test(targetOwner)) {
                    events.add(SimpleConditionEvent.violated(javaClass,
                            "%s constructs %s directly in %s; build it via the support package's TestHelper instead"
                                    .formatted(javaClass.getName(), targetOwner.getName(), sourceFile.get())));
                }
            }
        }
    }

    private static final class UsedWithMethodCondition extends ArchCondition<JavaMethod> {

        private UsedWithMethodCondition() {
            super("be used at least once");
        }

        @Override
        public void check(JavaMethod javaMethod, ConditionEvents events) {
            if (javaMethod.getAccessesToSelf().isEmpty()) {
                events.add(SimpleConditionEvent.violated(javaMethod,
                        "%s is never used".formatted(javaMethod.getFullName())));
            }
        }
    }

    private static final class NonEmptyCompactConstructorCondition extends ArchCondition<JavaClass> {

        private NonEmptyCompactConstructorCondition() {
            super("define a non-empty compact constructor");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            var sourceFile = resolveSourceFile(javaClass);
            if (sourceFile.isEmpty()) {
                var message = "Cannot verify compact constructor for %s: source file not found".formatted(javaClass.getName());
                events.add(SimpleConditionEvent.violated(javaClass, message));
                return;
            }
            var source = String.join("\n", readLines(sourceFile.get()));
            var constructorMatcher = Pattern
                    .compile("(?:public\\s+|protected\\s+|private\\s+)?" + Pattern.quote(javaClass.getSimpleName()) + "\\s*\\{")
                    .matcher(source);
            if (!constructorMatcher.find()) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s must declare a compact constructor".formatted(javaClass.getName())));
                return;
            }
            var openingBraceIndex = constructorMatcher.end() - 1;
            var closingBraceIndex = findMatchingClosingBrace(source, openingBraceIndex);
            if (closingBraceIndex < 0) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s has an invalid compact constructor declaration".formatted(javaClass.getName())));
                return;
            }
            var constructorBody = source.substring(openingBraceIndex + 1, closingBraceIndex).trim();
            if (constructorBody.isEmpty()) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s compact constructor must contain defaulting or validation logic".formatted(javaClass.getName())));
            }
        }
    }

    private static Optional<Path> resolveSourceFile(JavaClass javaClass) {
        var packagePath = javaClass.getPackageName().replace('.', '/');
        var packagePrefix = javaClass.getPackageName() + ".";
        var binaryNameWithoutPackage = javaClass.getName().startsWith(packagePrefix)
                ? javaClass.getName().substring(packagePrefix.length())
                : javaClass.getName();
        var nestedClassSeparatorIndex = binaryNameWithoutPackage.indexOf('$');
        var topLevelClassName = nestedClassSeparatorIndex >= 0
                ? binaryNameWithoutPackage.substring(0, nestedClassSeparatorIndex)
                : binaryNameWithoutPackage;
        var mainSource = moduleRoot.resolve(Path.of("src/main/java", packagePath, topLevelClassName + ".java"));
        if (Files.exists(mainSource)) {
            return Optional.of(mainSource);
        }
        var testSource = moduleRoot.resolve(Path.of("src/test/java", packagePath, topLevelClassName + ".java"));
        if (Files.exists(testSource)) {
            return Optional.of(testSource);
        }
        return Optional.empty();
    }

    private static List<String> readLines(Path sourceFile) {
        try {
            return Files.readAllLines(sourceFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read source file " + sourceFile, e);
        }
    }

    private static String extractStatement(List<String> lines, int lineNumber) {
        if (lineNumber < 1 || lineNumber > lines.size()) {
            return "";
        }
        var statementBuilder = new StringBuilder();
        var index = lineNumber - 1;
        var maxLine = Math.min(lines.size() - 1, index + 20);
        for (int i = index; i <= maxLine; i++) {
            statementBuilder.append(lines.get(i));
            if (lines.get(i).contains(";")) {
                break;
            }
        }
        return statementBuilder.toString();
    }

    private static int findMatchingClosingBrace(String source, int openingBraceIndex) {
        var depth = 0;
        for (int i = openingBraceIndex; i < source.length(); i++) {
            var current = source.charAt(i);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int findMatchingClosingParenthesis(String source, int openingParenthesisIndex) {
        var depth = 0;
        for (int i = openingParenthesisIndex; i < source.length(); i++) {
            var current = source.charAt(i);
            if (current == '(') {
                depth++;
            } else if (current == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static String capitalize(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private ArchitectureCheck() {
    }
}