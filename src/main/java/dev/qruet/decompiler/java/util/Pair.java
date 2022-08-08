package dev.qruet.decompiler.java.util;

public class Pair<T, K> {

    private T key;
    private K val;

    public Pair() {
        this.key = null;
        this.val = null;
    }

    public Pair(T key, K value) {
        this.key = key;
        this.val = value;
    }

    public void setKey(T key) {
        this.key = key;
    }

    public void setValue(K val) {
        this.val = val;
    }

    public T getKey() {
        return key;
    }

    public K getValue() {
        return val;
    }

    @Override
    public String toString() {
        return "[" + key + ", " + val + "]";
    }

}
