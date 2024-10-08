#!/bin/bash
set -eEuo pipefail
# the canonical path of this script
SELF_PATH=$(realpath -- "$0")
readonly SELF_PATH SELF_DIR=${SELF_PATH%/*}

readonly BASH_BUDDY_ROOT="$SELF_DIR/bash-buddy"
# shellcheck disable=SC1091
source "$BASH_BUDDY_ROOT/lib/trap_error_info.sh"
# shellcheck disable=SC1091
source "$BASH_BUDDY_ROOT/lib/common_utils.sh"
# shellcheck disable=SC1091
source "$BASH_BUDDY_ROOT/lib/java_utils.sh"
# shellcheck disable=SC1091
source "$BASH_BUDDY_ROOT/lib/maven_utils.sh"

################################################################################
# ci build logic
################################################################################

readonly default_build_jdk_version=17
# shellcheck disable=SC2034
readonly JDK_VERSIONS=(
  8
  11
  "$default_build_jdk_version"
  21
  23
)

# here use `install` and `-D performRelease` intended
#   to check release operations.
#
# De-activate a maven profile from command line
#   https://stackoverflow.com/questions/25201430
#
# shellcheck disable=SC2034
readonly MVU_MVN_OPTS=(
  "${MVU_DEFAULT_MVN_OPTS[@]}"
  -DperformRelease -P'!gen-sign'
  ${CI_MORE_MVN_OPTS:+${CI_MORE_MVN_OPTS}}
  dependency:properties
)

# cd to the project root directory
readonly PROJECT_ROOT=${SELF_DIR%/*}
cd "$PROJECT_ROOT"

########################################
# build and test by default version jdk
########################################

jvu::switch_to_jdk "$default_build_jdk_version"

cu::head_line_echo "build and test with Java $default_build_jdk_version: $JAVA_HOME"
mvu::mvn_cmd clean install

########################################
# test by multiply version jdks
########################################

SUREFIRE_TEST_GOAL=(surefire:test)
# about CI env var
#   https://docs.github.com/en/actions/learn-github-actions/variables#default-environment-variables
[ "${CI:-}" = true ] && SUREFIRE_TEST_GOAL=(jacoco:prepare-agent "${SUREFIRE_TEST_GOAL[@]}" jacoco:report)
readonly SUREFIRE_TEST_GOAL

for jdk_version in "${JDK_VERSIONS[@]}"; do
  jvu::switch_to_jdk "$jdk_version"

  # just test without build
  cu::head_line_echo "test with Java $jdk_version: $JAVA_HOME"

  ##############################
  # run unit test
  ##############################

  # skip default jdk, already tested above
  if [ "$jdk_version" != "$default_build_jdk_version" ]; then
    # just test without build
    cu::head_line_echo "test without agents: $JAVA_HOME"
    mvu::mvn_cmd "${SUREFIRE_TEST_GOAL[@]}"
  fi

  cu::head_line_echo test under hello and world agents
  STUDY_AGENT_RUN_MODE=hello-and-world-agents \
    mvu::mvn_cmd "${SUREFIRE_TEST_GOAL[@]}"

  cu::head_line_echo test under hello agent
  STUDY_AGENT_RUN_MODE=only-hello-agent STUDY_AGENT_ENABLE_CLASS_LOG=true \
    mvu::mvn_cmd "${SUREFIRE_TEST_GOAL[@]}"

  cu::head_line_echo test under hello agent twice
  STUDY_AGENT_RUN_MODE=only-hello-agent-twice \
    mvu::mvn_cmd "${SUREFIRE_TEST_GOAL[@]}"

  ##############################
  # run main test
  ##############################

  cu::head_line_echo run Main without agents
  mvu::mvn_cmd exec:exec -pl main-runner

  cu::head_line_echo "test jvm should fail if throw exception in agent premain"
  tmp_output_file="target/study_hello_agent_throw_exception_$$_$RANDOM.tmp"
  STUDY_AGENT_RUN_MODE=only-hello-agent STUDY_HELLO_AGENT_THROW_EXCEPTION=true \
    mvu::mvn_cmd exec:exec -pl main-runner 2>&1 |
    tee "$tmp_output_file" &&
    cu::die "jvm should fail if throw exception in agent premain!"
  cu::log_then_run grep "IllegalStateException.*throw exception for jvm start failure test by setting STUDY_HELLO_AGENT_THROW_EXCEPTION env var!" "$tmp_output_file" ||
    cu::die "should contains exception message!"

  cu::head_line_echo run Main under hello and world agents
  STUDY_AGENT_RUN_MODE=hello-and-world-agents \
    mvu::mvn_cmd exec:exec -pl main-runner

  cu::head_line_echo run Main under hello agent
  STUDY_AGENT_RUN_MODE=only-hello-agent STUDY_AGENT_ENABLE_CLASS_LOG=true \
    mvu::mvn_cmd exec:exec -pl main-runner

  cu::head_line_echo run Main under hello agent twice
  STUDY_AGENT_RUN_MODE=only-hello-agent-twice \
    mvu::mvn_cmd exec:exec -pl main-runner
done
