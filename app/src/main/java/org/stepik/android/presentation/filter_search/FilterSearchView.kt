package org.stepik.android.presentation.filter_search

import org.stepik.android.domain.filter_search.Difficulty
import org.stepik.android.domain.filter_search.Language
import org.stepik.android.domain.filter_search.Price
import org.stepik.android.domain.filter_search.Type

interface FilterSearchView {

    sealed class State {
        object Idle : State()
        data class Content(
            val rootState: RootState,
            val priceState: PriceState,
            val withDiscount: Boolean,
            val withCertificate: Boolean,
            val difficultyState: DifficultyState,
            val languageState: LanguageState,
            val typeState: TypeState
        ) : State()
    }

    data class DifficultyState(
        val initialList: List<Difficulty>,
        val selectionList: List<Difficulty>
    )

    data class PriceState(
        val initialPrice: Price,
        val selectionPrice: Price
    )

    data class LanguageState(
        val initialList: List<Language>,
        val selectionList: List<Language>
    )

    data class TypeState(
        val initialList: List<Type>,
        val selectionList: List<Type>
    )

    sealed class RootState {
        object RootScreen : RootState()
        object PriceScreen : RootState()
        object DifficultyScreen : RootState()
        object LanguageScreen : RootState()
        object TypeScreen : RootState()
    }

    fun setState(state: State)
    fun closeDialog()
}