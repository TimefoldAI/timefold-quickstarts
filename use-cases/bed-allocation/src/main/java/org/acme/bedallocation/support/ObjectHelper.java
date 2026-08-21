package org.acme.bedallocation.support;

import java.util.List;

public final class ObjectHelper {

    private ObjectHelper() {
    } // static helper

    public static <T> List<T> immutableCopy(List<T> list) {
        return list == null ? List.of() : List.copyOf(list);
    }
}
