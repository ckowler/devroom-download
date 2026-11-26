# Dependency and provenance audit

## Upstream reviewed

Repository: https://github.com/anefzaoui/misterdownloadmanager
- Root license file: GNU GPL version 3.
- Cargo workspace metadata declares `GPL-3.0-or-later`.
- Current workspace uses Rust 2024 and requires Rust 1.98.
- Download engine is `crates/engine` (`mdm-engine`); torrent support is `crates/torrent` (`mdm-torrent`, librqbit).
- The upstream architecture document describes a probe, dynamic segment planner, workers, host limits, shared limiter, atomic resume state, and event channel.

This repository currently contains no copied source from Mister Download Manager. The initial DEVROOM HTTP implementation is independently written and uses reqwest plus thiserror. We use upstream documentation only to understand product architecture.

## Rules before adopting any upstream code

1. Record upstream repository, exact commit, files, and modifications.
2. Read the license for the exact component and all transitive dependencies.
3. Assess distribution obligations for the complete Android APK and source.
4. Preserve copyright notices and license texts where required.
5. Generate an SBOM and third-party notices bundle for releases.
6. Reject dependencies whose license or maintenance status conflicts with distribution plans.

Treat upstream code as GPL-3.0-or-later unless a specific file has a clearly documented alternative license. Do not copy source before completing this review and accepting the corresponding distribution obligations.

## Current dependency policy

- Prefer permissive, maintained open-source dependencies.
- No commercial APIs, mandatory paid services, advertising SDKs, or telemetry SDKs.
- Review native binaries separately; include source/build provenance where required.
- The root project license remains undecided until the audit is complete. Do not add a permissive root LICENSE file prematurely.
