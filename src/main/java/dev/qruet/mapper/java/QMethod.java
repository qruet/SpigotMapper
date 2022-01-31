package dev.qruet.mapper.java;

import dev.qruet.mapper.java.util.Pair;
import dev.qruet.mapper.jd.JDPrinter;

import java.lang.reflect.ParameterizedType;
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

        SimpleType type = SimpleType.of(returnType);
        StringBuilder header = new StringBuilder(type + " " + name + "(");
        System.out.println("Building method " + header);
        System.out.println("Args[" + parameters.length + "]: " + Arrays.asList(parameters));

        int index = 0;
        if(parameters.length == 0) {
            header.append(")");
            index = classBody.indexOf(header.toString());
        } else {
            int lastIndex = classBody.lastIndexOf(header.toString());
            while (index != -1 && index++ <= lastIndex) {
                index = classBody.indexOf(header.toString(), index);
                int endix = classBody.indexOf(")", index);
                String pH = classBody.substring(index + header.length(), endix); // parameter header
                System.out.println("Analyze parameter header: " + pH);

                char[] pHa = pH.toCharArray();

                List<SimpleField> fields = new ArrayList<>();
                List<Pair<String, String>> entries = new ArrayList<>();

                int eF = 0;
                boolean f1 = false;

                Pair<String, String> entry = new Pair<>();
                StringBuilder val = new StringBuilder();
                for(int i = 0; i < pHa.length; i++) {
                    char c = pHa[i];
                    if(f1) {
                        if(c == ' ')
                            f1 = false;
                        continue;
                    }

                    if(c == '@') {
                        f1 = true;
                        continue;
                    }

                    if(c == '<')
                        eF++;
                    else if(c == '>')
                        eF--;

                    if(eF != 0 || (c != ' ' && c != ',')) {
                        val.append(c);
                    }

                    if(eF == 0 && !val.isEmpty() && (c == ' ' || c == ',' || (i == (pHa.length - 1)))) {
                        if(entry.getKey() == null) {
                            entry.setKey(val.toString());
                        }
                        else {
                            entry.setValue(val.toString());
                            entries.add(entry);
                            entry = new Pair<>();
                        }
                        val = new StringBuilder();
                    }
                }

                System.out.println(Arrays.asList(entries));
                if(entries.size() != parameters.length)
                    continue;

                boolean f2 = true;
                for (int i = 0; i < entries.size(); i ++) {
                    String arg = entries.get(i).getKey();
                    String t = SimpleType.of(parameters[i]).toString();
                    System.out.println(arg + "  vs  " + t);
                    if (!arg.equals(SimpleType.of(parameters[i]).toString())) {
                        f2 = false;
                    }
                }

                if (f2)
                    break;
            }
        }

        System.out.println("Index: " + index);

        String trim = classBody;
        trim = trim.substring(index);
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
