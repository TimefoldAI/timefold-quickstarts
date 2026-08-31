///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.tngtech.archunit:archunit:1.4.2
//DEPS org.slf4j:slf4j-nop:2.0.17
//JAVA 21+

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

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
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.domain.JavaType;
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
    private static final String PLANNING_ID = "ai.timefold.solver.core.api.domain.common.PlanningId";
    private static final String PLANNING_SOLUTION = "ai.timefold.solver.core.api.domain.solution.PlanningSolution";
    private static final String PLANNING_VARIABLE = "ai.timefold.solver.core.api.domain.variable.PlanningVariable";
    private static final String PLANNING_LIST_VARIABLE = "ai.timefold.solver.core.api.domain.variable.PlanningListVariable";
    private static final String ABSTRACT_BASIC_DEMO_DATA_GENERATOR =
            "ai.timefold.solver.service.definition.api.data.AbstractBasicDemoDataGenerator";

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
                domainFieldsMustNotUsePlainSet(),
                justificationRecordsMustNotDefineZeroArgumentConstructors(),
                justificationRecordsMustNotReplaceNullStringsWithEmptyStrings(),
                dtoRecordsMustNotCreateMutableArrayLists(),
                testUtilityClassesMustResideInSupportPackage(),
                everyModuleMustHaveATestHelperInSupportPackage(),
                testClassesMustBuildDomainAndDtoObjectsViaTestHelper(),
                everyModuleMustHaveASingleJustificationFile(),
                dtoTypesMustEndWithDto(),
                withMethodsMustBeUsed(),
                onlyDtoPackageMayUseSchemaAnnotation(),
                beanValidationApiMustNotBeUsed(),
                classesMustResideInValidSubpackages(),
                onlyInterfacesEnumsAndRecordsInDtoPackage(),
                dtoPackageMustNotDeclareNestedClasses(),
                dtoRecordsMustDeclareCompactConstructorWhenNormalizingFields(),
                dtoClassesMustNotUseJsonSetter(),
                dtoTypesMustResideInInputOrOutputSubpackage(),
                layerRule("dto.input", "dto.output", not(resideInAPackage(basePackage + ".dto.output.."))),
                identifierFieldsMustHaveMatchingEqualsAndHashCode(),
                planningVariableValueTypesMustDeclarePlanningId(),
                demoDataGeneratorsMustNotExtendAbstractBasicDemoDataGenerator(),
                constraintsMustNotUseBareStringAsConstraint());
    }

    /*
     * AbstractBasicDemoDataGenerator.demoMetaData() is final and always returns the fixed
     * DEFAULT_BASIC_META_DATA singleton, so a subclass has no way to set its own demo data
     * description (short or long). Implement the DemoDataGenerator interface directly instead,
     * where demoMetaData() can be overridden to supply a real description.
     */
    private static ArchRule demoDataGeneratorsMustNotExtendAbstractBasicDemoDataGenerator() {
        return noClasses()
                .should().beAssignableTo(ABSTRACT_BASIC_DEMO_DATA_GENERATOR)
                .as("Classes must not extend AbstractBasicDemoDataGenerator, since it cannot set a demo data "
                        + "description; implement DemoDataGenerator directly instead");
    }

    /*
     * ConstraintBuilder#asConstraint(String) is a default method that just wraps the name into a bare
     * DefaultConstraintMetadata: no description, no constraint group. asConstraint(ConstraintMetadata)
     * (typically called with a ConstraintInfo, which also carries a ConstraintGroupInfo) is the only
     * way to give a constraint a description and put it in a group. Flagging the bare-String overload
     * directly, since a constraint that "just" reused an existing string constant for its name would
     * otherwise be indistinguishable from one that took the time to add a real description and group.
     */
    private static ArchRule constraintsMustNotUseBareStringAsConstraint() {
        return classes()
                .should(new NoBareStringAsConstraintCondition())
                .as("Constraints must not use asConstraint(String); use asConstraint(ConstraintMetadata) "
                        + "(e.g. a ConstraintInfo with a ConstraintGroupInfo) instead, so every constraint "
                        + "carries a description and belongs to a constraint group");
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

    /*
     * java.util.Set makes no guarantee about iteration order (HashSet in particular reorders on rehash),
     * so two solves of the same problem can iterate a domain Set field in a different order and diverge in
     * subtle, hard-to-reproduce ways (e.g. move selection order, tie-breaking). SequencedSet (e.g.
     * LinkedHashSet) or a List both fix the order to insertion order, so either is an acceptable fix; this
     * rule only inspects field/record component declarations, not method signatures: a field is where the
     * divergence actually originates.
     */
    private static ArchRule domainFieldsMustNotUsePlainSet() {
        return classes()
                .that().resideInAPackage(basePackage + ".domain..")
                .should(new NoPlainSetFieldCondition())
                .as("Domain fields and record components must use SequencedSet or a List instead of Set, since "
                        + "Set's undefined iteration order can make the solver non-reproducible in subtle ways");
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

    private static ArchRule testClassesMustBuildDomainAndDtoObjectsViaTestHelper() {
        return classes()
                .should(new DomainConstructionOutsideSupportCondition())
                .as("Test classes must build domain and DTO objects via the support package's TestHelper instead of calling their constructors directly");
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

    /*
     * These two quickstarts (bed-allocation and conference-scheduling) describe input constraints via
     * @Schema attributes (required, minLength, minItems, minimum, maximum, ...) instead of Bean Validation,
     * so a field's constraint and its OpenAPI documentation can never drift out of sync. This is an
     * alignment preference for these two quickstarts specifically, not a hard requirement for every
     * quickstart in the repository.
     */
    private static ArchRule beanValidationApiMustNotBeUsed() {
        return classes()
                .should(new NoBeanValidationApiUsageCondition())
                .as("Classes must not use the Bean Validation API; describe input constraints via @Schema attributes instead");
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

    private static ArchRule onlyInterfacesEnumsAndRecordsInDtoPackage() {
        return classes()
                .that().resideInAPackage("..dto..")
                .and().areTopLevelClasses()
                .should().beInterfaces()
                .orShould().beRecords()
                .orShould().beEnums()
                .as("The DTO package may only contain interfaces, records and enums");
    }

    private static ArchRule dtoPackageMustNotDeclareNestedClasses() {
        return classes()
                .that().resideInAPackage("..dto..")
                .should().notBeNestedClasses()
                .as("DTO types must be simple: no nested classes (e.g. no builder pattern) in the DTO package");
    }

    /*
     * Input DTOs are deserialized straight from user-supplied JSON, so an optional collection/map field
     * (not marked @Schema(required = true)) or a nullable nested DTO field (not marked required) can
     * legitimately arrive as null. Rather than relying on Jackson-specific @JsonSetter(nulls =
     * Nulls.AS_EMPTY) to paper over that at deserialization time, such fields must be defaulted to an
     * empty collection/map (or an empty nested DTO instance) in a compact constructor: plain Java, works
     * regardless of the JSON library, and is exercised by unit tests without going through Jackson at all.
     * Fields marked @Schema(required = true) don't need this: the wire contract states they must be
     * present, so there's nothing to default away.
     *
     * Output DTOs are excluded entirely: a null there is a meaningful result (e.g. "unassigned"), not an
     * absent optional input, so it must never be silently defaulted away.
     *
     * As the "Metric" classes are not deserialized (only serialized), we still allow a canonical constructor
     * there for input validation regardless of their fields.
     */
    private static ArchRule dtoRecordsMustDeclareCompactConstructorWhenNormalizingFields() {
        return classes()
                .that().areRecords()
                .and().resideInAPackage(basePackage + ".dto..")
                .and().haveSimpleNameNotEndingWith("Metrics")
                .should(new DtoRecordConstructorCondition())
                .as("DTO input records must declare a (non-empty) compact constructor when they have an optional "
                        + "collection/map field or a nullable nested DTO field to normalize, and no explicit constructor "
                        + "otherwise (Input/OutputMetrics types may still use a compact constructor for validation)");
    }

    private static ArchRule dtoClassesMustNotUseJsonSetter() {
        return classes()
                .that().resideInAPackage(basePackage + ".dto..")
                .should(new NoJsonSetterAnnotationCondition())
                .as("DTO classes must not use @JsonSetter; normalize optional fields via a compact constructor instead");
    }

    private static ArchRule dtoTypesMustResideInInputOrOutputSubpackage() {
        return classes()
                .that().resideInAPackage(basePackage + ".dto..")
                .should().resideInAnyPackage(basePackage + ".dto.input..", basePackage + ".dto.output..")
                .as("DTO types must reside in a dto.input or dto.output subpackage, not directly in the dto package");
    }

    private static ArchRule identifierFieldsMustHaveMatchingEqualsAndHashCode() {
        return classes()
                .should(new IdentifierEqualsAndHashCodeCondition())
                .as("Classes with a guaranteed unique identifier (via @PlanningId, or an 'id' field by convention) "
                        + "must override equals() and hashCode() based on exactly that field");
    }

    /*
     * Timefold uses @PlanningId to identify planning entities/values across solving events: rebasing a
     * working solution, multithreaded solving, real-time planning (FactChanges), and move mechanics for
     * list variables. Without it on the value type of a @PlanningVariable/@PlanningListVariable, those
     * features silently fall back to reference identity or misbehave, so every domain type referenced
     * that way must carry @PlanningId on its identifier field (enums are exempt: they're inherently
     * unique per constant, so annotating them would be meaningless).
     */
    private static ArchRule planningVariableValueTypesMustDeclarePlanningId() {
        return classes()
                .that().resideInAPackage(basePackage + ".domain..")
                .should(new PlanningVariableValueTypeCondition())
                .as("Domain types used as the value of a @PlanningVariable or @PlanningListVariable must declare "
                        + "@PlanningId on their identifier field");
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

    private static final class NoPlainSetFieldCondition extends ArchCondition<JavaClass> {

        private static final String SET_TYPE = "java.util.Set";
        private static final String SEQUENCED_SET_TYPE = "java.util.SequencedSet";

        private NoPlainSetFieldCondition() {
            super("not declare a field or record component of type java.util.Set");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            for (JavaField field : javaClass.getFields()) {
                if (isPlainSetType(field)) {
                    events.add(SimpleConditionEvent.violated(javaClass,
                            "%s declares field '%s' of type Set; use SequencedSet or a List instead so iteration order is well-defined"
                                    .formatted(javaClass.getName(), field.getName())));
                }
            }
        }

        private static boolean isPlainSetType(JavaField field) {
            var rawType = field.getRawType();
            return rawType.isAssignableTo(SET_TYPE) && !rawType.isAssignableTo(SEQUENCED_SET_TYPE);
        }
    }

    private static final class NoBeanValidationApiUsageCondition extends ArchCondition<JavaClass> {

        private static final Pattern JAKARTA_VALIDATION_IMPORT_PATTERN =
                Pattern.compile("^import\\s+jakarta\\.validation\\.", Pattern.MULTILINE);

        private NoBeanValidationApiUsageCondition() {
            super("not use the Bean Validation API");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            var sourceFile = resolveSourceFile(javaClass);
            if (sourceFile.isEmpty()) {
                return;
            }
            var source = String.join("\n", readLines(sourceFile.get()));
            if (JAKARTA_VALIDATION_IMPORT_PATTERN.matcher(source).find()) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s imports the Bean Validation API in %s; describe input constraints via @Schema attributes instead"
                                .formatted(javaClass.getName(), sourceFile.get())));
            }
        }
    }

    private static final class NoBareStringAsConstraintCondition extends ArchCondition<JavaClass> {

        private NoBareStringAsConstraintCondition() {
            super("not call asConstraint(String)");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            for (JavaMethod method : javaClass.getMethods()) {
                for (JavaMethodCall call : method.getMethodCallsFromSelf()) {
                    if (isBareStringAsConstraintCall(call)) {
                        events.add(SimpleConditionEvent.violated(javaClass,
                                "%s calls asConstraint(String) in %s(); use asConstraint(ConstraintMetadata) instead"
                                        .formatted(javaClass.getName(), method.getName())));
                    }
                }
            }
        }

        private static boolean isBareStringAsConstraintCall(JavaMethodCall call) {
            var target = call.getTarget();
            if (!target.getName().equals("asConstraint")) {
                return false;
            }
            var rawParameterTypes = target.getRawParameterTypes();
            return rawParameterTypes.size() == 1 && rawParameterTypes.get(0).getName().equals("java.lang.String");
        }
    }

    /*
     * Records without an explicit equals()/hashCode() still expose those methods in bytecode, but
     * the compiler-generated ones are marked final (unlike an explicit override), which is how this
     * distinguishes "relies on the default, all-components equality" from "actually overridden".
     *
     * The identifier field is resolved two ways: via @PlanningId when present (needed for classes
     * whose identifier isn't literally called "id", e.g. Talk.code), and via a literal "id" or "name"
     * field otherwise (see resolveIdentifierFieldName). The rule isn't about @PlanningId itself:
     * @PlanningId is a Timefold solver concern (mutable classes that need lookup for multithreaded
     * or real-time solving) and is a no-op on immutable classes (records/enums always resolve to
     * ImmutableLookupStrategy, which never even inspects @PlanningId). This rule is about identity
     * in general: any class guaranteed to carry a unique identifier, annotated or simply named "id"
     * or "name" by convention, should base equals/hashCode on exactly that field, whether or not
     * @PlanningId happens to be present.
     *
     * Why this is worth enforcing even where the solver doesn't require it: these domain records
     * (Room, Speaker, and so on) are used as join/group keys in constraint streams (e.g.
     * equal(Stay::getRoom)), where equals()/hashCode() run on essentially every move evaluation.
     * Relying on the compiler's default, all-components equality there is correct but needlessly
     * expensive: it recomputes a hash over every nested field/collection (e.g. hashing a Set or Map
     * member) on every call, instead of a cheap, cached String hash. Measured on bed-allocation's
     * demo dataset, switching Room/Bed/Department to id-based equals/hashCode improved
     * move-evaluation throughput by about 18%. So this rule is primarily a performance guardrail,
     * not a correctness requirement; don't be alarmed that it fires on classes that would solve
     * just fine without it.
     */
    private static final class IdentifierEqualsAndHashCodeCondition extends ArchCondition<JavaClass> {

        private IdentifierEqualsAndHashCodeCondition() {
            super("override equals() and hashCode() based on exactly the identifier field");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            var identifierFieldName = resolveIdentifierFieldName(javaClass);
            if (identifierFieldName.isEmpty()) {
                return;
            }
            var fieldName = identifierFieldName.get();

            var equalsMethod = findDeclaredMethod(javaClass, "equals", Object.class);
            if (equalsMethod.isEmpty() || isCompilerGenerated(javaClass, equalsMethod.get())) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s has an identifier field '%s' but does not explicitly override equals(Object)"
                                .formatted(javaClass.getName(), fieldName)));
            } else {
                checkUsesExactlyThatField(javaClass, equalsMethod.get(), fieldName, "equals", events);
            }

            var hashCodeMethod = findDeclaredMethod(javaClass, "hashCode");
            if (hashCodeMethod.isEmpty() || isCompilerGenerated(javaClass, hashCodeMethod.get())) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s has an identifier field '%s' but does not explicitly override hashCode()"
                                .formatted(javaClass.getName(), fieldName)));
            } else {
                checkUsesExactlyThatField(javaClass, hashCodeMethod.get(), fieldName, "hashCode", events);
            }
        }

        private static Optional<String> resolveIdentifierFieldName(JavaClass javaClass) {
            var planningIdField = javaClass.getFields().stream()
                    .filter(field -> field.isAnnotatedWith(PLANNING_ID))
                    .map(JavaField::getName)
                    .findFirst();
            if (planningIdField.isPresent()) {
                return planningIdField;
            }
            // @PlanningId may target a method (e.g. a getter) instead of a field.
            var planningIdMethod = javaClass.getMethods().stream()
                    .filter(method -> method.isAnnotatedWith(PLANNING_ID))
                    .map(JavaMethod::getName)
                    .findFirst();
            if (planningIdMethod.isPresent()) {
                return planningIdMethod;
            }
            // No @PlanningId: fall back to the "id"/"name" naming convention used throughout the
            // domain model, so immutable value classes (records) get the same protection without
            // needing an annotation that would be a no-op on them anyway. Scoped to the domain
            // package only -- DTOs and test builders also happen to have such fields/setters, but
            // aren't identity types: they're plain data carriers, never looked up or hashed by the
            // solver. "id" takes priority over "name" for classes that have both (e.g. Room), since
            // "name" there is just a label, not the identifier; "name" only counts as the identifier
            // for classes that have no separate "id" field (e.g. TalkType). The @PlanningSolution
            // container itself is excluded: it's a one-per-solve object, never put in a Set/Map or
            // used as a join key, so a "name" field there is just a label, not an identifier.
            if (!resideInAPackage(basePackage + ".domain..").test(javaClass)
                    || javaClass.isAnnotatedWith(PLANNING_SOLUTION)) {
                return Optional.empty();
            }
            var fieldNames = javaClass.getFields().stream().map(JavaField::getName).collect(Collectors.toSet());
            if (fieldNames.contains("id")) {
                return Optional.of("id");
            }
            if (fieldNames.contains("name")) {
                return Optional.of("name");
            }
            return Optional.empty();
        }

        private static Optional<JavaMethod> findDeclaredMethod(JavaClass javaClass, String name, Class<?>... paramTypes) {
            return javaClass.getMethods().stream()
                    .filter(method -> method.getName().equals(name))
                    .filter(method -> parameterTypesMatch(method, paramTypes))
                    .findFirst();
        }

        private static boolean parameterTypesMatch(JavaMethod method, Class<?>... paramTypes) {
            var rawParameterTypes = method.getRawParameterTypes();
            if (rawParameterTypes.size() != paramTypes.length) {
                return false;
            }
            for (int i = 0; i < paramTypes.length; i++) {
                if (!rawParameterTypes.get(i).getName().equals(paramTypes[i].getName())) {
                    return false;
                }
            }
            return true;
        }

        private static boolean isCompilerGenerated(JavaClass javaClass, JavaMethod method) {
            return javaClass.isRecord() && method.getModifiers().contains(JavaModifier.FINAL);
        }

        private static void checkUsesExactlyThatField(JavaClass javaClass, JavaMethod method, String fieldName,
                String methodLabel, ConditionEvents events) {
            var usedFields = fieldsAccessedWithin(javaClass, method);
            if (!usedFields.contains(fieldName)) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s's %s() does not use the identifier field '%s'"
                                .formatted(javaClass.getName(), methodLabel, fieldName)));
                return;
            }
            var otherFields = new java.util.TreeSet<>(usedFields);
            otherFields.remove(fieldName);
            if (!otherFields.isEmpty()) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s's %s() must be based on exactly the identifier field '%s', but also uses %s"
                                .formatted(javaClass.getName(), methodLabel, fieldName, otherFields)));
            }
        }

        private static java.util.Set<String> fieldsAccessedWithin(JavaClass javaClass, JavaMethod method) {
            var result = new java.util.HashSet<String>();
            for (JavaField field : javaClass.getFields()) {
                var accessedDirectly = method.getFieldAccesses().stream()
                        .anyMatch(access -> access.getTargetOwner().equals(javaClass)
                                && access.getName().equals(field.getName()));
                var accessedViaAccessor = method.getMethodCallsFromSelf().stream()
                        .anyMatch(call -> call.getTargetOwner().equals(javaClass)
                                && isAccessorMethodName(call.getName(), field.getName()));
                if (accessedDirectly || accessedViaAccessor) {
                    result.add(field.getName());
                }
            }
            return result;
        }

        private static boolean isAccessorMethodName(String methodName, String fieldName) {
            return methodName.equals(fieldName)
                    || methodName.equals("get" + capitalize(fieldName))
                    || methodName.equals("is" + capitalize(fieldName));
        }
    }

    private static final class PlanningVariableValueTypeCondition extends ArchCondition<JavaClass> {

        private PlanningVariableValueTypeCondition() {
            super("only reference planning variable value types that declare @PlanningId");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            for (JavaField field : javaClass.getFields()) {
                if (field.isAnnotatedWith(PLANNING_VARIABLE)) {
                    checkValueType(javaClass, field.getName(), field.getRawType(), events);
                } else if (field.isAnnotatedWith(PLANNING_LIST_VARIABLE)) {
                    listElementType(field.getType())
                            .ifPresent(valueType -> checkValueType(javaClass, field.getName(), valueType, events));
                }
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (method.isAnnotatedWith(PLANNING_VARIABLE)) {
                    checkValueType(javaClass, method.getName() + "()", method.getRawReturnType(), events);
                } else if (method.isAnnotatedWith(PLANNING_LIST_VARIABLE)) {
                    listElementType(method.getReturnType())
                            .ifPresent(valueType -> checkValueType(javaClass, method.getName() + "()", valueType, events));
                }
            }
        }

        private static Optional<JavaClass> listElementType(JavaType type) {
            if (type instanceof JavaParameterizedType parameterizedType) {
                var typeArguments = parameterizedType.getActualTypeArguments();
                if (!typeArguments.isEmpty()) {
                    return Optional.of(typeArguments.get(0).toErasure());
                }
            }
            return Optional.empty();
        }

        private static void checkValueType(JavaClass owner, String memberName, JavaClass valueType, ConditionEvents events) {
            if (!resideInAPackage(basePackage + ".domain..").test(valueType) || valueType.isEnum()) {
                return; // out of scope (not a domain type), or an enum, whose constants are inherently unique
            }
            if (hasPlanningId(valueType)) {
                return;
            }
            events.add(SimpleConditionEvent.violated(owner,
                    "%s's planning variable '%s' has value type %s, which has no @PlanningId on its identifier field"
                            .formatted(owner.getName(), memberName, valueType.getName())));
        }

        private static boolean hasPlanningId(JavaClass javaClass) {
            return javaClass.getFields().stream().anyMatch(field -> field.isAnnotatedWith(PLANNING_ID))
                    || javaClass.getMethods().stream().anyMatch(method -> method.isAnnotatedWith(PLANNING_ID));
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

    private static final class DtoRecordConstructorCondition extends ArchCondition<JavaClass> {

        // The Service Module reflectively instantiates ModelConfigOverrides implementations with a
        // no-argument constructor at build time to generate the default config profile.
        private static final String MODEL_CONFIG_OVERRIDES = "ai.timefold.solver.service.definition.api.ModelConfigOverrides";

        private DtoRecordConstructorCondition() {
            super("declare a compact constructor exactly when normalizing optional collection/map or nested DTO fields");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            var constructors = javaClass.getConstructors();
            var hasZeroArgumentConstructor = constructors.stream().anyMatch(c -> c.getParameters().isEmpty());
            if (constructors.size() == 2 && hasZeroArgumentConstructor
                    && assignableTo(MODEL_CONFIG_OVERRIDES).test(javaClass)) {
                return; // the framework-required default-config-profile constructor
            }
            if (constructors.size() > 1) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s declares %d constructors; DTO records must rely on a single canonical (optionally compact) constructor"
                                .formatted(javaClass.getName(), constructors.size())));
                return;
            }
            var sourceFile = resolveSourceFile(javaClass);
            if (sourceFile.isEmpty()) {
                return;
            }
            var source = String.join("\n", readLines(sourceFile.get()));
            if (requiresCompactConstructor(javaClass)) {
                checkHasNonEmptyCompactConstructor(javaClass, source, sourceFile.get(), events);
            } else {
                checkHasNoExplicitConstructor(javaClass, source, sourceFile.get(), events);
            }
        }

        private static boolean requiresCompactConstructor(JavaClass javaClass) {
            if (!resideInAPackage(basePackage + ".dto.input..").test(javaClass)) {
                return false;
            }
            return javaClass.getFields().stream().anyMatch(DtoRecordConstructorCondition::needsNormalization);
        }

        private static boolean needsNormalization(JavaField field) {
            var rawType = field.getRawType();
            var isCollectionOrMap =
                    rawType.isAssignableTo(java.util.Collection.class) || rawType.isAssignableTo(java.util.Map.class);
            if (isCollectionOrMap) {
                return !isRequired(field);
            }
            var isNestedDto = rawType.isRecord() && resideInAPackage(basePackage + ".dto..").test(rawType);
            return isNestedDto && !isRequired(field);
        }

        private static boolean isRequired(JavaField field) {
            return field.tryGetAnnotationOfType(SCHEMA_ANNOTATION)
                    .flatMap(annotation -> annotation.get("required"))
                    .map(Boolean.class::cast)
                    .orElse(false);
        }

        private static void checkHasNonEmptyCompactConstructor(JavaClass javaClass, String source, Path sourceFile,
                ConditionEvents events) {
            var compactConstructorPattern = Pattern
                    .compile("\\b(?:public|protected|private)\\s+" + Pattern.quote(javaClass.getSimpleName()) + "\\s*\\{");
            var constructorMatcher = compactConstructorPattern.matcher(source);
            if (!constructorMatcher.find()) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s has an optional collection/map or nullable nested DTO field to normalize, but declares no compact constructor in %s"
                                .formatted(javaClass.getName(), sourceFile)));
                return;
            }
            var openingBraceIndex = constructorMatcher.end() - 1;
            var closingBraceIndex = findMatchingClosingBrace(source, openingBraceIndex);
            var constructorBody =
                    closingBraceIndex >= 0 ? source.substring(openingBraceIndex + 1, closingBraceIndex).trim() : "";
            if (constructorBody.isEmpty()) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s's compact constructor in %s must contain normalization logic (e.g. default null collections/maps/nested DTOs to an empty instance)"
                                .formatted(javaClass.getName(), sourceFile)));
            }
        }

        private static void checkHasNoExplicitConstructor(JavaClass javaClass, String source, Path sourceFile,
                ConditionEvents events) {
            var explicitConstructorPattern = Pattern.compile("\\b(?:public|protected|private)\\s+"
                    + Pattern.quote(javaClass.getSimpleName()) + "\\s*(?:\\([^)]*\\))?\\s*\\{");
            if (explicitConstructorPattern.matcher(source).find()) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        ("%s declares an explicit (compact or canonical) constructor in %s, but has no optional collection/map "
                                + "or nullable nested DTO field requiring normalization; DTO records must rely on the implicit "
                                + "canonical constructor and use @Schema attributes instead")
                                        .formatted(javaClass.getName(), sourceFile)));
            }
        }
    }

    private static final class NoJsonSetterAnnotationCondition extends ArchCondition<JavaClass> {

        private static final Pattern JSON_SETTER_PATTERN = Pattern.compile("@JsonSetter\\b");

        private NoJsonSetterAnnotationCondition() {
            super("not use @JsonSetter");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            var sourceFile = resolveSourceFile(javaClass);
            if (sourceFile.isEmpty()) {
                return;
            }
            var source = String.join("\n", readLines(sourceFile.get()));
            if (JSON_SETTER_PATTERN.matcher(source).find()) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        "%s uses @JsonSetter in %s; normalize the field via a compact constructor instead"
                                .formatted(javaClass.getName(), sourceFile.get())));
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
            super("build domain and DTO objects via the support package's TestHelper");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            var sourceFile = resolveSourceFile(javaClass);
            if (sourceFile.isEmpty() || !sourceFile.get().startsWith(moduleRoot.resolve("src/test/java"))) {
                return; // only test sources are constrained; production code may construct domain/dto objects freely
            }
            if (javaClass.getPackageName().contains(".support")) {
                return; // support package helpers are allowed to construct domain and dto objects
            }
            var domainOrDto = resideInAPackage(basePackage + ".domain..").or(resideInAPackage(basePackage + ".dto.."));
            for (JavaConstructorCall call : javaClass.getConstructorCallsFromSelf()) {
                var targetOwner = call.getTargetOwner();
                if (domainOrDto.test(targetOwner)) {
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