/*
 * Copyright 2020 Square Inc.
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
package com.squareup.workflow.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.squareup.workflow.ui.ViewEnvironment
import com.squareup.workflow.ui.ViewRegistry
import com.squareup.workflow.ui.compose.internal.WorkflowRendering

/**
 * Renders [rendering] into the composition using this [ViewEnvironment]'s
 * [ViewRegistry][com.squareup.workflow.ui.ViewRegistry] to generate the view.
 *
 * This function fulfills a similar role as
 * [WorkflowViewStub][com.squareup.workflow.ui.WorkflowViewStub], but is much more convenient to use
 * from Composable functions.
 *
 * ## Example
 *
 * ```
 * data class FramedRendering(
 *   val borderColor: Color,
 *   val child: Any
 * )
 *
 * val FramedContainerViewFactory = composedViewFactory<FramedRendering> { rendering, environment ->
 *   Surface(border = Border(rendering.borderColor, 8.dp)) {
 *     WorkflowRendering(rendering.child, environment)
 *   }
 * }
 * ```
 *
 * @param rendering The workflow rendering to display. May be of any type for which a
 * [ViewFactory][com.squareup.workflow.ui.ViewFactory] has been registered in this
 * environment's [ViewRegistry].
 * @param modifier A [Modifier] that will be applied to composable used to show [rendering].
 *
 * @throws IllegalArgumentException if no factory can be found for [rendering]'s type.
 */
@Composable fun WorkflowRendering(
  rendering: Any,
  viewEnvironment: ViewEnvironment,
  modifier: Modifier = Modifier
) {
  val viewRegistry = remember(viewEnvironment) { viewEnvironment[ViewRegistry] }
  val renderingType = rendering::class
  val viewFactory = remember(viewRegistry, renderingType) {
    viewRegistry.getFactoryFor(renderingType)
  }
  WorkflowRendering(rendering, viewFactory, viewEnvironment, modifier)
}
