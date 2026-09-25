# CLOTUS

Functional Android entity runtime, created as a new project because the source repository contained no CLOTUS implementation.

## Build

```bash
gradle :app:assembleDebug
cp app/build/outputs/apk/debug/app-debug.apk dist/android/CLOTUS-debug.apk
```

The debug application id is `com.clotus.entity.debug`; the release application id is `com.clotus.entity`. Version `1.0.0`, code `1`.

The core persists identity, seed, runtime state, memory and structured audit records under the app-private Android files directory. Writes use a temporary file, fsync and rename. Conversation is connected to the core, not hardcoded UI-only responses. Snapshots are real JSON copies and are ready for rollback through the core API.

Camera, microphone, STT, network and background autonomy are explicitly reported as NOT_VERIFIED/UNAVAILABLE until exercised on a real Android device. TTS is only marked AVAILABLE when the Android service exists; real device verification is still required.
