<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Kafka Topic Schema Companion Changelog

## [Unreleased]

## [0.1.1]

### Added

- Review/star CTA: after 10 distinct real findings, a one-time
  notification asks whether to rate the plugin on Marketplace, with a
  permanent "Don't ask again" option. Standard mechanism used
  catalog-wide since 2026-08-24 (`CONSTITUTION.md` §7.2), rolled out
  to this plugin now.

## [0.1.0]

### Added

- Validates a `<topic>.sample.json`/`<topic>.message.json` file against
  its sibling `<topic>.schema.json`, inline in the editor.
- Covers the common JSON Schema subset: `type`, `required`,
  `properties`, `items`, `enum`.
- Each violation anchored at the exact spot in the sample file, with
  the schema file name and JSONPath-style location in the message.
- 100% static PSI analysis, no broker connection, no schema registry,
  no network calls, no telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/kafka-topic-schema-companion/compare/0.1.1...HEAD
[0.1.1]: https://github.com/GapHunterLabs/kafka-topic-schema-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/kafka-topic-schema-companion/commits/0.1.0
