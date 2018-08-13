package com.developersam.primitivize.codegen

/**
 * [CodeConvertible] defines a set of methods that will help the conversion from this AST node to
 * the target code.
 */
internal interface CodeConvertible {

    /**
     * [acceptConversion] accepts the conversion from the [converter] and lets the [converter]
     * add information about this node. to the converter.
     */
    fun acceptConversion(converter: AstToCodeConverter)

}
