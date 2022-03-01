package dev.qruet.mapper.java.simple;

import dev.qruet.mapper.java.util.Pair;

import java.lang.reflect.Type;
import java.util.*;

public class SimpleType {

    public static SimpleType of(Type type) {
        return of(type.getTypeName());
    }

    private static SimpleType of(String typeName) {
        System.out.println("#of(" + typeName + ")");
        String generics = "";
        String prefix = "";

        if (typeName.startsWith("? ")) {
            prefix = typeName.substring(0, typeName.indexOf(" ", "? ".length()));
        }

        SimpleClass clazz = SimpleClass.of(typeName);
        typeName = typeName.substring(typeName.indexOf(clazz.getName()));
        if (typeName.contains("<"))
            generics = typeName.substring(typeName.indexOf("<") + 1, typeName.lastIndexOf(">"));

        return new SimpleType(clazz, generics, typeName.endsWith("[]"), prefix);
    }

    private final SimpleClass clazz;
    private String prefix;
    private boolean array;

    private SimpleType[] generics;

    private SimpleType(SimpleClass clazz, String generics, boolean array, String special_prefix) {
        System.out.println("SimpleType(" + clazz + ", " + generics + ", " + array + ", " + special_prefix + ")");
        this.clazz = clazz;
        this.prefix = special_prefix;
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
                    if (sub.startsWith("? ")) {
                        String prefix = sub.substring(0, sub.indexOf(" ", "? ".length()));
                        i += prefix.length();
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
                this.generics[i] = of(typeName);
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
        System.out.println("#toString(" + getName() + ")");
        StringBuilder header = new StringBuilder(prefix.isBlank() ? getName() : prefix + " " + getName());
        if (hasGeneric()) {
            // handle generics
            header.append("<");
            List<String> vals = new ArrayList<>();
            System.out.println("Generic Count: " + generics.length);
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
