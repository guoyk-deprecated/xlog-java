package net.landzero.xlog.utils;

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

}
