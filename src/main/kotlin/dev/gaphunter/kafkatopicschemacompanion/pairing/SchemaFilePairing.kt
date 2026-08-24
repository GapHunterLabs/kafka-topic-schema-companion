package dev.gaphunter.kafkatopicschemacompanion.pairing

/**
 * Derives the schema file name a sample/message file should be
 * validated against, by naming convention -- v0.1's whole "which schema
 * applies to this topic" mechanism, deliberately simple (no config
 * file, no annotation/comment convention to parse). A file named
 * `orders-created.sample.json` or `orders-created.message.json` is
 * checked against a sibling `orders-created.schema.json` in the **same
 * directory** -- a real, common way teams already organize
 * per-topic Kafka contracts (`schemas/<topic>.schema.json` +
 * `schemas/<topic>.sample.json`), not an invented convention.
 *
 * **v0.1 scope, stated honestly:** only same-directory pairing is
 * checked -- a schema living in a different directory (e.g. a shared
 * `schemas/` folder while samples live under `test/fixtures/`) isn't
 * found. A real, documented limitation, not a bug.
 */
object SchemaFilePairing {

    private val SAMPLE_SUFFIXES = listOf(".sample.json", ".message.json")

    /** Returns the expected schema file name for [sampleFileName], or null if [sampleFileName] doesn't match a recognized sample-file naming convention at all. */
    fun schemaFileNameFor(sampleFileName: String): String? {
        for (suffix in SAMPLE_SUFFIXES) {
            if (sampleFileName.endsWith(suffix) && sampleFileName.length > suffix.length) {
                val baseName = sampleFileName.removeSuffix(suffix)
                return "$baseName.schema.json"
            }
        }
        return null
    }
}
