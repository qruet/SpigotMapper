package dev.qruet.mapper.java;

import dev.qruet.mapper.jd.JDLoader;
import dev.qruet.mapper.jd.JDPrinter;
import org.jd.core.v1.ClassFileToJavaSourceDecompiler;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Qruet
 */
public class QClass {

    private final Class<?> clazz;
    private final List<QMethod> methods;
    private final JDPrinter printer;

    public QClass(Class<?> clazz) {
        this.clazz = clazz;
        this.methods = new LinkedList<>();

        JDPrinter printer = new JDPrinter();
        ClassFileToJavaSourceDecompiler decompiler = new ClassFileToJavaSourceDecompiler();
        try {
            decompiler.decompile(new JDLoader(clazz.getClassLoader()), printer, clazz.getName().replaceAll("\\.", "/"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.printer = printer;

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getDeclaringClass() != clazz)
                continue;
            methods.add(new QMethod(this, method.getName(), method.getGenericReturnType(), method.getGenericParameterTypes()));
        }
    }

    public String decompile() {
        return printer.toString();
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