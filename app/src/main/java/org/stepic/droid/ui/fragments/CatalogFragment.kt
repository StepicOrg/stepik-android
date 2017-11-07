package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_catalog.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.CatalogPresenter
import org.stepic.droid.core.presenters.contracts.CatalogView
import org.stepic.droid.model.CoursesCarouselInfo
import org.stepic.droid.ui.adapters.CatalogAdapter
import org.stepic.droid.ui.util.initCenteredToolbar
import javax.inject.Inject

class CatalogFragment : FragmentBase(),
        CatalogView {

    companion object {
        fun newInstance(): FragmentBase = CatalogFragment()
    }

    @Inject
    lateinit var catalogPresenter: CatalogPresenter

    private val courseCarouselInfoList = mutableListOf<CoursesCarouselInfo>()

    override fun injectComponent() {
        App
                .Companion
                .component()
                .catalogComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_catalog, container, false)


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCenteredToolbar(R.string.catalog_title, showHomeButton = false)
        initMainRecycler()

        catalogPresenter.attachView(this)
        catalogPresenter.onCatalogOpened()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        catalogPresenter.detachView(this)
    }

    private fun initMainRecycler() {
        catalogRecyclerView.layoutManager = LinearLayoutManager(context)
        catalogRecyclerView.adapter = CatalogAdapter(courseCarouselInfoList)
    }

    override fun showCarousels(courseItems: List<CoursesCarouselInfo>) {
        this.courseCarouselInfoList.clear()
        this.courseCarouselInfoList.addAll(courseItems)
        catalogRecyclerView.adapter.notifyDataSetChanged()
    }

    override fun offlineMode() {
        //do nothing
    }

}
