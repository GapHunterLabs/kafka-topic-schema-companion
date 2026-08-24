package dev.gaphunter.kafkatopicschemacompanion.validate

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JsonSchemaValidatorTest : BasePlatformTestCase() {

    private fun schemaOf(text: String): JsonObject =
        (myFixture.configureByText("schema.json", text) as JsonFile).topLevelValue as JsonObject

    private fun sampleOf(text: String) = myFixture.configureByText("sample.json", text).let {
        (it as JsonFile).topLevelValue!!
    }

    fun `test a fully matching sample produces no violations`() {
        val schema = schemaOf(
            """{ "type": "object", "required": ["sku"], "properties": { "sku": { "type": "string" } } }""",
        )
        val sample = sampleOf("""{ "sku": "WIDGET-A" }""")

        assertTrue(JsonSchemaValidator.validate(schema, sample).isEmpty())
    }

    fun `test a wrong top-level type is flagged`() {
        val schema = schemaOf("""{ "type": "object" }""")
        val sample = sampleOf("[]")

        val violations = JsonSchemaValidator.validate(schema, sample)
        assertEquals(1, violations.size)
        assertTrue(violations[0].message.contains("object"))
        assertTrue(violations[0].message.contains("array"))
    }

    fun `test a missing required property is flagged with its path`() {
        val schema = schemaOf(
            """{ "type": "object", "required": ["sku", "qty"], "properties": { "sku": { "type": "string" }, "qty": { "type": "integer" } } }""",
        )
        val sample = sampleOf("""{ "sku": "WIDGET-A" }""")

        val violations = JsonSchemaValidator.validate(schema, sample)
        assertEquals(1, violations.size)
        assertTrue(violations[0].message.contains("qty"))
    }

    fun `test a nested property type mismatch is flagged with a dotted path`() {
        val schema = schemaOf(
            """{ "type": "object", "properties": { "price": { "type": "number" } } }""",
        )
        val sample = sampleOf("""{ "price": "not a number" }""")

        val violations = JsonSchemaValidator.validate(schema, sample)
        assertEquals(1, violations.size)
        assertEquals("$.price", violations[0].path)
    }

    fun `test array items are validated against the items schema`() {
        val schema = schemaOf(
            """{ "type": "array", "items": { "type": "object", "required": ["sku"], "properties": { "sku": { "type": "string" } } } }""",
        )
        val sample = sampleOf("""[ { "sku": "A" }, { "notSku": "B" } ]""")

        val violations = JsonSchemaValidator.validate(schema, sample)
        assertEquals(1, violations.size)
        assertEquals("$[1]", violations[0].path)
    }

    fun `test an integer type rejects a decimal value`() {
        val schema = schemaOf("""{ "type": "integer" }""")
        val sample = sampleOf("3.5")

        assertEquals(1, JsonSchemaValidator.validate(schema, sample).size)
    }

    fun `test an integer type accepts a whole number`() {
        val schema = schemaOf("""{ "type": "integer" }""")
        val sample = sampleOf("3")

        assertTrue(JsonSchemaValidator.validate(schema, sample).isEmpty())
    }

    fun `test a value outside the declared enum is flagged`() {
        val schema = schemaOf("""{ "type": "string", "enum": ["shipped", "pending"] }""")
        val sample = sampleOf("\"cancelled\"")

        assertEquals(1, JsonSchemaValidator.validate(schema, sample).size)
    }

    fun `test a value inside the declared enum is not flagged`() {
        val schema = schemaOf("""{ "type": "string", "enum": ["shipped", "pending"] }""")
        val sample = sampleOf("\"shipped\"")

        assertTrue(JsonSchemaValidator.validate(schema, sample).isEmpty())
    }

    fun `test a schema with no type or properties at all validates anything without crashing`() {
        val schema = schemaOf("{}")
        val sample = sampleOf("""{ "anything": [1, 2, { "nested": true }] }""")

        assertTrue(JsonSchemaValidator.validate(schema, sample).isEmpty())
    }
}
