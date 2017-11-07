package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_catalog.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.model.CourseListItem
import org.stepic.droid.ui.adapters.CatalogAdapter
import org.stepic.droid.ui.util.initCenteredToolbar

class CatalogFragment : FragmentBase() {

    companion object {
        fun newInstance(): FragmentBase = CatalogFragment()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_catalog, container, false)


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCenteredToolbar(R.string.catalog_title, showHomeButton = false)
        initMainRecycler()
    }

    private fun initMainRecycler() {
        catalogRecyclerView.layoutManager = LinearLayoutManager(context)
        catalogRecyclerView.adapter = CatalogAdapter(emptyList<CourseListItem>())
    }

}
