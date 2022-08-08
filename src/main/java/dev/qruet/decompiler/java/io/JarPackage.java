package dev.qruet.decompiler.java.io;

import java.net.URLClassLoader;
import java.util.*;
import java.lang.Class;

public class JarPackage implements Package {

    private final URLClassLoader loader;
    private final String path;
    private final List<dev.qruet.decompiler.java.io.Class> classes;
    private final List<Package> subpackages;

    public JarPackage(URLClassLoader loader, String path, String... classes) {
        this.path = path.replaceAll("/", ".");
        this.loader = loader;

        this.classes = new LinkedList<>();
        this.subpackages = new LinkedList<>();

        for (String clazz : classes)
            addClass(clazz);
    }

    public dev.qruet.decompiler.java.io.Class addClass(String rel_path) {
        final String path = this.path + "." + rel_path;
        dev.qruet.decompiler.java.io.Class c = null;
        try {
            Class<?> clazz = Class.forName(path, false, loader);
            c = new JarClass(clazz);
            this.classes.add(c);
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
        return c;
    }

    public Package addSubpackage(String path) {
        Package p = new JarPackage(loader, getPath() + "." + path);
        this.subpackages.add(p);
        return p;
    }

    public void addSubpackage(Package subpackage) {
        this.subpackages.add(subpackage);
    }

    public void pack() {
        List<dev.qruet.decompiler.java.io.Class> copy = new LinkedList<>(classes);
        for(dev.qruet.decompiler.java.io.Class c0 : copy) {
            if(c0.isSubclass()) {
                dev.qruet.decompiler.java.io.Class clazz = classes.stream().filter(c -> c.getPath().equals(c0.getParentClassPath())).findAny().orElse(null);
                if(clazz != null) {
                    clazz.addSubclass(c0);
                    classes.remove(c0);
                }
            }
        }
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Collection<dev.qruet.decompiler.java.io.Class> getClasses() {
        return classes;
    }

    @Override
    public Collection<Package> getSubpackages() {
        return subpackages;
    }
}
