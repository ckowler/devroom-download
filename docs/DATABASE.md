# Local data model

Logical entities for the Room schema:

- Download: stable ID, URL reference, display name, MIME type, expected size, received bytes, state, destination URI, timestamps.
- ResourceValidator: ETag, Last-Modified, range support, resolved URL metadata; sensitive query values excluded from logs.
- Segment: download ID, byte start/end, received bytes, state, retry count, temporary storage reference.
- Queue and QueueItem: ordering, concurrency policy, priority, enabled state.
- Schedule: user-selected time/window and network constraints.
- DownloadEvent: bounded local state-transition and error history.
- AppSettings: concurrency limits, Wi-Fi preference, destination preferences, theme, locale.

Persist only what is needed to resume and manage tasks. Credentials and cookies require a separately reviewed secure-storage design and explicit consent.
