package io.foldright.study.agent.utils.transform;

import java.security.ProtectionDomain;
import java.util.Set;


public interface Transformlet {
    byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws Exception;

    Set<String> targetClassNames();
}
