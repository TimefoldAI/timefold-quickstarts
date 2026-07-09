package org.acme.vehiclerouting;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import ai.timefold.solver.service.definition.api.metrics.ModelInputMetrics;
import ai.timefold.solver.service.definition.api.metrics.ModelOutputMetrics;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

@AnalyzeClasses(packagesOf = ProjectStructureTest.class)
class ProjectStructureTest {

    private static final String BASE_PACKAGE = ProjectStructureTest.class.getPackageName();

    @ArchTest
    static final ArchRule no_class_in_domain_may_access_class_in_dto = noClassInDomainMayAccessClassInDto();

    @ArchTest
    static final ArchRule no_class_in_dto_may_access_class_in_domain = noClassInDtoMayAccessClassInDomain();

    @ArchTest
    static final ArchRule no_class_in_dto_may_access_class_in_solver = noClassInDtoMayAccessClassInSolver();

    @ArchTest
    static final ArchRule no_class_in_dto_may_access_class_in_service = noClassInDtoMayAccessClassInService();

    @ArchTest
    static final ArchRule no_class_in_domain_may_access_class_in_service = noClassInDomainMayAccessClassInService();

    @ArchTest
    static final ArchRule no_class_in_domain_may_access_class_in_rest = noClassInDomainMayAccessClassInRest();

    @ArchTest
    static final ArchRule no_class_in_domain_may_access_class_in_demo = noClassInDomainMayAccessClassInDemo();

    @ArchTest
    static final ArchRule no_class_in_solver_may_access_class_in_rest = noClassInSolverMayAccessClassInRest();

    @ArchTest
    static final ArchRule no_class_in_solver_may_access_class_in_demo = noClassInSolverMayAccessClassInDemo();

    @ArchTest
    static final ArchRule no_class_in_demo_may_access_class_in_rest = noClassInDemoMayAccessClassInRest();

    @ArchTest
    static final ArchRule repository_must_not_contain_python_or_awk_scripts = repositoryMustNotContainPythonOrAwkScripts();

    @ArchTest
    static final ArchRule domain_setters_must_return_void = domainSettersMustReturnVoid();

    @ArchTest
    static final ArchRule justification_records_must_not_define_zero_argument_constructors =
            justificationRecordsMustNotDefineZeroArgumentConstructors();

    @ArchTest
    static final ArchRule justification_records_must_not_replace_null_strings_with_empty_strings =
            justificationRecordsMustNotReplaceNullStringsWithEmptyStrings();

    @ArchTest
    static final ArchRule dto_records_must_not_create_mutable_arraylists = dtoRecordsMustNotCreateMutableArrayLists();

    @ArchTest
    static final ArchRule test_utility_classes_must_reside_in_support_package = testUtilityClassesMustResideInSupportPackage();

    @ArchTest
    static final ArchRule dto_types_must_end_with_dto = dtoTypesMustEndWithDto();

    @ArchTest
    static final ArchRule with_methods_must_be_used = withMethodsMustBeUsed();

    private static ArchRule noClassInDomainMayAccessClassInDto() {
        return classes()
                .that(resideInAPackage(BASE_PACKAGE + ".domain.."))
                .should()
                .onlyDependOnClassesThat(
                        not(resideInAPackage(BASE_PACKAGE + ".dto.."))
                                .or(assignableTo(ModelInputMetrics.class))
                                .or(assignableTo(ModelOutputMetrics.class)))
                .as("Domain layer must not depend on DTO layer");
    }

    private static ArchRule noClassInDtoMayAccessClassInDomain() {
        return classes()
                .that(resideInAPackage(BASE_PACKAGE + ".dto.."))
                .should()
                .onlyDependOnClassesThat(not(resideInAPackage(BASE_PACKAGE + ".domain..")))
                .as("DTO layer must not depend on Domain layer");
    }

