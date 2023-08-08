package org.rj.codegen.codegenservice.util;

import java.util.regex.Pattern;

public class Constants {
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";

    public static final Pattern PATTERN_LOAD = Pattern.compile("<load:(.*?)>");

}
