package io.foldright.study.world.agent;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.lang.instrument.Instrumentation;


public class WorldAgent {
    public static void premain(final String agentArgs, @NonNull final Instrumentation inst) {
        System.out.println("World Agent!");
    }
}
