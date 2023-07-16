package io.foldright.study.agent.utils.transform;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;


class ClassInfo {
    private final String className;
    private final byte[] classFileBuffer;
    private final ClassLoader loader;

    // SuppressFBWarnings for classFileBuffer/loader parameter:
    // SuppressFBWarnings for classFileBuffer parameter:
    //   [ERROR] new com.alibaba.ttl.threadpool.agent.internal.transformlet.ClassInfo(String, byte[], ClassLoader)
    //   may expose internal representation by storing an externally mutable object
    //   into ClassInfo.classFileBuffer/loader
    public ClassInfo(@NonNull String className,
                     @NonNull @SuppressFBWarnings({"EI_EXPOSE_REP2"}) byte[] classFileBuffer,
                     @Nullable @SuppressFBWarnings({"EI_EXPOSE_REP2"}) ClassLoader loader) {
        this.className = className;
        this.classFileBuffer = classFileBuffer;
        this.loader = loader;
    }

    @NonNull
    public String getClassName() {
        return className;
    }

    private CtClass ctClass;

    @NonNull
    @SuppressFBWarnings({"EI_EXPOSE_REP"})
    // [ERROR] Medium: com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo.getCtClass()
    // may expose internal representation
    // by returning ClassInfo.ctClass [com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo]
    public CtClass getCtClass() throws IOException {
        if (ctClass != null) return ctClass;

        final ClassPool classPool = new ClassPool(true);
        if (loader == null) {
            classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
        } else {
            classPool.appendClassPath(new LoaderClassPath(loader));
        }

        final CtClass clazz = classPool.makeClass(new ByteArrayInputStream(classFileBuffer), false);
        clazz.defrost();

        this.ctClass = clazz;
        return clazz;
    }

    private boolean modified = false;

    public boolean isModified() {
        return modified;
    }

    public void setModified() {
        this.modified = true;
    }
}
