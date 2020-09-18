/*
 * Copyright 2017 Square Inc.
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

apply(from = rootProject.file(".buildscript/configure-maven-publish.gradle"))

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  compileOnly(Dependencies2.AndroidX.annotations)
  compileOnly(Dependencies2.Annotations.intellij)

  api(project(":legacy:legacy-workflow-core"))
  // For Snapshot.
  api(project(":workflow-core"))
  api(Dependencies2.Kotlin.Stdlib.jdk6)
  api(Dependencies2.Kotlin.Coroutines.core)
  api(Dependencies2.okio)
  api(Dependencies2.RxJava2.rxjava2)

  implementation(Dependencies2.Kotlin.Coroutines.rx2)

  testImplementation(project(":internal-testing-utils"))
  testImplementation(Dependencies2.Kotlin.Test.jdk)
  testImplementation(Dependencies2.Kotlin.Test.mockito)
  testImplementation(Dependencies2.Test.hamcrestCore)
  testImplementation(Dependencies2.Test.junit)
  testImplementation(Dependencies2.Test.truth)
  testImplementation(Dependencies2.Test.mockito)
  testImplementation(Dependencies2.RxJava2.extensions)
}
