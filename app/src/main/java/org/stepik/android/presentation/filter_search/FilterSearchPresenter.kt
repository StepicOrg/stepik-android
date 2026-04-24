package org.stepik.android.presentation.filter_search

import org.stepik.android.domain.filter.model.CourseListFilterQuery
import org.stepik.android.domain.filter_search.Difficulty
import org.stepik.android.domain.filter_search.Language
import org.stepik.android.domain.filter_search.Price
import org.stepik.android.domain.filter_search.Type
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.app.core.model.cast
import javax.inject.Inject

class FilterSearchPresenter @Inject constructor() : PresenterBase<FilterSearchView>() {
    private var state: FilterSearchView.State = FilterSearchView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: FilterSearchView) {
        super.attachView(view)
        view.setState(state)
    }

    fun initializeState(courseListFilterQuery: CourseListFilterQuery) {
        if (state !is FilterSearchView.State.Idle) return

        val price = Price(
            start = courseListFilterQuery.priceGte?.toInt() ?: -1,
            end = courseListFilterQuery.priceLte?.toInt() ?: -1
        )

        val difficulty = courseListFilterQuery.difficulty ?: emptyList()
        val language = courseListFilterQuery.language ?: emptyList()
        val type = courseListFilterQuery.type ?: emptyList()

        state = FilterSearchView.State.Content(
            rootState = FilterSearchView.RootState.RootScreen,
            priceState = FilterSearchView.PriceState(
                initialPrice = price,
                selectionPrice = price
            ),
            withDiscount = courseListFilterQuery.promoPriceGte ?: false,
            withCertificate = courseListFilterQuery.withCertificate ?: false,
            difficultyState = FilterSearchView.DifficultyState(
                initialList = difficulty,
                selectionList = difficulty
            ),
            languageState = FilterSearchView.LanguageState(
                initialList = language,
                selectionList = language
            ),
            typeState = FilterSearchView.TypeState(
                initialList = type,
                selectionList = type
            )
        )
    }

    /**
     * Root
     */

    fun resetAllFilters() {
        if (state is FilterSearchView.State.Idle) return

        state = FilterSearchView.State.Content(
            rootState = FilterSearchView.RootState.RootScreen,
            priceState = FilterSearchView.PriceState(
                initialPrice = Price(Price.NOT_SET, Price.NOT_SET),
                selectionPrice = Price(Price.NOT_SET, Price.NOT_SET)
            ),
            withDiscount = false,
            withCertificate = false,
            difficultyState = FilterSearchView.DifficultyState(
                initialList = emptyList(),
                selectionList = emptyList()
            ),
            languageState = FilterSearchView.LanguageState(
                initialList = listOf(Language.Any),
                selectionList = listOf(Language.Any)
            ),
            typeState = FilterSearchView.TypeState(
                initialList = emptyList(),
                selectionList = emptyList()
            )
        )
    }

    /**
     * Subscreens visibility functions
     */
    fun openPrice() {
        if (state is FilterSearchView.State.Idle) return
        state = state
            .cast<FilterSearchView.State.Content>()
            .copy(rootState = FilterSearchView.RootState.PriceScreen)
    }

    fun openDifficulty() {
        if (state is FilterSearchView.State.Idle) return
        state = state
            .cast<FilterSearchView.State.Content>()
            .copy(rootState = FilterSearchView.RootState.DifficultyScreen)
    }

    fun openLanguage() {
        if (state is FilterSearchView.State.Idle) return
        state = state
            .cast<FilterSearchView.State.Content>()
            .copy(rootState = FilterSearchView.RootState.LanguageScreen)
    }

    fun openType() {
        if (state is FilterSearchView.State.Idle) return
        state = state
            .cast<FilterSearchView.State.Content>()
            .copy(rootState = FilterSearchView.RootState.TypeScreen)
    }

    /**
     * Price
     */
    fun selectPrice(price: Price) {
        if (state is FilterSearchView.State.Idle) return
        val content = state.cast<FilterSearchView.State.Content>()
        state = content
            .cast<FilterSearchView.State.Content>()
            .copy(priceState = content.priceState.copy(selectionPrice = price))
    }

    fun submitPrice(price: Price) {
        if (state is FilterSearchView.State.Idle) return
        val content = state.cast<FilterSearchView.State.Content>()
        state = content
            .cast<FilterSearchView.State.Content>()
            .copy(
                rootState = FilterSearchView.RootState.RootScreen,
                priceState = content.priceState.copy(initialPrice = price, selectionPrice = price)
            )
    }

    fun onPriceBackPressed() {
        if (state is FilterSearchView.State.Idle) return
        val content = state.cast<FilterSearchView.State.Content>()
        state = content
            .cast<FilterSearchView.State.Content>()
            .copy(
                rootState = FilterSearchView.RootState.RootScreen,
                priceState = content.priceState.copy(selectionPrice = content.priceState.initialPrice)
            )
    }

    fun resetPrice() {
        if (state is FilterSearchView.State.Idle) return
        val content = state.cast<FilterSearchView.State.Content>()
        state = content
            .cast<FilterSearchView.State.Content>()
            .copy(priceState = content.priceState.copy(selectionPrice = Price(Price.NOT_SET, Price.NOT_SET)))
    }

    /**
     * Discount and certificate
     */
    fun selectDiscount() {
        if (state is FilterSearchView.State.Idle) return
        val content = state.cast<FilterSearchView.State.Content>()
        state = content.copy(withDiscount = !content.withDiscount)
    }

    fun selectCertificate() {
        if (state is FilterSearchView.State.Idle) return
        val content = state.cast<FilterSearchView.State.Content>()
        state = content.copy(withCertificate = !content.withCertificate)
    }

    /**
     * Difficulty
     */
    fun selectDifficulty(difficulty: List<Difficulty>) {
        if (state is FilterSearchView.State.Idle) return
        val content = state.cast<FilterSearchView.State.Content>()
        state = content
            .cast<FilterSearchView.State.Content>()
            .copy(difficultyState = content.difficultyState.copy(selectionList = difficulty))
    }

    fun submitDifficulty(difficulty: List<Difficulty>) {
        if (state is FilterSearchView.State.Idle) return
        state = state
            .cast<FilterSearchView.State.Content>()
            .copy(
                rootState = FilterSearchView.RootState.RootScreen,
                difficultyState = FilterSearchView.DifficultyState(initialList = difficulty, selectionList = difficulty)
            )
    }

    fun onDifficultyBackPressed() {
        if (state is FilterSearchView.State.Idle) return
        val content = state.cast<FilterSearchView.State.Content>()
        state = content
            .cast<FilterSearchView.State.Content>()
            .copy(
                rootState = FilterSearchView.RootState.RootScreen,
                difficultyState = content.difficultyState.copy(selectionList = content.difficultyState.initialList)
            )
    }

    fun resetDifficulty() {
        if (state is FilterSearchView.State.Idle) return
        val content = state.cast<FilterSearchView.State.Content>()
        state = content
            .cast<FilterSearchView.State.Content>()
            .copy(difficultyState = content.difficultyState.copy(selectionList = emptyList()))
    }

    /**
     * Language
     */
    fun selectLanguage(language: List<Language>) {
        if (state is FilterSearchView.State.Idle) return
        val content = state.cast<FilterSearchView.State.Content>()
        state = content
            .cast<FilterSearchView.State.Content>()
            .copy(languageState = content.languageState.copy(selectionList = language))
    }

    fun submitLanguage(language: List<Language>) {
        if (state is FilterSearchView.State.Idle) return
        state = state
            .cast<FilterSearchView.State.Content>()
            .copy(
                rootState = FilterSearchView.RootState.RootScreen,
                languageState = FilterSearchView.LanguageState(initialList = language, selectionList = language)
            )
    }

    fun onLanguageBackPressed() {
        if (state is FilterSearchView.State.Idle) return
        val content = state.cast<FilterSearchView.State.Content>()
        state = content
            .cast<FilterSearchView.State.Content>()
            .copy(
                rootState = FilterSearchView.RootState.RootScreen,
                languageState = content.languageState.copy(selectionList = content.languageState.initialList)
            )
    }

    fun resetLanguage() {
        if (state is FilterSearchView.State.Idle) return
        val content = state.cast<FilterSearchView.State.Content>()
        state = content
            .cast<FilterSearchView.State.Content>()
            .copy(languageState = content.languageState.copy(selectionList = listOf(Language.Any)))
    }

    /**
     * Type
     */
    fun selectType(type: List<Type>) {
        if (state is FilterSearchView.State.Idle) return
        val content = state.cast<FilterSearchView.State.Content>()
        state = content
            .cast<FilterSearchView.State.Content>()
            .copy(typeState = content.typeState.copy(selectionList = type))
    }

    fun submitType(type: List<Type>) {
        if (state is FilterSearchView.State.Idle) return
        state = state
            .cast<FilterSearchView.State.Content>()
            .copy(
                rootState = FilterSearchView.RootState.RootScreen,
                typeState = FilterSearchView.TypeState(initialList = type, selectionList = type)
            )
    }

    fun onTypeBackPressed() {
        if (state is FilterSearchView.State.Idle) return
        val content = state.cast<FilterSearchView.State.Content>()
        state = content
            .cast<FilterSearchView.State.Content>()
            .copy(
                rootState = FilterSearchView.RootState.RootScreen,
                typeState = content.typeState.copy(selectionList = content.typeState.initialList)
            )
    }

    fun resetType() {
        if (state is FilterSearchView.State.Idle) return
        val content = state.cast<FilterSearchView.State.Content>()
        state = content
            .cast<FilterSearchView.State.Content>()
            .copy(typeState = content.typeState.copy(selectionList = emptyList()))
    }

    /**
     * Mapping choices to CourseListFilterQuery
     */
    fun mapStateToCourseListFilterQuery(courseListId: Long?): CourseListFilterQuery {
        if (state is FilterSearchView.State.Idle) return CourseListFilterQuery()
        val content = state.cast<FilterSearchView.State.Content>()

        val price = content.priceState.initialPrice
        val startPrice = if (price.start == -1) {
            null
        } else {
            price.start.toString()
        }

        val endPrice = if (price.end == -1) {
            null
        } else {
            price.end.toString()
        }

        return CourseListFilterQuery(
            courseList = courseListId,
            language = content.languageState.initialList,
            withCertificate = content.withCertificate,
            difficulty = content.difficultyState.initialList,
            type = content.typeState.initialList,
            priceGte = startPrice,
            priceLte = endPrice,
            promoPriceGte = content.withDiscount
        )
    }
}