package dev.qruet.mapper.java.simple;

import dev.qruet.mapper.java.util.Pair;

import java.lang.reflect.Type;
import java.util.*;

public class SimpleType {

    public static SimpleType of(Type type) {
        Pair<String, String> out = split(type.getTypeName());
        return new SimpleType(out.getKey(), out.getValue());
    }

    private final String path;
    private final String name;
    private final boolean primitive;

    private SimpleType[] generics;

    private SimpleType(String path, String name) {
        this.path = path;
        primitive = path == null || path.isBlank() || !path.contains(".");
        if (!primitive && name.contains("<")) {
            // handle generic type
            String generic = name.substring(name.indexOf("<") + 1, name.indexOf(">"));
            String[] generics = generic.split(", ");
            this.generics = new SimpleType[generics.length];

            for (int i = 0; i < this.generics.length; i++) {
                Pair<String, String> out = split(generics[i]);
                System.out.println("Creating generic: " + out.getKey() + ", " + out.getValue());
                this.generics[i] = new SimpleType(out.getKey(), out.getValue());
            }

            this.name = name.substring(0, name.indexOf("<"));
        } else {
            this.name = name;
        }
    }

    public Collection<SimpleType> generics() {
        return Collections.unmodifiableCollection(Arrays.asList(this.generics));
    }

    public boolean isGeneric() {
        return generics != null;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public String getName() {
        return name;
    }

    public String getPackage() {
        return path;
    }

    @Override
    public String toString() {
        StringBuilder header = new StringBuilder(getName());
        if (isGeneric()) {
            // handle generics
            header.append("<");
            List<String> vals = new ArrayList<>();
            for (SimpleType type : generics)
                vals.add(type.toString());
            header.append(String.join(", ", vals));
            header.append(">");
        }

        return header.toString();
    }

    private static Pair<String, String> split(String type) {
        int sI = type.indexOf("<");

        SimpleClass sC = null;
        if (sI > -1)
            sC = SimpleClass.of(type.substring(0, sI));
        else
            sC = SimpleClass.of(type);

        return new Pair<>(sC.getPath(), sC.getName() + (sI > -1 ? type.substring(sI) + ">" : ""));
    }
}
