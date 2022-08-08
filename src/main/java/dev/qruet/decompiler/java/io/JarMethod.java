package dev.qruet.decompiler.java.io;

import dev.qruet.decompiler.java.Visibility;
import dev.qruet.decompiler.java.element.QElement;
import dev.qruet.decompiler.java.simple.SimpleField;
import dev.qruet.decompiler.java.simple.SimpleType;
import dev.qruet.decompiler.java.util.Pair;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Qruet
 */
public class JarMethod {

    private final JarClass parent;
    private final String name;
    private final Type returnType;
    private final Type[] parameters;
    private final Body body;
    private final boolean is_static;
    private final boolean is_final;
    private final Visibility visibility;

    public JarMethod(final JarClass parent, Method method) {
        this.parent = parent;
        this.name = method.getName();

        this.is_static = Modifier.isStatic(method.getModifiers());
        this.is_final = Modifier.isFinal(method.getModifiers());

        this.parameters = method.getGenericParameterTypes();
        this.returnType = method.getGenericReturnType();

        this.visibility = Visibility.fromModifier(method.getModifiers());

        this.body = new Body();
        final String classBody = parent.decompile();

        SimpleType type = SimpleType.of(returnType);
        StringBuilder header = new StringBuilder(type + " " + name + "(");

        int index = 0;
        if (parameters.length == 0) {
            header.append(")");
            index = classBody.indexOf(header.toString());
            if (index == -1 && type.hasGenerics()) {
                SimpleType[] generics = populateGenerics(type, new ArrayList<>()).toArray(new SimpleType[0]);
                int a = type.getSimpleClass().hasParent() ? 1 : 0;
                boolean[][] patterns = buildRandomPattern(generics.length + a);

                int i = 0;
                do {
                    boolean[] pattern = patterns[i++];
                    int j = 0;
                    if (type.getSimpleClass().hasParent()) {
                        j = 1;
                        type.showParent(pattern[0]);
                    }

                    // configure
                    for (int k = 0; k < generics.length; k++, j++)
                        generics[k].showParent(pattern[j]);
                    // ------

                    header = new StringBuilder(type + " " + name + "()");
                    index = classBody.indexOf(header.toString());
                } while (index == -1 && i < patterns.length);

                if (index == -1) {
                    // final index search with wildcard generics
                    type.showGenerics(false);
                    Pattern p = Pattern.compile(type + "<.*?> " + name + "\\(\\)");
                    type.showGenerics(true);
                    Matcher match = p.matcher(classBody);
                    if (match.find())
                        index = match.start();
                }
            }
        } else {
            int lastIndex = classBody.lastIndexOf(header.toString());
            while (index != -1 && index++ <= lastIndex) {
                index = classBody.indexOf(header.toString(), index);
                int endix = classBody.indexOf(")", index);
                String pH = classBody.substring(index + header.length(), endix); // parameter header


                char[] pHa = pH.toCharArray();

                List<SimpleField> fields = new ArrayList<>();
                List<Pair<String, String>> entries = new ArrayList<>();

                int eF = 0;
                boolean f1 = false;

                Pair<String, String> entry = new Pair<>();
                StringBuilder val = new StringBuilder();
                for (int i = 0; i < pHa.length; i++) {
                    char c = pHa[i];
                    if (f1) {
                        if (c == ' ')
                            f1 = false;
                        continue;
                    }

                    if (c == '@') {
                        f1 = true;
                        continue;
                    }

                    if (c == '<')
                        eF++;
                    else if (c == '>')
                        eF--;

                    if (eF != 0 || (c != ' ' && c != ',')) {
                        val.append(c);
                    }

                    if (eF == 0 && !val.isEmpty() && (c == ' ' || c == ',' || (i == (pHa.length - 1)))) {
                        if (val.toString().equals("final")) {
                            val = new StringBuilder();
                            continue;
                        }

                        if (entry.getKey() == null) {
                            entry.setKey(val.toString());
                        } else {
                            entry.setValue(val.toString());
                            entries.add(entry);
                            entry = new Pair<>();
                        }
                        val = new StringBuilder();
                    }
                }

                if (entries.size() != parameters.length)
                    continue;

                boolean f2 = true;
                for (int i = 0; i < entries.size(); i++) {
                    String arg = entries.get(i).getKey();
                    SimpleType t1 = SimpleType.of(parameters[i]);
                    if (!compare(arg, t1)) {
                        f2 = false;
                        break;
                    }
                }

                if (f2)
                    break;
            }
        }

        if (index == -1) {
            System.out.println("=====================");
            System.out.println("Failed to build method: " + header);
            System.out.println("=====================");
        }

        System.out.println("Index = " + index);

        String trim = classBody;
        trim = trim.substring(index);
        trim = trim.substring(trim.indexOf("{") + 1);

        int c = 1;
        char oc = 0;
        int f, i = 0;
        for (; c != 0; i++) {
            char ch = trim.charAt(i);
            if (ch == '\'') {
                oc = (oc == 0 ? '\'' : (oc == '\'' ? 0 : oc));
            } else if (ch == '"') {
                oc = (oc == 0 ? '"' : (oc == '"' ? 0 : oc));
            } else if (oc != 0 && ch == '\\') {
                i++;
                continue;
            }

            if (oc != 0)
                continue;

            if (ch == '{')
                c++;
            else if (ch == '}')
                c--;
        }

        trim = trim.substring(0, i - 1);
        body.addAll(Arrays.stream(trim.split("\n")).filter(ln -> !ln.isBlank()).collect(Collectors.toList()));
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public boolean isFinal() {
        return is_final;
    }

    public boolean isStatic() {
        return is_static;
    }

    public Type returnType() {
        return returnType;
    }

    public Body getBody() {
        return body;
    }

    public List<Type> parameters() {
        return Arrays.asList(parameters);
    }

    public String getName() {
        return name;
    }

    private boolean compare(String str, SimpleType gen) {
        String og = str;

        SimpleType[] generics = null;
        boolean[][] patterns = null;

        if (gen.hasGenerics() || gen.getSimpleClass().hasParent()) {
            if (gen.hasGenerics()) {
                generics = populateGenerics(gen, new ArrayList<>()).toArray(new SimpleType[0]);
            }

            int a = gen.getSimpleClass().hasParent() ? 1 : 0;
            patterns = buildRandomPattern(generics == null ? a : generics.length + a);
        }

        og = og.replace("...", "[]");

        int i = 0;
        do {
            if (patterns != null) {
                boolean[] pattern = patterns[i++];
                int j = 0;
                if (gen.getSimpleClass().hasParent()) {
                    j = 1;
                    gen.showParent(pattern[0]);
                }

                if (generics != null) {
                    // configure
                    for (int k = 0; k < generics.length; k++, j++)
                        generics[k].showParent(pattern[j]);
                    // ------
                }
            }

            String t1_str = gen.toString();

            Pattern p = Pattern.compile("<.*?>");

            boolean f1 = p.matcher(t1_str).find();
            boolean f2 = p.matcher(og).find();

            if (f1 && !f2) {
                t1_str = t1_str.replaceAll("<.*?>", "");
            } else if (!f1 && f2)
                og = og.replaceAll("<.*?>", "");

            System.out.println(og + " vs " + t1_str);

            if (og.equals(t1_str)) {
                return true;
            }
        } while (patterns != null && i < patterns.length);
        return false;
    }

    private List<SimpleType> populateGenerics(SimpleType type, List<SimpleType> generics) {
        if (!type.hasGenerics())
            return generics;

        for (SimpleType generic : type.generics()) {
            generics.add(generic);
            populateGenerics(generic, generics);
        }

        return generics;
    }

    private boolean[][] buildRandomPattern(int n) {
        int possibilities = (int) Math.pow(2, n);
        boolean[][] base = new boolean[possibilities][n];
        for (int i = 0; i < possibilities; i++) {
            String bin = Integer.toBinaryString(i);
            while (bin.length() < n)
                bin = "0" + bin; // add leading zeros

            char[] chars = bin.toCharArray();

            // convert string to bool array
            boolean[] boolArray = new boolean[n];
            for (int j = 0; j < chars.length; j++) {
                boolArray[j] = chars[j] == '0' ? true : false;
            }

            base[i] = boolArray;
        }
        return base;
    }

    private class Body {

        private LinkedList<Line> lines = new LinkedList<>();

        public void addAll(Collection<String> lines) {
            for (String line : lines)
                this.lines.add(new Line(line));
        }

        public Collection<Line> lines() {
            return Collections.unmodifiableCollection(lines);
        }

        private class Line {

            private LinkedList<QElement<?>> elements = new LinkedList<>();

            public Line(String line) {
                for (String elem : line.split(" ")) {
                    if (elem.startsWith("this.")) {
                        String raw = elem.substring("this.".length()).replace(";", "");
                        SimpleType ref = SimpleType.of(raw);
                        elements.add(new QElement(parent.getField(ref.getName()), raw));
                    } else {
                        elements.add(new QElement<>(elem, elem));
                    }
                }
            }

        }

    }

}
