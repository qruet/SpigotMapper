package dev.qruet.mapper;

import dev.qruet.mapper.java.QClass;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class JarScanner {

    private final URLClassLoader classLoader;
    private final JarFile jar;
    private Consumer<QClass> consumer;
    private BiConsumer<String, Throwable> ex_consume;
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

    public void onException(BiConsumer<String, Throwable> consumer) {
        this.ex_consume = consumer;
    }

    public void scan() {
        System.out.println("Scanning w/ filters: " + Arrays.toString(filters));
        boolean skip = true;
        try {
            java.util.Enumeration enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
                if (file.isDirectory()) {
                    // check if we should skip the following classes in the current package
                    skip = filters != null && Arrays.stream(filters).noneMatch(f -> file.getName().replaceAll("/", ".").startsWith(f));
                    continue;
                }

                if (skip)
                    continue;

                String path = file.getName();
                path = path.replaceAll("/", ".");
                int in = path.indexOf(".class");
                try {
                    if (in > -1)
                        loadClass(path.substring(0, in));
                } catch (Throwable e) {
                    if (this.ex_consume != null)
                        this.ex_consume.accept(path.substring(0, in), e);
                    else
                        e.printStackTrace();
                }
            }
            jar.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadClass(String path) throws Throwable {
        System.out.println("\n---------------");
        System.out.println("Load: " + path);
        System.out.println("---------------\n");

        Class<?> clazz = Class.forName(path, false, classLoader);

        QClass c = new QClass(clazz);
        if (consumer != null)
            consumer.accept(c);
    }

}
