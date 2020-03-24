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

import ReactiveSwift
import Workflow
import class Workflow.Lifetime


extension Signal: AnyWorkflowConvertible where Error == Never {

    public func asAnyWorkflow() -> AnyWorkflow<Void, Value> {
        return SignalWorkflow(signal: self).asAnyWorkflow()
    }

}

extension Signal {

    public func take(during lifetime: Lifetime) -> Signal<Value, Error> {
        take(during: lifetime.reactiveLifetime)
    }

}

private struct SignalWorkflow<Value>: Workflow {
    var signal: Signal<Value, Never>
    typealias Output = Value
    typealias State = Void
    func makeInitialState() -> State {
        return ()
    }
    func workflowDidChange(from previousWorkflow: SignalWorkflow, state: inout State) {
    }
    typealias Rendering = Void
    func render(state: State, context: RenderContext<SignalWorkflow>) -> Rendering {
        let sink = context.makeSink(of: AnyWorkflowAction.self)
        context.runSideEffect(key: "") { [signal] lifetime in
            signal
                .take(during: lifetime)
                .map { AnyWorkflowAction(sendingOutput: $0) }
                .observe(on: QueueScheduler.workflowExecution)
                .observeValues(sink.send)
        }
        return ()
    }
}