    private static ArchRule noClassInDtoMayAccessClassInSolver() {
        return classes()
                .that(resideInAPackage(BASE_PACKAGE + ".dto.."))
                .should()
                .onlyDependOnClassesThat(not(resideInAPackage(BASE_PACKAGE + ".solver..")))
                .as("DTO layer must not depend on Solver layer");
    }

    private static ArchRule noClassInDtoMayAccessClassInService() {
        return classes()
                .that(resideInAPackage(BASE_PACKAGE + ".dto.."))
                .should()
                .onlyDependOnClassesThat(not(resideInAPackage(BASE_PACKAGE + ".service..")))
                .as("DTO layer must not depend on Service layer");
    }

    private static ArchRule noClassInDomainMayAccessClassInService() {
        return classes()
                .that(resideInAPackage(BASE_PACKAGE + ".domain.."))
                .should()
                .onlyDependOnClassesThat(not(resideInAPackage(BASE_PACKAGE + ".service..")))
                .as("Domain layer must not depend on Service layer");
    }

    private static ArchRule noClassInDomainMayAccessClassInRest() {
        return classes()
                .that(resideInAPackage(BASE_PACKAGE + ".domain.."))
                .should()
                .onlyDependOnClassesThat(not(resideInAPackage(BASE_PACKAGE + ".rest..")))
                .as("Domain layer must not depend on Rest layer");
    }

    private static ArchRule noClassInDomainMayAccessClassInDemo() {
        return classes()
                .that(resideInAPackage(BASE_PACKAGE + ".domain.."))
                .should()
                .onlyDependOnClassesThat(not(resideInAPackage(BASE_PACKAGE + ".demo..")))
                .as("Domain layer must not depend on Demo layer");
    }

    private static ArchRule noClassInSolverMayAccessClassInRest() {
        return classes()
                .that(resideInAPackage(BASE_PACKAGE + ".solver.."))
                .should()
                .onlyDependOnClassesThat(not(resideInAPackage(BASE_PACKAGE + ".rest..")))
                .as("Solver layer must not depend on Rest layer");
    }

    private static ArchRule noClassInSolverMayAccessClassInDemo() {
        return classes()
                .that(resideInAPackage(BASE_PACKAGE + ".solver.."))
                .and().haveSimpleNameNotEndingWith("Test")
                .should()
                .onlyDependOnClassesThat(not(resideInAPackage(BASE_PACKAGE + ".demo..")))
                .as("Solver layer must not depend on Demo layer (tests excluded)");
    }

    private static ArchRule noClassInDemoMayAccessClassInRest() {
        return classes()
                .that(resideInAPackage(BASE_PACKAGE + ".demo.."))
                .should()
                .onlyDependOnClassesThat(not(resideInAPackage(BASE_PACKAGE + ".rest..")))
                .as("Demo layer must not depend on Rest layer");
    }

    private static ArchRule repositoryMustNotContainPythonOrAwkScripts() {
        return classes()
                .that().haveFullyQualifiedName(ProjectStructureTest.class.getName())
                .should(notHaveFilesMatching("glob:**/*.py", "glob:**/*.awk"))
                .as("Repository must not contain Python or awk scripts");
    }

    private static ArchRule domainSettersMustReturnVoid() {
        return methods()
                .that().arePublic()
                .and().haveNameMatching("set[A-Z].*")
                .and().areDeclaredInClassesThat().resideInAPackage(BASE_PACKAGE + ".domain..")
                .should().haveRawReturnType(void.class)
                .as("Domain setters must follow the JavaBean contract and return void");
    }

    private static ArchRule justificationRecordsMustNotDefineZeroArgumentConstructors() {
        return classes()
                .that().areRecords()
                .and().resideInAPackage(BASE_PACKAGE + ".domain.justification..")
                .should(notDeclareZeroArgumentConstructors())
                .allowEmptyShould(true)
                .as("Constraint justification records must not declare zero-argument constructors");
    }

