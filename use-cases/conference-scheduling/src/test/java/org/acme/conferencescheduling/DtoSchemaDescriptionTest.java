package org.acme.conferencescheduling;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.importer.ClassFileImporter;

/**
 * Guards the quality of the generated OpenAPI schema: every DTO record and every one of its
 * components must carry a {@link Schema} annotation with a meaningful description.
 */
class DtoSchemaDescriptionTest {

    private static final String DTO_PACKAGE = DtoSchemaDescriptionTest.class.getPackageName() + ".dto";

    @Test
    void everyDtoRecordAndComponentHasSchemaDescription() {
        var dtoRecords = new ClassFileImporter().importPackages(DTO_PACKAGE).stream()
                .map(javaClass -> javaClass.reflect())
                .filter(Class::isRecord)
                .toList();
        assertThat(dtoRecords).isNotEmpty();
        for (Class<?> dtoRecord : dtoRecords) {
            assertThat(dtoRecord.getAnnotation(Schema.class))
                    .as("%s must be annotated with @Schema", dtoRecord.getName())
                    .isNotNull()
                    .extracting(Schema::description).asString()
                    .as("%s must have a non-blank @Schema description", dtoRecord.getName())
                    .isNotBlank();
            for (var component : dtoRecord.getRecordComponents()) {
                // @Schema does not target RECORD_COMPONENT, so it lands on the accessor method.
                Schema schema = component.getAccessor().getAnnotation(Schema.class);
                assertThat(schema)
                        .as("%s.%s must be annotated with @Schema", dtoRecord.getSimpleName(), component.getName())
                        .isNotNull();
                assertThat(schema.description())
                        .as("%s.%s must have a non-blank @Schema description", dtoRecord.getSimpleName(),
                                component.getName())
                        .isNotBlank();
            }
        }
    }
}
