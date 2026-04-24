package org.stepik.android.view.filter.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.databinding.BottomSheetDialogFilterSearchBinding
import org.stepik.android.domain.filter.model.CourseListFilterQuery
import org.stepik.android.domain.filter_search.Difficulty
import org.stepik.android.domain.filter_search.Language
import org.stepik.android.domain.filter_search.Price
import org.stepik.android.domain.filter_search.Type
import org.stepik.android.presentation.filter_search.FilterSearchPresenter
import org.stepik.android.presentation.filter_search.FilterSearchView
import org.stepik.android.view.catalog.ui.fragment.CatalogFragment
import ru.nobird.android.view.base.ui.extension.setTextIfChanged
import ru.nobird.app.core.model.safeCast
import javax.inject.Inject

class FilterSearchBottomSheetDialogFragment : BottomSheetDialogFragment(), FilterSearchView {
    companion object {
        const val TAG = "FilterSearchBottomSheetDialogFragment"
        private const val ARG_FILTER_QUERY = "filter_query"

        fun newInstance(filterQuery: CourseListFilterQuery): DialogFragment {
            val args = Bundle().apply {
                putParcelable(ARG_FILTER_QUERY, filterQuery)
            }
            return FilterSearchBottomSheetDialogFragment().apply {
                arguments = args
            }
        }
    }

    private val filterQuery: CourseListFilterQuery? by lazy { arguments?.getParcelable(ARG_FILTER_QUERY) }
    private val filterRootBinding: BottomSheetDialogFilterSearchBinding by viewBinding(BottomSheetDialogFilterSearchBinding::bind)

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val presenter: FilterSearchPresenter by viewModels { viewModelFactory }

    private val onDifficultyCheckBoxClickListener = OnClickListener { _ ->
        presenter.selectDifficulty(resolveSelectedDifficulty())
    }

    private val onLanguageCheckBoxClickListener = OnClickListener { _ ->
        presenter.selectLanguage(resolveSelectedLanguage())
    }