    private static ArchRule justificationRecordsMustNotReplaceNullStringsWithEmptyStrings() {
        return classes()
                .that().areRecords()
                .and().resideInAPackage(BASE_PACKAGE + ".domain.justification..")
                .should(notReplaceNullStringsWithEmptyStrings())
                .allowEmptyShould(true)
                .as("Constraint justification records must preserve null strings instead of converting them to empty strings");
    }

    private static ArchRule dtoRecordsMustNotCreateMutableArrayLists() {
        return classes()
                .that().areRecords()
                .and().resideInAPackage(BASE_PACKAGE + ".dto..")
                .should(notCreateMutableArrayLists())
                .as("DTO records must not create mutable ArrayList instances");
    }

    private static ArchRule testUtilityClassesMustResideInSupportPackage() {
        return classes()
                .should(resideInSupportPackageIfDeclaredInTestSources())
                .as("Test utility classes must reside in a dedicated support package");
    }

    private static ArchRule dtoTypesMustEndWithDto() {
        return classes()
                .that().resideInAPackage(BASE_PACKAGE + ".dto..")
                .should(haveDtoTypeNamingConvention())
                .as("Types in the DTO package must follow DTO naming conventions");
    }

    private static ArchRule withMethodsMustBeUsed() {
        return methods()
                .that().haveNameMatching("with[A-Z].*")
                .and().areDeclaredInClassesThat().resideInAPackage(BASE_PACKAGE + "..")
                .should(beUsedAtLeastOnce())
                .as("withXxx methods must be used at least once");
    }

    @ArchTest
    static final ArchRule only_dto_package_may_use_schema_annotation = onlyDtoPackageMayUseSchemaAnnotation();

    private static ArchRule onlyDtoPackageMayUseSchemaAnnotation() {
        return classes()
                .that(not(resideInAPackage(BASE_PACKAGE + ".dto..")))
                .and(not(resideInAPackage(BASE_PACKAGE + ".domain.justification..")))
                .should()
                .notBeAnnotatedWith(Schema.class)
                .as("Only DTO and domain.justification packages may use @Schema annotation");
    }

    @ArchTest
    static final ArchRule classes_must_reside_in_valid_subpackages = classes()
            .that().haveNameNotMatching(".*Test$")
            .and().haveNameNotMatching(".*Test\\$.*")
            .should()
            .resideInAnyPackage("..domain..", "..dto..", "..solver..", "..rest..", "..service..", "..demo..", "..enricher..",
                    "..support..");

    @ArchTest
    static final ArchRule only_interfaces_and_records_in_dto_package =
            classes()
                    .that().resideInAPackage("..dto..")
                    .should().beInterfaces()
                    .orShould().beRecords()
                    .orShould().beEnums();

    @ArchTest
    static final ArchRule record_constructor_calls_must_not_pass_null_literals =
            recordConstructorCallsMustNotPassNullLiterals();

    @ArchTest
    static final ArchRule records_must_have_compact_constructor_with_logic =
            classes()
                    .that().areRecords()
                    .and().haveNameNotMatching(".*Test\\$.*")
                    .should(haveNonEmptyCompactConstructor())
                    .as("Records must define a non-empty compact constructor to apply defaults or validation");

    @ArchTest
    static final ArchRule records_must_have_at_least_one_defined_constructor =
            classes()
                    .that().areRecords()
                    .and().haveNameNotMatching(".*Test\\$.*")
                    .should(haveAtLeastOneDefinedConstructor())
                    .as("Records must define at least one constructor");

    @ArchTest
    static final ArchRule records_must_have_with_method_for_every_component =
            classes()
                    .that().areRecords()
                    .and(not(resideInAPackage(BASE_PACKAGE + ".domain..")))
                    .and().haveNameNotMatching(".*Test\\$.*")
                    .should(haveWithMethodForEveryRecordComponent())
                    .as("Records must define one withXxx method for every record component");

