package io.foldright.study.main;

public enum StudyAgentMode {
    NO_AGENTS,
    HELLO_AND_WORLD_AGENTS,
    ONLY_HELLO_AGENT,

    ;

    public static StudyAgentMode getStudyAgentMode() {
        final String envVarName = "STUDY_AGENT_RUN_MODE";
        final String value = System.getenv(envVarName);
        if (value == null || "".equals(value)) {
            return NO_AGENTS;
        } else if ("hello-and-world-agents".equals(value)) {
            return HELLO_AND_WORLD_AGENTS;
        } else if ("only-hello-agent".equals(value)) {
            return ONLY_HELLO_AGENT;
        } else {
            throw new IllegalStateException("Illegal value of env var(" + envVarName + "): " + value);
        }
    }
}
