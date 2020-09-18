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
plugins {
  id("com.android.library")
  kotlin("android")
  id("org.jetbrains.dokka")
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

apply(from = rootProject.file(".buildscript/configure-maven-publish.gradle"))

apply(from = rootProject.file(".buildscript/configure-android-defaults.gradle"))

// See https://github.com/Kotlin/kotlinx.coroutines/issues/1064#issuecomment-479412940
android.packagingOptions.exclude("**/*.kotlin_*")

dependencies {
  api(project(":workflow-core"))
  api(project(":workflow-ui:backstack-common"))
  api(project(":workflow-ui:core-android"))

  api(Dependencies2.AndroidX.transition)
  api(Dependencies2.Kotlin.Stdlib.jdk6)
  api(Dependencies2.RxJava2.rxjava2)

  implementation(project(":workflow-runtime"))
  implementation(Dependencies2.AndroidX.activity)
  implementation(Dependencies2.AndroidX.fragment)
  implementation(Dependencies2.AndroidX.Lifecycle.reactivestreams)
  implementation(Dependencies2.AndroidX.savedstate)
  implementation(Dependencies2.Kotlin.Coroutines.android)
  implementation(Dependencies2.Kotlin.Coroutines.core)
  implementation(Dependencies2.Kotlin.Coroutines.rx2)

  testImplementation(Dependencies2.Test.junit)
  testImplementation(Dependencies2.Test.truth)
  testImplementation(Dependencies2.Kotlin.Coroutines.test)
  testImplementation(Dependencies2.Kotlin.Test.jdk)
  testImplementation(Dependencies2.Kotlin.Test.mockito)
}