    private static ArchRule recordConstructorCallsMustNotPassNullLiterals() {
        return classes()
                .should(notInstantiateRecordsWithNullLiteralArguments())
                .as("Record constructors must not be called with null literals");
    }

    private static ArchCondition<JavaClass> haveNonEmptyCompactConstructor() {
        return new NonEmptyCompactConstructorConditionTest();
    }

    private static ArchCondition<JavaClass> haveAtLeastOneDefinedConstructor() {
        return new AtLeastOneDefinedConstructorConditionTest();
    }

    private static ArchCondition<JavaClass> haveWithMethodForEveryRecordComponent() {
        return new WithMethodForEveryRecordComponentConditionTest();
    }

    private static ArchCondition<JavaClass> notInstantiateRecordsWithNullLiteralArguments() {
        return new NoNullLiteralInDtoRecordConstructorsConditionTest();
    }

    private static ArchCondition<JavaClass> notHaveFilesMatching(String... syntaxAndPatterns) {
        return new NoRepositoryFilesMatchingPatternConditionTest(syntaxAndPatterns);
    }

    private static ArchCondition<JavaClass> notDeclareZeroArgumentConstructors() {
        return new NoZeroArgumentConstructorsConditionTest();
    }

    private static ArchCondition<JavaClass> notReplaceNullStringsWithEmptyStrings() {
        return new NoNullStringToEmptyStringConditionTest();
    }

    private static ArchCondition<JavaClass> notCreateMutableArrayLists() {
        return new NoMutableArrayListsConditionTest();
    }

    private static ArchCondition<JavaClass> resideInSupportPackageIfDeclaredInTestSources() {
        return new TestUtilitiesMustResideInSupportPackageConditionTest();
    }

    private static ArchCondition<JavaMethod> beUsedAtLeastOnce() {
        return new UsedWithMethodConditionTest();
    }

    private static ArchCondition<JavaClass> haveDtoTypeNamingConvention() {
        return new DtoTypeNamingConventionConditionTest();
    }

    private static final class NoNullLiteralInDtoRecordConstructorsConditionTest extends ArchCondition<JavaClass> {

        private NoNullLiteralInDtoRecordConstructorsConditionTest() {
            super("not call DTO record constructors with null literals");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            if (javaClass.getPackageName().startsWith(BASE_PACKAGE + ".dto")) {
                return;
            }
            var sourceFile = resolveSourceFile(javaClass);
            if (sourceFile.isEmpty()) {
                return;
            }
            var lines = readLines(sourceFile.get());
            for (JavaConstructorCall constructorCall : javaClass.getConstructorCallsFromSelf()) {
                if (!constructorCall.getTargetOwner().isRecord()
                        || !constructorCall.getTargetOwner().getPackageName().startsWith(BASE_PACKAGE + ".dto")) {
                    continue;
                }
                var statement = extractStatement(lines, constructorCall.getLineNumber());
                if (statement.matches("(?s).*\\bnull\\b.*")) {
                    var message = "%s calls %s with a null literal at %s:%d".formatted(
                            javaClass.getName(),
                            constructorCall.getTarget().getFullName(),
                            sourceFile.get(),
                            constructorCall.getLineNumber());
                    events.add(SimpleConditionEvent.violated(constructorCall, message));
                }
            }
        }
    }

    private static final class NoRepositoryFilesMatchingPatternConditionTest extends ArchCondition<JavaClass> {

        private final List<String> syntaxAndPatterns;

