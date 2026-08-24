# Demo data for screenshots

`orders-created.schema.json` + a deliberately mismatched
`orders-created.sample.json` (wrong type on `qty`, `status` outside the
declared enum) so the screenshot shows real warnings, not an empty pass.

## How to get the screenshot

1. `./gradlew runIde` from the `kafka-topic-schema-companion` folder,
   open this `demo/` folder as the project.
2. Full Screen, open `orders-created.sample.json` — 2 inline warnings
   should appear (`qty` wrong type, `status` outside enum).
3. Screenshot with both warnings visible, save into
   `kafka-topic-schema-companion/docs/screenshots/`. Close the sandbox.
