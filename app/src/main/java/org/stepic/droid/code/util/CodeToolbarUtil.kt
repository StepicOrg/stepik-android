package org.stepic.droid.code.util

object CodeToolbarUtil {
    //it should be language specific, fix it in later versions
    private const val INDENT_SYMBOL = "  " // 2 spaces

    fun mapToolbarSymbolToPrintable(symbol: String): String {
        return if (symbol.equals("tab", ignoreCase = true)) {
            INDENT_SYMBOL
        } else {
            symbol
        }
    }
}
