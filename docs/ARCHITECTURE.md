# Architecture

## Layers
- Presentation: Kotlin, Jetpack Compose, screen state and navigation.
- Application: use cases, orchestration, repositories, event/state flow.
- Android platform: UIDT where applicable, WorkManager for deferrable work, notifications, SAF/MediaStore.
- Native core: Rust scheduler, HTTP transport adapter, segment planner, state machine, integrity verification.
- Optional engines: media, BitTorrent, and archive adapters behind stable contracts.

## Boundaries
Rust must not depend on Android UI or Room. It exposes a small versioned API. Kotlin owns Android lifecycle, permissions, notifications, and durable app metadata. A bridge translates typed commands and events.

Room is the user-visible source of truth. Native execution state is checkpointed through an explicit persistence contract. Startup reconciliation compares task IDs, validators, partial-file metadata, and completed byte ranges.

## State machine
QUEUED -> PROBING -> RUNNING -> PAUSING -> PAUSED -> RUNNING -> VERIFYING -> COMPLETED

Terminal or actionable states: FAILED, CANCELLED, NEEDS_USER_ACTION. Transitions are validated centrally.

## Segmentation
Probe range support using HEAD where reliable, otherwise a bounded GET with Range bytes=0-0. Require a valid 206 Partial Content and consistent Content-Range before enabling segments. Use ETag/Last-Modified validators and If-Range when resuming. Re-probe after 416; do not assume completion.

## Android runtime
Use the platform's user-initiated data transfer mechanism where applicable for user-started long transfers. Use WorkManager for deferrable/retryable work. Do not rely on an unrestricted background service.

## Optional engines
Media, BitTorrent, and archive handling are isolated adapters. Their native binaries, update cadence, and licenses are reviewed separately. Removing an optional adapter must not break core HTTP downloads.
