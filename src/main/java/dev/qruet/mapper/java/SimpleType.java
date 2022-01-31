package dev.qruet.mapper.java;

import java.lang.reflect.Type;
import java.util.Map;

public class SimpleType {

    public static SimpleType of(Type type) {
        Map.Entry<String, String> entry = split(type.getTypeName());
        return new SimpleType(entry.getKey(), entry.getValue());
    }

    private final String path;
    private final String name;
    private final boolean primitive;
    private final boolean generic;

    private SimpleType genericType;

    private SimpleType(String path, String name) {
        System.out.println("Type: " + path + " " + name);
        this.path = path;
        primitive = path == null || path.isBlank() || !path.contains(".");
        generic = !primitive && name.contains("<");
        if(generic) {
            String generic = name.substring(name.indexOf("<") + 1, name.indexOf(">"));
            Map.Entry<String, String> val = split(generic);
            this.genericType = new SimpleType(val.getKey(), val.getValue());
            this.name = name.substring(0, name.indexOf("<"));
        } else {
            this.name = name;
        }
    }

    public SimpleType getGenericType() {
        return genericType;
    }

    public boolean isGeneric() {
        return generic;
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

        SimpleType type = this;
        int i;
        for(i = 0; isGeneric(); i++, type = type.getGenericType())
            header.append("<" + type.getName());
        for(; i > 0; i--)
            header.append(">");

        return header.toString();
    }

    private static Map.Entry<String, String> split(String type) {
        int index = -1;
        int i = 0;
        for(char c : type.toCharArray()) {
            if(c == '<')
                break;
            if(c == '.')
                index = i;

            i++;
        }
        int finalIndex = index;
        return new Map.Entry<>() {
            @Override
            public String getKey() {
                if(finalIndex == -1)
                    return "";
                return type.substring(0, finalIndex);
            }

            @Override
            public String getValue() {
                if(finalIndex == -1)
                    return type;
                return type.substring(finalIndex + 1);
            }

            @Override
            public String setValue(String value) {
                return null;
            }
        };
    }

}
