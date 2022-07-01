package dev.qruet.mapper.java;

import java.util.*;

public class QPackage {

    private final List<QClass> classes;
    private final String path;

    public QPackage(String path) {
        this.path = path;
        this.classes = new LinkedList<>(); // TODO Allocate an ArrayList instance
    }

    public void addClass(QClass clazz) {
        this.classes.add(clazz);
    }

    public Collection<QClass> classes() {
        return Collections.unmodifiableCollection(classes);
    }

}
