package dev.qruet.mapper.java.simple;

import dev.qruet.mapper.java.util.Pair;

import java.lang.reflect.Type;
import java.util.*;

public class SimpleType {

    public static SimpleType of(Type type) {
        return of(type.getTypeName(), false);
    }

    private static SimpleType of(String typeName, boolean extended) {
        String generics = "";
        SimpleClass clazz = SimpleClass.of(typeName);
        String clazzFull = clazz.getName() + (clazz.hasGeneric() ? "<" + clazz.getRawGeneric() + ">" : "");
        typeName = typeName.substring(typeName.indexOf(clazzFull) + clazzFull.length());
        if (typeName.contains("<"))
            generics = typeName.substring(typeName.indexOf("<") + 1, typeName.lastIndexOf(">"));

        return new SimpleType(clazz, generics, typeName.endsWith("[]"), extended);
    }

    private final SimpleClass clazz;
    private boolean extended;
    private boolean array;

    private SimpleType[] generics;

    private SimpleType(SimpleClass clazz, String generics, boolean array, boolean extended) {
        System.out.println("SimpleType(" + clazz + ", " + generics + ", " + array + ", " + extended + ")");
        this.clazz = clazz;
        this.extended = extended;
        this.array = array;

        if (!generics.isEmpty()) {
            // handle generic type
            System.out.println("Generic: " + generics);

            List<String> split = new ArrayList<>();

            int s = 0, eF = 0;
            for (int i = 0; i < generics.length(); i++) {
                char c = generics.charAt(i);
                if (c == '?') {
                    String sub = generics.substring(i);
                    if (sub.startsWith("? extends")) {
                        i += "? extends".length();
                    }
                } else if (eF == 0 && c == ',') {
                    split.add(generics.substring(s, i));
                } else if (eF == 0 && c == ' ') {
                    s = i + 1; // start index
                } else if (c == '<') {
                    eF++; // end flag
                } else if (c == '>')
                    eF--;
            }
            split.add(generics.substring(s) + (eF > 0 ? '>' : ""));
            System.out.println("Split: " + split);

            this.generics = new SimpleType[split.size()];

            for (int i = 0; i < this.generics.length; i++) {
                String typeName = split.get(i);
                System.out.println("Creating generic: " + typeName);
                this.generics[i] = of(typeName, typeName.startsWith("? extends"));
            }

        }
    }

    public Collection<SimpleType> generics() {
        return Collections.unmodifiableCollection(Arrays.asList(this.generics));
    }

    public boolean isArray() {
        return array;
    }

    public boolean hasGeneric() {
        return generics != null;
    }

    public boolean isPrimitive() {
        return clazz.isPrimitive();
    }

    public String getName() {
        return clazz.toString();
    }

    public String getPackage() {
        return clazz.getPath();
    }

    public SimpleClass getSimpleClass() {
        return clazz;
    }

    @Override
    public String toString() {
        StringBuilder header = new StringBuilder(extended ? "? extends " + getName() : getName());
        if (hasGeneric()) {
            // handle generics
            header.append("<");
            List<String> vals = new ArrayList<>();
            for (SimpleType type : generics)
                vals.add(type.toString());
            // TODO resolve recursive issues with nested generic types
            header.append(String.join(", ", vals));
            header.append(">");
        }

        if (isArray())
            header.append("[]");

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
