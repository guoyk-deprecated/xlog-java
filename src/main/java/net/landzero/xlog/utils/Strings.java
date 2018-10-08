package net.landzero.xlog.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Strings {

    public static boolean isEmpty(@Nullable String s) {
        if (s == null)
            return true;
        if (s.length() == 0)
            return true;
        return s.trim().length() == 0;
    }

    @Nullable
    public static String normalize(@Nullable String s) {
        if (s == null)
            return null;
        s = s.trim();
        if (s.length() == 0)
            return null;
        return s;
    }

    @Nullable
    public static String normalize(@Nullable Object o) {
        if (o == null) return null;
        return normalize(o.toString());
    }

    @NotNull
    public static String safeNormalize(@Nullable String s) {
        if (s == null)
            return "";
        return s.trim();
    }

    @NotNull
    public static String safeNormalize(@Nullable Object o) {
        if (o == null) return "NULL";
        return safeNormalize(o.toString());
    }

    @NotNull
    public static String normalizeKeyword(@Nullable String s) {
        s = normalize(s);
        if (s == null) return "null";
        return s.replaceAll("[\\s,\\[\\]]+", "_");
    }


    @NotNull
    public static String normalizeKeyword(@Nullable Object o) {
        if (o == null) return "null";
        return normalizeKeyword(o.toString());
    }

}
