package dev.gaphunter.kafkatopicschemacompanion.model

import com.intellij.json.psi.JsonValue

/**
 * One mismatch found by [dev.gaphunter.kafkatopicschemacompanion.validate.JsonSchemaValidator]
 * between a sample message and its schema. [path] is a JSONPath-style
 * pointer to where in the document the mismatch is (`$.items[0].sku`),
 * for the problem message; [element] is the real PSI node the mismatch
 * was found on, for anchoring the inspection warning at the right spot
 * in the editor instead of only at the top of the file.
 */
data class SchemaViolation(val path: String, val message: String, val element: JsonValue)
