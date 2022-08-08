package dev.qruet.decompiler.java.io;

import dev.qruet.decompiler.jd.JDLoader;
import dev.qruet.decompiler.jd.JDPrinter;
import org.jd.core.v1.ClassFileToJavaSourceDecompiler;

import java.lang.reflect.Field;
import java.util.*;
import java.lang.Class;

/**
 * @author Qruet
 */
public class JarClass implements dev.qruet.decompiler.java.io.Class {

    private final Class<?> clazz;
    private final boolean isSubclass;
    private String parent;
    private final List<dev.qruet.decompiler.java.io.Class> subclasses;
    private final String name;
    private final String path;
    private final Map<String, Field> fieldMap;
    private final List<JarMethod> methods;
    private JDPrinter printer;

    public JarClass(Class<?> clazz) {
        this.clazz = clazz;

        this.path = clazz.getName();
        this.isSubclass = path.contains("$");
        this.name = isSubclass ? path.substring(path.lastIndexOf(".") + 1) : clazz.getSimpleName();
        if (isSubclass)
            this.parent = clazz.getPackageName() + "." + this.name.substring(0, this.name.indexOf('$'));

        this.fieldMap = new HashMap<>();
        this.methods = new LinkedList<>();
        this.subclasses = new LinkedList<>();
    }

    public String decompile() {
        if(printer != null)
            return printer.toString();

        printer = new JDPrinter();
        ClassFileToJavaSourceDecompiler decompiler = new ClassFileToJavaSourceDecompiler();
        try {
            decompiler.decompile(new JDLoader(clazz.getClassLoader()), printer, clazz.getName().replaceAll("\\.", "/"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            this.fieldMap.put(field.getName(), field);
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getDeclaringClass() != clazz || method.isBridge() || method.isSynthetic())
                continue;
            // TODO Hardcode check and skip for Enum#values() function for any class of type Enum.
            if (clazz.isEnum()) {
                if (method.getName().equals("values")) {
                    continue;
                }
            }

            System.out.println("Building: " + method.toGenericString());
            methods.add(new JarMethod(this, method));
        }*/

        return decompile();
    }

    public Field getField(String name) {
        return fieldMap.get(name);
    }

    public String getName() {
        return name;
    }

    public boolean isSubclass() {
        return isSubclass;
    }

    public String getParentClassPath() {
        return parent;
    }

    @Override
    public String getPath() {
        return path;
    }

    public String getPackage() {
        return clazz.getPackageName();
    }

    public Collection<JarMethod> methods() {
        return Collections.unmodifiableCollection(methods);
    }

    @Override
    public void addSubclass(dev.qruet.decompiler.java.io.Class sub) {
        subclasses.add(sub);
    }

    @Override
    public Collection<dev.qruet.decompiler.java.io.Class> getSubclasses() {
        return subclasses;
    }

    public String toString() {
        return getPackage() + "." + getName();
    }

}