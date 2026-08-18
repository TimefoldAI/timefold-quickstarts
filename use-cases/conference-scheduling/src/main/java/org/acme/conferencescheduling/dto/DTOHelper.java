package org.acme.conferencescheduling.dto;

import java.util.List;

public class DTOHelper {
    public static List<String> immutableCopy(List<String> list) {
        return list == null ? List.of() : List.copyOf(list);
    }
}
