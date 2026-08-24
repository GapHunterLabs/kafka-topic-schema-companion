# Kafka Topic Schema Companion

Validates a Kafka sample/message JSON file (named `<topic>.sample.json`
or `<topic>.message.json`) against its sibling `<topic>.schema.json`
JSON Schema file in the same directory — inline, in the editor, no
broker connection, no schema registry.

## Why it exists

Checking whether a hand-written or copy-pasted sample Kafka message
still matches its topic's schema today means either running a real
integration test against a broker + schema registry, or eyeballing both
files side by side. Neither is available offline, and neither points at
the exact field that's wrong.

## Why built this way

- **100% static PSI analysis, never a real Kafka connection.** No
  broker, no schema registry client, no network call — everything is
  computed from files already on disk.
- **Hand-rolled JSON Schema validator over real JSON PSI**, not an
  external library — same "don't reinvent a parser for a format the
  platform already parses correctly" principle already proven by
  `json-schema-companion`'s `JsonPointer` (RFC 6901), applied here to
  structural validation instead of reference resolution.
- **Naming-convention pairing**, not a config file: `orders-created.
  sample.json` pairs with `orders-created.schema.json` in the same
  directory — a real, common way teams already organize per-topic Kafka
  contracts.

## v0.1 scope — stated honestly, not exhaustively

Covers `type`, `required`, `properties`, `items`, `enum`. **Not
evaluated in v0.1** (real, documented limitations): `$ref`,
`additionalProperties`, `pattern`, numeric bounds
(`minimum`/`maximum`/etc.), `oneOf`/`anyOf`/`allOf`, `format`. Only
same-directory pairing is checked — a schema in a different directory
than its sample isn't found yet.

## Usage

Name your files `<topic>.schema.json` and `<topic>.sample.json` (or
`<topic>.message.json`) in the same directory. Open the sample file —
any real mismatch shows as an inline warning.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
