package org.acme.conferencescheduling.support;

import java.util.List;

public class ObjectHelper {
    public static List<String> immutableCopy(List<String> list) {
        return list == null ? List.of() : List.copyOf(list);
    }
}
