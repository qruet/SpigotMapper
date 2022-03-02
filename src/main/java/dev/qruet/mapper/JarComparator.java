package dev.qruet.mapper;

import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;

public class JarComparator {

    private static final String PACKAGE = "org.yaml";
    private final JarScanner scanner;

    public JarComparator(URLClassLoader loader, JarFile jar1, JarFile jar2) {
        this.scanner = new JarScanner(loader, jar1);
    }

    public void compareJars() {
        AtomicInteger counter = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        System.out.println("Beginning scan.");
        scanner.filter(PACKAGE);
        scanner.onClassLoad((clazz) -> {
            counter.getAndIncrement();
            System.out.println("Finished loading class: " + clazz);
        });
        scanner.onException((clazz, e) -> {
            failed.getAndIncrement();
            System.out.println("Failed to load class, " + clazz + ".");
        });
        scanner.scan();
        System.out.println("\n\nLoaded " + counter + " classes within " + PACKAGE + "!");
        System.out.println(failed + " classes failed to load.\n\n");
    }

}
