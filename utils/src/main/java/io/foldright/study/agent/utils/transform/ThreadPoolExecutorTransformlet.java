package io.foldright.study.agent.utils.transform;

import javassist.CtMethod;

import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class ThreadPoolExecutorTransformlet implements Transformlet {
    public static final String THREAD_POOL_EXECUTOR_CLASS_NAME = "java.util.concurrent.ThreadPoolExecutor";

    private final String name;

    public ThreadPoolExecutorTransformlet(String name) {
        this.name = name;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws Exception {
        final ClassInfo classInfo = new ClassInfo(THREAD_POOL_EXECUTOR_CLASS_NAME, classFileBuffer, loader);

        // HACK toString method
        final CtMethod toString = classInfo.getCtClass().getDeclaredMethod("toString");
        final String code = "$_ = $_ + \"[[[AddedBy" + name + "Transformlet]]]\";";
        toString.insertAfter(code);
        log("insertAfter toString method of " + THREAD_POOL_EXECUTOR_CLASS_NAME + ": " + code);

        return classInfo.getCtClass().toBytecode();
    }

    @Override
    public Set<String> targetClassNames() {
        return new HashSet<>(Collections.singletonList(THREAD_POOL_EXECUTOR_CLASS_NAME));
    }

    private void log(String msg) {
        System.out.println("[" + name + "Transformlet] " + msg);
    }
}
