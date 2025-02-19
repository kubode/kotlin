// IGNORE_BACKEND: JS
// RUN_PLAIN_BOX_FUNCTION
// INFER_MAIN_MODULE

// MODULE: export_interface
// FILE: lib.kt

@JsExport
interface I {
    val value: Int
    var variable: Int
    fun foo(): String
}

open class NotExportedClass(override var value: Int) : I {
    override var variable: Int = value
    override open fun foo(): String = "Not Exported"
}

@JsExport
class ExportedClass(override val value: Int) : I {
    override var variable: Int = value
    override fun foo(): String = "Exported"
}

@JsExport
class AnotherOne : NotExportedClass(42) {
    override fun foo(): String = "Another One Exported"
}

@JsExport
fun generateNotExported(value: Int): NotExportedClass {
    return NotExportedClass(value)
}

@JsExport
fun consume(i: I): String {
    return "Value is ${i.value}, variable is ${i.variable} and result is '${i.foo()}'"
}

// FILE: test.js
function box() {
    const { I, ExportedClass, AnotherOne, generateNotExported, consume } = this["export_interface"]

    if (I !== undefined) return "Fail: module should not export interface in runtime"

    const exported = new ExportedClass(1)
    const another = new AnotherOne()
    const notExported = generateNotExported (3)

    if (exported.foo() !== "Exported") return "Fail: foo function was not generated for ExportedClass"
    if (another.foo() !== "Another One Exported") return "Fail: foo function was not generated for AnotherOne"
    if (notExported.foo() !== "Not Exported") return "Fail: foo function was not generated for NotExportedClass"

    if (exported.value !== 1) return "Fail: value getter was not generated for ExportedClass"
    if (another.value !== 42) return "Fail: value getter was not generated for AnotherOne"
    if (notExported.value !== 3) return "Fail: value getter was not generated for NotExportedClass"

    if (exported.variable !== 1) return "Fail: variable getter was not generated for ExportedClass"
    if (another.variable !== 42) return "Fail: variable getter was not generated for AnotherOne"
    if (notExported.variable !== 3) return "Fail: variable getter was not generated for NotExportedClass"

    exported.variable = 101
    another.variable = 102
    notExported.variable = 103

    if (exported.variable !== 101) return "Fail: variable setter was not generated for ExportedClass"
    if (another.variable !== 102) return "Fail: variable setter was not generated for AnotherOne"
    if (notExported.variable !== 103) return "Fail: variable setter was not generated for NotExportedClass"

    notExported.value = 42
    if (notExported.value !== 3) return "Fail: value setter was generated for NotExportedClass, but it shouldn't"

    if (consume(exported) !== "Value is 1, variable is 101 and result is 'Exported'") return "Fail: methods or fields of ExportedClass was mangled"
    if (consume(another) !== "Value is 42, variable is 102 and result is 'Another One Exported'") return "Fail: methods or fields of AnotherOne was mangled"
    if (consume(notExported) !== "Value is 3, variable is 103 and result is 'Not Exported'") return "Fail: methods or fields of NotExported was mangled"

    return "OK"
}