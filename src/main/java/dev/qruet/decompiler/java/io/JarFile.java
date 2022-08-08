package dev.qruet.decompiler.java.io;

import dev.qruet.decompiler.Main;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;

public class JarFile implements Jar {

    final URLClassLoader classLoader;
    final String name;

    private final List<Package> root = new ArrayList<>();
    private List<Package> cache = new LinkedList<>();

    public JarFile(File file) {
        URLClassLoader child = null;
        java.util.jar.JarFile jarFile = null;
        try {
            jarFile = new java.util.jar.JarFile(file);
            child = new URLClassLoader(new URL[]{file.toURL()}, Main.class.getClassLoader());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.classLoader = child;
        this.name = file.getName();

        populateRootLayer(jarFile);

        List<Package> packages = new LinkedList<>();
        for (Package p : root)
            collect(packages, p);

        this.cache = packages;

        try {
            jarFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateRootLayer(java.util.jar.JarFile file) {
        java.util.Enumeration<JarEntry> enumEntries = file.entries();
        while (enumEntries.hasMoreElements()) {
            JarEntry entry = enumEntries.nextElement();
            if (entry.isDirectory())
                continue;

            String path = entry.getName();
            int split = path.lastIndexOf('.');
            path = path.replaceAll("/", ".");
            String ext = path.substring(split + 1);

            if (split == -1)
                continue;

            path = path.substring(0, split); // remove extension

            split = path.lastIndexOf('.');
            if (split == -1)
                continue;

            String name = path.substring(split + 1);
            path = path.substring(0, split);

            final String packagePath = path;

            Package pack = getOrCreatePackage(packagePath);
            if (ext.equals("class")) {
                pack.addClass(name);
            }
        }


    }

    private Package getOrCreatePackage(String path) {
        for (Package p : root) {
            if (path.startsWith(p.getPath())) {
                if (path.equals(p.getPath()))
                    return p;
                return seekDown0(path, p);
            }
        }

        Package newPackage = new JarPackage(classLoader, path);
        addRoot(newPackage);
        return newPackage;
    }

    public Package getPackage(String path) {
        for (Package p : root) {
            if (path.startsWith(p.getPath())) {
                if (path.equals(p.getPath()))
                    return p;
                Package out = seekDown1(path, p);
                return path.equals(out.getPath()) ? out : null;
            }
        }
        return null;
    }
    private void addRoot(Package pack) {
        boolean added = false;
        for (int i = 0; i < root.size(); i++) {
            Package p = root.get(i);
            if (p.getPath().startsWith(pack.getPath())) {
                if (!added) {
                    added = true;
                    root.set(i, pack);
                } else {
                    root.remove(i);
                }
                pack.addSubpackage(p);
            }
        }

        if (!added)
            root.add(pack);
    }

    private Collection<Package> getPackages(String path) {
        for (int i = 0; i < root.size(); i++) {
            Package p = root.get(i);
            if (path.startsWith(p.getPath())) {
                if (path.equals(p.getPath()))
                    return p.getSubpackages();
                return seekDown1(path, p).getSubpackages();
            }
        }
        return null;
    }

    private Package seekDown0(String path, Package pack) {
        for (Package sub : pack.getSubpackages()) {
            if (path.startsWith(sub.getPath())) {
                if (path.equals(sub.getPath()))
                    return sub;
                return seekDown0(path, sub);
            }
        }
        return pack.addSubpackage(path.replace(pack.getPath() + ".", ""));
    }

    private Package seekDown1(String path, Package pack) {
        for (Package sub : pack.getSubpackages()) {
            if (path.startsWith(sub.getPath())) {
                if (path.equals(sub.getPath()))
                    return sub;
                return seekDown1(path, sub);
            }
        }
        return pack;
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<Package> getPackages() {
        return cache;
    }

    @Override
    public Collection<Package> getSubpackagesFromPath(String path) {
        return path.isBlank() ? root : getPackages(path);
    }

    public Class getClass(String classpath) {
        String packagePath = classpath.substring(0, classpath.lastIndexOf("."));
        Package pack = getPackage(packagePath);
        if(pack == null) {
            System.err.println("Failed to find package for " + classpath);
            return null;
        }
        return pack.getClasses().stream().filter(c -> c.getPath().equals(classpath)).findAny().orElse(null);
    }

    private void collect(List<Package> list, Package current) {
        current.pack();
        list.add(current);
        for (Package sub : current.getSubpackages())
            collect(list, sub);
    }
}
