package org.stepic.droid.code.util

import android.os.Bundle
import org.stepic.droid.analytic.Analytic

object CodeToolbarUtil {
    fun mapToolbarSymbolToPrintable(symbol: String, indentSize: Int): String {
        return if (symbol.equals("tab", ignoreCase = true)) {
            " ".repeat(indentSize)
        } else {
            symbol
        }
    }

    fun reportSelectedSymbol(analytic: Analytic, language: String?, symbol: String) {
        val bundle = Bundle()
        bundle.putString(Analytic.Code.TOOLBAR_SELECTED_LANGUAGE, language)
        bundle.putString(Analytic.Code.TOOLBAR_SELECTED_SYMBOL, symbol)
        bundle.putString(Analytic.Code.TOOLBAR_SELECTED_LANGUAGE_SYMBOL, "$language $symbol")
        analytic.reportEvent(Analytic.Code.TOOLBAR_SELECTED, bundle)
    }
}
