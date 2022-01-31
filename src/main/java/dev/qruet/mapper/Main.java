package dev.qruet.mapper;

import dev.qruet.mapper.java.QClass;
import dev.qruet.mapper.java.QMethod;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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

        File jar2 = new File(path2);
        if(!jar2.exists()) {
            System.out.println("Failed to find jar file, " + jar2.getAbsolutePath() + ".");
            return;
        }

        File logFile = buildFile(logPath);
        logger = new Logger(logFile);

        try {
            JarFile jarFile1 = new JarFile(jar1);
            JarFile jarFile2 = new JarFile(jar2);

            URLClassLoader child = new URLClassLoader(new URL[]{jar1.toURL(), jar2.toURL()}, Main.class.getClassLoader());
            JarComparator comparator = new JarComparator(child, jarFile1, jarFile2);
            comparator.compareJars();
        } catch(IOException e){
            e.printStackTrace();
        }
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
