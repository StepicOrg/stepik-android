package org.stepik.android.view.debug.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import dev.androidbroadcast.vbpd.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.databinding.DialogBackendFeaturesBinding
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.debug.interactor.BackendFeaturesDebugInteractor
import org.stepik.android.domain.debug.model.AuthMarketingAgreementDebugOverride
import org.stepik.android.domain.debug.model.BackendFeaturesDebugData
import javax.inject.Inject

class BackendFeaturesDialogFragment : DialogFragment(R.layout.dialog_backend_features) {
    companion object {
        const val TAG = "BackendFeaturesDialogFragment"

        fun newInstance(): DialogFragment =
            BackendFeaturesDialogFragment()
    }

    @Inject
    lateinit var backendFeaturesDebugInteractor: BackendFeaturesDebugInteractor

    @Inject
    @field:BackgroundScheduler
    lateinit var backgroundScheduler: Scheduler

    @Inject
    @field:MainScheduler
    lateinit var mainScheduler: Scheduler

    private val compositeDisposable = CompositeDisposable()
    private var currentData: BackendFeaturesDebugData? = null

    private val backendFeaturesBinding: DialogBackendFeaturesBinding by viewBinding(DialogBackendFeaturesBinding::bind)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        setStyle(STYLE_NO_TITLE, R.style.ThemeOverlay_AppTheme_Dialog_Fullscreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backendFeaturesBinding.appBarLayoutBinding.viewCenteredToolbarBinding.centeredToolbarTitle
            .setText(R.string.debug_backend_features_subtitle)

        backendFeaturesBinding.backendFeaturesRefresh.setOnClickListener {
            loadFeatures(forceUpdate = true)
        }
        backendFeaturesBinding.backendFeaturesClearOverrides.setOnClickListener {
            currentData?.let {
                render(backendFeaturesDebugInteractor.clearOverrides(it.serverFeatures))
            }
        }
        backendFeaturesBinding.authMarketingChooseOverride.setOnClickListener {
            showAuthMarketingOverrideDialog()
        }
        backendFeaturesBinding.rubricatorChooseOverride.setOnClickListener {
            showRubricatorOverrideDialog()
        }

        loadFeatures(forceUpdate = true)
    }

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
    }

    private fun injectComponent() {
        App.component()
            .backendFeaturesComponentBuilder()
            .build()
            .inject(this)
    }

    private fun loadFeatures(forceUpdate: Boolean) {
        backendFeaturesBinding.backendFeaturesRefresh.isEnabled = false
        compositeDisposable += backendFeaturesDebugInteractor
            .getDebugData(forceUpdate)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = {
                    backendFeaturesBinding.backendFeaturesRefresh.isEnabled = true
                    render(it)
                },
                onError = {
                    backendFeaturesBinding.backendFeaturesRefresh.isEnabled = true
                    Toast.makeText(requireContext(), it.message ?: it.javaClass.simpleName, Toast.LENGTH_SHORT).show()
                }
            )
    }

    private fun render(data: BackendFeaturesDebugData) {
        currentData = data

        backendFeaturesBinding.backendFeaturesError.isVisible = data.featuresError != null
        backendFeaturesBinding.backendFeaturesError.text =
            data.featuresError?.let { getString(R.string.debug_backend_features_error, it) }

        backendFeaturesBinding.authMarketingServerValue.text =
            getString(R.string.debug_backend_features_server_value, valueToString(data.serverAuthMarketingAgreement))
        backendFeaturesBinding.authMarketingActiveValue.text =
            getString(R.string.debug_backend_features_active_value, valueToString(data.activeAuthMarketingAgreement))
        backendFeaturesBinding.authMarketingOverrideValue.text =
            getString(R.string.debug_backend_features_override, data.authMarketingAgreementOverride.name)

        backendFeaturesBinding.rubricatorServerValue.text =
            getString(R.string.debug_backend_features_server_value, valueToString(data.serverRubricatorCacheFile))
        backendFeaturesBinding.rubricatorActiveValue.text =
            getString(R.string.debug_backend_features_active_value, valueToString(data.activeRubricatorCacheFile))
        backendFeaturesBinding.rubricatorOverrideValue.text =
            getString(
                R.string.debug_backend_features_override,
                data.rubricatorOverrideCacheFile ?: getString(R.string.debug_backend_features_rubricator_server)
            )
    }

    private fun showAuthMarketingOverrideDialog() {
        val data = currentData ?: return
        val values = AuthMarketingAgreementDebugOverride.values()
        val labels = arrayOf(
            getString(R.string.debug_backend_features_auth_server),
            getString(R.string.debug_backend_features_auth_force_enabled),
            getString(R.string.debug_backend_features_auth_force_disabled)
        )
        val checkedIndex = values.indexOf(data.authMarketingAgreementOverride)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.debug_backend_features_choose_auth)
            .setSingleChoiceItems(labels, checkedIndex) { dialog, which ->
                render(
                    backendFeaturesDebugInteractor.setAuthMarketingAgreementOverride(
                        values[which],
                        data.serverFeatures
                    )
                )
                dialog.dismiss()
            }
            .setNegativeButton(R.string.debug_alert_dialog_cancel) { _, _ -> }
            .show()
    }

    private fun showRubricatorOverrideDialog() {
        val data = currentData ?: return
        val labels = arrayOf(
            getString(R.string.debug_backend_features_rubricator_server),
            getString(R.string.debug_backend_features_rubricator_custom)
        )
        val checkedIndex = if (data.rubricatorOverrideCacheFile == null) 0 else 1

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.debug_backend_features_choose_rubricator)
            .setSingleChoiceItems(labels, checkedIndex) { dialog, which ->
                dialog.dismiss()
                if (which == 0) {
                    render(backendFeaturesDebugInteractor.setRubricatorOverrideCacheFile(null, data.serverFeatures))
                } else {
                    showRubricatorCustomDialog(data)
                }
            }
            .setNegativeButton(R.string.debug_alert_dialog_cancel) { _, _ -> }
            .show()
    }

    private fun showRubricatorCustomDialog(data: BackendFeaturesDebugData) {
        val editText = EditText(requireContext()).apply {
            hint = getString(R.string.debug_backend_features_rubricator_custom_hint)
            setSingleLine()
            setText(data.rubricatorOverrideCacheFile ?: data.serverRubricatorCacheFile.orEmpty())
            setSelection(text.length)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.debug_backend_features_rubricator_custom)
            .setView(editText)
            .setPositiveButton(R.string.save) { _, _ ->
                render(
                    backendFeaturesDebugInteractor.setRubricatorOverrideCacheFile(
                        editText.text?.toString()?.trim(),
                        data.serverFeatures
                    )
                )
            }
            .setNegativeButton(R.string.debug_alert_dialog_cancel) { _, _ -> }
            .show()
    }

    private fun valueToString(value: Any?): String =
        value?.toString() ?: "missing"
}
