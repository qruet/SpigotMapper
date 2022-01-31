package dev.qruet.mapper;

import java.net.URLClassLoader;
import java.util.jar.JarFile;

public class JarComparator {

    private final JarScanner scanner;

    public JarComparator(URLClassLoader loader, JarFile jar1, JarFile jar2) {
        this.scanner = new JarScanner(loader, jar1);
    }

    public void compareJars() {
        System.out.println("Beginning scan.");
        scanner.filter("net.minecraft.server");
        scanner.onClassLoad((clazz) -> {
            System.out.println("Finished loading class: " + clazz);
        });
        scanner.scan();
    }

}
