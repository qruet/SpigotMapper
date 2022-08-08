package dev.qruet.decompiler.java.io;

import java.util.Collection;

public interface Package {

    String getPath();

    Collection<Class> getClasses();

    Collection<Package> getSubpackages();

    Package addSubpackage(String rel_path);

    void addSubpackage(Package subpackage);

    Class addClass(String rel_path);

    void pack();

}
