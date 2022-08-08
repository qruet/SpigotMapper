package dev.qruet.decompiler.java.element;

public class QElement<T> {

    protected final T ref;
    protected final String name;

    public QElement(T ref, String name) {
        this.ref = ref;
        this.name = name;
    }

    public T reference() {
        return ref;
    }

    @Override
    public String toString() {
        return name;
    }

}
