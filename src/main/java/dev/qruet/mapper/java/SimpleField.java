package dev.qruet.mapper.java;

import java.lang.reflect.Field;

public class SimpleField {

    public static SimpleField of(Field field) {
        return of(SimpleType.of(field.getGenericType()), field.getName());
    }

    public static SimpleField of(SimpleType type, String name) {
        return new SimpleField(type, name);
    }

    private final SimpleType type;
    private final String name;

    private SimpleField(SimpleType type, String name) {
        this.type = type;
        this.name = name;
    }

    public SimpleType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

}
