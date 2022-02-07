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
    private Generic generic;

    private SimpleClass(String path) {
        if (path.length() > 1) {
            int sF = path.indexOf("<");
            if (path.charAt(sF + 2) == '>') {
                generic = new Generic(new SimpleClass("" + path.charAt(sF + 1)));
            } else if (sF > -1) {
                String sub = path.substring(sF + 1, path.indexOf(">"));
                if (sub.startsWith("? super")) {
                    String subType = sub.substring("? super ".length());
                    if(subType.contains(".")) {
                        generic = new Generic("? super ", new SimpleClass(subType));
                    } else {
                        generic = new Generic(new SimpleClass(sub));
                    }
                }
            }
        }

        if (path.contains("<"))
            path = path.substring(0, path.indexOf("<"));

        if (path.startsWith("? extends "))
            path = path.substring("? extends ".length());

        if (path.contains("$")) {
            // handle subclass
            String[] split = path.split("\\$");
            this.name = split[1];
            this.parent = new SimpleClass(split[0]);
            this.path = split[0];
        } else if (path.contains(".")) {
            int index = path.lastIndexOf(".");
            this.name = path.substring(index + 1);
            this.path = path.substring(0, index);
        } else {
            this.name = path;
        }
    }

    public boolean hasGeneric() {
        return generic != null;
    }

    public String getGeneric() {
        return generic.toString();
    }

    public String getRawGeneric() {
        return generic.toFullString();
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
        return getName() + (hasGeneric() ? "<" + getGeneric() + ">" : "");
    }

    private static class Generic {
        private String prefix = "";
        private SimpleClass post;

        public Generic(SimpleClass post) {
            this.post = post;
        }

        public Generic(String prefix, SimpleClass post) {
            this.prefix = prefix;
            this.post = post;
        }

        @Override
        public String toString() {
            return prefix + post;
        }

        public String toFullString() {
            return prefix + (post.getPath() == null ? "" : post.getPath()) + post;
        }

    }

}
