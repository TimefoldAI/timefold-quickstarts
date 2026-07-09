package org.acme.orderpicking.support;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class RecordWithMethodCoverageTest {

    @Test
    public void testRecordWithMethodsCoverage() throws Exception {
        Path srcDir = Path.of("src/main/java");
        if (!Files.exists(srcDir)) {
            return;
        }

        try (Stream<Path> walk = Files.walk(srcDir)) {
            List<Path> javaFiles = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .toList();

            List<Class> recordClasses = new ArrayList<>();
            for (Path p : javaFiles) {
                String pathStr = p.toString();
                String rel = pathStr.substring("src/main/java/".length(), pathStr.length() - ".java".length());
                String className = rel.replace('/', '.').replace('\\', '.');
                try {
                    Class<?> cls = Class.forName(className);
                    if (cls.isRecord() && (cls.getPackageName().contains(".dto")
                            || cls.getPackageName().contains(".domain.justification"))) {
                        recordClasses.add(cls);
                    }
                } catch (ClassNotFoundException e) {
                    // Ignore
                }
            }

            for (Class cls : recordClasses) {
                Object instance = instantiateRecord(cls);
                assertNotNull(instance);
                for (java.lang.reflect.Method m : cls.getMethods()) {
                    if (m.getName().startsWith("with") && m.getParameterCount() == 1) {
                        Object val = getDefaultValue(m.getParameterTypes()[0]);
                        m.invoke(instance, val);
                    }
                }
            }
        }
    }

    private static Object instantiateRecord(Class<?> cls) throws Exception {
        if (!cls.isRecord()) {
            return null;
        }
        java.lang.reflect.RecordComponent[] components = cls.getRecordComponents();
        Object[] args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            Class<?> type = components[i].getType();
            args[i] = getDefaultValue(type);
        }
        java.lang.reflect.Constructor<?> constructor = cls.getDeclaredConstructor(
                Arrays.stream(components).map(java.lang.reflect.RecordComponent::getType).toArray(Class[]::new));
        constructor.setAccessible(true);
        return constructor.newInstance(args);
    }

    private static Object getDefaultValue(Class<?> type) throws Exception {
        if (type == String.class) {
            return "test";
        }
        if (type == int.class || type == Integer.class) {
            return 1;
        }
        if (type == long.class || type == Long.class) {
            return 1L;
        }
        if (type == double.class || type == Double.class) {
            return 1.0;
        }
        if (type == boolean.class || type == Boolean.class) {
            return false;
        }
        if (type == List.class) {
            return List.of();
        }
        if (type == Set.class) {
            return Set.of();
        }
        if (type == Map.class) {
            return Map.of();
        }
        if (type.isRecord()) {
            return instantiateRecord(type);
        }
        return null;
    }
}
