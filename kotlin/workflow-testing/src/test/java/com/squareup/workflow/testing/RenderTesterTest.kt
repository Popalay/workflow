/*
 * Copyright 2019 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.workflow.testing

import com.squareup.workflow.StatelessWorkflow
import com.squareup.workflow.Workflow
import com.squareup.workflow.WorkflowAction.Companion.enterState
import com.squareup.workflow.renderChild
import com.squareup.workflow.stateful
import com.squareup.workflow.stateless
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RenderTesterTest {

  @Test fun `stateless input and rendering`() {
    val workflow = Workflow.stateless<String, String, String> { input ->
      return@stateless "input: $input"
    } as StatelessWorkflow

    workflow.testRender("start") {
      assertEquals("input: start", rendering)
    }
  }

  @Test fun `stateful workflow gets state`() {
    val workflow = Workflow.stateful<String, String, Nothing, String>(
        initialState = { _, _ -> fail("Expected initialState not to be called.") },
        render = { input, state -> "input=$input, state=$state" },
        snapshot = { fail("Expected snapshotState not to be called.") }
    )

    workflow.testRender(input = "foo", state = "bar") {
      assertEquals("input=foo, state=bar", rendering)
    }
  }

  @Test fun `testRenderInitialState uses correct state`() {
    val workflow = Workflow.stateful<String, String, String, String>(
        initialState = { input, _ -> input },
        render = { input, state -> "input: $input, state: $state" },
        snapshot = { fail() }
    )

    workflow.testRenderInitialState("initial") {
      assertEquals("input: initial, state: initial", rendering)
    }
  }

  @Test fun `assert no composition`() {
    val workflow = Workflow.stateless<Unit, Nothing, Unit> { Unit } as StatelessWorkflow

    workflow.testRender {
      assertNoWorkflowsRendered()
      assertNoWorkersRan()
    }
  }

  @Test fun `renders child with input`() {
    val child = MockChildWorkflow<String, String> { "input: $it" }
    val workflow = Workflow.stateless<Unit, Nothing, String> {
      "child: " + renderChild(child, "foo")
    } as StatelessWorkflow

    workflow.testRender {
      assertEquals("foo", child.lastSeenInput)
      assertEquals("child: input: foo", rendering)
    }
  }

  @Test fun `renders worker output`() {
    val worker = MockWorker<String>("worker")
    val workflow = Workflow.stateful<Unit, String, String, Unit>(
        initialState = { _, _ -> fail() },
        render = { _, _ ->
          runningWorkerUntilFinished(worker) {
            enterState(
                "state: $it",
                emittingOutput = "output: $it"
            )
          }
        },
        snapshot = { fail() }
    )

    workflow.testRender("") {
      assertNoWorkflowsRendered()
      worker.assertRan()

      // Output
      val (outputState, output) = worker.handleOutput("work!")
      assertEquals("state: Output(value=work!)", outputState)
      assertEquals("output: Output(value=work!)", output)

      // Finish
      val (finishState, finish) = worker.handleFinish()
      assertEquals("state: Finished", finishState)
      assertEquals("output: Finished", finish)
    }
  }

  @Test fun `child workflow output`() {
    val child: Workflow<Unit, String, Unit> = MockChildWorkflow(Unit)
    val workflow = Workflow.stateful<Unit, String, String, Unit>(
        initialState = { _, _ -> fail() },
        render = { _, _ ->
          renderChild(child) {
            enterState(
                "state: $it",
                emittingOutput = "output: $it"
            )
          }
        },
        snapshot = { fail() }
    )

    workflow.testRender("") {
      assertNoWorkersRan()
      child.assertRendered()
      val (state, output) = child.handleOutput("output!")
      assertEquals("state: output!", state)
      assertEquals("output: output!", output)
    }
  }

  @Test fun `getEventResult works`() {
    val workflow = Workflow.stateful<Unit, String, String, (String) -> Unit>(
        initialState = { _, _ -> fail() },
        render = { _, state ->
          onEvent { event ->
            enterState(
                newState = "from $state on $event",
                emittingOutput = "event: $event"
            )
          }
        },
        snapshot = { fail() }
    )

    workflow.testRender(state = "initial") {
      rendering.invoke("foo")

      val (state, output) = getEventResult()
      assertEquals("from initial on foo", state)
      assertEquals("event: foo", output)
    }
  }
}
