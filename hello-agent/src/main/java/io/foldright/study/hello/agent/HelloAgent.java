package io.foldright.study.hello.agent;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.foldright.study.agent.utils.transform.DispatchTransformer;
import io.foldright.study.agent.utils.transform.ThreadPoolExecutorTransformlet;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.List;

import static io.foldright.study.agent.utils.transform.ThreadPoolExecutorTransformlet.THREAD_POOL_EXECUTOR_CLASS_NAME;
import static io.foldright.study.agent.utils.Utils.isClassLoaded;


/**
 * a demo agent.
 */
public class HelloAgent {
    private static final String NAME = "Hello";

    public static void premain(final String agentArgs, @NonNull final Instrumentation inst) {
        System.out.println("[" + NAME + "Agent] Enter premain!");

        if (isClassLoaded(inst, THREAD_POOL_EXECUTOR_CLASS_NAME))
            throw new IllegalStateException("class " + THREAD_POOL_EXECUTOR_CLASS_NAME + " already loaded");

        List<ThreadPoolExecutorTransformlet> transformlets = Collections.singletonList(
                new ThreadPoolExecutorTransformlet(NAME)
        );
        inst.addTransformer(new DispatchTransformer(NAME, transformlets));
    }
}
