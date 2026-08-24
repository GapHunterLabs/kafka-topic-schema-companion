package dev.gaphunter.kafkatopicschemacompanion.inspection

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * End-to-end: real PSI + real inspection registration via `myFixture`,
 * not a direct unit call into [KafkaMessageSchemaInspection]'s
 * internals -- the validation logic itself is already covered
 * exhaustively by
 * [dev.gaphunter.kafkatopicschemacompanion.validate.JsonSchemaValidatorTest].
 * This confirms the sibling-file pairing and inspection wiring fire
 * real warnings end to end.
 */
class KafkaMessageSchemaInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(KafkaMessageSchemaInspection::class.java)
    }

    fun `test a matching sample produces no warning`() {
        myFixture.addFileToProject(
            "orders-created.schema.json",
            """{ "type": "object", "required": ["sku"], "properties": { "sku": { "type": "string" } } }""",
        )
        myFixture.configureByText("orders-created.sample.json", """{ "sku": "WIDGET-A" }""")

        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("Kafka message schema mismatch") == true })
    }

    fun `test a mismatching sample produces a warning naming the schema file and the violated path`() {
        myFixture.addFileToProject(
            "orders-created.schema.json",
            """{ "type": "object", "required": ["sku"], "properties": { "sku": { "type": "string" } } }""",
        )
        myFixture.configureByText("orders-created.sample.json", """{ "sku": 123 }""")

        val highlights = myFixture.doHighlighting()
        val warning = highlights.singleOrNull { it.description?.contains("Kafka message schema mismatch") == true }
        assertNotNull("expected exactly one schema-mismatch warning", warning)
        assertTrue(warning!!.description.contains("orders-created.schema.json"))
        assertTrue(warning.description.contains("\$.sku"))
    }

    fun `test a file with no matching schema sibling produces no warning and no crash`() {
        myFixture.configureByText("orders-created.sample.json", """{ "sku": 123 }""")

        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("Kafka message schema mismatch") == true })
    }

    fun `test a plain json file not matching the sample naming convention is never checked`() {
        myFixture.addFileToProject("package.schema.json", """{ "type": "object", "required": ["name"] }""")
        myFixture.configureByText("package.json", """{ "notName": 1 }""")

        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("Kafka message schema mismatch") == true })
    }

    fun `test a message-json suffix file is also checked`() {
        myFixture.addFileToProject(
            "orders-created.schema.json",
            """{ "type": "object", "required": ["sku"] }""",
        )
        myFixture.configureByText("orders-created.message.json", "{}")

        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("Kafka message schema mismatch") == true })
    }
}
