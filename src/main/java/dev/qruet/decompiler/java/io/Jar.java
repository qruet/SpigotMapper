package dev.qruet.decompiler.java.io;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Collection;

public interface Jar {

    public String getName();

    public Collection<Package> getPackages();

    public Collection<Package> getSubpackagesFromPath(String path);

    public URLClassLoader getClassLoader();

    public Class getClass(String classpath);

    public Package getPackage(String path);

    public static Jar getJarFromFile(File file) {
        return new JarFile(file);
    }

}
