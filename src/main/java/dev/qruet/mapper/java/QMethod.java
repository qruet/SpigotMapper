package dev.qruet.mapper.java;

import dev.qruet.mapper.jd.JDPrinter;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Qruet
 */
public class QMethod {

    private final QClass parent;
    private final String name;
    private final Type returnType;
    private final Type[] parameters;
    private final List<String> body;

    public QMethod(final QClass parent, final String name, Type returnType, Type... params) {
        this.parent = parent;
        this.name = name;
        this.parameters = params;
        this.returnType = returnType;

        this.body = new ArrayList<>();
        final String classBody = parent.decompile();
        String trim = classBody;

        SimpleType type = SimpleType.of(returnType);
        StringBuilder header = new StringBuilder(type + " " + name + "(");
        List<String> arguments = new ArrayList<>();
        for(Type t : params) {
            arguments.add(SimpleType.of(t) + " ?");
        }
        header.append(String.join(", ", arguments) + ")");

        System.out.println("Building method " + header);
        // TODO Ignore parameter field names

        trim = trim.substring(classBody.indexOf(header.toString()));
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

    public Type returnType() {
        return returnType;
    }

    public Iterator<String> getBody() {
        return body.iterator();
    }

    public Collection<Type> parameters() {
        return Collections.unmodifiableCollection(Arrays.asList(parameters));
    }

    public String getName() {
        return name;
    }

}
