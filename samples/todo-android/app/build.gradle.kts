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
  id("com.android.application")
  kotlin("android")
}

apply(from = rootProject.file(".buildscript/configure-android-defaults.gradle"))
apply(from = rootProject.file(".buildscript/android-ui-tests.gradle"))

android {
  defaultConfig {
    applicationId = "com.squareup.sample.todo"
    multiDexEnabled = true
  }

  testOptions {
    animationsDisabled = true
  }
}

dependencies {
  implementation(project(":samples:containers:android"))
  implementation(project(":samples:todo-android:common"))
  implementation(project(":workflow-ui:core-android"))
  implementation(project(":workflow-core"))
  implementation(project(":workflow-runtime"))
  implementation(project(":workflow-tracing"))

  implementation(Dependencies2.AndroidX.appcompat)
  implementation(Dependencies2.AndroidX.constraint_layout)
  implementation(Dependencies2.AndroidX.material)
  implementation(Dependencies2.Kotlin.Coroutines.rx2)
  implementation(Dependencies2.okio)
  implementation(Dependencies2.rxandroid2)
  implementation(Dependencies2.RxJava2.rxjava2)
  implementation(Dependencies2.timber)

  testImplementation(Dependencies2.Test.junit)
  testImplementation(Dependencies2.Test.truth)

  androidTestImplementation(Dependencies2.Test.AndroidX.uiautomator)
}
