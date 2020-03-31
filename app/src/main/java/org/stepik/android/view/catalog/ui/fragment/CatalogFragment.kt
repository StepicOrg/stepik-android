package org.stepik.android.view.catalog.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.catalog.CatalogPresenter
import org.stepik.android.presentation.catalog.CatalogView
import javax.inject.Inject

class CatalogFragment : Fragment(), CatalogView {
    companion object {
        fun newInstance(): Fragment =
            CatalogFragment()
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var catalogPresenter: CatalogPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        catalogPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CatalogPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_catalog_new, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun injectComponent() {
        App.component()
            .catalogNewComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        catalogPresenter.attachView(this)
    }

    override fun onStop() {
        catalogPresenter.detachView(this)
        super.onStop()
    }
}