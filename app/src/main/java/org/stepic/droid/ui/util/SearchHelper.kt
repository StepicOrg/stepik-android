package org.stepic.droid.ui.util

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import org.stepic.droid.R
import org.stepic.droid.ui.util.CloseIconHolder.getCloseIconDrawableRes

object SearchHelper {
    fun createSearch(menu: Menu, inflater: MenuInflater, activity: Activity): MenuItem {
        inflater.inflate(R.menu.search_menu, menu)
        val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView

        val closeImageView = searchView.findViewById<ImageView>(R.id.search_close_btn)
        closeImageView.setImageDrawable(ContextCompat.getDrawable(activity, getCloseIconDrawableRes()))

        val componentName = activity.componentName
        val searchableInfo = searchManager.getSearchableInfo(componentName)
        searchView.setSearchableInfo(searchableInfo)
        searchView.maxWidth = 20000//it is dirty workaround for expand in landscape

        return menuItem
    }
}
