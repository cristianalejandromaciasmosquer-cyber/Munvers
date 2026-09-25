# CLOTUS Android Architecture

## CURRENT ARCHITECTURE
The repository had only a minimal README and no pre-existing CLOTUS runtime, frontend, backend, package manifest or tests. Therefore this implementation is a new modular Android application, not a replacement of an existing runtime.

## ANDROID ARCHITECTURE
- `ClotusCore`: identity, memory, decisions/conversation, snapshots, rollback, audit and capability reporting.
- `MainActivity`: UI shell only; delegates state changes to the core.
- Android private files: persistent state, memory, snapshots and audit.
- `app`: Android Gradle application, package `com.clotus.entity`.

## BRIDGE REQUIRED
The UI calls the core directly in this native implementation. Future Android APIs must be introduced behind a DEVICE boundary; they must not write protected identity fields directly.

## NATIVE FEATURES REQUIRED
The manifest declares Internet, camera and microphone because the capability boundary is prepared. Runtime permissions and real capture verification are intentionally not claimed by this initial build. TTS is queried through the Android service when conversation is used.

## KNOWN LIMITATIONS
No source CLOTUS data was present to preserve. No Android device/emulator is available through repository operations, so APK installation, Android runtime boot, camera, microphone, STT, network and background behavior cannot be verified here. Debug builds use `.debug` application ID to avoid colliding with a release installation.
