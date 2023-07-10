package io.foldright.study.hello.agent;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.lang.instrument.Instrumentation;


public class HelloAgent {
    public static void premain(final String agentArgs, @NonNull final Instrumentation inst) {
        System.out.println("Hello Agent!");
    }
}
