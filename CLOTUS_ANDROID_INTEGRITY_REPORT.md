# CLOTUS Android Integrity Report

## Baseline
The source repository baseline contained only `README.md`; there was no identity, seed, memory, visual DNA, permissions, capabilities, goals, experiences or evolution history to compare.

## New runtime invariants
- Identity and seed are generated once on first app start and persisted in app-private storage.
- Subsequent starts load the same identity and seed.
- Protected fields are identified in the identity record.
- Memory and audit writes use atomic temporary-file replacement.
- Snapshot data contains identity, runtime state and memory.
- Rollback restores real JSON state and persists it.

## Verification status
- Source-level persistence design: PARTIALLY_VERIFIED by code inspection.
- Android build: NOT_VERIFIED in this environment.
- Installation: NOT_VERIFIED — no Android device/emulator available.
- Identity persistence on device: NOT_VERIFIED until an APK is installed and restarted.
- Camera, microphone, STT, network, background: NOT_VERIFIED.

No claim is made that a device-dependent capability is functional merely because its API boundary exists.
