package io.foldright.study.main

import io.foldright.study.main.StudyAgentMode.*
import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContainOnlyOnce
import io.kotest.matchers.string.shouldNotContain
import java.util.concurrent.Executors


class MainTest : FunSpec({
    test("agent hacked toString method of ThreadPoolExecutor") {
        val executor = Executors.newCachedThreadPool() as java.util.concurrent.ThreadPoolExecutor
        val addedByHelloTransformlet = "[[[AddedByHelloTransformlet]]]"
        val addedByWorldTransformlet = "[[[AddedByWorldTransformlet]]]"

        val executorString = executor.toString()
        when (val studyAgentMode = getStudyAgentMode()) {
            NO_AGENTS -> {
                executorString.shouldNotContain(addedByHelloTransformlet)
                executorString.shouldNotContain(addedByWorldTransformlet)
            }

            HELLO_AND_WORLD_AGENTS -> {
                executorString.shouldContainOnlyOnce(addedByHelloTransformlet)
                executorString.shouldContainOnlyOnce(addedByWorldTransformlet)
            }

            ONLY_HELLO_AGENT -> {
                executorString.shouldContainOnlyOnce(addedByHelloTransformlet)
                executorString.shouldNotContain(addedByWorldTransformlet)
            }

            ONLY_HELLO_AGENT_TWICE -> {
                executorString.shouldContainOnlyOnce(addedByHelloTransformlet + addedByHelloTransformlet)
                executorString.shouldNotContain(addedByWorldTransformlet)
            }

            else -> fail("Unknown StudyAgentMode: $studyAgentMode")
        }
    }
})
