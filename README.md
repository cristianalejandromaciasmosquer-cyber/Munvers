# CLOTUS

A functional Android app shell for a persistent CLOTUS runtime with identity, memory, audit, snapshots and rollback in app-private storage.

## Project status

This repository did not contain a prior CLOTUS implementation, so the app was created as a new Android module centered on the identity and storage model described in the requirements.

## Android package

- Application ID: `com.clotus.entity`
- Debug variant: `com.clotus.entity.debug`
- Version: `1.0.0`
- Version code: `1`

## Core behavior

- Persistent identity generation
- Seed-like runtime identity value
- Memory journal persisted to device storage
- Structured audit log
- Snapshot and rollback support
- App lifecycle persistence
- Protected identity fields remain consistent

## Important caveat

The environment available to this session does not expose an Android build toolchain or an Android device/emulator, so no real APK was generated here. The project is prepared for Android compilation, but the actual APK must be built in an environment with the Android SDK installed.
