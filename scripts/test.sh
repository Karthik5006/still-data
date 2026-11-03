#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
still_sdk="${STILL_ANDROID_SDK:-/workspace/android/toolchain/sdk}"
still_ecj="${STILL_ECJ_JAR:-/workspace/android/toolchain/ecj.jar}"
mkdir -p build/tests
java -jar "$still_ecj" -source 1.8 -target 1.8 -proc:none -nowarn -bootclasspath "$still_sdk/platforms/android-35/android.jar" -d build/tests app/src/main/java/app/stilldata/guard/RulePlanner.java app/src/main/java/app/stilldata/guard/UsageWindows.java app/src/main/java/app/stilldata/guard/FeatureState.java app/src/main/java/app/stilldata/guard/SceneMotion.java app/src/test/java/app/stilldata/guard/CoreTests.java
java -cp build/tests app.stilldata.guard.CoreTests
