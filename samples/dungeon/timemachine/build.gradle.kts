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
plugins {
  `java-library`
  kotlin("jvm")
}

dependencies {
  implementation(project(":workflow-core"))

  implementation(Dependencies2.Kotlin.Stdlib.jdk8)

  testImplementation(Dependencies2.Kotlin.Test.jdk)
  testImplementation(Dependencies2.Test.hamcrestCore)
  testImplementation(Dependencies2.Test.junit)
  testImplementation(Dependencies2.Test.truth)
  testImplementation(Dependencies2.RxJava2.extensions)
  testImplementation(project(":workflow-testing"))
}
