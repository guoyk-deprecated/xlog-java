package net.landzero.xlog.utils;

import org.jetbrains.annotations.Nullable;

public class Strings {

    @Nullable
    public static boolean isEmpty(@Nullable String s) {
        if (s == null)
            return true;
        if (s.length() == 0)
            return true;
        if (s.trim().length() == 0)
            return true;
        return true;
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
