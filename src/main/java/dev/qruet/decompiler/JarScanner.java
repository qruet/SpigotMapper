package dev.qruet.decompiler;

import dev.qruet.decompiler.java.io.JarClass;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.jar.JarFile;

public class JarScanner {

    private final URLClassLoader classLoader;
    private final JarFile jar;
    private Consumer<JarClass> consumer;

    private Consumer<String> scan_consumer;
    private BiConsumer<String, Throwable> ex_consume;
    private String[] filters;

    public JarScanner(URLClassLoader classLoader, JarFile file) {
        this.classLoader = classLoader;
        this.jar = file;
    }

    public void filter(String... packages) {
        this.filters = packages;
    }

    public void onScan(Consumer<String> consumer) {
        this.scan_consumer = consumer;
    }
    public void onClassLoad(Consumer<JarClass> consumer) {
        this.consumer = consumer;
    }

    public void onException(BiConsumer<String, Throwable> consumer) {
        this.ex_consume = consumer;
    }

    public void scan(boolean load) {
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
                    if (in > -1) {
                        this.scan_consumer.accept(path);
                        if(load)
                            loadClass(path.substring(0, in));
                    }
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

        JarClass c = new JarClass(clazz);
        if (consumer != null)
            consumer.accept(c);
    }

}
