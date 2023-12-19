package io.foldright.study.agent.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static java.security.AccessController.doPrivileged;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;


public class Utils {
    public static boolean isClassLoaded(@NonNull final Instrumentation inst, @NonNull String className) {
        return getLoadedClasses(inst).containsKey(className);
    }

    public static boolean isClassLoadedByClassLoader(@NonNull final Instrumentation inst, @NonNull String className, ClassLoader classLoader) {
        Map<String, Map<ClassLoader, Set<Class<?>>>> loadedClasses = getLoadedClasses(inst);
        if (!loadedClasses.containsKey(className)) return false;

        classLoader = classLoader == null ? NULL_CLASS_LOADER : classLoader;
        return loadedClasses.get(className).containsKey(classLoader);
    }

    @NonNull
    private static Map<String, Map<ClassLoader, Set<Class<?>>>> getLoadedClasses(@NonNull Instrumentation inst) {
        return Arrays.stream((Class<?>[]) inst.getAllLoadedClasses()).collect(
                groupingBy(
                        Class::getName,
                        groupingBy(
                                clazz -> {
                                    ClassLoader classLoader = clazz.getClassLoader();
                                    return classLoader == null ? NULL_CLASS_LOADER : classLoader;
                                },
                                toSet()
                        )
                )
        );
    }

    @SuppressWarnings("removal")
    private static final ClassLoader NULL_CLASS_LOADER = doPrivileged((PrivilegedAction<ClassLoader>) () -> new ClassLoader() {
    });

    public static void logLoadedClasses(@NonNull String agentName, @NonNull Instrumentation inst) {
        if (!System.getenv().containsKey("STUDY_AGENT_ENABLE_CLASS_LOG")) return;

        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            System.out.println("[" + agentName + "Agent] Loaded class " + clazz.getName() + " by " + clazz.getClassLoader());
        }
    }

    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    public static void logThrowable(@NonNull Throwable t) {
        PrintWriter pw = new PrintWriter(System.err);
        pw.println(t);
        t.printStackTrace(pw);
        pw.flush();
    }

    @NonNull
    public static String classFileToName(@NonNull final String classFile) {
        return classFile.replace('/', '.');
    }
}
