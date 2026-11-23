# Source configuration contract

This document defines the app-side contract; it is not an implementation of any third-party API.

## HLS source

A source configuration contains:

- `url`: absolute HTTPS URL to an authorized HLS master or media playlist.
- `headers`: optional request headers explicitly supplied by the user or an authorized service integration.
- `cacheAllowed`: whether the content provider permits local caching.

## Validation

- Accept HTTPS by default.
- Reject malformed URLs and unsupported schemes.
- Do not silently rewrite signed URLs or remove query parameters.
- Do not infer tokens, cookies, or authorization headers.
- Redact query strings and sensitive header values from diagnostic output.

## Error categories

- `INVALID_URL`: malformed or unsupported URL.
- `HTTP_ERROR`: upstream HTTP failure.
- `PARSING_ERROR`: invalid or unsupported HLS playlist.
- `PLAYBACK_ERROR`: Media3 playback failure.
- `CACHE_DISABLED`: caching is not permitted for this source.
- `CACHE_ERROR`: local cache I/O or storage failure.
