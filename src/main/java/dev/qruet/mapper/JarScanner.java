package dev.qruet.mapper;

import dev.qruet.mapper.java.QClass;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.jar.JarFile;

public class JarScanner {

    private final URLClassLoader classLoader;
    private final JarFile jar;
    private Consumer<QClass> consumer;
    private String[] filters;

    public JarScanner(URLClassLoader classLoader, JarFile file) {
        this.classLoader = classLoader;
        this.jar = file;
    }

    public void filter(String... packages) {
        this.filters = packages;
    }

    public void onClassLoad(Consumer<QClass> consumer) {
        this.consumer = consumer;
    }

    public void scan() {
        boolean skip = true;
        try {
            java.util.Enumeration enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
                if(file.isDirectory()) {
                    if(filters.length == 0 || Arrays.stream(filters).anyMatch(f -> file.getName().contains(f.replaceAll("\\.", "/")))) {
                        skip = false;
                    } else
                        skip = true;
                    continue;
                }

                if(skip)
                    continue;

                String path = file.getName();
                path = path.replaceAll("/", ".");
                loadClass(path.substring(0, path.indexOf(".class")));
            }
            jar.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void loadClass(String path) {
        System.out.println("\n---------------");
        System.out.println("Load: " + path);
        System.out.println("---------------\n");
        try {
            Class<?> clazz = Class.forName(path, false, classLoader);
            QClass c = new QClass(clazz);
            if(consumer != null)
                consumer.accept(c);
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
