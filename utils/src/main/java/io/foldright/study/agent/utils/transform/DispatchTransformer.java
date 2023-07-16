package io.foldright.study.agent.utils.transform;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static io.foldright.study.agent.utils.Utils.classFileToName;


public final class DispatchTransformer implements ClassFileTransformer {
    /**
     * "<code>null</code> if no transform is performed",
     * see {@code @return} of {@link ClassFileTransformer#transform(ClassLoader, String, Class, ProtectionDomain, byte[])}
     */
    @SuppressFBWarnings("MS_PKGPROTECT")
    // [ERROR] com.alibaba.ttl.threadpool.agent.TtlTransformer.transform(ClassLoader, String, Class, ProtectionDomain, byte[])
    //         may expose internal representation by returning TtlTransformer.NO_TRANSFORM
    // the value is null, so there is NO "EI_EXPOSE_REP" problem actually.
    public static final byte[] NO_TRANSFORM = null;

    private final String name;
    private final Map<String, ? extends Transformlet> transformlets;

    public DispatchTransformer(String name, Collection<? extends Transformlet> transformlets) {
        Map<String, Transformlet> transformletMap = new HashMap<>();
        for (Transformlet transformlet : transformlets) {
            for (String className : transformlet.targetClassNames()) {
                transformletMap.put(className, transformlet);
            }
        }
        this.name = name;
        this.transformlets = transformletMap;
    }

    @Override
    public byte[] transform(@Nullable final ClassLoader loader, @Nullable final String classFile, final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain, @NonNull final byte[] classFileBuffer) {
        try {
            // Lambda has no class file, no need to transform, just return.
            if (classFile == null) return NO_TRANSFORM;

            if (System.getenv().containsKey("STUDY_AGENT_ENABLE_CLASS_LOG"))
                System.out.println("[" + name + "Transformer] try transform " + classFile);

            final String className = classFileToName(classFile);
            final Transformlet tr = transformlets.get(className);
            if (tr == null) return NO_TRANSFORM;
            return tr.transform(loader, classFile, classBeingRedefined, protectionDomain, classFileBuffer);
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
}