    private val onTypeCheckBoxClickListener = OnClickListener { _ ->
        presenter.selectType(resolveSelectedType())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.ThemeOverlay_AppTheme_BottomSheetDialog)
        filterQuery?.let(presenter::initializeState)
    }

    private fun injectComponent() {
        App.component()
            .filterSearchComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        presenter.attachView(this)
    }

    override fun onStop() {
        presenter.detachView(this)
        super.onStop()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottom_sheet_dialog_filter_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        filterRootBinding.filterRootReset.setOnClickListener { presenter.resetAllFilters() }
        filterRootBinding.difficultyItem.setOnClickListener { presenter.openDifficulty() }
        filterRootBinding.priceItem.setOnClickListener { presenter.openPrice() }
        filterRootBinding.discountSwitch.setOnClickListener { _ -> presenter.selectDiscount() }
        filterRootBinding.certificatesSwitch.setOnClickListener { _ -> presenter.selectCertificate() }
        filterRootBinding.typeItem.setOnClickListener { _ -> presenter.openType() }
        filterRootBinding.languageItem.setOnClickListener { _ -> presenter.openLanguage() }

        filterRootBinding.applyFilterAction.setOnClickListener {
            val newFilterQuery = presenter.mapStateToCourseListFilterQuery(filterQuery?.courseList)
            if (newFilterQuery != filterQuery || parentFragment is CatalogFragment) {
                (activity.safeCast<Callback>() ?: parentFragment.safeCast<Callback>())
                    ?.onSyncFilterQueryWithParent(newFilterQuery)
            }
            dismiss()
        }

        /**
         * Difficulty
         */
        filterRootBinding.difficultyRoot.apply {
            beginnerCheckbox.setOnClickListener(onDifficultyCheckBoxClickListener)
            intermediateCheckbox.setOnClickListener(onDifficultyCheckBoxClickListener)
            expertCheckbox.setOnClickListener(onDifficultyCheckBoxClickListener)

            applyFilterAction.setOnClickListener {
                val difficulty = resolveSelectedDifficulty()
                presenter.submitDifficulty(difficulty)
            }

            difficultyBackArrow.setOnClickListener { presenter.onDifficultyBackPressed() }
            filterDifficultyReset.setOnClickListener { presenter.resetDifficulty() }
        }

        /**
         * Price
         */
        filterRootBinding.priceRoot.apply {
            startPriceField.doAfterTextChanged {
                presenter.selectPrice(resolveSelectedPrice())
            }
            endPriceField.doAfterTextChanged {
                presenter.selectPrice(resolveSelectedPrice())
            }

            priceDefaultFree.setOnClickListener {
                presenter.selectPrice(Price(0, 0))
            }
            priceDefault5000.setOnClickListener {
                presenter.selectPrice(Price(Price.NOT_SET, 5000))
            }
            priceDefault10000.setOnClickListener {
                presenter.selectPrice(Price(Price.NOT_SET, 10000))
            }

            applyFilterAction.setOnClickListener {
                val price = resolveSelectedPrice()
                presenter.submitPrice(price)
            }

            priceBackArrow.setOnClickListener { presenter.onPriceBackPressed() }
            filterPriceReset.setOnClickListener { presenter.resetPrice() }
        }

        /**
         * Language
         */
        filterRootBinding.languageRoot.apply {
            russianCheckbox.setOnClickListener(onLanguageCheckBoxClickListener)
            englishCheckbox.setOnClickListener(onLanguageCheckBoxClickListener)

            applyFilterAction.setOnClickListener {
                val language = resolveSelectedLanguage()
                presenter.submitLanguage(language)
            }

            languageBackArrow.setOnClickListener { presenter.onLanguageBackPressed() }
            filterLanguageReset.setOnClickListener { presenter.resetLanguage() }
        }

        /**
         * Type
         */
        filterRootBinding.typeRoot.apply {
            courseCheckbox.setOnClickListener(onTypeCheckBoxClickListener)
            programCheckbox.setOnClickListener(onTypeCheckBoxClickListener)

            applyFilterAction.setOnClickListener {
                val type = resolveSelectedType()
                presenter.submitType(type)
            }

            typeBackArrow.setOnClickListener { presenter.onTypeBackPressed() }
            filterTypeReset.setOnClickListener { presenter.resetType() }
        }
    }

    override fun setState(state: FilterSearchView.State) {
        if (state is FilterSearchView.State.Idle) return
        val contentState = state as FilterSearchView.State.Content

        // Visibility
        when (contentState.rootState) {
            is FilterSearchView.RootState.RootScreen -> {
                filterRootBinding.rootScreen.isVisible = true
                filterRootBinding.priceRoot.root.isVisible = false
                filterRootBinding.difficultyRoot.root.isVisible = false
                filterRootBinding.languageRoot.root.isVisible = false
                filterRootBinding.typeRoot.root.isVisible = false
            }
            is FilterSearchView.RootState.DifficultyScreen -> {
                filterRootBinding.rootScreen.isVisible = false
                filterRootBinding.priceRoot.root.isVisible = false
                filterRootBinding.difficultyRoot.root.isVisible = true
                filterRootBinding.languageRoot.root.isVisible = false
                filterRootBinding.typeRoot.root.isVisible = false
            }
            is FilterSearchView.RootState.PriceScreen -> {
                filterRootBinding.rootScreen.isVisible = false
                filterRootBinding.priceRoot.root.isVisible = true
                filterRootBinding.difficultyRoot.root.isVisible = false
                filterRootBinding.languageRoot.root.isVisible = false
                filterRootBinding.typeRoot.root.isVisible = false
            }
            is FilterSearchView.RootState.LanguageScreen -> {
                filterRootBinding.rootScreen.isVisible = false
                filterRootBinding.priceRoot.root.isVisible = false
                filterRootBinding.difficultyRoot.root.isVisible = false
                filterRootBinding.languageRoot.root.isVisible = true
                filterRootBinding.typeRoot.root.isVisible = false
            }
            is FilterSearchView.RootState.TypeScreen -> {
                filterRootBinding.rootScreen.isVisible = false
                filterRootBinding.priceRoot.root.isVisible = false
                filterRootBinding.difficultyRoot.root.isVisible = false
                filterRootBinding.languageRoot.root.isVisible = false
                filterRootBinding.typeRoot.root.isVisible = true
            }
        }

        // Set data RootScreen
        filterRootBinding.difficultyBody.text = resolveDifficultyText(contentState.difficultyState.initialList)

        filterRootBinding.priceBody.text = resolvePriceText(contentState.priceState.initialPrice)

        filterRootBinding.certificatesSwitch.isChecked = contentState.withCertificate
        filterRootBinding.discountSwitch.isChecked = contentState.withDiscount

        filterRootBinding.languageBody.text = resolveLanguageText(contentState.languageState.initialList)

        val initialType = contentState.typeState.initialList.toSet()
        filterRootBinding.typeBody.isVisible = initialType.isNotEmpty()
        filterRootBinding.typeBody.text = resolveTypeText(contentState.typeState.initialList)

        // Set data DifficultyScreen
        filterRootBinding.difficultyRoot.apply {
            val difficulty = contentState.difficultyState.selectionList
            difficultyBody.text = resolveDifficultyText(difficulty)
            beginnerCheckbox.isChecked = difficulty.contains(Difficulty.Easy)
            intermediateCheckbox.isChecked = difficulty.contains(Difficulty.Normal)
            expertCheckbox.isChecked = difficulty.contains(Difficulty.Hard)
        }

        // Set data PriceScreen
        filterRootBinding.priceRoot.apply {
            val price = contentState.priceState.selectionPrice
            priceBody.text = resolvePriceText(price)

            val startText = price
                .start
                .takeIf { it != Price.NOT_SET }
                ?.toString()
                ?: ""
            startPriceField.setTextIfChanged(startText)

            val endText = price
                .end
                .takeIf { it != Price.NOT_SET }
                ?.toString()
                ?: ""
            endPriceField.setTextIfChanged(endText)

            when {
                price.start == 0 && price.end == 0 -> priceDefaultsRadioGroup.check(R.id.priceDefaultFree)
                price.start == Price.NOT_SET && price.end == 5000 -> priceDefaultsRadioGroup.check(R.id.priceDefault5000)
                price.start == Price.NOT_SET && price.end == 10000 -> priceDefaultsRadioGroup.check(R.id.priceDefault10000)
                else -> priceDefaultsRadioGroup.clearCheck()
            }
        }

        // Set data TypeScreen
        filterRootBinding.typeRoot.apply {
            val type = contentState.typeState.selectionList
            typeBody.isVisible = type.isNotEmpty()
            typeBody.text = resolveTypeText(type)
            courseCheckbox.isChecked = type.contains(Type.Course)
            programCheckbox.isChecked = type.contains(Type.Program)
        }

        // Set data LanguageScreen
        filterRootBinding.languageRoot.apply {
            val language = contentState.languageState.selectionList
            languageBody.text = resolveLanguageText(language)
            russianCheckbox.isChecked = language.contains(Language.Russian)
            englishCheckbox.isChecked = language.contains(Language.English)
        }
    }

    override fun closeDialog() {
        dismiss()
    }

    /**
     * Difficulty
     */
    private fun resolveSelectedDifficulty(): List<Difficulty> =
        buildList {
            filterRootBinding.difficultyRoot.apply {
                if (beginnerCheckbox.isChecked) add(Difficulty.Easy)
                if (intermediateCheckbox.isChecked) add(Difficulty.Normal)
                if (expertCheckbox.isChecked) add(Difficulty.Hard)
            }
        }

    private fun resolveDifficultyText(difficulty: List<Difficulty>): String =
        if (difficulty.isEmpty()) {
            getString(R.string.filter_dialog_level_all)
        } else {
            difficulty.joinToString { getString(it.stringResId) }
        }

    /**
     * Price
     */
    private fun resolveSelectedPrice(): Price {
        val startPriceText = filterRootBinding.priceRoot.startPriceField.text ?: ""
        val endPriceText = filterRootBinding.priceRoot.endPriceField.text ?: ""

        val startPrice = if (startPriceText.isEmpty()) {
            Price.NOT_SET
        } else {
            startPriceText.toString().toInt()
        }

        val endPrice = if (endPriceText.isEmpty()) {
            Price.NOT_SET
        } else {
            endPriceText.toString().toInt()
        }

        return Price(startPrice, endPrice)
    }

    private fun resolvePriceText(price: Price): String =
        buildString {
            val startPrice = price.start
            val endPrice = price.end
            if (startPrice == Price.NOT_SET && endPrice == Price.NOT_SET) {
                append(getString(R.string.filter_dialog_price_any))
            } else {
                if (startPrice != Price.NOT_SET) {
                    append(startPrice)
                }
                append(" ${getString(R.string.filter_dialog_price_divider)} ")
                if (endPrice != Price.NOT_SET) {
                    append(endPrice)
                }
                append(" ${getString(R.string.filter_dialog_price_ruble_sign)}")
            }
        }

    /**
     * Language
     */
    private fun resolveSelectedLanguage(): List<Language> {
        val result = buildList {
            if (filterRootBinding.languageRoot.russianCheckbox.isChecked) add(Language.Russian)
            if (filterRootBinding.languageRoot.englishCheckbox.isChecked) add(Language.English)
        }
        return result.ifEmpty { listOf(Language.Any) }
    }

    private fun resolveLanguageText(languages: List<Language>): String =
        languages.joinToString { getString(it.stringResId) }

    /**
     * Type
     */
    private fun resolveSelectedType(): List<Type> =
        buildList {
            filterRootBinding.typeRoot.apply {
            if (courseCheckbox.isChecked) add(Type.Course)
            if (programCheckbox.isChecked) add(Type.Program)
            }
        }

    private fun resolveTypeText(types: List<Type>): String =
        if (types.isEmpty()) {
            ""
        } else {
            types.joinToString { getString(it.stringResId) }
        }

    interface Callback {
        fun onSyncFilterQueryWithParent(filterQuery: CourseListFilterQuery)
    }
}