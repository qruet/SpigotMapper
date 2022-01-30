package dev.qruet.mapper.java;

import dev.qruet.mapper.jd.JDPrinter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Qruet
 */
public class QMethod {

    private final QClass parent;
    private final String name;
    private final Class<?> returnType;
    private final Class<?>[] parameters;
    private final List<String> body;

    public QMethod(final QClass parent, final String name, Class<?> returnType, Class<?>... params) {
        this.parent = parent;
        this.name = name;
        this.parameters = params;
        this.returnType = returnType;

        this.body = new ArrayList<>();

        final String classBody = parent.decompile();
        String trim = classBody;
        trim = trim.substring(classBody.indexOf(returnType.getSimpleName() + " " + name + "("));
        trim = trim.substring(trim.indexOf("{") + 1);

        int c = 1;
        int i = 0;
        while(c != 0) {
            if(trim.charAt(i) == '{')
                c++;
            else if(trim.charAt(i) == '}')
                c--;
            i++; // iterate
        }

        trim = trim.substring(0, i - 1);
        body.addAll(Arrays.stream(trim.split("\n")).filter(ln -> !ln.isBlank()).collect(Collectors.toList()));
    }

    public Class<?> returnType() {
        return returnType;
    }

    public Iterator<String> getBody() {
        return body.iterator();
    }

    public Collection<Class<?>> parameters() {
        return Collections.unmodifiableCollection(Arrays.asList(parameters));
    }

    public String getName() {
        return name;
    }

}
