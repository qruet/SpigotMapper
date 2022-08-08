package dev.qruet.decompiler.java.io;

import java.lang.reflect.Field;
import java.util.Collection;

public interface Class {

    public String decompile();

    public Field getField(String name);

    public String getName();

    public String getPackage();

    public Collection<JarMethod> methods();

    public void addSubclass(Class sub);

    public Collection<Class> getSubclasses();

    public boolean isSubclass();

    public String getParentClassPath();

    public String getPath();

}
