package dev.qruet.mapper.java.simple;

/***
 * Derived properties from string path of class (does not include generics, see {@link SimpleType})
 */
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
        if (path.startsWith("? "))
            path = path.substring(path.indexOf(" ", "? ".length()) + 1);

        // ignore generics
        path = trim(path);

        int i1 = path.indexOf("[");
        if (i1 > -1)
            path = path.substring(0, i1);

        int i2 = path.lastIndexOf("$");
        if (i2 > -1) {
            // handle subclass
            this.path = path.substring(0, i2);
            this.parent = new SimpleClass(this.path);
            this.name = path.substring(i2 + 1);
        } else if (path.contains(".")) {
            int index = path.lastIndexOf(".");
            this.name = path.substring(index + 1);
            this.path = path.substring(0, index);
        } else {
            this.name = path;
        }
    }

    /**
     * Removes generics from class path
     * @param path Class path
     * @return Trimmed class path
     */
    private String trim(String path) {
        StringBuilder trimmed = new StringBuilder();
        int f = 0; // flag
        for(int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if(c == '>')
                f--;
            else if(c == '<')
                f++;
            else if(f == 0)
                trimmed.append(c);
        }

        return trimmed.toString();
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

    public String toString() {
        return getName();
    }

}
