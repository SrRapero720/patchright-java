package io.github.patchright.patcher.assist;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class PatchLog {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static void info(String... message) {
        System.out.println(LocalDateTime.now().format(TIME_FORMATTER) + "--INFO--" + Arrays.toString(message));
    }

    public static void error(String... message) {
        System.err.println(LocalDateTime.now().format(TIME_FORMATTER) + "--ERROR-- " + Arrays.toString(message));
    }
}
