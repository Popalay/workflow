package com.squareup.workflow.ui

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.google.common.truth.Truth.assertThat
import com.squareup.workflow.RenderingAndSnapshot
import com.squareup.workflow.Snapshot
import com.squareup.workflow.Workflow
import com.squareup.workflow.WorkflowSession
import com.squareup.workflow.action
import com.squareup.workflow.asWorker
import com.squareup.workflow.stateless
import com.squareup.workflow.ui.WorkflowRunner.Config
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkflowRunnerViewModelTest {

  private val scope = CoroutineScope(Unconfined)

  @Test fun snapshotUpdatedOnHostEmission() {
    val snapshot1 = Snapshot.of("one")
    val snapshot2 = Snapshot.of("two")
    val snapshotsChannel = Channel<RenderingAndSnapshot<Unit>>(UNLIMITED)
    val snapshotsFlow = flow { snapshotsChannel.consumeEach { emit(it) } }
    val session = WorkflowSession(
        renderingsAndSnapshots = snapshotsFlow,
        outputs = emptyFlow()
    )

    val runner = WorkflowRunnerViewModel(scope, session)

    assertThat(runner.getLastSnapshotForTest()).isEqualTo(Snapshot.EMPTY)

    snapshotsChannel.offer(RenderingAndSnapshot(Unit, snapshot1))
    assertThat(runner.getLastSnapshotForTest()).isEqualTo(snapshot1)

    snapshotsChannel.offer(RenderingAndSnapshot(Unit, snapshot2))
    assertThat(runner.getLastSnapshotForTest()).isEqualTo(snapshot2)
  }

  @Test fun hostCancelledOnResultAndNoSooner() {
    var cancelled = false
    scope.coroutineContext[Job]!!.invokeOnCompletion { e ->
      if (e is CancellationException) {
        cancelled = true
      } else throw AssertionError(
          "Expected ${CancellationException::class.java.simpleName}", e
      )
    }
    val session = WorkflowSession<String, Nothing>(
        renderingsAndSnapshots = emptyFlow(),
        outputs = flowOf("fnord")
    )
    val runner = WorkflowRunnerViewModel(scope, session)

    assertThat(cancelled).isFalse()
    val tester = runner.result.test()
    assertThat(cancelled).isTrue()
    tester.assertComplete()
    assertThat(tester.values()).isEqualTo(listOf("fnord"))
  }

  @Test fun hostCancelledOnCleared() {
    var cancelled = false
    scope.coroutineContext[Job]!!.invokeOnCompletion { e ->
      if (e is CancellationException) {
        cancelled = true
      } else throw AssertionError(
          "Expected ${CancellationException::class.java.simpleName}", e
      )
    }
    val session = WorkflowSession<Nothing, Nothing>(
        renderingsAndSnapshots = emptyFlow(),
        outputs = emptyFlow()
    )
    val runner = WorkflowRunnerViewModel(scope, session)

    assertThat(cancelled).isFalse()
    val tester = runner.result.test()
    runner.clearForTest()
    assertThat(cancelled).isTrue()
    tester.assertComplete()
    tester.assertNoValues()
  }

  @Test fun resultDelivered() {
    val outputs = BroadcastChannel<String>(1)
    val runner = Workflow
        .stateless<Unit, String, Unit> {
          runningWorker(outputs.asWorker()) { action { setOutput(it) } }
          Unit
        }
        .run()

    val tester = runner.result.test()

    tester.assertNotComplete()
    runBlocking { outputs.send("fnord") }
    tester.assertComplete()
    assertThat(tester.values()).isEqualTo(listOf("fnord"))
  }

  @Test fun resultEmptyOnCleared() {
    val runner = Workflow
        .stateless<Unit, String, Unit> {
          runningWorker(flowNever<String>().asWorker()) { action { setOutput(it) } }
        }
        .run()

    val tester = runner.result.test()

    tester.assertNotComplete()
    runner.clearForTest()
    tester.assertComplete()
    tester.assertNoValues()
  }

  private fun <O : Any, R : Any> Workflow<Unit, O, R>.run(): WorkflowRunnerViewModel<O> {
    val registryOwner = object : SavedStateRegistryOwner {
      lateinit var controller: SavedStateRegistryController

      override fun getLifecycle(): Lifecycle {
        return object : Lifecycle() {
          override fun addObserver(observer: LifecycleObserver) = Unit
          override fun removeObserver(observer: LifecycleObserver) = Unit
          override fun getCurrentState() = State.INITIALIZED
        }
      }

      override fun getSavedStateRegistry(): SavedStateRegistry {
        return controller.savedStateRegistry
      }
    }

    registryOwner.controller = SavedStateRegistryController.create(registryOwner)
        .apply { performRestore(null) }

    @Suppress("UNCHECKED_CAST")
    return WorkflowRunnerViewModel
        .Factory(registryOwner.savedStateRegistry) {
          Config(this, Unit, Unconfined)
        }
        .create(WorkflowRunnerViewModel::class.java) as WorkflowRunnerViewModel<O>
  }

  private fun <T> flowNever(): Flow<T> {
    return flow { suspendCancellableCoroutine { } }
  }
}
