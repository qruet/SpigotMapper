package dev.qruet.mapper.java.simple;

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
        String postfix = "";

        if (typeName.startsWith("? ")) {
            prefix = typeName.substring(0, typeName.indexOf(" ", "? ".length()));
        }

        SimpleClass clazz = SimpleClass.of(typeName);
        typeName = typeName.substring(typeName.indexOf(clazz.getName()));

        int s1 = typeName.indexOf("<");
        if (s1 > -1) {
            generics = typeName.substring(s1 + 1, typeName.lastIndexOf(">"));
            typeName = typeName.replace(generics, "");
        }

        int s2 = typeName.indexOf("[");
        if(s2 > -1)
            postfix = typeName.substring(s2, typeName.lastIndexOf("]") + 1);

        return new SimpleType(clazz, generics, postfix, prefix);
    }

    private final SimpleClass clazz;
    private String prefix;
    private String postfix;

    private SimpleType[] generics;

    private boolean displayParent = false;

    private SimpleType(SimpleClass clazz, String generics, String postfix, String special_prefix) {
        System.out.println("SimpleType([" + clazz + "], [" + generics + "], [" + postfix + "], [" + special_prefix + "])");
        this.clazz = clazz;
        this.prefix = special_prefix;
        this.postfix = postfix;

        if (!generics.isEmpty()) {
            // handle generic type

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

            this.generics = new SimpleType[split.size()];

            for (int i = 0; i < this.generics.length; i++) {
                String typeName = split.get(i);
                this.generics[i] = of(typeName);
            }

        }
    }

    public Collection<SimpleType> generics() {
        return Collections.unmodifiableCollection(Arrays.asList(this.generics));
    }

    public void showParent(boolean show) {
        displayParent = show;
    }

    public boolean isArray() {
        return !postfix.isBlank();
    }

    public boolean hasGenerics() {
        return generics != null;
    }

    public boolean isPrimitive() {
        return clazz.isPrimitive();
    }

    public String getName() {
        return clazz.hasParent() && displayParent ? clazz.getParent().getName() + "." + clazz : clazz.getName();
    }

    public String getPackage() {
        return clazz.getPath();
    }

    public SimpleClass getSimpleClass() {
        return clazz;
    }

    @Override
    public String toString() {
        StringBuilder header = new StringBuilder(prefix.isBlank() ? getName() : prefix + " " + getName());
        if (hasGenerics()) {
            // handle generics
            header.append("<");
            List<String> vals = new ArrayList<>();
            for (SimpleType type : generics)
                vals.add(type.toString());
            // TODO resolve recursive issues with nested generic types
            header.append(String.join(", ", vals));
            header.append(">");
        }

        header.append(postfix);

        return header.toString();
    }

}
