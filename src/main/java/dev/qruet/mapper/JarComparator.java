package dev.qruet.mapper;

import dev.qruet.mapper.java.QClass;
import dev.qruet.mapper.java.QMethod;
import dev.qruet.mapper.java.Visibility;

import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;

/**
 * Manages the scanner for finding differences between two jars
 */
public class JarComparator {

    private static final String PACKAGE = "";
    private final JarScanner scanner;
    private QClass mainClass;

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
            if (this.mainClass == null && clazz.methods().stream().anyMatch(this::isMain)) {
                this.mainClass = clazz;
                System.out.println("\n\nFound main class: " + mainClass.getName() + "\n\n");
            }
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

    private boolean isMain(QMethod method) {
        return method.getVisibility() == Visibility.PUBLIC && method.isStatic() && method.returnType().getTypeName().equals("void") &&
                method.getName().equals("main") && method.parameters().size() == 1 && method.parameters().get(0) == String[].class;
    }

}
