package org.stepic.droid.code.data

import org.stepic.droid.R
import org.stepic.droid.base.App


class AutocompleteContainer {

    private val autocomplete = hashMapOf(
            "cpp"  to AutocompleteDictionary(App.getAppContext().resources.getStringArray(R.array.autocomplete_words_cpp)),
            "cs"   to AutocompleteDictionary(App.getAppContext().resources.getStringArray(R.array.autocomplete_words_cs)),
            "css"  to AutocompleteDictionary(App.getAppContext().resources.getStringArray(R.array.autocomplete_words_css)),
            "html" to AutocompleteDictionary(App.getAppContext().resources.getStringArray(R.array.autocomplete_words_html)),
            "java" to AutocompleteDictionary(App.getAppContext().resources.getStringArray(R.array.autocomplete_words_java)),
            "js"   to AutocompleteDictionary(App.getAppContext().resources.getStringArray(R.array.autocomplete_words_js)),
            "php"  to AutocompleteDictionary(App.getAppContext().resources.getStringArray(R.array.autocomplete_words_php)),
            "py"   to AutocompleteDictionary(App.getAppContext().resources.getStringArray(R.array.autocomplete_words_py)),
            "rb"   to AutocompleteDictionary(App.getAppContext().resources.getStringArray(R.array.autocomplete_words_rb))
    )

    fun getAutoCompleteForLangAndPrefix(lang: String, prefix: String): List<String> =
            autocomplete[lang]?.getAutocompleteForPrefix(prefix) ?: emptyList()
}