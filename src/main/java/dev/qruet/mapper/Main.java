package dev.qruet.mapper;

import dev.qruet.mapper.jd.JDLoader;
import dev.qruet.mapper.jd.JDPrinter;
import org.jd.core.v1.ClassFileToJavaSourceDecompiler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Scanner;
import java.util.jar.JarFile;

public class Main {

    public static Logger logger;
    private static final String LOG_PATH = "mappings.txt";

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Expected, a path to the obfuscated jar and a 2nd argument to the deobfuscated jar.");
            return;
        }

        String path1 = args[0];
        String path2 = args[1];
        String logPath = LOG_PATH;
        if(args.length == 3)
            logPath = args[2];

        File jar1 = new File(path1);
        if(!jar1.exists()) {
            System.out.println("Failed to find jar file, " + jar1.getAbsolutePath() + ".");
            return;
        }

        File logFile = buildFile(logPath);
        logger = new Logger(logFile);

        try {
            URLClassLoader child = new URLClassLoader(new URL[]{jar1.toURL()}, Main.class.getClassLoader());
            JDPrinter printer = new JDPrinter();
            ClassFileToJavaSourceDecompiler decompiler = new ClassFileToJavaSourceDecompiler();
            decompiler.decompile(new JDLoader(child), printer, "net/minecraft/server/AdvancementDataPlayer");
            System.out.println(printer);

        } catch(Exception e) {
            e.printStackTrace();
        }

        /*boolean skip = true;
        try {
            java.util.jar.JarFile jar = new JarFile(jar1);
            java.util.Enumeration enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
                if(file.isDirectory()) {
                    if(file.getName().contains("net/minecraft/server"))
                       skip = false;
                    else
                        skip = true;
                    continue;
                }

                if(skip)
                    continue;

                System.out.println("Reading class: " + file.getName());
                java.io.InputStream is = jar.getInputStream(file); // get the input stream
                Scanner scanner = new Scanner(is);
                while (scanner.hasNextLine()) {  // write contents of 'is' to 'fos'
                    System.out.println(scanner.nextLine().replaceAll("[^\\x20-\\x7e]", ""));
                }
                scanner.close();
            }
            jar.close();
        } catch(IOException e) {
            e.printStackTrace();
        }*/
    }

    private static File buildFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                File parent = file.getParentFile(); // get parent directory
                if (parent != null) // check if parent directory is specified/exists
                    parent.mkdirs(); // build directories

                file.createNewFile(); // create file
            } catch (IOException e) {
                System.out.println("Failed to create log file, " + path + ".");
                return null;
            }
        }
        return file;
    }
}
