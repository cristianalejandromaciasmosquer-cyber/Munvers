# CLOTUS Android Build Report

APK PATH: `dist/android/CLOTUS-debug.apk` (expected after build)
APK SIZE: NOT VERIFIED — build not executed from repository tools
VERSION: 1.0.0
VERSION CODE: 1
PACKAGE ID: com.clotus.entity.debug (debug) / com.clotus.entity (release)

BUILD: NOT RUN
TYPECHECK: NOT RUN (native Java project)
LINT: NOT RUN
CORE TESTS: NOT RUN
FULL TESTS: NOT RUN
ANDROID BUILD: NOT RUN
INSTALLATION: NOT VERIFIED — NO ANDROID DEVICE AVAILABLE

Build command:

```bash
gradle :app:assembleDebug
mkdir -p dist/android
cp app/build/outputs/apk/debug/app-debug.apk dist/android/CLOTUS-debug.apk
```

This report deliberately distinguishes source implementation from runtime/device evidence.
