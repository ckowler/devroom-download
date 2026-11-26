# Dependency and provenance audit

No third-party source code is copied into this repository at the foundation stage.

Before adopting any component from Mister Download Manager or another project:
1. Record upstream repository, exact commit, files, and modifications.
2. Read the license for the exact component and transitive dependencies.
3. Assess distribution obligations for the complete Android APK and source.
4. Preserve copyright notices and license texts where required.
5. Generate an SBOM and third-party notices bundle for releases.
6. Reject dependencies whose license or maintenance status conflicts with distribution plans.

Mister Download Manager is identified as GPL-3.0 upstream. Treat code reuse as GPL-governed unless a specific file has a clearly documented alternative license. Architectural ideas may inform an independent implementation; do not copy source before completing this review.

The root project license remains undecided until the audit is complete.
