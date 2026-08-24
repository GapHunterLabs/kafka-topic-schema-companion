package dev.gaphunter.kafkatopicschemacompanion.validate

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonBooleanLiteral
import com.intellij.json.psi.JsonNullLiteral
import com.intellij.json.psi.JsonNumberLiteral
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.json.psi.JsonValue
import dev.gaphunter.kafkatopicschemacompanion.model.SchemaViolation

/**
 * Hand-rolled validator for the common JSON Schema subset (`type`,
 * `required`, `properties`, `items`, `enum`) against real JSON PSI --
 * same "don't reinvent a parser for a format the platform already
 * parses correctly" principle as `json-schema-companion`'s `JsonPointer`
 * (RFC 6901), applied here to structural validation instead of
 * reference resolution. No external JSON Schema library, no network
 * call, no `$id`/remote schema resolution.
 *
 * **v0.1 scope, stated honestly:** `$ref`, `additionalProperties`,
 * `pattern`, `minimum`/`maximum`/`minLength`/`maxLength`,
 * `oneOf`/`anyOf`/`allOf`, and `format` are not evaluated -- a schema
 * using only these keywords produces zero violations even if the sample
 * genuinely doesn't satisfy them. Real, documented limitations, not
 * silent failures pretending to be complete coverage.
 */
object JsonSchemaValidator {

    fun validate(schema: JsonObject, value: JsonValue): List<SchemaViolation> {
        val violations = mutableListOf<SchemaViolation>()
        validateNode(schema, value, "$", violations)
        return violations
    }

    private fun validateNode(schema: JsonObject, value: JsonValue, path: String, violations: MutableList<SchemaViolation>) {
        validateType(schema, value, path, violations)
        validateEnum(schema, value, path, violations)
        if (value is JsonObject) validateObject(schema, value, path, violations)
        if (value is JsonArray) validateArray(schema, value, path, violations)
    }

    private fun declaredType(schema: JsonObject): String? =
        (schema.findProperty("type")?.value as? JsonStringLiteral)?.value

    private fun actualTypeName(value: JsonValue): String = when (value) {
        is JsonObject -> "object"
        is JsonArray -> "array"
        is JsonStringLiteral -> "string"
        is JsonBooleanLiteral -> "boolean"
        is JsonNullLiteral -> "null"
        is JsonNumberLiteral -> "number"
        else -> "unknown"
    }

    /** JSON Schema's `"integer"` is a stricter subset of `"number"` -- a whole number, no fractional part written (`3`, not `3.0`/`3e0`). Same text-based int-vs-decimal check already proven in `json-to-code-companion`'s `JsonTypeInferrer.inferNumber`. */
    private fun isIntegerLiteral(value: JsonNumberLiteral): Boolean =
        !value.text.contains('.') && !value.text.contains('e', ignoreCase = true)

    private fun validateType(schema: JsonObject, value: JsonValue, path: String, violations: MutableList<SchemaViolation>) {
        val expected = declaredType(schema) ?: return
        val actual = actualTypeName(value)
        val matches = when (expected) {
            "integer" -> value is JsonNumberLiteral && isIntegerLiteral(value)
            else -> expected == actual
        }
        if (!matches) {
            violations += SchemaViolation(path, "expected type \"$expected\" but found \"$actual\"", value)
        }
    }

    private fun validateEnum(schema: JsonObject, value: JsonValue, path: String, violations: MutableList<SchemaViolation>) {
        val enumArray = schema.findProperty("enum")?.value as? JsonArray ?: return
        val allowedTexts = enumArray.valueList.map { it.text }
        if (value.text !in allowedTexts) {
            violations += SchemaViolation(path, "value ${value.text} is not one of the allowed enum values", value)
        }
    }

    private fun validateObject(schema: JsonObject, value: JsonObject, path: String, violations: MutableList<SchemaViolation>) {
        val requiredNames = (schema.findProperty("required")?.value as? JsonArray)
            ?.valueList
            ?.mapNotNull { (it as? JsonStringLiteral)?.value }
            .orEmpty()
        for (name in requiredNames) {
            if (value.findProperty(name) == null) {
                violations += SchemaViolation(path, "missing required property \"$name\"", value)
            }
        }

        val propertiesSchema = schema.findProperty("properties")?.value as? JsonObject ?: return
        for (property in value.propertyList) {
            val propertySchema = propertiesSchema.findProperty(property.name)?.value as? JsonObject ?: continue
            val propertyValue = property.value ?: continue
            validateNode(propertySchema, propertyValue, "$path.${property.name}", violations)
        }
    }

    private fun validateArray(schema: JsonObject, value: JsonArray, path: String, violations: MutableList<SchemaViolation>) {
        val itemsSchema = schema.findProperty("items")?.value as? JsonObject ?: return
        for ((index, item) in value.valueList.withIndex()) {
            validateNode(itemsSchema, item, "$path[$index]", violations)
        }
    }
}
