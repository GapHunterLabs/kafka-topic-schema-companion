package dev.gaphunter.kafkatopicschemacompanion.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import dev.gaphunter.kafkatopicschemacompanion.pairing.SchemaFilePairing
import dev.gaphunter.kafkatopicschemacompanion.validate.JsonSchemaValidator

/**
 * For any file matching the `*.sample.json`/`*.message.json` naming
 * convention ([SchemaFilePairing]), finds its sibling `*.schema.json`
 * in the same directory and validates the sample's structure against it
 * ([JsonSchemaValidator]), flagging every real mismatch inline.
 *
 * **Scope, deliberate:** only the file currently open/inspected is
 * checked -- no project-wide indexing, no background scan. If the
 * sibling schema file doesn't exist yet, or isn't valid JSON itself,
 * this reports nothing (an honest "nothing to check yet", not an error
 * about the schema file's own state -- that's a problem for a schema
 * file's own tooling, not this inspection).
 */
class KafkaMessageSchemaInspection : LocalInspectionTool() {

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is JsonFile) return null
        val virtualFile = file.virtualFile ?: return null

        val schemaFileName = SchemaFilePairing.schemaFileNameFor(virtualFile.name) ?: return null
        val schemaVirtualFile = virtualFile.parent?.findChild(schemaFileName) ?: return null
        val schemaFile = PsiManager.getInstance(file.project).findFile(schemaVirtualFile) as? JsonFile ?: return null
        val schemaRoot = schemaFile.topLevelValue as? JsonObject ?: return null

        val sampleRoot = file.topLevelValue ?: return null

        val violations = JsonSchemaValidator.validate(schemaRoot, sampleRoot)
        if (violations.isEmpty()) return null

        val problems = violations.mapNotNull { violation ->
            val anchor = leafOf(violation.element)
            manager.createProblemDescriptor(
                anchor,
                TextRange(0, anchor.textLength),
                "Kafka message schema mismatch (${schemaFileName}) at ${violation.path}: ${violation.message}",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly,
            )
        }

        return if (problems.isEmpty()) null else problems.toTypedArray()
    }

    /** Leaf-anchored, never a composite node (`SDK_GOTCHAS.md` §20) -- an object/array violation (e.g. "missing required property") anchors on that node's own opening brace/bracket, its real first leaf token. */
    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}
