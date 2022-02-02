package dev.qruet.mapper.java.simple;

public class SimpleClass {

    public static SimpleClass of(Class<?> clazz) {
        return of(clazz.getName());
    }

    public static SimpleClass of(String classPath) {
        return new SimpleClass(classPath);
    }

    private final String name;
    private SimpleClass parent;
    private String path;

    private SimpleClass(String path) {
        if(path.contains("$")) {
            // handle subclass
            String[] split = path.split("\\$");
            this.name = split[1];
            this.parent = new SimpleClass(split[0]);
            this.path = split[0];
        } else if(path.contains(".")) {
            int index = path.lastIndexOf(".");
            this.name = path.substring(index + 1);
            this.path = path.substring(0, index);
        } else {
            this.name = path;
        }
    }

    public String getName() {
        return name;
    }

    public boolean isPrimitive() {
        return path == null;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public SimpleClass getParent() {
        return parent;
    }

    public String getPath() {
        return path;
    }

}
