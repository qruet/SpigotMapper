package dev.qruet.mapper.java;

import dev.qruet.mapper.java.simple.SimpleField;
import dev.qruet.mapper.java.simple.SimpleType;
import dev.qruet.mapper.java.util.Pair;

import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;
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
        System.out.println("Args[" + parameters.length + "]: " + Arrays.toString(parameters));

        int index = 0;
        if (parameters.length == 0) {
            header.append(")");
            index = classBody.indexOf(header.toString());
            while (type.hasGenerics() && index == -1) {
                SimpleType[] generics = type.generics().toArray(new SimpleType[0]);
                boolean[][] patterns = buildRandomPattern(generics.length);

                int i = 0;
                do {
                    boolean[] pattern = patterns[i++];
                    for (int j = 0; j < pattern.length; j++)
                        generics[j].showParent(pattern[j]);

                    header = new StringBuilder(type + " " + name + "()");
                    System.out.println("Trying: " + header);
                    index = classBody.indexOf(header.toString());
                } while (index == -1 && i < patterns.length);
            }
        } else {
            int lastIndex = classBody.lastIndexOf(header.toString());
            while (index != -1 && index++ <= lastIndex) {
                index = classBody.indexOf(header.toString(), index);
                int endix = classBody.indexOf(")", index);
                String pH = classBody.substring(index + header.length(), endix); // parameter header
                System.out.println("\nChecking against method parameter header: " + pH + "\n");


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

                System.out.println("1: " + Arrays.asList(entries));
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

        System.out.println("Index: " + index);
        if (index == -1) {
            System.out.println("=====================");
            System.out.println("Failed to build method: " + header);
            System.out.println("=====================");
        }

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
                System.out.println("OC = " + oc);
            } else if (ch == '"') {
                oc = (oc == 0 ? '"' : (oc == '"' ? 0 : oc));
                System.out.println("OC = " + oc);
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
        System.out.println("Done!\n");
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

    private boolean compare(String og, SimpleType gen) {
        System.out.println("[BEFORE]: " + og + "  vs  " + gen);
        SimpleType[] generics = null;
        boolean[][] patterns = null;

        if (gen.hasGenerics() || gen.getSimpleClass().hasParent()) {
            if (gen.hasGenerics()) {
                generics = populateGenerics(gen, new ArrayList<>()).toArray(new SimpleType[0]);
                System.out.println("Generics: " + Arrays.toString(generics));
            }

            int a = gen.getSimpleClass().hasParent() ? 1 : 0;
            patterns = buildRandomPattern(generics == null ? a : generics.length + a);
        }

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
                    for (; j < generics.length; j++)
                        generics[j].showParent(pattern[j]);
                    // ------
                }
            }

            String t1_str = gen.toString();

            Pattern p = Pattern.compile("<.*?>");
            if (p.matcher(t1_str).find() && !p.matcher(og).find()) {
                t1_str = t1_str.replaceAll("<.*?>", "");
            } else if (!p.matcher(t1_str).find() && p.matcher(og).find())
                og = og.replaceAll("<.*?>", "");

            System.out.println("[AFTER]: " + og + "  vs  " + t1_str);
            if (og.equals(t1_str)) {
                return true;
            }
        } while (patterns != null && i < patterns.length);
        return false;
    }

    private List<SimpleType> populateGenerics(SimpleType type, List<SimpleType> generics) {
        if(!type.hasGenerics())
            return generics;

        for(SimpleType generic : type.generics()) {
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

}
