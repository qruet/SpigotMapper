package dev.qruet.mapper.java;

import dev.qruet.mapper.jd.JDLoader;
import dev.qruet.mapper.jd.JDPrinter;
import org.jd.core.v1.ClassFileToJavaSourceDecompiler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Qruet
 */
public class QClass {

    private final Class<?> clazz;
    private final Map<String, Field> fieldMap;
    private final List<QMethod> methods;
    private final JDPrinter printer;

    public QClass(Class<?> clazz) {
        this.clazz = clazz;
        this.fieldMap = new HashMap<>();
        this.methods = new LinkedList<>();

        JDPrinter printer = new JDPrinter();
        ClassFileToJavaSourceDecompiler decompiler = new ClassFileToJavaSourceDecompiler();
        try {
            decompiler.decompile(new JDLoader(clazz.getClassLoader()), printer, clazz.getName().replaceAll("\\.", "/"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.printer = printer;

        Field[] fields = clazz.getDeclaredFields();
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
            methods.add(new QMethod(this, method));
        }
    }

    public String decompile() {
        return printer.toString();
    }

    public Field getField(String name) {
        return fieldMap.get(name);
    }

    public String getName() {
        return clazz.getSimpleName();
    }

    public String getPackage() {
        return clazz.getPackageName();
    }

    public Collection<QMethod> methods() {
        return Collections.unmodifiableCollection(methods);
    }

    public String toString() {
        return getPackage() + "." + getName();
    }

}