        private NoRepositoryFilesMatchingPatternConditionTest(String... syntaxAndPatterns) {
            super("not have forbidden script files in the repository");
            this.syntaxAndPatterns = List.of(syntaxAndPatterns);
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            var repositoryRoot = Path.of("").toAbsolutePath().normalize();
            var pathMatchers = syntaxAndPatterns.stream()
                    .map(pattern -> FileSystems.getDefault().getPathMatcher(pattern))
                    .toList();
            try (var pathStream = Files.walk(repositoryRoot)) {
                pathStream
                        .filter(Files::isRegularFile)
                        .filter(path -> !path.startsWith(repositoryRoot.resolve("target")))
                        .filter(path -> !path.startsWith(repositoryRoot.resolve(".git")))
                        .map(repositoryRoot::relativize)
                        .filter(path -> pathMatchers.stream().anyMatch(pathMatcher -> pathMatcher.matches(path)))
                        .sorted()
                        .forEach(path -> events.add(SimpleConditionEvent.violated(javaClass,
                                "Forbidden script file found: " + path)));
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to scan repository root " + repositoryRoot, e);
            }
        }
    }

    private static final class NoZeroArgumentConstructorsConditionTest extends ArchCondition<JavaClass> {

        private NoZeroArgumentConstructorsConditionTest() {
            super("not declare zero-argument constructors");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            var hasZeroArgumentConstructor = Arrays.stream(javaClass.reflect().getDeclaredConstructors())
                    .anyMatch(constructor -> constructor.getParameterCount() == 0);
            if (hasZeroArgumentConstructor) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s declares a zero-argument constructor".formatted(javaClass.getName())));
            }
        }
    }

    private static final class DtoTypeNamingConventionConditionTest extends ArchCondition<JavaClass> {

        private DtoTypeNamingConventionConditionTest() {
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

    private static final class NoNullStringToEmptyStringConditionTest extends ArchCondition<JavaClass> {

        private static final Pattern NULL_TO_EMPTY_STRING_PATTERN =
                Pattern.compile("==\\s*null\\s*\\?\\s*\"\"\\s*:");

        private NoNullStringToEmptyStringConditionTest() {
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

    private static final class NoMutableArrayListsConditionTest extends ArchCondition<JavaClass> {

        private static final Pattern MUTABLE_ARRAY_LIST_PATTERN =
                Pattern.compile("\\bnew\\s+ArrayList\\s*(?:<[^>]*>)?\\s*\\(");

        private NoMutableArrayListsConditionTest() {
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

    private static final class TestUtilitiesMustResideInSupportPackageConditionTest extends ArchCondition<JavaClass> {

        private TestUtilitiesMustResideInSupportPackageConditionTest() {
            super("reside in ..support.. when declared in src/test/java");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            var sourceFile = resolveSourceFile(javaClass);
            if (sourceFile.isEmpty() || !sourceFile.get().startsWith(Path.of("src/test/java"))) {
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

    private static final class UsedWithMethodConditionTest extends ArchCondition<JavaMethod> {

        private UsedWithMethodConditionTest() {
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
        var mainSource = Path.of("src/main/java", packagePath, topLevelClassName + ".java");
        if (Files.exists(mainSource)) {
            return Optional.of(mainSource);
        }
        var testSource = Path.of("src/test/java", packagePath, topLevelClassName + ".java");
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

    private static final class NonEmptyCompactConstructorConditionTest extends ArchCondition<JavaClass> {

        private NonEmptyCompactConstructorConditionTest() {
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
                var message = "%s must declare a compact constructor".formatted(javaClass.getName());
                events.add(SimpleConditionEvent.violated(javaClass, message));
                return;
            }
            var openingBraceIndex = constructorMatcher.end() - 1;
            var closingBraceIndex = findMatchingClosingBrace(source, openingBraceIndex);
            if (closingBraceIndex < 0) {
                var message = "%s has an invalid compact constructor declaration".formatted(javaClass.getName());
                events.add(SimpleConditionEvent.violated(javaClass, message));
                return;
            }
            var constructorBody = source.substring(openingBraceIndex + 1, closingBraceIndex).trim();
            if (constructorBody.isEmpty()) {
                var message =
                        "%s compact constructor must contain defaulting or validation logic".formatted(javaClass.getName());
                events.add(SimpleConditionEvent.violated(javaClass, message));
            }
        }
    }

    private static final class AtLeastOneDefinedConstructorConditionTest extends ArchCondition<JavaClass> {

        private AtLeastOneDefinedConstructorConditionTest() {
            super("define at least one constructor");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            var sourceFile = resolveSourceFile(javaClass);
            if (sourceFile.isEmpty()) {
                var message = "Cannot verify constructor declarations for %s: source file not found"
                        .formatted(javaClass.getName());
                events.add(SimpleConditionEvent.violated(javaClass, message));
                return;
            }
            var source = String.join("\n", readLines(sourceFile.get()));
            var recordMatcher = Pattern
                    .compile("\\brecord\\s+" + Pattern.quote(javaClass.getSimpleName()) + "\\s*\\(")
                    .matcher(source);
            if (!recordMatcher.find()) {
                var message = "%s must declare at least one constructor".formatted(javaClass.getName());
                events.add(SimpleConditionEvent.violated(javaClass, message));
                return;
            }
            var recordHeaderOpeningParenthesis = recordMatcher.end() - 1;
            var recordHeaderClosingParenthesis = findMatchingClosingParenthesis(source, recordHeaderOpeningParenthesis);
            if (recordHeaderClosingParenthesis < 0) {
                var message = "%s must declare at least one constructor".formatted(javaClass.getName());
                events.add(SimpleConditionEvent.violated(javaClass, message));
                return;
            }
            var recordBodyOpeningBrace = source.indexOf('{', recordHeaderClosingParenthesis);
            if (recordBodyOpeningBrace < 0) {
                var message = "%s must declare at least one constructor".formatted(javaClass.getName());
                events.add(SimpleConditionEvent.violated(javaClass, message));
                return;
            }
            var recordBodyClosingBrace = findMatchingClosingBrace(source, recordBodyOpeningBrace);
            if (recordBodyClosingBrace < 0) {
                var message = "%s must declare at least one constructor".formatted(javaClass.getName());
                events.add(SimpleConditionEvent.violated(javaClass, message));
                return;
            }
            var recordBody = source.substring(recordBodyOpeningBrace + 1, recordBodyClosingBrace);
            var constructorMatcher = Pattern
                    .compile("(?:public\\s+|protected\\s+|private\\s+)?" + Pattern.quote(javaClass.getSimpleName())
                            + "\\s*(?:\\{|\\([^)]*\\)\\s*\\{)")
                    .matcher(recordBody);
            if (!constructorMatcher.find()) {
                var message = "%s must declare at least one constructor".formatted(javaClass.getName());
                events.add(SimpleConditionEvent.violated(javaClass, message));
            }
        }
    }

    private static final class WithMethodForEveryRecordComponentConditionTest extends ArchCondition<JavaClass> {

        private WithMethodForEveryRecordComponentConditionTest() {
            super("define one withXxx method per record component");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            var reflectedClass = javaClass.reflect();
            var declaredMethods = reflectedClass.getDeclaredMethods();
            for (var recordComponent : reflectedClass.getRecordComponents()) {
                var expectedMethodName = "with" + Character.toUpperCase(recordComponent.getName().charAt(0))
                        + recordComponent.getName().substring(1);
                var hasWithMethod = Arrays.stream(declaredMethods)
                        .anyMatch(method -> method.getName().equals(expectedMethodName)
                                && !Modifier.isStatic(method.getModifiers())
                                && method.getParameterCount() == 1
                                && method.getParameterTypes()[0].equals(recordComponent.getType())
                                && method.getReturnType().equals(reflectedClass));
                if (!hasWithMethod) {
                    var message = "%s must declare %s(%s) returning %s"
                            .formatted(javaClass.getName(), expectedMethodName, recordComponent.getType().getSimpleName(),
                                    reflectedClass.getSimpleName());
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        }
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

}
