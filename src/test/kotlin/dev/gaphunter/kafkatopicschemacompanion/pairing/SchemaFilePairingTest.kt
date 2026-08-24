package dev.gaphunter.kafkatopicschemacompanion.pairing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SchemaFilePairingTest {

    @Test
    fun `a sample-json file pairs with the same-name schema-json file`() {
        assertEquals("orders-created.schema.json", SchemaFilePairing.schemaFileNameFor("orders-created.sample.json"))
    }

    @Test
    fun `a message-json file pairs with the same-name schema-json file`() {
        assertEquals("orders-created.schema.json", SchemaFilePairing.schemaFileNameFor("orders-created.message.json"))
    }

    @Test
    fun `a schema-json file itself is not treated as a sample file`() {
        assertNull(SchemaFilePairing.schemaFileNameFor("orders-created.schema.json"))
    }

    @Test
    fun `an unrelated json file is not treated as a sample file`() {
        assertNull(SchemaFilePairing.schemaFileNameFor("package.json"))
    }

    @Test
    fun `a bare suffix with no topic name is not treated as a sample file`() {
        assertNull(SchemaFilePairing.schemaFileNameFor(".sample.json"))
    }
}
