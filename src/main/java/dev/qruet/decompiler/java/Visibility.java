package dev.qruet.decompiler.java;

import java.lang.reflect.Modifier;

public enum Visibility {

    PUBLIC, PROTECTED, PRIVATE;

    public static Visibility fromModifier(int modifier) {
        if (Modifier.isPublic(modifier))
            return Visibility.PUBLIC;
        else if (Modifier.isProtected(modifier))
            return Visibility.PROTECTED;
        else
            return Visibility.PRIVATE;
    }

}
